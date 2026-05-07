package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    @Test
    void handlesUnexpectedExceptions() {
        var handler = new GlobalExceptionHandler();

        var response = handler.handleException(new Exception("Unexpected"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("error", "Internal server error");
    }
}
