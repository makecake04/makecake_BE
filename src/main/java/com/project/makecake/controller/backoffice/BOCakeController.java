package com.project.makecake.controller.backoffice;

import com.project.makecake.dto.openapi.OpenApiImgDto;
import com.project.makecake.model.Cake;
import com.project.makecake.model.Store;
import com.project.makecake.service.backoffice.BOCakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BOCakeController {

    private final BOCakeService BOCakeService;

    // (관리자용) 가게별 케이크 사진 리스트 조회 API
    @GetMapping("/back-office/stores/{storeId}/cakes")
    public List<Cake> GetCakeListAtBackoffice(@PathVariable long storeId) {
        return BOCakeService.GetCakeListAtBackoffice(storeId);
    }

    // (관리자용) 케이크 사진 삭제 API
    @DeleteMapping("/back-office/cakes/{cakeId}")
    public long deleteCake(@PathVariable long cakeId) {
        return BOCakeService.deleteCake(cakeId);
    }

    // (관리자용) 케이크 사진 저장 API
    @PostMapping("/back-office/stores/{storeId}/cakes")
    public void addCakeList(
            @PathVariable long storeId,
            @RequestParam List<MultipartFile> imgFileList
    ) throws IOException {
        BOCakeService.addCakeList(storeId, imgFileList);
    }
}