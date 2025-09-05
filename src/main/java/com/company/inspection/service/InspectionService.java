package com.company.inspection.service;

import com.company.inspection.dto.request.CreateInspectionRequest;
import com.company.inspection.dto.response.InspectionResponse;
import com.company.inspection.dto.response.QuestionResponse;
import com.company.inspection.entity.*;
import com.company.inspection.repository.InspectionRepository;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final QuestionService questionService;

    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final String YES_ANSWER = "YES";

    @WithSpan("inspection.getQuestions")
    @Transactional(readOnly = true)
    public InspectionResponse getInspectionQuestions(String carId) {
        log.debug("Getting inspection questions for car: {}", carId);

        List<Question> questions = questionService.getAllActiveQuestions();

        Optional<Inspection> latestInspection = inspectionRepository
                .findFirstByCarIdAndCompletedOrderByCreatedAtDesc(carId, true);

        InspectionResponse.InspectionResponseBuilder responseBuilder = InspectionResponse.builder()
                .carId(carId)
                .hasPreviousInspection(latestInspection.isPresent());

        latestInspection.ifPresent(inspection -> {
            responseBuilder
                    .inspectionId(inspection.getId())
                    .status(COMPLETED_STATUS)
                    .lastInspectionDate(inspection.getCreatedAt());
            log.debug("Found previous completed inspection for car: {} with ID: {}", carId, inspection.getId());
        });

        Map<Long, InspectionAnswer> previousAnswersMap = latestInspection
                .map(inspection -> inspection.getAnswers().stream()
                        .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a)))
                .orElse(Map.of());

        List<QuestionResponse> questionResponses = questions.stream()
                .map(question -> buildQuestionResponse(question, previousAnswersMap))
                .collect(Collectors.toList());

        return responseBuilder.questions(questionResponses).build();
    }

    private QuestionResponse buildQuestionResponse(Question question, Map<Long, InspectionAnswer> previousAnswersMap) {
        QuestionResponse.QuestionResponseBuilder builder = QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .orderIndex(question.getOrderIndex());

        InspectionAnswer previousAnswer = previousAnswersMap.get(question.getId());
        if (previousAnswer != null) {
            builder.previousAnswer(QuestionResponse.PreviousAnswer.builder()
                    .answer(previousAnswer.getAnswer().name())
                    .description(previousAnswer.getDescription())
                    .photos(previousAnswer.getPhotos().stream()
                            .map(photo -> QuestionResponse.PhotoInfo.builder()
                                    .url(photo.getPhotoUrl())
                                    .isNew(false)
                                    .build())
                            .toList())
                    .build());
        }

        return builder.build();
    }

    @WithSpan("inspection.create")
    @Transactional
    public InspectionResponse createInspection(CreateInspectionRequest request) {
        validateCreateInspectionRequest(request);

        Inspection inspection = Inspection.builder()
                .carId(request.getCarId())
                .inspectionDate(LocalDateTime.now())
                .completed(false)
                .build();

        inspectionRepository.save(inspection);

        request.getAnswers().forEach(answerRequest -> {
            InspectionAnswer answer = processAnswer(inspection, answerRequest);
            inspection.addAnswer(answer);
        });

        inspection.markAsCompleted();
        inspectionRepository.save(inspection);

        return buildInspectionResponse(inspection);
    }

    private InspectionAnswer processAnswer(Inspection inspection, CreateInspectionRequest.AnswerRequest answerRequest) {
        Question question = questionService.getQuestionById(answerRequest.getQuestionId());

        InspectionAnswer answer = InspectionAnswer.builder()
                .inspection(inspection)
                .question(question)
                .answer(InspectionAnswer.AnswerType.valueOf(answerRequest.getAnswer().toUpperCase()))
                .description(answerRequest.getDescription())
                .build();

        if (YES_ANSWER.equalsIgnoreCase(answerRequest.getAnswer()) && answerRequest.getPhotoUrls() != null) {
            List<InspectionPhoto> photos = answerRequest.getPhotoUrls().stream()
                    .map(url -> InspectionPhoto.builder()
                            .answer(answer)
                            .photoUrl(url)
                            .isNew(true)
                            .build())
                    .toList();
            answer.setPhotos(photos);
        }

        return answer;
    }

    private void validateCreateInspectionRequest(CreateInspectionRequest request) {
        if (request.getCarId() == null || request.getCarId().trim().isEmpty())
            throw new IllegalArgumentException("Car ID cannot be null or empty");
        if (request.getAnswers() == null || request.getAnswers().isEmpty())
            throw new IllegalArgumentException("Answers cannot be null or empty");

        request.getAnswers().forEach(this::validateAnswerRequest);
    }

    private void validateAnswerRequest(CreateInspectionRequest.AnswerRequest answer) {
        if (answer.getQuestionId() == null)
            throw new IllegalArgumentException("Question ID cannot be null");
        if (answer.getAnswer() == null || answer.getAnswer().trim().isEmpty())
            throw new IllegalArgumentException("Answer cannot be null or empty");

        if (YES_ANSWER.equalsIgnoreCase(answer.getAnswer())) {
            if (answer.getDescription() == null || answer.getDescription().trim().isEmpty())
                throw new IllegalArgumentException("Description required for YES answers");
            if (answer.getPhotoUrls() == null || answer.getPhotoUrls().isEmpty())
                throw new IllegalArgumentException("Photos required for YES answers");
        }
    }

    private InspectionResponse buildInspectionResponse(Inspection inspection) {
        return InspectionResponse.builder()
                .inspectionId(inspection.getId())
                .carId(inspection.getCarId())
                .status(COMPLETED_STATUS)
                .createdAt(inspection.getCreatedAt())
                .build();
    }
}
