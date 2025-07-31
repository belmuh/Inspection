package com.company.inspection.service;

import com.company.inspection.dto.request.CreateInspectionRequest;
import com.company.inspection.dto.response.InspectionResponse;
import com.company.inspection.dto.response.QuestionResponse;
import com.company.inspection.entity.*;
import com.company.inspection.exception.ResourceNotFoundException;
import com.company.inspection.repository.InspectionAnswerRepository;
import com.company.inspection.repository.InspectionPhotoRepository;
import com.company.inspection.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final InspectionAnswerRepository answerRepository;
    private final InspectionPhotoRepository photoRepository;
    private final QuestionService questionService;

    /**
     * READ METHOD: Get questions with previous inspection data for a car
     * This method implements the Read requirement from the test case
     */
    public InspectionResponse getInspectionQuestions(String carId) {
        log.debug("Getting inspection questions for car: {}", carId);

        // Get all active questions
        List<Question> questions = questionService.getAllActiveQuestions();

        // ÖNCE yarım kalan inspection'ı ara
        Optional<Inspection> draftInspection = inspectionRepository
                .findFirstByCarIdAndStatusOrderByCreatedAtDesc(carId, Inspection.InspectionStatus.IN_PROGRESS);

        // Get latest completed inspection for this car
        Optional<Inspection> completedInspection = draftInspection.isEmpty() ? inspectionRepository
                .findFirstByCarIdAndStatusOrderByCreatedAtDesc(carId, Inspection.InspectionStatus.COMPLETED):
                Optional.empty();

        // Hangisi varsa onu kullan (öncelik draft'ta)
        Optional<Inspection> latestInspection = draftInspection.or(() -> completedInspection);

        // Build response
        InspectionResponse.InspectionResponseBuilder responseBuilder = InspectionResponse.builder()
                .carId(carId)
                .hasPreviousInspection(latestInspection.isPresent());

        if (latestInspection.isPresent()) {
            responseBuilder
                    .inspectionId(latestInspection.get().getId()) // Denetim ID'sini ekle
                    .status(latestInspection.get().getStatus().name()) // Durum bilgisini ekle
                    .lastInspectionDate(latestInspection.get().getCreatedAt());
            log.debug("Found previous inspection for car: {} with ID: {} at {}",
                    carId, latestInspection.get().getId(), latestInspection.get().getCreatedAt());

        }

        // Get previous answers if exists
        Map<Long, InspectionAnswer> previousAnswersMap = latestInspection
                .map(inspection -> answerRepository.findByInspectionIdWithPhotos(inspection.getId())
                        .stream()
                        .collect(Collectors.toMap(answer -> answer.getQuestion().getId(), answer -> answer)))
                .orElse(Map.of());

        // Build question responses
        List<QuestionResponse> questionResponses = questions.stream()
                .map(question -> {
                    QuestionResponse.QuestionResponseBuilder questionBuilder = QuestionResponse.builder()
                            .id(question.getId())
                            .questionText(question.getQuestionText())
                            .orderIndex(question.getOrderIndex());

                    // Add previous answer data if exists
                    InspectionAnswer previousAnswer = previousAnswersMap.get(question.getId());
                    if (previousAnswer != null) {
                        QuestionResponse.PreviousAnswer prevAnswer = QuestionResponse.PreviousAnswer.builder()
                                .answer(previousAnswer.getAnswer().name())
                                .description(previousAnswer.getDescription())
                                .photos(previousAnswer.getPhotos().stream()
                                        .map(photo -> QuestionResponse.PhotoInfo.builder()
                                                .url(photo.getPhotoUrl())
                                                .isNew(false) // Previous photos are not new
                                                .build())
                                        .collect(Collectors.toList()))
                                .build();
                        questionBuilder.previousAnswer(prevAnswer);
                    }

                    return questionBuilder.build();
                })
                .collect(Collectors.toList());

        InspectionResponse response = responseBuilder
                .questions(questionResponses)
                .build();

        log.info("Retrieved {} questions for car: {}, has previous inspection: {}",
                questions.size(), carId, latestInspection.isPresent());

        return response;
    }

    /**
     * CREATE METHOD: Create new inspection with answers
     * This method implements the Create requirement from the test case
     */
    @Transactional
    public InspectionResponse createInspection(CreateInspectionRequest request) {
        log.debug("Creating new inspection for car: {}", request.getCarId());

        // Validate request
        validateCreateInspectionRequest(request);

        Optional<Inspection> existingInspection = inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                        request.getCarId(), Inspection.InspectionStatus.IN_PROGRESS);

        final Inspection inspectionToSave;

        if (existingInspection.isPresent()) {
            // Mevcut bir denetim varsa, onu güncelle
            inspectionToSave = existingInspection.get();
            log.debug("Found an existing IN_PROGRESS inspection with id: {}. Updating it.",
                    inspectionToSave.getId());
        } else {
            // Create new inspection
            inspectionToSave = Inspection.builder()
                    .carId(request.getCarId())
                    .inspectionDate(LocalDateTime.now())
                    .status(Inspection.InspectionStatus.IN_PROGRESS)
                    .build();

            log.debug("No existing IN_PROGRESS inspection found. Creating a new one.");
        }
        Inspection savedInspection = inspectionRepository.save(inspectionToSave);
        log.debug("Saved inspection with id: {}", savedInspection.getId());

        // Process answers
        List<InspectionAnswer> savedAnswers = new ArrayList<>();
        for (CreateInspectionRequest.AnswerRequest answerRequest : request.getAnswers()) {
            InspectionAnswer answer = processAnswer(savedInspection, answerRequest);
            savedAnswers.add(answer);
        }

        // Mark inspection as completed
        savedInspection.markAsCompleted();
        inspectionRepository.save(savedInspection);

        log.info("Successfully created inspection with id: {} for car: {} with {} answers",
                savedInspection.getId(), request.getCarId(), savedAnswers.size());

        // Return response
        return InspectionResponse.builder()
                .inspectionId(savedInspection.getId())
                .carId(savedInspection.getCarId())
                .status(savedInspection.getStatus().name())
                .createdAt(savedInspection.getCreatedAt())
                .build();
    }

    /**
     * Process individual answer with photos
     */
    private InspectionAnswer processAnswer(Inspection inspection, CreateInspectionRequest.AnswerRequest answerRequest) {
        log.debug("Processing answer for question: {}", answerRequest.getQuestionId());
        Optional<InspectionAnswer> existingAnswer = answerRepository.findByInspectionIdAndQuestionId(inspection.getId(), answerRequest.getQuestionId());

        final InspectionAnswer answerToSave;

        if (existingAnswer.isPresent()) {
            // exists answer - update
            answerToSave = existingAnswer.get();
            answerToSave.setAnswer(InspectionAnswer.AnswerType.valueOf(answerRequest.getAnswer().toUpperCase()));
            answerToSave.setDescription(answerRequest.getDescription());
            // Diğer alanları da ihtiyaca göre güncelleyebilirsin
            log.debug("Updating existing answer with id: {}", answerToSave.getId());
        } else {

            // create
            Question question = questionService.getQuestionById(answerRequest.getQuestionId());

            // Create answer
            answerToSave = InspectionAnswer.builder()
                    .inspection(inspection)
                    .question(question)
                    .answer(InspectionAnswer.AnswerType.valueOf(answerRequest.getAnswer().toUpperCase()))
                    .description(answerRequest.getDescription())
                    .build();
        }
        InspectionAnswer savedAnswer = answerRepository.save(answerToSave);

        // Process photos for YES answers
        if (savedAnswer.isYesAnswer() && answerRequest.getPhotoUrls() != null) {
            List<InspectionPhoto> photos = answerRequest.getPhotoUrls().stream()
                    .map(photoUrl -> InspectionPhoto.builder()
                            .answer(savedAnswer)
                            .photoUrl(photoUrl)
                            .isNew(true) // New photos are always marked as new
                            .build())
                    .collect(Collectors.toList());

            List<InspectionPhoto> savedPhotos = photoRepository.saveAll(photos);
            log.debug("Saved {} photos for answer: {}", savedPhotos.size(), savedAnswer.getId());
        }

        return savedAnswer;
    }

    /**
     * Validate create inspection request
     */
    private void validateCreateInspectionRequest(CreateInspectionRequest request) {
        if (request.getCarId() == null || request.getCarId().trim().isEmpty()) {
            throw new IllegalArgumentException("Car ID cannot be null or empty");
        }

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("Answers cannot be null or empty");
        }

        // Validate each answer
        for (CreateInspectionRequest.AnswerRequest answerRequest : request.getAnswers()) {
            validateAnswerRequest(answerRequest);
        }
    }

    /**
     * Validate individual answer request
     */
    private void validateAnswerRequest(CreateInspectionRequest.AnswerRequest answerRequest) {
        if (answerRequest.getQuestionId() == null) {
            throw new IllegalArgumentException("Question ID cannot be null");
        }

        if (answerRequest.getAnswer() == null || answerRequest.getAnswer().trim().isEmpty()) {
            throw new IllegalArgumentException("Answer cannot be null or empty");
        }

        // Validate YES answers
        if ("YES".equalsIgnoreCase(answerRequest.getAnswer())) {
            if (answerRequest.getDescription() == null || answerRequest.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Description is required for YES answers");
            }

            if (answerRequest.getPhotoUrls() == null || answerRequest.getPhotoUrls().isEmpty()) {
                throw new IllegalArgumentException("At least one photo is required for YES answers");
            }

            if (answerRequest.getPhotoUrls().size() > 3) {
                throw new IllegalArgumentException("Maximum 3 photos allowed per answer");
            }

            // Validate photo URLs
            for (String photoUrl : answerRequest.getPhotoUrls()) {
                if (photoUrl == null || photoUrl.trim().isEmpty()) {
                    throw new IllegalArgumentException("Photo URL cannot be null or empty");
                }
            }
        }
    }

    /**
     * Get inspection by ID
     */
    public Inspection getInspectionById(Long inspectionId) {
        log.debug("Getting inspection by id: {}", inspectionId);
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> {
                    log.error("Inspection not found with id: {}", inspectionId);
                    return new ResourceNotFoundException("Inspection not found with id: " + inspectionId);
                });
    }

    /**
     * Get all inspections for a car
     */
    public List<Inspection> getInspectionsByCarId(String carId) {
        log.debug("Getting inspections for car: {}", carId);
        List<Inspection> inspections = inspectionRepository.findByCarIdOrderByCreatedAtDesc(carId);
        log.debug("Found {} inspections for car: {}", inspections.size(), carId);
        return inspections;
    }

    /**
     * Get latest completed inspection for a car
     */
    public Optional<Inspection> getLatestCompletedInspection(String carId) {
        log.debug("Getting latest completed inspection for car: {}", carId);
        return inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.COMPLETED);
    }

    /**
     * Check if car has any previous inspections
     */
    public boolean carHasPreviousInspections(String carId) {
        boolean hasPrevious = inspectionRepository.existsByCarIdAndStatus(
                carId, Inspection.InspectionStatus.COMPLETED);
        log.debug("Car {} has previous inspections: {}", carId, hasPrevious);
        return hasPrevious;
    }
}