package com.project.makecake.service;

import com.project.makecake.dto.ImageInfoDto;
import com.project.makecake.enums.DesignState;
import com.project.makecake.enums.FolderName;
import com.project.makecake.model.*;
import com.project.makecake.repository.*;
import com.project.makecake.dto.LikeDto;
import com.project.makecake.dto.PostRequestDto;
import com.project.makecake.dto.DesignResponseDto;
import com.project.makecake.dto.PostDetailResponseDto;
import com.project.makecake.dto.PostSimpleResponseDto;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final DesignRepository designRepository;
    private final S3UploadService s3UploadService;
    private final StoreRepository storeRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    // 게시된 도안 사진 리스트 조회 메소드
    public List<PostSimpleResponseDto> getPostList(UserDetailsImpl userDetails, int page, String sortType) {

        // 비로그인 유저는 null 처리
        User user = null;
        if (userDetails!=null) {
            user = userDetails.getUser();
        }

        // 15개씩 가져오기
        Page<Post> foundPostList;
        if (sortType==null || sortType.equals("createdDate")) {
            Sort sort = Sort.by(Sort.Direction.DESC,"postId");
            Pageable pageable = PageRequest.of(page,18,sort);
            foundPostList = postRepository.findAll(pageable);
        } else {
            Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,sortType), new Sort.Order(Sort.Direction.DESC,"postId"));
            Pageable pageable = PageRequest.of(page,18,sort);
            foundPostList = postRepository.findAll(pageable);
        }

        // 반환 Dto에 담기 + 좋아요 반영
        List<PostSimpleResponseDto> responseDtoList = new ArrayList<>();
        for (Post post : foundPostList) {
            boolean myLike = false; // myLike 디폴트 : false
            if(user!=null) { // 로그인 유저는 좋아요 여부 반영
                Optional<PostLike> foundPostLike = postLikeRepository.findByUserAndPost(user,post);
                if (foundPostLike.isPresent()) {
                    myLike = true;
                }
            }
            PostSimpleResponseDto responseDto = new PostSimpleResponseDto(post,myLike);
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 도안 그리고 난 후 이미지 저장 메소드
    @Transactional
    public DesignResponseDto addDesign(UserDetailsImpl userDetails, MultipartFile img) throws IOException {
        User user = userDetails.getUser();

        // S3에 이미지 업로드하고 업로드 정보 받아오기
        ImageInfoDto imgInfo = s3UploadService.uploadFile(img, FolderName.DESIGN.name());

        // 디비에 저장
        Design design = new Design(imgInfo,user);
        Design savedDesign = designRepository.save(design);

        return new DesignResponseDto(savedDesign);
    }

    // 도안 게시글 작성 메소드
    @Transactional
    public HashMap<String,Long> addPost(Long designId, UserDetailsImpl userDetails, PostRequestDto requestDto) {
        User user = userDetails.getUser();

        // 도안 찾기
        Design foundDesign = designRepository.findById(designId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 도안입니다."));

        // 이미 게시글이 작성된 도안일 경우 exception (도안 하나당 게시글 하나만 작성 가능)
        if (foundDesign.getState().equals(DesignState.POST)) {
            throw new IllegalArgumentException("이미 게시된 도안입니다.");
        }

        // 도안 게시글 저장
        // 제작 매장을 기입한 경우
        HashMap<String,Long> responseDto = new HashMap<>();

        if (requestDto.isMade()&&requestDto.getStoreId()!=null) {
            Store foundStore = storeRepository.findById(requestDto.getStoreId())
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 매장입니다."));
            Post post = new Post(requestDto,user,foundDesign,foundStore);
            Post savedPost = postRepository.save(post);
            foundDesign.post(); // 도안 게시글 상태 POST로 변경
            responseDto.put("postId",savedPost.getPostId());

        // 제작 매장을 기입하지 않은 경우
        } else {
            Post post = new Post(requestDto,user,foundDesign);
            Post savedPost = postRepository.save(post);
            foundDesign.post(); // 도안 게시글 상태 POST로 변경
            responseDto.put("postId",savedPost.getPostId());
        }
        return responseDto;
    }

    // 도안 게시글 수정 메소드
    @Transactional
    public void editPost(Long postId, UserDetailsImpl userDetails, PostRequestDto requestDto) {
        User user = userDetails.getUser();

        // 게시글 찾기
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 동일 유저인지 확인
        if (!foundPost.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("다른 사람이 쓴 게시글은 수정할 수 없습니다.");
        }

        // 게시글 수정
        // 제작 매장을 기입한 경우
        if (requestDto.isMade()&&requestDto.getStoreId()!=null) {
            Store foundStore = storeRepository.findById(requestDto.getStoreId())
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 매장입니다."));
            foundPost.update(requestDto,foundStore);
            postRepository.save(foundPost);
        // 제작 매장을 기입하지 않은 경우
        } else {
            foundPost.update(requestDto);
            postRepository.save(foundPost);
        }
    }

    // 도안 게시글 삭제
    @Transactional
    public void deletePost(Long postId, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        // 게시글 찾기
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 동일 유저인지 확인
        if (!foundPost.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("다른 사람의 게시글은 삭제할 수 없습니다.");
        }

        // 도안과 관계 끊고 도안을 UNPOST로 바꾸기
        Design connectDesign = foundPost.getDesign();
        foundPost.deleteRelation();
        connectDesign.unpost();

        // 도안 게시글 좋아요 삭제
        postLikeRepository.deleteAllByPost(foundPost);

        // 도안 게시글 댓글 삭제
        commentRepository.deleteAllByPost(foundPost);

        // 게시글 삭제
        postRepository.delete(foundPost);
    }

    // 도안 게시글 좋아요
    @Transactional
    public LikeDto likePost(Long postId, LikeDto requestDto, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        // 게시글 찾기
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // myLike가 true이면 새로운 postLike 저장
        if (requestDto.isMyLike()) {
            PostLike postLike = new PostLike(foundPost, user);
            postLikeRepository.save(postLike);
            // myLike가 false이면 기존 postLike 삭제
        } else {
            postLikeRepository.deleteByUserAndPost(user,foundPost);
        }
        // likeCnt 변경
        boolean likeResult = foundPost.likePost(requestDto.isMyLike());
        return new LikeDto(likeResult);
    }

    // 도안 게시글 상세 조회
    @Transactional
    public PostDetailResponseDto getPostDetails(Long postId, UserDetailsImpl userDetails) {

        // 비로그인 유저는 null 처리
        User user = null;
        if (userDetails!=null) {
            user = userDetails.getUser();
        }

        // 게시글 찾기
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 조회수 추가
        foundPost.viewPost();

        // myLike 디폴트 false
        boolean myLike = false;

        // 로그인한 회원이면 좋아요 내역 찾아서 반영
        if(user!=null) {
            Optional<PostLike> foundPostLike = postLikeRepository.findByUserAndPost(user,foundPost);
            if (foundPostLike.isPresent()) {
                myLike = true;
            }
        }

        // 댓글 수 세기
        int commentCnt = commentRepository.countByPost(foundPost).intValue();

        return new PostDetailResponseDto(foundPost, myLike, commentCnt);
    }

    // 도안 삭제
    public void removeDesign(UserDetailsImpl userDetails, Long designId) {

        // 도안 찾기
        Design foundDesign = designRepository.findById(designId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 도안입니다."));

        // 게시되지 않은 도안인지 확인
        if (foundDesign.getState().equals(DesignState.POST)) {
            throw new IllegalArgumentException("게시중인 도안은 삭제할 수 없습니다.");
        }

        // 동일 유저인지 확인
        if (!foundDesign.getUser().getUserId().equals(userDetails.getUser().getUserId())) {
            throw new IllegalArgumentException("다른 사람의 도안은 삭제할 수 없습니다.");
        }

        designRepository.deleteById(designId);
    }
}
