package com.hotel.booking.client;

import com.hotel.booking.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign pour communiquer avec User Service
 */
@FeignClient(
        name = "user-service",
        url = "${services.user-service.url:http://localhost:8084}",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * Récupérer un utilisateur par ID
     */
    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable String id);
}
