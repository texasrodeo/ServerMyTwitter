package com.mytwitter.server.util;

public interface BusinessConstants {
    interface CollectionNames{
        String POSTS_COLLECTION_NAME = "posts";
        String USERS_COLLECTION_NAME = "users";
        String COMMENTS_COLLECTION_NAME = "comments";
        String LIKES_COLLECTION_NAME = "likes";
    }

    interface UserDocumentFields{
        String IMAGE_URL = "avatarUrl";
        String USERNAME = "username";
    }

    interface DefaultValues{
        String DEFAULT_USER_AVATAR_REF = "https://firebasestorage.googleapis.com/v0/b/mytwitterfirebase-cc082.appspot.com/o/avatars%2FdefaultAvatar%2Fdefault-avatar.png?alt=media&token=c6f1ff34-059a-40a6-93de-19924a91cca7";
    }

    interface LikeStatuses{
        String active = "1";
        String inactive = "0";
    }
}

