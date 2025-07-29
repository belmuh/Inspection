package com.company.inspection.repository;

import com.company.inspection.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Find all active questions ordered by orderIndex
    List<Question> findByIsActiveTrueOrderByOrderIndex();

    // Find all questions ordered by orderIndex (including inactive)
    List<Question> findAllByOrderByOrderIndex();

    // Find active questions by order index range
    List<Question> findByIsActiveTrueAndOrderIndexBetweenOrderByOrderIndex(Integer startIndex, Integer endIndex);

    // Check if question with specific order index exists
    boolean existsByOrderIndex(Integer orderIndex);

    // Find question by order index
    Question findByOrderIndex(Integer orderIndex);

    // Count active questions
    long countByIsActiveTrue();

    // Custom query to find questions with specific text pattern
    @Query("SELECT q FROM Question q WHERE q.isActive = true AND LOWER(q.questionText) LIKE LOWER(CONCAT('%', :searchText, '%')) ORDER BY q.orderIndex")
    List<Question> findActiveQuestionsByTextContaining(String searchText);

    // Get max order index for new question positioning
    @Query("SELECT COALESCE(MAX(q.orderIndex),0) FROM Question q")
    Integer findMaxOrderIndex();

    // Find questions that need reordering after deletion
    @Query("SELECT q FROM Question q WHERE q.orderIndex > :deletedOrderIndex ORDER BY q.orderIndex")
    List<Question> findQuestionsToReorder(Integer deletedOrderIndex);

}
