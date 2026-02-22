package com.socialskedme.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LateWebhookEvent {

    private String type;          // "post.published" | "post.failed"

    @JsonProperty("post_id")
    private String postId;        // Late post ID

    private String error;         // error message for post.failed
}
