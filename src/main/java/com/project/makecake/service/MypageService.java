package com.project.makecake.service;

import com.project.makecake.dto.*;
import com.project.makecake.model.*;
import com.project.makecake.repository.*;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MypageService {

    private final UserRepository userRepository;
    private final DesignRepository designRepository;
    private final CakeLikeRepository cakeLikeRepository;
    private final CommentRepository commentRepository;
    private final StoreLikeRepository storeLikeRepository;
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final PostLikeRepository postLikeRepository;

    // 마이프로필 조회 메소드
    public MypageResponseDto getMyProfile(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인을 해주세요.");
        }

        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("유저가 존재하지 않습니다.")
        );
        String email = foundUser.getUsername();
        if (foundUser.getProviderEmail() != null){
            email = foundUser.getProviderEmail();
        }
        MypageResponseDto responseDto = MypageResponseDto.builder()
                .nickname(foundUser.getNickname())
                .profileImg(foundUser.getProfileImgUrl())
                .email(email)
                .build();
        return responseDto;
    }

    // 내가 그린 도안 조회 메소드
    public List<MyDesignResponseDto> getMyDesignList(UserDetailsImpl userDetails, String option, int page) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인을 해주세요.");
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자입니다.")
        );

        Pageable pageable = PageRequest.of(page, 18);
        List<MyDesignResponseDto> responseDtoList = new ArrayList<>();
        if (option.equals("nonpost")){
            Page<Design> foundDesignList = designRepository.findByUserAndPost(foundUser, false, pageable);
            for (Design design : foundDesignList){
                MyDesignResponseDto responseDto = MyDesignResponseDto.builder()
                        .designId(design.getDesignId())
                        .img(design.getImgUrl())
                        .build();
                responseDtoList.add(responseDto);
            }
        } else if (option.equals("post")){
            Page<Post> foundPostList = postRepository.findByUser(foundUser, pageable);
            for (Post post : foundPostList){
                MyDesignResponseDto responseDto = MyDesignResponseDto.builder()
                        .postId(post.getPostId())
                        .designId(post.getDesign().getDesignId())
                        .img(post.getDesign().getImgUrl())
                        .build();
                responseDtoList.add(responseDto);
            }
        }
        return responseDtoList;
    }

    // 내가 게시 안 한 도안 상세 조회 메소드
    public DesignResponseDto getDesignDetails(Long designId, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인을 해주세요.");
        }

        // 도안 찾기
        Design foundDesign = designRepository.findById(designId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 도안입니다."));

        DesignResponseDto responseDto = new DesignResponseDto(foundDesign);

        return responseDto;
    }

    // 내가 좋아요 한 게시글 조회 메소드
    public List<MyReactPostResponceDto> getMyLikePostList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인을 해주세요.");
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("유저가 존재하지 않습니다.")
        );
        Pageable pageable = PageRequest.of(page, 5);
        Page<PostLike> foundPostList = postLikeRepository.findByUser(foundUser, pageable);
        List<MyReactPostResponceDto> responseDtoList = new ArrayList<>();
        for (PostLike postLike : foundPostList){
            MyReactPostResponceDto responseDto = MyReactPostResponceDto.builder()
                    .postId(postLike.getPost().getPostId())
                    .img(postLike.getPost().getDesign().getImgUrl())
                    .nickname(postLike.getPost().getUser().getNickname())
                    .profileImg(postLike.getPost().getUser().getProfileImgUrl())
                    .content(postLike.getPost().getContent())
                    .createdDate(postLike.getPost().getCreatedAt())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 내가 남긴 댓글 조회 메소드
    public List<MyCommentResponseDto> getMyCommentList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인을 해주세요.");
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자입니다.")
        );
        Pageable pageable = PageRequest.of(page, 5);
        Page<Comment> foundCommentList = commentRepository.findByUser(foundUser, pageable);
        List<MyCommentResponseDto> responseDtoList = new ArrayList<>();
        for (Comment comment : foundCommentList){
            MyCommentResponseDto responseDto = MyCommentResponseDto.builder()
                    .commentId(comment.getCommentId())
                    .content(comment.getContent())
                    .createdDate(comment.getCreatedAt())
                    .postId(comment.getPost().getPostId())
                    .postTitle(comment.getPost().getTitle())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 내가 좋아요 한 매장 조회 메소드
    public List<MyReactStoreResponseDto> getMyLikeStoreList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인을 해주세요.");
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자입니다.")
        );
        Pageable pageable = PageRequest.of(page, 8);
        Page<StoreLike> foundStoreLikeList = storeLikeRepository.findByUser(foundUser, pageable);
        List<MyReactStoreResponseDto> responseDtoList = new ArrayList<>();
        for (StoreLike storeLike : foundStoreLikeList){
            String address = storeLike.getStore().getFullAddress();
            String[] custom = address.split(" ");
            String addressSimple = custom[0] + " " + custom[1] + " " + custom[2];
            MyReactStoreResponseDto responseDto = MyReactStoreResponseDto.builder()
                    .storeId(storeLike.getStore().getStoreId())
                    .name(storeLike.getStore().getName())
                    .addressSimple(addressSimple)
                    .mainImg(storeLike.getStore().getMainImg())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 내가 남긴 후기 조회 메소드
    public List<MyReviewResponseDto> getMyReviewList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인을 해주세요.");
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자입니다.")
        );
        Pageable pageable = PageRequest.of(page, 5);
        Page<Review> foundReviewList = reviewRepository.findByUser(foundUser, pageable);
        List<MyReviewResponseDto> responseDtoList = new ArrayList<>();
        for (Review review : foundReviewList){
            ReviewImg reviewImg = reviewImgRepository.findTop1ByReview(review);
//            String reviewImgUrl = "https://makecake.s3.ap-northeast-2.amazonaws.com/PROFILE/18d2090b-1b98-4c34-b92b-a9f50d03bd53makecake_default.png";
            String reviewImgUrl = "";
            if (reviewImg != null) {
                reviewImgUrl = reviewImg.getImgUrl();
            }
//            List<ReviewImg> findReviewImg = reviewImgRepository.findByReview(review);
//            List<String> reviewImgList = new ArrayList<>();
//            if (findReviewImg != null){
//                for (int i = 0; i < findReviewImg.size(); i++){
//                    String reviewImg = findReviewImg.get(i).getImgName();
//                    reviewImgList.add(reviewImg);
//                }
//            }
            MyReviewResponseDto responseDto = MyReviewResponseDto.builder()
                    .reviewId(review.getReviewId())
                    .storeId(review.getStore().getStoreId())
                    .name(review.getStore().getName())
                    .content(review.getContent())
                    .createdDate(review.getCreatedAt())
//                    .reviewImages(reviewImgList)
                    .reviewImages(reviewImgUrl)
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 내가 좋아요 한 케이크 조회 메소드
    public List<MyReactCakeResponseDto> getMyLikeCakeList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인을 해주세요.");
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자입니다.")
        );
        Pageable pageable = PageRequest.of(page, 8);
        Page<CakeLike> foundCakeList = cakeLikeRepository.findByUser(foundUser, pageable);
        List<MyReactCakeResponseDto> responseDtoList = new ArrayList<>();
        for (CakeLike cakeLike : foundCakeList) {
            MyReactCakeResponseDto responseDto = MyReactCakeResponseDto.builder()
                    .cakeId(cakeLike.getCake().getCakeId())
                    .img(cakeLike.getCake().getUrl())
                    .storeName(cakeLike.getCake().getStore().getName())
                    .likeCnt(cakeLike.getCake().getLikeCnt())
                    .myLike(true)
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

}
