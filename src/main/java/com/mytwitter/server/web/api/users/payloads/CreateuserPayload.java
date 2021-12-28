package com.mytwitter.server.web.api.users.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateuserPayload {
    String username;
    String email;
    String phone;
    String imageUrl;
}
