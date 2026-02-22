package com.socialskedme.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectUrlRequest {

    @NotBlank
    private String platform;

    private String accountName;
}
