package com.example.VirtualMessenger.Controllers;

import com.example.VirtualMessenger.DTOs.User;
import com.example.VirtualMessenger.Services.UsersGetService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/get")
public class UsersGetController {

    private final UsersGetService usersGetService;

    public UsersGetController(UsersGetService usersGetService) {
        this.usersGetService = usersGetService;
    }

    @GetMapping(path = "/users/{name}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<User>> getUsersByName(@PathVariable String name, Authentication authentication){
        return usersGetService.getUsersByName(name,authentication);
    }

    @GetMapping("/user/{phone}")
    public Mono<User> getUserByPhone(@PathVariable String phone){
        return usersGetService.getUserByPhone(phone);
    }

    @GetMapping("/my/phone")
    public Mono<String> getMyPhone(Authentication authentication){
        String phone = (String) authentication.getPrincipal();
        return Mono.just(phone);
    }
    @GetMapping("/get/me")
    public Mono<User> getMe (Authentication authentication){
        return usersGetService.getMe(authentication);
    }
    @GetMapping("/set/online")
    public Mono<Void> setOnline(Authentication authentication){
        return usersGetService.setOnline(authentication);
    }
    @PostMapping("/change/user")
    public Mono<Void> changeUser(@RequestPart("photo") FilePart filePart,@RequestPart("name") String name,Authentication authentication){
        String myPhone = authentication.getPrincipal().toString();
        return usersGetService.changeUser(filePart,name,myPhone);
    }
}
