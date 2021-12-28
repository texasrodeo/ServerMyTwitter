package com.mytwitter.server.web.api.comments;


import com.mytwitter.server.models.Comment;
import com.mytwitter.server.services.CommentService;
import com.mytwitter.server.web.api.comments.payloads.AddCommentPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/comments")
@CrossOrigin
public class CommentController {

    @Autowired
    CommentService commentService;

    @GetMapping("/post")
    public Iterable<Comment> getCommentsForPost(@RequestParam String postId, @RequestParam int page, @RequestParam int perPage){
        return commentService.getCommentsForPost(postId, page, perPage);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addComment(@RequestBody AddCommentPayload payload){
        try{
            commentService.addComment(payload.getPostId(), payload.getText(), payload.getAuthorId());
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}
