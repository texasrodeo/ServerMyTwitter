package com.mytwitter.server.services;


import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.mytwitter.server.business.auth.models.User;
import com.mytwitter.server.exceptions.UserNotFoundException;
import com.mytwitter.server.models.Post;
import com.mytwitter.server.util.BusinessConstants;
import javafx.geometry.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class PostsService {
    Logger logger = LoggerFactory.getLogger(PostsService.class);

    public Post getPost(String id) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            DocumentReference documentReference = dbFirestore.collection(BusinessConstants.CollectionNames.POSTS_COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = documentReference.get();

            DocumentSnapshot document = future.get();

            Post post;
            if (document.exists()) {
                post = document.toObject(Post.class);
                ApiFuture<DocumentSnapshot> authorSnapshotFuture = post.getAuthorRef().get();
                DocumentSnapshot authorShapshot = authorSnapshotFuture.get();
                if (authorShapshot.exists()) {
                    User author = authorShapshot.toObject(User.class);
                    post.setAuthor(author);
                }
                return post;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("An error occured in getPost() method", e);
            return null;
        }

    }

    public Iterable<Post> getPosts(int page, int perPage) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();

            CollectionReference posts = dbFirestore.collection(BusinessConstants.CollectionNames.POSTS_COLLECTION_NAME);

            Query firstPage = posts.limit(perPage);

            ApiFuture<QuerySnapshot> future = firstPage.get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            for (int i = 2; i <= page; i++) {
                QueryDocumentSnapshot lastDoc = docs.get(docs.size() - 1);
                Query secondPage = posts.startAfter(lastDoc).limit(perPage);
                future = secondPage.get();
                docs = future.get().getDocuments();
            }

            List<Post> result = new ArrayList<>();

            for (QueryDocumentSnapshot document : docs) {
                Post post = document.toObject(Post.class);
                ApiFuture<DocumentSnapshot> authorSnapshotFuture = post.getAuthorRef().get();
                DocumentSnapshot authorShapshot = authorSnapshotFuture.get();
                if (authorShapshot.exists()) {
                    User author = authorShapshot.toObject(User.class);
                    post.setAuthor(author);
                }
                result.add(post);
            }

            return result;
        } catch (Exception e) {
            logger.error("An error occured in getPosts() method", e);
            return null;
        }
    }


    public Iterable<Post> getPostsForUser(String userId, int page, int perPage) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();

            CollectionReference posts = dbFirestore.collection(BusinessConstants.CollectionNames.POSTS_COLLECTION_NAME);


            DocumentReference userRef = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME).document(userId);
            DocumentSnapshot userDocument = userRef.get().get();

            if (userDocument.exists()) {
                User author = userDocument.toObject(User.class);
                Query userPostsQuery = posts.whereEqualTo("authorRef", userDocument.getReference());


                ApiFuture<QuerySnapshot> userPostsQuerySnapshotApiFuture = userPostsQuery.get();
                List<QueryDocumentSnapshot> userPosts = userPostsQuerySnapshotApiFuture.get().getDocuments();


                List<Post> allUserPosts = new ArrayList<>();

                for (QueryDocumentSnapshot document : userPosts) {
                    Post post = document.toObject(Post.class);
                    post.setAuthor(author);
                    allUserPosts.add(post);
                }

                int firstIndex = 0;
                int lastIndex = perPage;
                for (int i = 1; i < page; i++) {
                    firstIndex = i*(perPage);
                    lastIndex = firstIndex+(perPage);
                }
                if(lastIndex>allUserPosts.size()){
                    return allUserPosts.subList(firstIndex, allUserPosts.size());
                }
                return allUserPosts.subList(firstIndex,lastIndex);
            }
            return null;
        } catch (Exception e) {
            logger.error("An error occurred in getPostsForUser() method", e);
            return null;
        }
    }

    public int getPostsCountForUser(String userId)  {
        try{
            Firestore dbFirestore = FirestoreClient.getFirestore();

            CollectionReference posts = dbFirestore.collection(BusinessConstants.CollectionNames.POSTS_COLLECTION_NAME);
            DocumentReference userRef = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME).document(userId);
            DocumentSnapshot userDocument = userRef.get().get();

            if (userDocument.exists()) {
                Query userPostsQuery = posts.whereEqualTo("authorRef", userDocument.getReference());
                ApiFuture<QuerySnapshot> userPostsQuerySnapshotApiFuture = userPostsQuery.get();
                return userPostsQuerySnapshotApiFuture.get().getDocuments().size();
            }
            return 0;
        } catch (Exception e){
            logger.error("An error occurred in getPostsCountForUser() method", e);
            return 0;
        }

    }

    public void addPost(String authorId, String postText, String imageUrl) throws ExecutionException, InterruptedException, UserNotFoundException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference userRef = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME).document(authorId);
        DocumentSnapshot userDocument = userRef.get().get();

        if (userDocument.exists()) {
            Map<String, Object> data = new HashMap<>();
            data.put("authorRef", userDocument.getReference());
            data.put("text", postText);
            data.put("imageUrl", imageUrl);
            data.put("creationDate", Timestamp.now());
            ApiFuture<DocumentReference> addedDocRef = dbFirestore.collection(BusinessConstants.CollectionNames.POSTS_COLLECTION_NAME).add(data);
            logger.debug("Added document with ID: " + addedDocRef.get().getId());
        }
        else{
            throw new UserNotFoundException("User "+authorId+" was not found");
        }




    }
}
