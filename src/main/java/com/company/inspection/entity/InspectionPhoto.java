package com.company.inspection.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class InspectionPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id" , nullable = false)
    @NotNull(message = "Inspection answer cannot be null")
    private InspectionAnswer answer;

    @NotBlank(message = "Photo URL cannot be blank")
    @Size(max = 500, message = "Photo URL cannot exceed 500 characters")
    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(name = "is_new", nullable = false)
    @Builder.Default
    private Boolean isNew = true;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    // Helper methods
    public boolean isNewPhoto(){
        return Boolean.TRUE.equals(this.isNew);
    }

    public boolean isPreviousPhoto(){
        return Boolean.FALSE.equals(this.isNew);
    }

    public void markAsNew() {
        this.isNew = true;
    }

    public void markAsPrevious() {
        this.isNew = false;
    }

    // Get file name from URL (utility method)
    public String getFileName() {
       if(photoUrl != null && photoUrl.contains("/")){
           return photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
       }
       return photoUrl;
    }

    // Get file extension (utility method)
    public String getFileExtension() {
        String fileName = getFileName();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    // Check if photo is image format
    public boolean isImageFormat() {
        String extension = getFileExtension();
        return extension.matches("jpg|jpeg|png|gif|bmp|webp");
    }
}
