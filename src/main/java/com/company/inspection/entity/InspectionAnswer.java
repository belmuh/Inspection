package com.company.inspection.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspection_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"photos"})
@Builder
public class InspectionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    @NotNull(message = "Inspection cannot be null")
    private Inspection inspection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @NotNull(message = "Question cannot be null")
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer", nullable = false, length = 10)
    @NotNull(message = "Answer cannot be null")
    private AnswerType answer;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InspectionPhoto> photos = new ArrayList<>();

    public enum AnswerType {
        YES,
        NO
    }

    // Helper methods
    public void addPhoto(InspectionPhoto photo) {
        photos.add(photo);
        photo.setAnswer(this);
    }

    public void removePhoto(InspectionPhoto photo) {
        photos.remove(photo);
        photo.setAnswer(null);
    }

    public boolean isYesAnswer() {
        return this.answer == AnswerType.YES;
    }

    public boolean isNoAnswer() {
        return this.answer == AnswerType.NO;
    }

    public boolean isValidYesAnswer() {
        if(answer == AnswerType.YES){
            return description != null && !description.trim().isEmpty() && photos != null && !photos.isEmpty();
        }
        return true;
    }

    public int getPhotoCount() {
        return photos != null ? photos.size() : 0;
    }
}
