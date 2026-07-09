package com.digiwealth.ai.service;

import com.digiwealth.ai.dto.request.AiChatRequest;
import com.digiwealth.ai.dto.response.AiChatResponse;

public interface AiService {
    AiChatResponse chat(Long userId, AiChatRequest request);
}
