package com.mytwitter.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;

import com.mytwitter.server.business.auth.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    @DocumentId
    String id;

    @JsonIgnore
    DocumentReference authorRef;

    User author;

    String text;

    String imageUrl;

    Timestamp creationDate;

}
