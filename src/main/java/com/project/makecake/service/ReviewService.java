package com.project.makecake.service;

import com.project.makecake.dto.HomeReviewDto;
import com.project.makecake.dto.ImageInfoDto;
import com.project.makecake.dto.ReviewResponseTempDto;
import com.project.makecake.enums.FolderName;
import com.project.makecake.model.*;
import com.project.makecake.repository.ReviewImgRepository;
import com.project.makecake.repository.ReviewRepository;
import com.project.makecake.repository.StoreRepository;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final S3UploadService s3UploadService;
    private final StoreRepository storeRepository;

    // (홈탭) 최신 매장 후기 조회 메소드 (5개)
    public List<HomeReviewDto> getReviewListAtHome() {
        List<Review> foundReviewList = reviewRepository.findTop5ByOrderByCreatedAtDesc();

        List<HomeReviewDto> responseDtoList = new ArrayList<>();
        for(Review review : foundReviewList){
            Store store = review.getStore();

            long reviewId = review.getReviewId();

            String img = "https://makecake.s3.ap-northeast-2.amazonaws.com/PROFILE/%EC%97%B0%ED%95%9C%EC%BC%80%EC%9D%B4%ED%81%AC.png";
            if(!reviewImgRepository.findAllByReview_ReviewId(reviewId).isEmpty()){
                img = reviewImgRepository.findAllByReview_ReviewId(reviewId).get(0).getImgUrl();
            }

            HomeReviewDto responseDto = HomeReviewDto.builder()
                    .reviewId(reviewId)
                    .nickname(review.getUser().getNickname())
                    .createdDate(review.getCreatedAt())
                    .content(review.getContent())
                    .storeId(store.getStoreId())
                    .storeName(store.getName())
                    .img(img)
                    .build();

            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 매장 후기 작성 메소드
    @Transactional
    public void addReview(long storeId, String content, List<MultipartFile> imgFileList, UserDetailsImpl userDetails) throws IOException {
        User user = userDetails.getUser();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 매장입니다."));

        // 리뷰 본문 저장
        Review review = Review.builder()
                .user(user)
                .store(store)
                .content(content)
                .build();

        reviewRepository.save(review);

        // 이미지 S3 업로드 및 DB 저장
        if(imgFileList != null){
            for(MultipartFile imgFile : imgFileList){
                ImageInfoDto imageInfoDto = s3UploadService.uploadFile(imgFile, FolderName.REVIEW.name());

                ReviewImg reviewImg = ReviewImg.builder()
                        .imgInfo(imageInfoDto)
                        .review(review)
                        .build();

                reviewImgRepository.save(reviewImg);
            }
        }
        // 매장 reviewCnt 변경
        boolean bool = true;
        store.countReview(bool);

        storeRepository.save(store);
    }

    // 매장 후기 삭제 메소드
    @Transactional
    public void deleteReview(long reviewId){
        Store store = reviewRepository.getById(reviewId).getStore();

        reviewImgRepository.deleteAllByReview_ReviewId(reviewId);
        reviewRepository.deleteById(reviewId);

        boolean bool = false;
        store.countReview(bool);
        storeRepository.save(store);
    }

    // 매장 후기 수정 메소드
    @Transactional
    public void editReview(long reviewId, String content, List<MultipartFile> imgFileList, List<String> imgUrlList, UserDetailsImpl userDetails) throws IOException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        if(!review.getUser().equals(userDetails.getUser())){
            throw new IllegalArgumentException("다른 회원이 작성한 리뷰는 수정할 수 없습니다.");
        }

        //imgUrlList (imgUrlList에서 없는 url을 삭제)
        if(reviewImgRepository.findAllByReview_ReviewId(reviewId) != null){
            List<ReviewImg> foundReviewImgList = reviewImgRepository.findAllByReview_ReviewId(reviewId);

            // DB에서 찾아온 foundReviewImgList, foundImgUrlList
            if(foundReviewImgList.size() != imgUrlList.size()){
                List<String> foundImgUrlList = new ArrayList<>();

                for(int i=0; i< foundReviewImgList.size(); i++){
                    foundImgUrlList.add(foundReviewImgList.get(i).getImgUrl());
                }

                // 삭제할 이미지 리스트 = 원래 이미지 리스트 - 삭제 되지 않은 이미지 리스트
                foundImgUrlList.removeAll(imgUrlList);

                for(int j=0; j< foundImgUrlList.size(); j++){
                    //s3에서 삭제
                    ReviewImg reviewImg = reviewImgRepository.findByImgUrl(foundImgUrlList.get(j));
                    s3UploadService.deleteFile(reviewImg.getImgName());

                    //db에서 삭제
                    reviewImgRepository.deleteByImgUrl(foundImgUrlList.get(j));
                }
            }
        }

        // 새로운 이미지를 추가한 경우
        if(imgFileList != null){
            for(MultipartFile imgFile : imgFileList){
                ImageInfoDto imageInfoDto = s3UploadService.uploadFile(imgFile, FolderName.REVIEW.name());
                ReviewImg reviewImg = ReviewImg.builder()
                        .imgInfo(imageInfoDto)
                        .review(review)
                        .build();
                reviewImgRepository.save(reviewImg);
            }
        }

        // 후기 본문 덮어쓰기
        review.edit(content);
        reviewRepository.save(review);
    }

    // 매장 후기 상세 조회 메소드
    public ReviewResponseTempDto getReviewDetails(long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        List<ReviewImg> foundReviewImgList = reviewImgRepository.findAllByReview_ReviewId(reviewId);

        String reviewImage = "";

        if(foundReviewImgList.size() != 0){
            reviewImage = foundReviewImgList.get(0).getImgUrl();
        }

        return new ReviewResponseTempDto(review, reviewImage);
    }
}
