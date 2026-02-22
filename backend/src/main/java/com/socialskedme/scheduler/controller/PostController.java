package com.socialskedme.scheduler.controller;

import com.socialskedme.scheduler.dto.CreatePostRequest;
import com.socialskedme.scheduler.dto.UpdatePostRequest;
import com.socialskedme.scheduler.model.Post;
import com.socialskedme.scheduler.model.User;
import com.socialskedme.scheduler.service.PostService;
import com.socialskedme.scheduler.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<Post>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = resolveUserId(principal);
        Page<Post> posts = postService.getPosts(userId, PageRequest.of(page, size, Sort.by("scheduledAt").descending()));
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = resolveUserId(principal);
        return ResponseEntity.ok(postService.getPost(id, userId));
    }

    @PostMapping
    public ResponseEntity<Post> create(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) throws Exception {
        UUID userId = resolveUserId(principal);
        Post post = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> update(
            @PathVariable UUID id,
            @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = resolveUserId(principal);
        return ResponseEntity.ok(postService.updatePost(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = resolveUserId(principal);
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(UserDetails principal) {
        User user = userService.getByEmail(principal.getUsername());
        return user.getId();
    }
}
