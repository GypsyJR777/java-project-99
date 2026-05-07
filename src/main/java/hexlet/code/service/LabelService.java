package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateRequest;
import hexlet.code.dto.label.LabelResponse;
import hexlet.code.dto.label.LabelUpdateRequest;
import java.util.List;

public interface LabelService {

    List<LabelResponse> getAllLabels();

    LabelResponse getLabel(Long id);

    LabelResponse createLabel(LabelCreateRequest request);

    LabelResponse updateLabel(Long id, LabelUpdateRequest request);

    void deleteLabel(Long id);

    void createDefaultsIfMissing();
}
