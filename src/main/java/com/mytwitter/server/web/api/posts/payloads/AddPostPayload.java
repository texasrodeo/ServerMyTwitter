package com.mytwitter.server.web.api.posts.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPostPayload {
    String authorId;
    String postText;
    String imageUrl;
}
