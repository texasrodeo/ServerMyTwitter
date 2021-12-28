package com.mytwitter.server.business.auth.models;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    static final long serialVersionUID = 4408418647685225829L;

    @DocumentId
    String id;
    String username;
    String email;
    String phone;
    String avatarUrl;
}
