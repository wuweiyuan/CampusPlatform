package com.campus.trade.campustradeserver.auth.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseDtoTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void userInfoResponseOnlySerializesPublicFields() throws Exception {
        UserInfoResponse user = new UserInfoResponse();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setEmail("student@example.com");
        user.setRole("USER");

        String json = objectMapper.writeValueAsString(user);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"username\":\"zhangsan\"");
        assertThat(json).contains("\"email\":\"student@example.com\"");
        assertThat(json).contains("\"role\":\"USER\"");
        assertThat(json).doesNotContain("password");
    }
}