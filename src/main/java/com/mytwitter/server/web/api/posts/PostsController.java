package com.mytwitter.server.web.api.posts;

import com.mytwitter.server.models.Post;
import com.mytwitter.server.services.PostsService;
import com.mytwitter.server.web.api.posts.payloads.AddPostPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/posts")
@CrossOrigin
public class PostsController {
    @Autowired
    PostsService postsService;

    @GetMapping("/post")
    public Post getPost(@RequestParam String id){
        return postsService.getPost(id);
    }

    @GetMapping
    public Iterable<Post> getPosts(@RequestParam int page, @RequestParam int perPage){
        return postsService.getPosts(page, perPage);
    }

    @GetMapping("/user")
    public Iterable<Post> getPostsForUser(@RequestParam String username,@RequestParam int page, @RequestParam int perPage){
        return postsService.getPostsForUser(username,page, perPage);
    }

    @GetMapping("/user/count")
    public int getPostsCountForUser(@RequestParam String username){
        return postsService.getPostsCountForUser(username);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addPost(@RequestBody AddPostPayload payload){
        try{
            postsService.addPost(payload.getAuthorId(), payload.getPostText(), payload.getImageUrl());
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
