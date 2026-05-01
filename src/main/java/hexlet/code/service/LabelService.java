package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateRequest;
import hexlet.code.dto.label.LabelResponse;
import hexlet.code.dto.label.LabelUpdateRequest;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LabelService {

    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;

    public LabelService(LabelRepository labelRepository, TaskRepository taskRepository) {
        this.labelRepository = labelRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> getAllLabels() {
        return labelRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public LabelResponse getLabel(Long id) {
        return toResponse(findLabel(id));
    }

    @Transactional
    public LabelResponse createLabel(LabelCreateRequest request) {
        var label = new Label();
        label.setName(request.name());
        return toResponse(labelRepository.save(label));
    }

    @Transactional
    public LabelResponse updateLabel(Long id, LabelUpdateRequest request) {
        var label = findLabel(id);
        if (request.name() != null) {
            label.setName(request.name());
        }
        return toResponse(labelRepository.save(label));
    }

    @Transactional
    public void deleteLabel(Long id) {
        var label = findLabel(id);
        if (taskRepository.existsByLabelsId(id)) {
            throw new DataIntegrityViolationException("Label is linked to task");
        }
        labelRepository.delete(label);
    }

    @Transactional
    public void createDefaultsIfMissing() {
        createDefaultIfMissing("feature");
        createDefaultIfMissing("bug");
    }

    private void createDefaultIfMissing(String name) {
        if (labelRepository.existsByName(name)) {
            return;
        }
        var label = new Label();
        label.setName(name);
        labelRepository.save(label);
    }

    private Label findLabel(Long id) {
        return labelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
    }

    private LabelResponse toResponse(Label label) {
        return new LabelResponse(
            label.getId(),
            label.getName(),
            label.getCreatedAt() == null ? null : label.getCreatedAt().toLocalDate()
        );
    }
}
