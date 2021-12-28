package com.mytwitter.server.web.api.comments.payloads;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCommentPayload {
    String postId;
    String text;
    String authorId;
}
