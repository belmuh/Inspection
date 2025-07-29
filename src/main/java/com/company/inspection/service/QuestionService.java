package com.company.inspection.service;

import com.company.inspection.entity.Question;
import com.company.inspection.exception.ResourceNotFoundException;
import com.company.inspection.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;

    /**
     * Get all active questions ordered by index
     */
    public List<Question> getAllActiveQuestions() {
        log.debug("Fetching all active questions");
        List<Question> questions = questionRepository.findByIsActiveTrueOrderByOrderIndex();
        log.debug("Found {} active questions", questions.size());
        return questions;
    }

    /**
     * Get all questions (including inactive) ordered by index
     */
    public List<Question> getAllQuestions() {
        log.debug("Fetching all questions");
        List<Question> questions = questionRepository.findAllByOrderByOrderIndex();
        log.debug("Found {} total questions", questions.size());
        return questions;
    }

    /**
     * Get question by ID
     */
    public Question getQuestionById(Long id) {
        log.debug("Fetching question with id: {}", id);
        return questionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Question not found with id: {}", id);
                    return new ResourceNotFoundException("Question not found with id: " + id);
                });
    }

    /**
     * Get question by order index
     */
    public Question getQuestionByOrderIndex(Integer orderIndex) {
        log.debug("Fetching question with order index: {}", orderIndex);
        Question question = questionRepository.findByOrderIndex(orderIndex);
        if (question == null) {
            log.error("Question not found with order index: {}", orderIndex);
            throw new ResourceNotFoundException("Question not found with order index: " + orderIndex);
        }
        return question;
    }

    /**
     * Search questions by text
     */
    public List<Question> searchQuestions(String searchText) {
        log.debug("Searching questions with text: {}", searchText);
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllActiveQuestions();
        }
        List<Question> questions = questionRepository.findActiveQuestionsByTextContaining(searchText.trim());
        log.debug("Found {} questions matching search text: {}", questions.size(), searchText);
        return questions;
    }

    /**
     * Create new question
     */
    @Transactional
    public Question createQuestion(String questionText) {
        log.debug("Creating new question: {}", questionText);

        // Get next order index
        Integer nextOrderIndex = questionRepository.findMaxOrderIndex() + 1;

        Question question = Question.builder()
                .questionText(questionText)
                .orderIndex(nextOrderIndex)
                .isActive(true)
                .build();

        Question savedQuestion = questionRepository.save(question);
        log.info("Created new question with id: {} and order index: {}", savedQuestion.getId(), nextOrderIndex);
        return savedQuestion;
    }

    /**
     * Update question text
     */
    @Transactional
    public Question updateQuestion(Long id, String questionText) {
        log.debug("Updating question with id: {}", id);

        Question question = getQuestionById(id);
        question.setQuestionText(questionText);

        Question updatedQuestion = questionRepository.save(question);
        log.info("Updated question with id: {}", id);
        return updatedQuestion;
    }

    /**
     * Activate/Deactivate question
     */
    @Transactional
    public Question toggleQuestionStatus(Long id) {
        log.debug("Toggling status for question with id: {}", id);

        Question question = getQuestionById(id);
        question.setIsActive(!question.getIsActive());

        Question updatedQuestion = questionRepository.save(question);
        log.info("Toggled question status with id: {} to {}", id, updatedQuestion.getIsActive());
        return updatedQuestion;
    }

    /**
     * Reorder question
     */
    @Transactional
    public void reorderQuestion(Long id, Integer newOrderIndex) {
        log.debug("Reordering question with id: {} to order index: {}", id, newOrderIndex);

        Question question = getQuestionById(id);
        Integer oldOrderIndex = question.getOrderIndex();

        if (oldOrderIndex.equals(newOrderIndex)) {
            log.debug("Question already at order index: {}", newOrderIndex);
            return;
        }

        // Update order indexes of other questions
        if (newOrderIndex < oldOrderIndex) {
            // Moving up: increment order indexes between new and old position
            List<Question> questionsToUpdate = questionRepository
                    .findByIsActiveTrueAndOrderIndexBetweenOrderByOrderIndex(newOrderIndex, oldOrderIndex - 1);
            questionsToUpdate.forEach(q -> q.setOrderIndex(q.getOrderIndex() + 1));
            questionRepository.saveAll(questionsToUpdate);
        } else {
            // Moving down: decrement order indexes between old and new position
            List<Question> questionsToUpdate = questionRepository
                    .findByIsActiveTrueAndOrderIndexBetweenOrderByOrderIndex(oldOrderIndex + 1, newOrderIndex);
            questionsToUpdate.forEach(q -> q.setOrderIndex(q.getOrderIndex() - 1));
            questionRepository.saveAll(questionsToUpdate);
        }

        // Update target question
        question.setOrderIndex(newOrderIndex);
        questionRepository.save(question);

        log.info("Reordered question with id: {} from {} to {}", id, oldOrderIndex, newOrderIndex);
    }

    /**
     * Delete question (soft delete by deactivating)
     */
    @Transactional
    public void deleteQuestion(Long id) {
        log.debug("Deleting question with id: {}", id);

        Question question = getQuestionById(id);
        question.setIsActive(false);
        questionRepository.save(question);

        log.info("Soft deleted question with id: {}", id);
    }

    /**
     * Get total count of active questions
     */
    public long getActiveQuestionCount() {
        long count = questionRepository.countByIsActiveTrue();
        log.debug("Total active questions count: {}", count);
        return count;
    }

    /**
     * Check if question exists by order index
     */
    public boolean questionExistsByOrderIndex(Integer orderIndex) {
        boolean exists = questionRepository.existsByOrderIndex(orderIndex);
        log.debug("Question exists with order index {}: {}", orderIndex, exists);
        return exists;
    }
}