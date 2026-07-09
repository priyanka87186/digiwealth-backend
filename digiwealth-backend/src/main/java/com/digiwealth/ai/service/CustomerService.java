package com.digiwealth.ai.service;

import com.digiwealth.ai.dto.response.DashboardResponse;
import com.digiwealth.ai.entity.User;

public interface CustomerService {
    User getProfile(Long userId);
    DashboardResponse getDashboard(Long userId);
}
