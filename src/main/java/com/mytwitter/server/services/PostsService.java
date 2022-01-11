package com.mytwitter.server.services;


import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.mytwitter.server.business.auth.models.User;
import com.mytwitter.server.exceptions.IncorrrectLikeStatusException;
import com.mytwitter.server.exceptions.PostNotFoundException;
import com.mytwitter.server.exceptions.UserNotFoundException;
import com.mytwitter.server.models.Like;
import com.mytwitter.server.models.Post;
import com.mytwitter.server.util.BusinessConstants;
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
                post.setLikes(getLikesForPost(documentReference));
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
                post.setLikes(getLikesForPost(document.getReference()));
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
                    post.setLikes(getLikesForPost(document.getReference()));
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

    private List<Like> getLikesForPost(DocumentReference postRef) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference likesCollection = dbFirestore.collection(BusinessConstants.CollectionNames.LIKES_COLLECTION_NAME);
        Query likeQuery = likesCollection.whereEqualTo("post", postRef);
        ApiFuture<QuerySnapshot> likeQuerySnapshotApiFuture = likeQuery.get();
        List<QueryDocumentSnapshot> queryDocumentSnapshots =  likeQuerySnapshotApiFuture.get().getDocuments();

        List<Like> res = new ArrayList<>();
        for(QueryDocumentSnapshot queryDocumentSnapshot:  queryDocumentSnapshots){
            Like l = queryDocumentSnapshot.toObject(Like.class);
            l.setUserId(l.getUser().getId());
            res.add(l);
        }
        return res;


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

    public void likePost(String postId, String userId, String likeStatus){
        try{
            if(validateLikeStatus(likeStatus)){
                Firestore dbFirestore = FirestoreClient.getFirestore();

                CollectionReference postsCollection = dbFirestore.collection(BusinessConstants.CollectionNames.POSTS_COLLECTION_NAME);
                CollectionReference usersCollection = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME);


                DocumentReference userRef = usersCollection.document(userId);
                DocumentSnapshot userDocument = userRef.get().get();
                if(userDocument.exists()){
                    DocumentReference postRef = postsCollection.document(postId);
                    DocumentSnapshot postDocument = postRef.get().get();
                    if(postDocument.exists()){
                        switch (likeStatus){
                            case BusinessConstants.LikeStatuses.active:
                                addLike(postRef, userRef);
                                break;
                            case BusinessConstants.LikeStatuses.inactive:
                                removeLike(postRef, userRef);
                                break;
                        }
                    }
                    else{
                        throw new PostNotFoundException("Post with id = " + postId + " was not found");
                    }

                }
                else{
                    throw new UserNotFoundException("User with id = " + userId + " was not found");
                }


            }
            else{
                throw new IncorrrectLikeStatusException("Incorrect like status");
            }
        }
        catch (Exception e){
            logger.error("An error occurred in likePost method", e);
        }


    }

    private void addLike(DocumentReference postRef, DocumentReference userRef){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference likesCollection = dbFirestore.collection(BusinessConstants.CollectionNames.LIKES_COLLECTION_NAME);
        Map<String, Object> data = new HashMap<>();
        data.put("post", postRef);
        data.put("user", userRef);
        ApiFuture<DocumentReference> addedDocRef = likesCollection.add(data);
    }

    private void removeLike(DocumentReference postRef, DocumentReference userRef) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference likesCollection = dbFirestore.collection(BusinessConstants.CollectionNames.LIKES_COLLECTION_NAME);
        Query likeQuery = likesCollection.whereEqualTo("user", userRef).whereEqualTo("post", postRef);
        ApiFuture<QuerySnapshot> likeQuerySnapshotApiFuture = likeQuery.get();
        DocumentSnapshot l =  likeQuerySnapshotApiFuture.get().getDocuments().get(0);

        l.getReference().delete();
    }

    private Boolean validateLikeStatus(String status){
        return status.equals(BusinessConstants.LikeStatuses.active) || status.equals(BusinessConstants.LikeStatuses.inactive);
    }
}
