package hexlet.code.app.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;

final class ControllerResponses {

    private ControllerResponses() {
    }

    static <T> ResponseEntity<List<T>> withTotalCount(List<T> items) {
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(items.size()))
            .body(items);
    }
}
