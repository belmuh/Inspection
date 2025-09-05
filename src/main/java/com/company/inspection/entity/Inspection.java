package com.company.inspection.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
public class Inspection {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NotBlank(message = "Car ID cannot be blank")
    @Size(max = 100, message = "Car ID cannot exceed 100 characters")
    @Column(name = "car_id", nullable = false, length = 100)
    @ToString.Include
    private String carId;

    @Column(name = "inspection_date", nullable = false)
    @Builder.Default
    private LocalDateTime inspectionDate = LocalDateTime.now();

    @Column(name = "completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InspectionAnswer> answers = new ArrayList<>();

    public void addAnswer(InspectionAnswer answer) {
        answers.add(answer);
        answer.setInspection(this);
    }

    public void removeAnswer(InspectionAnswer answer) {
        answers.remove(answer);
        answer.setInspection(null);
    }

    public void markAsCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return !completed;
    }

}
