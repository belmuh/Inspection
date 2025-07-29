package com.company.inspection.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {

    private Long id;
    private String questionText;
    private Integer orderIndex;
    private PreviousAnswer previousAnswer; // Only included if there's a previous inspection

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PreviousAnswer {
        private String answer; // "YES" or "NO"
        private String description;
        private List<PhotoInfo> photos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PhotoInfo {
        private String url;
        private Boolean isNew; // false for previous photos, true for new photos

        // Utility methods
        public boolean isNewPhoto() {
            return Boolean.TRUE.equals(this.isNew);
        }

        public boolean isPreviousPhoto() {
            return Boolean.FALSE.equals(this.isNew);
        }
    }

    // Utility methods
    public boolean hasPreviousAnswer() {
        return this.previousAnswer != null;
    }

    public boolean hasPreviousPhotos() {
        return this.previousAnswer != null &&
                this.previousAnswer.getPhotos() != null &&
                !this.previousAnswer.getPhotos().isEmpty();
    }

    public int getPreviousPhotoCount() {
        if (!hasPreviousPhotos()) {
            return 0;
        }
        return this.previousAnswer.getPhotos().size();
    }
}