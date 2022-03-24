package com.project.makecake.dto;

import com.project.makecake.model.Review;
import lombok.Getter;

import java.util.List;
import java.util.List;

@Getter

public class ReviewResponseDto {
    private Long reviewId;
    private String writerNickname;
    private String createdDate;
    private String content;
    private List<String> reviewImages;

    public ReviewResponseDto(Review review, List<String> reviewImages){
        this.reviewId = review.getReviewId();
        this.writerNickname = review.getUser().getNickname();
        this.createdDate = review.getCreatedAt();
        this.content = review.getContent();
        this.reviewImages = reviewImages;
    }
}
