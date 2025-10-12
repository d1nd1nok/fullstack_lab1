package com.taskmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        PasswordEncoder mockEncoder = mock(PasswordEncoder.class);
        when(mockEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(mockEncoder.matches(anyString(), anyString())).thenReturn(true);
        return mockEncoder;
    }
}
