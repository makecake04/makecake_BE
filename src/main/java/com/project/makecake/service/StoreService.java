package com.project.makecake.service;

import com.project.makecake.dto.*;
import com.project.makecake.model.*;
import com.project.makecake.repository.*;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreLikeRepository storeLikeRepository;
    private final StoreUrlRepository storeUrlRepository;
    private final MenuRepository menuRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final CakeRepository cakeRepository;
    private final CakeLikeRepository cakeLikeRepository;

    //홈탭 : 핫 매장 리스트
    @Transactional
    public List<HomeStoreDto> getHomeStoreList() {
        List<HomeStoreDto> homeStoreDtoList = new ArrayList<>();

        List<Store> rawList = storeRepository.findTop5ByOrderByLikeCntDesc();

        for(Store eachStore : rawList){
            Long storeId = eachStore.getStoreId();
            String name = eachStore.getName();
            String mainImg = eachStore.getMainImg();
            int likeCnt = eachStore.getLikeCnt();

            HomeStoreDto homeStoreDto = new HomeStoreDto(storeId, name, mainImg,likeCnt);
            homeStoreDtoList.add(homeStoreDto);
        }
        return homeStoreDtoList;
    }

    //매장 좋아요
    @Transactional
    public Boolean likeStore(Boolean myLike, Long storeId, User user) {
        //true 추가(좋아요 누르기), false 삭제(좋아요 취소)
        Store store = storeRepository.getById(storeId);

        if (myLike) {
            //storeLike에 추가하기 (user 구현 완료 시 넣기)
            StoreLike storeLike = new StoreLike();
            storeLike.setStore(store);
            storeLike.setUser(user);
            storeLikeRepository.save(storeLike);
            store.setLikeCnt(store.getLikeCnt() +1);
            storeRepository.save(store);
            return true;
        } else {
            storeLikeRepository.deleteByStoreAndUser(store, user);
            store.setLikeCnt(store.getLikeCnt() -1);
            storeRepository.save(store);
            return false;
        }
    }

    //매장 상세
    @Transactional
    public StoreDetailResponseDto getStoreDetail(Long storeId, UserDetailsImpl userDetails) {
        StoreDetailResponseDto responseDto = new StoreDetailResponseDto();
        Store store = storeRepository.findById(storeId).get();
        responseDto.setStoreId(store.getStoreId());
        responseDto.setMainImg(store.getMainImg());
        responseDto.setName(store.getName());
        responseDto.setRoadAddress(store.getRoadAddress());
        responseDto.setFullAddress(store.getFullAddress());
        responseDto.setDescription(store.getDescription());
        responseDto.setOpenTimeString(store.getOpenTimeString());
        responseDto.setLikeCnt(store.getLikeCnt());

        Boolean myLike = false;

        System.out.println("마이라이크");
        if(userDetails != null){
            if(storeLikeRepository.findByStoreAndUser(store, userDetails.getUser()) != null){
                myLike = true;
            }
        }
        responseDto.setMyLike(myLike);

        //urls
        List<StoreDetailUrlDto> urls = new ArrayList<>();
        List<StoreUrl> rawUrlList = storeUrlRepository.findAllByStore_StoreId(storeId);
        for(StoreUrl rawUrl : rawUrlList){
            StoreDetailUrlDto urlDto = new StoreDetailUrlDto();
            urlDto.setUrl(rawUrl.getUrl());
            urlDto.setType(rawUrl.getType());
            urls.add(urlDto);
        }
        responseDto.setUrls(urls);

        //menus
        List<StoreDetailMenuDto> menus = new ArrayList<>();
        List<Menu> rawMenuList = menuRepository.findAllByStore_StoreId(storeId);
        for(Menu rawMenu : rawMenuList){
            StoreDetailMenuDto menuDto = new StoreDetailMenuDto();
            menuDto.setName(rawMenu.getName());
            menuDto.setPrice(rawMenu.getPrice());
            menuDto.setChanges(rawMenu.getChanges());
            menus.add(menuDto);
        }
        responseDto.setMenus(menus);

        //reviews 최근 3개만
        List<ReviewResponseDto> reviews = new ArrayList<>();
        List<Review> rawReviewList = reviewRepository.findTop3ByStoreOrderByCreatedAtDesc(store);
        for(Review rawReview : rawReviewList){
            ReviewResponseDto reviewDto = new ReviewResponseDto();
            long reviewId = rawReview.getReviewId();
            reviewDto.setReviewId(reviewId);
            reviewDto.setWriterNickname(rawReview.getUser().getNickname());
            reviewDto.setCreatedDate(rawReview.getCreatedAt());
            reviewDto.setContent(rawReview.getContent());

            List<String> reviewImages = new ArrayList<>();
            List<ReviewImg> rawReviewImgList = reviewImgRepository.findAllByReview_ReviewId(reviewId);
            for(ReviewImg rawReviewImg : rawReviewImgList){
                reviewImages.add(rawReviewImg.getImgUrl());
            }

            reviewDto.setReviewImages(reviewImages);

            reviews.add(reviewDto);
        }
        responseDto.setReviews(reviews);

        //cakeImages 최근 9개만
        List<StoreDetailCakeResponseDto> cakeImages = new ArrayList<>();
        List<Cake> rawCakeList = cakeRepository.findTop9ByStoreOrderByCreatedAtDesc(store);
        for(Cake rawCake : rawCakeList){
            StoreDetailCakeResponseDto cakeDto = new StoreDetailCakeResponseDto();
            cakeDto.setCakeId(rawCake.getCakeId());
            cakeDto.setImg(rawCake.getUrl());
            cakeDto.setLikeCnt(rawCake.getLikeCnt());

            Boolean myCakeLike = false;
            if(userDetails != null){
                User user = userDetails.getUser();
                if(cakeLikeRepository.findByUserAndCake(user, rawCake).isPresent()){
                    myCakeLike = true;
                }
            }
            cakeDto.setMyLike(myCakeLike);
            cakeImages.add(cakeDto);
        }
        responseDto.setCakeImages(cakeImages);
        return responseDto;
    }

    //매장 상세정보- 케이크
    @Transactional
    public List<StoreDetailCakeResponseDto> getStoreDetailCakes(Long storeId, UserDetailsImpl userDetails) {
        List<StoreDetailCakeResponseDto> responseDto = new ArrayList<>();
        Store store = storeRepository.getById(storeId);

        //이 부분 무한 스크롤로 구현 시 수정 필요함
        List<Cake> rawCakeList = cakeRepository.findTop9ByStoreOrderByCreatedAtDesc(store);

        //이 아래부분 겹치는 코드 - 코드 리팩토링 필요
        for(Cake rawCake : rawCakeList){
            StoreDetailCakeResponseDto cakeDto = new StoreDetailCakeResponseDto();
            cakeDto.setCakeId(rawCake.getCakeId());
            cakeDto.setImg(rawCake.getUrl());
            cakeDto.setLikeCnt(rawCake.getLikeCnt());

            Boolean myCakeLike = false;
            if(userDetails != null){
                User user = userDetails.getUser();
                if(cakeLikeRepository.findByUserAndCake(user, rawCake).isPresent()){
                    myCakeLike = true;
                }
            }
            cakeDto.setMyLike(myCakeLike);
            responseDto.add(cakeDto);
        }
        return responseDto;
    }

    //매장 상세정보 - 리뷰
    @Transactional
    public List<ReviewResponseDto> getStoreDetailReviews(Long storeId){
        List<ReviewResponseDto> reviews = new ArrayList<>();
        Store store = storeRepository.getById(storeId);

        //이 부분 무한 스크롤로 구현 시 수정 필요함
        List<Review> rawReviewList = reviewRepository.findTop3ByStoreOrderByCreatedAtDesc(store);

        //이 아래부분 겹치는 코드 - 코드 리팩토링 필요
        for(Review rawReview : rawReviewList){
            ReviewResponseDto reviewDto = new ReviewResponseDto();
            long reviewId = rawReview.getReviewId();
            reviewDto.setReviewId(reviewId);
            reviewDto.setWriterNickname(rawReview.getUser().getNickname());
            reviewDto.setCreatedDate(rawReview.getCreatedAt());
            reviewDto.setContent(rawReview.getContent());

            List<String> reviewImages = new ArrayList<>();
            List<ReviewImg> rawReviewImgList = reviewImgRepository.findAllByReview_ReviewId(reviewId);
            for(ReviewImg rawReviewImg : rawReviewImgList){
                reviewImages.add(rawReviewImg.getImgUrl());
            }

            reviewDto.setReviewImages(reviewImages);

            reviews.add(reviewDto);
        }
        return reviews;
    }
}
