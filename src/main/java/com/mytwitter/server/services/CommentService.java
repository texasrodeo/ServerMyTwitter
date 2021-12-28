package com.mytwitter.server.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.mytwitter.server.business.auth.models.User;
import com.mytwitter.server.exceptions.PostNotFoundException;
import com.mytwitter.server.exceptions.UserNotFoundException;
import com.mytwitter.server.models.Comment;
import com.mytwitter.server.util.BusinessConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class CommentService {
    Logger logger = LoggerFactory.getLogger(CommentService.class);

    public Iterable<Comment> getCommentsForPost(String postId, int page, int perPage){
        try{
            Firestore dbFirestore = FirestoreClient.getFirestore();

            CollectionReference posts = dbFirestore.collection(BusinessConstants.CollectionNames.POSTS_COLLECTION_NAME);
            DocumentReference postRef = posts.document(postId);
            DocumentSnapshot postSnapshot = postRef.get().get();
            if(postSnapshot.exists()){
                CollectionReference comments = dbFirestore.collection(BusinessConstants.CollectionNames.COMMENTS_COLLECTION_NAME);
                Query commentsForPostQuery = comments.whereEqualTo("postRef", postSnapshot.getReference());


                ApiFuture<QuerySnapshot> commentsPostsQuerySnapshotApiFuture = commentsForPostQuery.get();
                List<QueryDocumentSnapshot> postsComments = commentsPostsQuerySnapshotApiFuture.get().getDocuments();

                List<Comment> result = new ArrayList<>();

                for (int i = (page-1)*perPage; i < (page-1)*perPage+perPage; i++) {
                    if(i<postsComments.size()){
                        QueryDocumentSnapshot lastDoc = postsComments.get(i);
                        Comment comment = lastDoc.toObject(Comment.class);

                        ApiFuture<DocumentSnapshot> future = comment.getAuthorRef().get();

                        DocumentSnapshot userSnapshot = future.get();
                        if(userSnapshot.exists()){
                            User author = userSnapshot.toObject(User.class);
                            comment.setAuthor(author);
                        }
                        result.add(comment);
                    }
                }
                return result;
            }
            return null;
        }
        catch (Exception e){
            logger.error("An error occurred in getCommentsForPost() method", e);
            return null;
        }
    }

    public void addComment(String postId, String text, String authorId) throws ExecutionException, InterruptedException, UserNotFoundException, PostNotFoundException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference userRef = dbFirestore.collection(BusinessConstants.CollectionNames.USERS_COLLECTION_NAME).document(authorId);
        if(userRef.get().get().exists()){
            DocumentReference postRef = dbFirestore.collection(BusinessConstants.CollectionNames.POSTS_COLLECTION_NAME).document(postId);
            if(postRef.get().get().exists()){
                Map<String, Object> docData = new HashMap<>();
                docData.put("authorRef", userRef);
                docData.put("creationDate", new Date());
                docData.put("postRef", postRef);
                docData.put("text", text);

                ApiFuture<DocumentReference> addedDocRef = dbFirestore.collection(BusinessConstants.CollectionNames.COMMENTS_COLLECTION_NAME).add(docData);
                logger.debug("Added comment with ID: " + addedDocRef.get().getId());
            }
            else {
                throw new PostNotFoundException("Post with id = " + postId + " was not found");
            }

        }
        else {
            throw new UserNotFoundException("User "+authorId+" was not found");
        }

    }
}
