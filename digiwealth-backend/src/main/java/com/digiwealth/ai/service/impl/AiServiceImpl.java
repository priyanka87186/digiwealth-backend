package com.digiwealth.ai.service.impl;

import com.digiwealth.ai.dto.request.AiChatRequest;
import com.digiwealth.ai.dto.response.AiChatResponse;
import com.digiwealth.ai.entity.AiChat;
import com.digiwealth.ai.entity.Goal;
import com.digiwealth.ai.entity.Investment;
import com.digiwealth.ai.entity.User;
import com.digiwealth.ai.exception.AiServiceException;
import com.digiwealth.ai.exception.ResourceNotFoundException;
import com.digiwealth.ai.repository.AiChatRepository;
import com.digiwealth.ai.repository.GoalRepository;
import com.digiwealth.ai.repository.InvestmentRepository;
import com.digiwealth.ai.repository.UserRepository;
import com.digiwealth.ai.service.AiService;
import com.digiwealth.ai.service.CustomerService;
import com.digiwealth.ai.service.FinancialHealthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * Builds a "Customer Context" prompt from the user's financial data (income, expenses,
 * savings, investments, goals) and sends it to the configured LLM provider (OpenAI or Gemini).
 * Set app.ai.provider=openai|gemini and the matching api-key in application.properties,
 * or via the OPENAI_API_KEY / GEMINI_API_KEY environment variables.
 */
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiChatRepository aiChatRepository;
    private final UserRepository userRepository;
    private final InvestmentRepository investmentRepository;
    private final GoalRepository goalRepository;
    private final CustomerService customerService;
    private final FinancialHealthService financialHealthService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.provider}")
    private String provider;

    //uncomment this and callOpenAi method if you want to use OpenAI instead of Gemini
 /*
    @Value("${app.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${app.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${app.ai.openai.model}")
    private String openAiModel;*/

    @Value("${app.ai.gemini.api-key}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.base-url}")
    private String geminiBaseUrl;

    /**
     * Rule-based stand-in for a real LLM call, so the AI Advisor works with zero setup.
     * Swap app.ai.provider to "openai" or "gemini" (with a real key) for genuine AI answers.
     */
    private String callMock(User user, String question) {
        var dashboard = customerService.getDashboard(user.getId());
        String q = question.toLowerCase();

        if (q.contains("save")) {
            return String.format(
                    "Based on your current monthly income of %s and expenses of %s, you're saving %s a month. "
                            + "A healthy target is putting aside at least 20%% of your income — consider automating a transfer "
                            + "right after your salary lands.",
                    dashboard.getMonthlyIncome(), dashboard.getMonthlyExpenses(), dashboard.getTotalSavings());
        }
        if (q.contains("invest")) {
            return String.format(
                    "You currently hold %s across your portfolio. Given your Financial Health Score of %d/100, "
                            + "consider increasing SIP contributions to diversified mutual funds before adding higher-risk assets.",
                    dashboard.getTotalInvestments(), dashboard.getFinancialHealthScore());
        }
        if (q.contains("retire")) {
            return "Early retirement depends on your investment growth rate and target corpus. "
                    + "As a rule of thumb, aim to invest 25-30x your annual expenses before considering retirement.";
        }
        if (q.contains("afford") || q.contains("vacation")) {
            return String.format(
                    "You have a total balance of %s and monthly savings of %s. As long as the trip doesn't dip into "
                            + "your emergency fund, it looks affordable — just track it as a Travel expense afterward.",
                    dashboard.getTotalBalance(), dashboard.getTotalSavings());
        }
        if (q.contains("portfolio") || q.contains("performing")) {
            return String.format(
                    "Your total investments are currently valued at %s. Check the Investment Portfolio page for a "
                            + "full profit/loss and allocation breakdown by asset type.",
                    dashboard.getTotalInvestments());
        }

        return String.format(
                "Here's a quick snapshot: balance %s, monthly income %s, monthly expenses %s, "
                        + "Financial Health Score %d/100. Ask me about saving, investing, retirement, or affordability "
                        + "for more specific guidance.",
                dashboard.getTotalBalance(), dashboard.getMonthlyIncome(), dashboard.getMonthlyExpenses(),
                dashboard.getFinancialHealthScore());
    }

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        
        String prompt = buildCustomerContextPrompt(user, request.getQuestion());
        String answer = switch (provider.toLowerCase()) {
            //case "openai" -> callOpenAi(prompt);
            case "gemini" -> callGemini(prompt);
            default -> callMock(user, request.getQuestion());
        };

        AiChat chat = AiChat.builder()
                .user(user)
                .question(request.getQuestion())
                .answer(answer)
                .build();
        chat = aiChatRepository.save(chat);

        return AiChatResponse.builder()
                .id(chat.getId())
                .question(chat.getQuestion())
                .answer(chat.getAnswer())
                .createdAt(chat.getCreatedAt())
                .build();
    }

    /**
     * Customer Context Builder: assembles a compact financial snapshot that grounds
     * the LLM's answer in the user's actual data rather than generic advice.
     */
    private String buildCustomerContextPrompt(User user, String question) {
        var dashboard = customerService.getDashboard(user.getId());
        List<Investment> investments = investmentRepository.findByUserId(user.getId());
        List<Goal> goals = goalRepository.findByUserId(user.getId());
        var health = financialHealthService.calculateHealthScore(user.getId());

        StringBuilder investmentSummary = new StringBuilder();
        for (Investment inv : investments) {
            investmentSummary.append(String.format("- %s: invested %.2f, current value %.2f%n",
                    inv.getInvestmentType(), inv.getInvestedAmount(), inv.getCurrentValue()));
        }

        StringBuilder goalSummary = new StringBuilder();
        for (Goal g : goals) {
            goalSummary.append(String.format("- %s: target %.2f by %s, current %.2f%n",
                    g.getGoalName(), g.getTargetAmount(), g.getTargetDate(), g.getCurrentAmount()));
        }

        return """
                You are DigiWealth AI, a friendly and knowledgeable personal wealth advisor avatar \
                embedded in a banking app. Answer the customer's question using ONLY the financial \
                context below. Be specific, concise, and actionable. Do not invent numbers that are \
                not provided. If the data is insufficient to answer precisely, say so and give general \
                best-practice guidance instead.

                CUSTOMER FINANCIAL CONTEXT:
                - Total Account Balance: %s
                - Monthly Income: %s
                - Monthly Expenses: %s
                - Monthly Savings: %s
                - Total Investments Value: %s
                - Active Goals Count: %d
                - Financial Health Score: %d/100

                INVESTMENT PORTFOLIO:
                %s

                FINANCIAL GOALS:
                %s

                CUSTOMER QUESTION:
                %s
                """.formatted(
                dashboard.getTotalBalance(), dashboard.getMonthlyIncome(), dashboard.getMonthlyExpenses(),
                dashboard.getTotalSavings(), dashboard.getTotalInvestments(), dashboard.getActiveGoals(),
                dashboard.getFinancialHealthScore(),
                investmentSummary.isEmpty() ? "(none on record)" : investmentSummary,
                goalSummary.isEmpty() ? "(none on record)" : goalSummary,
                question
        );
    }

   /* private String callOpenAi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", openAiModel);
            ArrayNode messages = body.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", prompt);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(openAiBaseUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (RestClientException | JsonProcessingException ex) {
            throw new AiServiceException("Failed to get a response from the OpenAI service", ex);
        }
    }*/

    private String callGemini(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode body = objectMapper.createObjectNode();
            ArrayNode contents = body.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", prompt);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            String url = geminiBaseUrl + "?key=" + geminiApiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (RestClientException | JsonProcessingException ex) {
            throw new AiServiceException("Failed to get a response from the Gemini service", ex);
        }
    }
}
