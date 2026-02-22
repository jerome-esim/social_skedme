package com.socialskedme.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectUrlResponse {
    private String connectUrl;
    private String profileId;
    private String platform;
    private String accountName;
}
