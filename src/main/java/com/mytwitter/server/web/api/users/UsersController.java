package com.mytwitter.server.web.api.users;

import com.mytwitter.server.business.auth.models.User;
import com.mytwitter.server.services.UserService;
import com.mytwitter.server.web.api.users.payloads.CreateuserPayload;
import com.mytwitter.server.web.api.users.payloads.UpdateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
public class UsersController {

    @Autowired
    UserService userService;

    @GetMapping("/user")
    public User getUser(@RequestParam String username){
        return userService.getUser(username);
    }

    @PostMapping("/user/update")
    public ResponseEntity<String> updateUser(@RequestBody UpdateUserPayload payload){
        try{
            userService.updateUser(payload.getEmail(), payload.getImageUrl(), payload.getUsername());
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/user/create")
    public ResponseEntity<String> createUser(@RequestBody CreateuserPayload payload){
        try{
            userService.createUser(payload.getEmail(), payload.getImageUrl(), payload.getUsername(), payload.getPhone());
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
