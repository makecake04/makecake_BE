package com.project.makecake.dto;

import com.project.makecake.model.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class PostDetailResponseDto {
    private Long postId;
    private String title;
    private Long designId;
    private String img;
    private String profileImg;
    private String nickname;
    private int likeCnt;
    private boolean myLike;
    private int viewCnt;
    private int commentCnt;
    private String createdDate;
    private String content;
    private String size;
    private String shape;
    private String purpose;
    private boolean made;
    private String storeName;

    // 생성자
    public PostDetailResponseDto(Post post, boolean myLike, int commentCnt) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.designId = post.getDesign().getDesignId();
        this.img = post.getDesign().getImgUrl();
        this.profileImg = post.getUser().getProfileImgUrl();
        this.nickname = post.getUser().getNickname();
        this.likeCnt = post.getLikeCnt();
        this.myLike = myLike;
        this.viewCnt = post.getViewCnt();
        this.commentCnt = commentCnt;
        this.createdDate = post.getCreatedAt();
        this.content = post.getContent();
        this.size = post.getSize();
        this.shape = post.getShape();
        this.purpose = post.getPurpose();
        if (post.isMade()) {
            this.made = post.isMade();
            this.storeName = post.getStore().getName();
        } else {
            this.made = post.isMade();
            this.storeName = null;
        }
    }

}