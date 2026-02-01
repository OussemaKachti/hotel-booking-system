package com.hotel.booking.client;

import com.hotel.booking.client.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback pour UserServiceClient
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserResponse getUserById(String id) {
        log.warn("User Service is unavailable, returning mock data for user ID: {}", id);
        
        UserResponse mockUser = new UserResponse();
        mockUser.setId(id);
        mockUser.setUsername("mockuser-" + id);
        mockUser.setEmail("mock@example.com");
        mockUser.setFirstName("Mock");
        mockUser.setLastName("User");
        
        return mockUser;
    }
}
