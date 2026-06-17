package com.medilinktunisia.authservice.exception;

import com.medilinktunisia.authservice.dto.response.MessageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEmailExists_shouldReturnConflict() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Un compte existe déjà avec cet email");

        ResponseEntity<MessageResponse> response = handler.handleEmailExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Un compte existe déjà avec cet email");
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleDuplicate_shouldReturnConflict() {
        DuplicateResourceException ex = new DuplicateResourceException("Ce numéro CIN est déjà utilisé");

        ResponseEntity<MessageResponse> response = handler.handleDuplicate(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Ce numéro CIN est déjà utilisé");
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleDataIntegrity_shouldReturnConflict() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate key");

        ResponseEntity<MessageResponse> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("déjà utilisées");
    }

    @Test
    void handleBadCredentials_shouldReturnUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<MessageResponse> response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleUsernameNotFound_shouldReturnUnauthorized() {
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");

        ResponseEntity<MessageResponse> response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleValidation_shouldReturnBadRequest() {
        MethodArgumentNotValidException ex = createValidationException("email", "ne doit pas être vide");

        ResponseEntity<MessageResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("email");
        assertThat(response.getBody().getMessage()).contains("ne doit pas être vide");
    }

    @Test
    void handleGeneric_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<MessageResponse> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Something went wrong");
    }

    private MethodArgumentNotValidException createValidationException(String field, String message) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", field, message));
        return new MethodArgumentNotValidException(null, bindingResult);
    }
}
