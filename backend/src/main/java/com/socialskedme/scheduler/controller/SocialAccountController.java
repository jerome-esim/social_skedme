package com.socialskedme.scheduler.controller;

import com.socialskedme.scheduler.dto.ConnectAccountRequest;
import com.socialskedme.scheduler.dto.ConnectUrlRequest;
import com.socialskedme.scheduler.dto.ConnectUrlResponse;
import com.socialskedme.scheduler.model.SocialAccount;
import com.socialskedme.scheduler.model.User;
import com.socialskedme.scheduler.service.LateApiService;
import com.socialskedme.scheduler.service.SocialAccountService;
import com.socialskedme.scheduler.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class SocialAccountController {

    private final SocialAccountService socialAccountService;
    private final LateApiService lateApiService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<SocialAccount>> list(@AuthenticationPrincipal UserDetails principal) {
        UUID userId = resolveUserId(principal);
        return ResponseEntity.ok(socialAccountService.getAccounts(userId));
    }

    @PostMapping("/connect-url")
    public ResponseEntity<ConnectUrlResponse> connectUrl(
            @Valid @RequestBody ConnectUrlRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) throws Exception {
        LateApiService.ConnectUrlResult result =
                lateApiService.createProfileAndGetConnectUrl(request.getPlatform(), request.getAccountName());
        return ResponseEntity.ok(new ConnectUrlResponse(
                result.connectUrl(),
                result.profileId(),
                request.getPlatform(),
                request.getAccountName()
        ));
    }

    @PostMapping("/connect")
    public ResponseEntity<SocialAccount> connect(
            @Valid @RequestBody ConnectAccountRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = resolveUserId(principal);
        SocialAccount account = socialAccountService.connectAccount(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = resolveUserId(principal);
        socialAccountService.deleteAccount(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(UserDetails principal) {
        User user = userService.getByEmail(principal.getUsername());
        return user.getId();
    }
}
