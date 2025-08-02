package com.company.inspection.service;

import com.company.inspection.dto.request.CreateInspectionRequest;
import com.company.inspection.dto.response.InspectionResponse;
import com.company.inspection.dto.response.QuestionResponse;
import com.company.inspection.entity.Inspection;
import com.company.inspection.entity.InspectionAnswer;
import com.company.inspection.entity.InspectionPhoto;
import com.company.inspection.entity.Question;
import com.company.inspection.exception.ResourceNotFoundException;
import com.company.inspection.repository.InspectionAnswerRepository;
import com.company.inspection.repository.InspectionPhotoRepository;
import com.company.inspection.repository.InspectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionAnswerRepository inspectionAnswerRepository;

    @Mock
    private InspectionPhotoRepository inspectionPhotoRepository;

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private InspectionService inspectionService;

    private String carId;
    private List<Question> mockQuestions;
    private Inspection mockInspection;
    private Inspection mockInProgressInspection;
    private CreateInspectionRequest createRequest;

    @BeforeEach
    void setUp(){
        carId = "CAR123";
        setupMockQuestions();
        setupMockInspection();
        setupMockInProgressInspection();
        setupCreateRequest();
    }

    // Read Method Tests
    @Test
    void shouldGetInspectionQuestionsForNewCar() {
        // Given - İlk kez ekspertiz yapılan araç
        when(questionService.getAllActiveQuestions()).thenReturn(mockQuestions);
        when(inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        when(inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.COMPLETED)).thenReturn(Optional.empty());

        // When
        InspectionResponse response = inspectionService.getInspectionQuestions(carId);

        // Then
        assertNotNull(response);
        assertEquals(carId, response.getCarId());
        assertFalse(response.getHasPreviousInspection());
        assertNull(response.getLastInspectionDate());
        assertNull(response.getInspectionId());
        assertNull(response.getStatus());
        assertEquals(2, response.getQuestions().size());

        // Verify no previous answers exist
        response.getQuestions().forEach(question ->
                assertNull(question.getPreviousAnswer())
        );

        verify(questionService).getAllActiveQuestions();
        verify(inspectionRepository).findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.IN_PROGRESS);
        verify(inspectionRepository).findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.COMPLETED);



    }

    @Test
    void shouldGetInspectionQuestionsWithPreviousCompletedData() {
        // Given - Önceki ekspertizi olan araç
        when(questionService.getAllActiveQuestions()).thenReturn(mockQuestions);
        when(inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(carId, Inspection.InspectionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        when(inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(carId, Inspection.InspectionStatus.COMPLETED))
                .thenReturn(Optional.of(mockInspection));

        List<InspectionAnswer> mockAnswers = createMockAnswersWithPhotos();
        when(inspectionAnswerRepository.findByInspectionIdWithPhotos(mockInspection.getId())).thenReturn(mockAnswers);

        // When
        InspectionResponse response = inspectionService.getInspectionQuestions(carId);

        // Then
        assertNotNull(response);
        assertEquals(carId, response.getCarId());
        assertTrue(response.getHasPreviousInspection());
        assertNotNull(response.getLastInspectionDate());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(2, response.getQuestions().size());

        // First question should have previous answer with photos
        QuestionResponse firstQuestion = response.getQuestions().get(0);
        assertNotNull(firstQuestion.getPreviousAnswer());
        assertEquals("YES", firstQuestion.getPreviousAnswer().getAnswer());
        assertEquals(2, firstQuestion.getPreviousAnswer().getPhotos().size());

        verify(questionService).getAllActiveQuestions();
        verify(inspectionAnswerRepository).findByInspectionIdWithPhotos(mockInspection.getId());
    }

    @Test
    void shouldGetInspectionQuestionWithInProgressData(){
        InspectionAnswer inProgressAnswer = new InspectionAnswer();
        inProgressAnswer.setId(3L);
        inProgressAnswer.setQuestion(mockQuestions.get(0));
        inProgressAnswer.setAnswer(InspectionAnswer.AnswerType.YES);
        mockInProgressInspection.setAnswers(Arrays.asList(inProgressAnswer));

        when(inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(carId,Inspection.InspectionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(mockInProgressInspection));

        when(questionService.getAllActiveQuestions()).thenReturn(mockQuestions);

        List<InspectionAnswer> mockAnswers = createMockAnswersWithPhotos();
        when(inspectionAnswerRepository.findByInspectionIdWithPhotos(mockInProgressInspection.getId())).thenReturn(mockAnswers);

        InspectionResponse response = inspectionService.getInspectionQuestions(carId);

        assertNotNull(response);
        assertEquals(carId, response.getCarId());
        assertTrue(response.getHasPreviousInspection());
        assertNotNull(response.getLastInspectionDate());
        assertEquals(mockInProgressInspection.getId(), response.getInspectionId());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals(2, response.getQuestions().size());

        QuestionResponse firstQuestion = response.getQuestions().get(0);
        assertNotNull(firstQuestion.getPreviousAnswer());
        assertEquals("YES", firstQuestion.getPreviousAnswer().getAnswer());

        QuestionResponse secondQuestion = response.getQuestions().get(1);
        assertNull(secondQuestion.getPreviousAnswer());

        verify(inspectionRepository).findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.IN_PROGRESS);
        verify(questionService).getAllActiveQuestions();
        verify(inspectionRepository, never()).findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.COMPLETED);


    }

    // Create Method Tests
    @Test
    void shouldCreateNewInspectionSuccessfully() {
        // Given
        Question question1 = mockQuestions.get(0);
        Question question2 = mockQuestions.get(1);

        when(inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        when(questionService.getQuestionById(1L)).thenReturn(question1);
        when(questionService.getQuestionById(2L)).thenReturn(question2);
        when(inspectionRepository.save(any(Inspection.class))).thenReturn(mockInspection);
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        InspectionAnswer savedAnswer1 = createMockAnswer(1L, InspectionAnswer.AnswerType.YES);
        InspectionAnswer savedAnswer2 = createMockAnswer(2L, InspectionAnswer.AnswerType.NO);
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class)))
                .thenReturn(savedAnswer1)
                .thenReturn(savedAnswer2);

        when(inspectionPhotoRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // When
        InspectionResponse response = inspectionService.createInspection(createRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockInspection.getId(), response.getInspectionId());
        assertEquals(carId, response.getCarId());
        assertEquals("COMPLETED", response.getStatus());

        // Verify inspection was saved twice (create + complete)
        verify(inspectionRepository, times(2)).save(any(Inspection.class));
        verify(inspectionAnswerRepository, times(2)).save(any(InspectionAnswer.class));
        verify(inspectionPhotoRepository).saveAll(anyList()); // For YES answer photos
    }

    @Test
    void shouldUpdateExistingInProgressInspection(){
        Question question1 = mockQuestions.get(0);
        Question question2 = mockQuestions.get(1);

        when(inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.IN_PROGRESS)).thenReturn(Optional.of(mockInProgressInspection));
        when(inspectionRepository.save(any(Inspection.class))).thenReturn(mockInProgressInspection);

        InspectionAnswer existingAnswer1 = createMockAnswer(1L, InspectionAnswer.AnswerType.NO);
        InspectionAnswer existingAnswer2 = createMockAnswer(2L, InspectionAnswer.AnswerType.NO);

        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(mockInProgressInspection.getId(), 1L))
                .thenReturn(Optional.of(existingAnswer1));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(mockInProgressInspection.getId(), 2L))
                .thenReturn(Optional.of(existingAnswer2));
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class)))
                .thenReturn(existingAnswer1)
                .thenReturn(existingAnswer2);

        InspectionResponse response = inspectionService.createInspection(createRequest);

        assertNotNull(response);
        assertEquals(mockInProgressInspection.getId(),response.getInspectionId());
        assertEquals(carId, response.getCarId());
        assertEquals("COMPLETED", response.getStatus());

        verify(inspectionRepository, times(2)).save(mockInProgressInspection);
        verify(inspectionAnswerRepository, times(2)).save(any(InspectionAnswer.class));
        verify(inspectionAnswerRepository, times(2)).findByInspectionIdAndQuestionId(anyLong(), anyLong());
        verify(inspectionPhotoRepository, times(1)).saveAll(anyList());

    }

    @Test
    void shouldValidateCarIdIsRequired() {
        // Given
        createRequest.setCarId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inspectionService.createInspection(createRequest)
        );

        assertEquals("Car ID cannot be null or empty", exception.getMessage());
        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void shouldValidateAnswersAreRequired() {
        // Given
        createRequest.setAnswers(Collections.emptyList());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inspectionService.createInspection(createRequest)
        );

        assertEquals("Answers cannot be null or empty", exception.getMessage());
        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void shouldValidateYesAnswerRequiresDescription() {
        // Given - YES answer without description
        createRequest.getAnswers().get(0).setAnswer("YES");
        createRequest.getAnswers().get(0).setDescription("");
        createRequest.getAnswers().get(0).setPhotoUrls(Arrays.asList("photo1.jpg"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inspectionService.createInspection(createRequest)
        );

        assertEquals("Description is required for YES answers", exception.getMessage());
    }

    @Test
    void shouldValidateYesAnswerRequiresPhotos() {
        // Given - YES answer without photos
        createRequest.getAnswers().get(0).setAnswer("YES");
        createRequest.getAnswers().get(0).setDescription("Valid description");
        createRequest.getAnswers().get(0).setPhotoUrls(Collections.emptyList());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inspectionService.createInspection(createRequest)
        );

        assertEquals("At least one photo is required for YES answers", exception.getMessage());
    }

    @Test
    void shouldValidateMaximumPhotoCount() {
        // Given - More than 3 photos
        List<String> tooManyPhotos = Arrays.asList(
                "photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg"
        );
        createRequest.getAnswers().get(0).setAnswer("YES");
        createRequest.getAnswers().get(0).setPhotoUrls(tooManyPhotos);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inspectionService.createInspection(createRequest)
        );

        assertEquals("Maximum 3 photos allowed per answer", exception.getMessage());
    }

    @Test
    void shouldAllowNoPhotosForNoAnswer() {
        // Given - NO answer without photos (valid case)
        createRequest.getAnswers().get(0).setAnswer("NO");
        createRequest.getAnswers().get(0).setPhotoUrls(Collections.emptyList());
        createRequest.getAnswers().get(0).setDescription("");

        Question question1 = mockQuestions.get(0);
        Question question2 = mockQuestions.get(1);

        when(inspectionRepository.findFirstByCarIdAndStatusOrderByCreatedAtDesc(
                carId, Inspection.InspectionStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        when(questionService.getQuestionById(1L)).thenReturn(question1);
        when(questionService.getQuestionById(2L)).thenReturn(question2);
        when(inspectionRepository.save(any(Inspection.class))).thenReturn(mockInspection);
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class)))
                .thenReturn(createMockAnswer(1L, InspectionAnswer.AnswerType.NO));

        // When
        InspectionResponse response = inspectionService.createInspection(createRequest);

        // Then
        assertNotNull(response);
        verify(inspectionPhotoRepository, never()).saveAll(anyList()); // No photos should be saved
    }


    // Utility method tests

    @Test
    void shouldGetInspectionById(){
        // Given
        Long inspectionId = 1L;
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(mockInspection));

        // When
        Inspection result = inspectionService.getInspectionById(inspectionId);

        // Then
        assertNotNull(result);
        assertEquals(mockInspection.getId(), result.getId());
        verify(inspectionRepository).findById(inspectionId);
    }

    @Test
    void shouldThrowExceptionWhenInspectionNotFound(){
        // Given
       Long inspectionId = 999L;
       when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> inspectionService.getInspectionById(inspectionId));
        assertEquals("Inspection not found with id: " + inspectionId, exception.getMessage());
    }

    @Test
    void shouldGetInspectionsByCarId(){
        // Given
        List<Inspection> mockInspections = Arrays.asList(mockInspection);
        when(inspectionRepository.findByCarIdOrderByCreatedAtDesc(carId)).thenReturn(mockInspections);

        // When
        List<Inspection> result = inspectionService.getInspectionsByCarId(carId);

        // Then
       assertNotNull(result);
       assertEquals(1, result.size());
       assertEquals(mockInspection.getId(), result.get(0).getId());
       verify(inspectionRepository).findByCarIdOrderByCreatedAtDesc(carId);
    }

    @Test
    void shouldCheckCarHasPreviousInspections(){
        // Given
        when(inspectionRepository.existsByCarIdAndStatus(carId, Inspection.InspectionStatus.COMPLETED)).thenReturn(true);

        // When
        boolean result = inspectionService.carHasPreviousInspections(carId);

        // Then
        assertTrue(result);
        verify(inspectionRepository).existsByCarIdAndStatus(carId, Inspection.InspectionStatus.COMPLETED);
    }

    // Helper methods
    private void setupMockQuestions(){
        mockQuestions = Arrays.asList(
                createQuestion(1L, "Araçta hasar var mı?",1),
                createQuestion(2l, "Lastikler yıpranmış mı?", 2)
        );
    }

    private Question createQuestion(Long id, String text, int orderIndex){
        Question question = new Question();
        question.setId(id);
        question.setQuestionText(text);
        question.setOrderIndex(orderIndex);
        question.setCreatedAt(LocalDateTime.now());
        return question;
    }

    private void setupMockInspection(){
        mockInspection = new Inspection();
        mockInspection.setId(1L);
        mockInspection.setCarId(carId);
        mockInspection.setStatus(Inspection.InspectionStatus.COMPLETED);
        mockInspection.setCreatedAt(LocalDateTime.now());
    }

    private void setupMockInProgressInspection(){
        mockInProgressInspection = new Inspection();
        mockInProgressInspection.setId(2L);
        mockInProgressInspection.setCarId(carId);
        mockInProgressInspection.setStatus(Inspection.InspectionStatus.IN_PROGRESS);
        mockInProgressInspection.setCreatedAt(LocalDateTime.now());

    }

    private InspectionAnswer createMockAnswer(Long questionId, InspectionAnswer.AnswerType answerType){
        InspectionAnswer answer = new InspectionAnswer();
        answer.setId(1L);
        answer.setAnswer(answerType);
        answer.setDescription(answerType == InspectionAnswer.AnswerType.YES ? "Test description" : "");
        return answer;
    }

    private List<InspectionAnswer> createMockAnswersWithPhotos() {
        InspectionAnswer answer1 = new InspectionAnswer();
        answer1.setId(1L);
        answer1.setAnswer(InspectionAnswer.AnswerType.YES);
        answer1.setDescription("Some damage found");
        answer1.setQuestion(mockQuestions.get(0));

        InspectionPhoto photo1 = new InspectionPhoto();
        photo1.setId(1L);
        photo1.setPhotoUrl("http://example.com/photo1.jpg");
        photo1.setIsNew(false);

        InspectionPhoto photo2 = new InspectionPhoto();
        photo2.setId(2L);
        photo2.setPhotoUrl("http://example.com/photo2.jpg");
        photo2.setIsNew(false);

        answer1.setPhotos(Arrays.asList(photo1, photo2));

        return Arrays.asList(answer1);
    }

    private void setupCreateRequest(){
        createRequest = new CreateInspectionRequest();
        createRequest.setCarId(carId);

        CreateInspectionRequest.AnswerRequest answer1 = new CreateInspectionRequest.AnswerRequest();
        answer1.setQuestionId(1L);
        answer1.setAnswer("YES");
        answer1.setDescription("Test description");
        answer1.setPhotoUrls(Arrays.asList("photo1.jpg", "photo2.jpg"));

        CreateInspectionRequest.AnswerRequest answer2 = new CreateInspectionRequest.AnswerRequest();
        answer2.setQuestionId(2L);
        answer2.setAnswer("NO");
        answer2.setDescription("");
        answer2.setPhotoUrls(Collections.emptyList());

        createRequest.setAnswers(Arrays.asList(answer1, answer2));
    }
}
