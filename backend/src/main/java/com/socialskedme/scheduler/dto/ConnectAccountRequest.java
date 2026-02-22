package com.socialskedme.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectAccountRequest {

    @NotBlank
    private String platform;

    @NotBlank
    private String lateAccountId;

    private String accountName;
}
