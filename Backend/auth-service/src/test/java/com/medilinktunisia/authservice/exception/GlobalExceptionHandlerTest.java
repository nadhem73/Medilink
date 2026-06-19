package com.medilinktunisia.authservice.exception;

import com.medilinktunisia.authservice.dto.response.MessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEmailAlreadyExists_returns409() {
        ResponseEntity<MessageResponse> response =
                handler.handleEmailExists(new EmailAlreadyExistsException("Email exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("Email exists");
    }

    @Test
    void handleDuplicateResource_returns409() {
        ResponseEntity<MessageResponse> response =
                handler.handleDuplicate(new DuplicateResourceException("Duplicate"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleBadCredentials_returns401() {
        ResponseEntity<MessageResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("Bad credentials"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleUsernameNotFound_returns401() {
        ResponseEntity<MessageResponse> response =
                handler.handleBadCredentials(new UsernameNotFoundException("Not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleGenericException_returns500() {
        ResponseEntity<MessageResponse> response =
                handler.handleGeneric(new RuntimeException("Unexpected error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
