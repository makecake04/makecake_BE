package com.project.makecake.controller;

import com.project.makecake.model.Cake;
import com.project.makecake.dto.CakeIdRequestDto;
import com.project.makecake.dto.LikeDto;
import com.project.makecake.dto.CakeResponseDto;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.CakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class CakeController {

    private final CakeService cakeService;

    // 케이크 사진 리스트 조회 API (18개씩)
    @GetMapping("/api/cakes")
    public List<CakeResponseDto> getCakeList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam int page
    ) {
        return cakeService.getCakeList(userDetails, page);
    }

    // 케이크 사진 상세 조회 API
    @PostMapping("/api/cakes/detail")
    public CakeResponseDto getCakeDetails(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CakeIdRequestDto requestDto
    ) {
        return cakeService.getCakeDetails(userDetails, requestDto.getCakeId());
    }

    // 케이크 좋아요 생성 및 삭제 API
    @PostMapping("/cakes/like/{cakeId}")
    public LikeDto saveCakeLike(
            @PathVariable long cakeId,
            @RequestBody LikeDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return cakeService.saveCakeLike(cakeId, requestDto, userDetails);
    }


    // (관리자용) 가게별 케이크 사진 리스트 조회 API
    @GetMapping("/api/temp/cakes/{storeId}")
    public List<Cake> GetCakeListAtBackoffice(@PathVariable long storeId) {
        return cakeService.GetCakeListAtBackoffice(storeId);
    }

    // (관리자용) 케이크 사진 삭제 API
    @DeleteMapping("/api/temp/cakes/{cakeId}")
    public long deleteCake(@PathVariable long cakeId) {
        return cakeService.deleteCake(cakeId);
    }

    // (관리자용) 케이크 사진 저장 API
    @PostMapping("/api/temp/cakes/{storeId}")
    public void addCakeList(
            @PathVariable long storeId,
            @RequestParam List<MultipartFile> imgFileList
    ) throws IOException {
        cakeService.addCakeList(storeId, imgFileList);
    }

}
