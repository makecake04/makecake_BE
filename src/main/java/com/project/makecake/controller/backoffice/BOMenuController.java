package com.project.makecake.controller.backoffice;

import com.project.makecake.dto.backoffice.FindStoreIdRequestDto;
import com.project.makecake.dto.backoffice.FindStoreIdResponseDto;
import com.project.makecake.dto.backoffice.MenuAndOptionRequestDto;
import com.project.makecake.dto.backoffice.MenuAndOptionResponseDto;
import com.project.makecake.service.backoffice.BOMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BOMenuController {

    private final BOMenuService BOMenuService;

    // 케이크 메뉴와 옵션 데이터를 잘 입력했는지 확인 API
    @Secured("ROLE_ADMIN")
    @PostMapping("/back-office/menus/peek")
    public MenuAndOptionResponseDto peekMenuAndOption(@RequestBody MenuAndOptionRequestDto requestDto){
        return BOMenuService.peekMenuAndOption(requestDto);
    }

    // 케이크 메뉴와 옵션 데이터 저장 API
    @Secured("ROLE_ADMIN")
    @PostMapping("/back-office/menus/add")
    public String addMenuAndOption(@RequestBody MenuAndOptionRequestDto requestDto){
        return BOMenuService.addMenuAndOption(BOMenuService.peekMenuAndOption(requestDto));
    }

    // 케이크 매장명 검색 시 storeId 반환 API
    @Secured("ROLE_ADMIN")
    @PostMapping("/back-office/stores/find-store-id")
    public FindStoreIdResponseDto findStoreId(@RequestBody FindStoreIdRequestDto requestDto){
        return BOMenuService.findStoreId(requestDto);
    }

    // 케이크 메뉴 삭제 메소드
    @Secured("ROLE_ADMIN")
    @DeleteMapping ("back-office/menus/cake-menus")
    public void deleteCakeMenu(long storeId){
        BOMenuService.deleteCakeMenu(storeId);
    }

    // 케이크 옵션 삭제 메소드
    @Secured("ROLE_ADMIN")
    @DeleteMapping("back-office/menus/cake-options")
    public void deleteCakeOption(long storeId){
        BOMenuService.deleteCakeOption(storeId);
    }

}
