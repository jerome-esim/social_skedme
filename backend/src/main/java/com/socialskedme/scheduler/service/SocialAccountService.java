package com.socialskedme.scheduler.service;

import com.socialskedme.scheduler.dto.ConnectAccountRequest;
import com.socialskedme.scheduler.model.SocialAccount;
import com.socialskedme.scheduler.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialAccountService {

    private final SocialAccountRepository socialAccountRepository;

    public List<SocialAccount> getAccounts(UUID userId) {
        return socialAccountRepository.findByUserId(userId);
    }

    public String getExistingProfileId(UUID userId) {
        return socialAccountRepository.findByUserId(userId).stream()
                .map(SocialAccount::getLateAccountId)
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .orElse(null);
    }

    public SocialAccount connectAccount(ConnectAccountRequest request, UUID userId) {
        SocialAccount account = SocialAccount.builder()
                .userId(userId)
                .platform(request.getPlatform())
                .lateAccountId(request.getLateAccountId())
                .accountName(request.getAccountName())
                .build();
        return socialAccountRepository.save(account);
    }

    public void deleteAccount(UUID accountId, UUID userId) {
        SocialAccount account = socialAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        socialAccountRepository.delete(account);
    }
}
