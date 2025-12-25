package com.zack.friendshub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfFriendshipRequestException extends RuntimeException {
    public SelfFriendshipRequestException(String message) {
        super(message);
    }
}
