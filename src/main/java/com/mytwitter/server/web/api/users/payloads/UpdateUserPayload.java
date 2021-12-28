package com.mytwitter.server.web.api.users.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserPayload {
    String email;
    String imageUrl;
    String username;
}
