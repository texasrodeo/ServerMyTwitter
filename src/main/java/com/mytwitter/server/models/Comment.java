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
public class Comment {
    @DocumentId
    String id;

    @JsonIgnore
    DocumentReference authorRef;

    @JsonIgnore
    DocumentReference postRef;

    User author;

    String text;

    Timestamp creationDate;
}
