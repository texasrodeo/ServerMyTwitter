package com.mytwitter.server.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.mytwitter.server.business.auth.models.User;
import com.mytwitter.server.exceptions.DuplicatedUserException;
import com.mytwitter.server.exceptions.UserNotFoundException;
import com.mytwitter.server.util.BusinessConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    Logger logger = LoggerFactory.getLogger(UserService.class);

    public User getUser(String email){
        try{
            Firestore dbFirestore = FirestoreClient.getFirestore();

            DocumentReference userRef = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME).document(email);
            DocumentSnapshot document = userRef.get().get();

            User user;
            if(document.exists()) {
                user = document.toObject(User.class);
                return user;
            }else {
                return null;
            }
        }
        catch (Exception e){
            logger.error("An error occured in getUser() method", e);
            return null;
        }
    }

    public void updateUser(String email, String imageUrl, String username) throws ExecutionException, InterruptedException, UserNotFoundException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference userRef = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME).document(email);
        DocumentSnapshot document = userRef.get().get();
        if(document.exists()) {
            ApiFuture<WriteResult> updatedImage = userRef.update(BusinessConstants.UserDocumentFields.IMAGE_URL, imageUrl);
            if(!document.get(BusinessConstants.UserDocumentFields.USERNAME).equals(username)){
                ApiFuture<WriteResult> updatedUsername = userRef.update(BusinessConstants.UserDocumentFields.USERNAME, username);
            }
        }else {
            throw new UserNotFoundException("User with id = "+email+" was not found");
        }
    }

    public void createUser(String email, String imageUrl, String username, String phone) throws ExecutionException, InterruptedException, DuplicatedUserException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference userRef = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME).document(email);
        if(!userRef.get().get().exists()){
            Map<String, Object> docData = new HashMap<>();
            docData.put("email", email);
            if(imageUrl==null || imageUrl.equals("")){
                docData.put("avatarUrl", BusinessConstants.DefaultValues.DEFAULT_USER_AVATAR_REF);
            }
            else{
                docData.put("avatarUrl", imageUrl);
            }
            docData.put("username", username);
            docData.put("phone", phone);

            ApiFuture<WriteResult> addedDocRef = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME).document(email).set(docData);
            logger.debug("Added user with email: " + email);
        }
        else{
            throw new DuplicatedUserException("User with email = " + email + " already exists");
        }



    }
}
