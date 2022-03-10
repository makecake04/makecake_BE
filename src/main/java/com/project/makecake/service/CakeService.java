package com.project.makecake.service;

import com.project.makecake.model.Cake;
import com.project.makecake.model.CakeLike;
import com.project.makecake.model.User;
import com.project.makecake.repository.CakeLikeRepository;
import com.project.makecake.repository.CakeRepository;
import com.project.makecake.repository.UserRepository;
import com.project.makecake.requestDto.LikeRequestDto;
import com.project.makecake.responseDto.LikeResponseDto;
import com.project.makecake.responseDto.CakeResponseDto;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CakeService {

    private final UserRepository userRepository;
    private final CakeRepository cakeRepository;
    private final CakeLikeRepository cakeLikeRepository;

    // 케이크 사진 리스트 불러오기 메소드
    @Transactional
    public List<CakeResponseDto> getAllCakes(UserDetailsImpl userDetails) {

        // 비로그인 유저는 null 처리
        User user = null;
        if (userDetails!=null) {
            user = userDetails.getUser();
        }

        // 일단 15개만 가져오기
        Sort sort = Sort.by(Sort.Direction.DESC,"likeCnt");
        Pageable pageable = PageRequest.of(0,15,sort);
        Page<Cake> foundCakeList = cakeRepository.findAll(pageable);

        // 반환 Dto에 담기 + 좋아요 반영
        List<CakeResponseDto> responseDtoList = new ArrayList<>();
        for (Cake cake : foundCakeList) {
            boolean myLike = false; // myLike 디폴트 : false
            if(user!=null) { // 로그인 유저는 좋아요 여부 반영
                Optional<CakeLike> foundCakeLike = cakeLikeRepository.findByUserAndCake(user,cake);
                if (foundCakeLike.isPresent()) {
                    myLike = true;
                }
            }
            CakeResponseDto responseDto = new CakeResponseDto(cake,myLike);
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 케이크 좋아요
    @Transactional
    public LikeResponseDto cakeLike(Long cakeId, LikeRequestDto requestDto, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        // 케이크 찾기
        Cake foundCake = cakeRepository.findById(cakeId)
                .orElseThrow(()->new IllegalArgumentException("케이크가 존재하지 않습니다."));

        // myLike가 true이면 새로운 cakeLike 저장
        if (requestDto.isMyLike()) {
            CakeLike cakeLike = new CakeLike(foundCake, user);
            cakeLikeRepository.save(cakeLike);
        // myLike가 false이면 기존 cakeLike 삭제
        } else {
            cakeLikeRepository.deleteByUserAndCake(user,foundCake);
        }
        // likeCnt 변경
        boolean likeResult = foundCake.likeCake(requestDto.isMyLike());
        return new LikeResponseDto(likeResult);

    }

}
