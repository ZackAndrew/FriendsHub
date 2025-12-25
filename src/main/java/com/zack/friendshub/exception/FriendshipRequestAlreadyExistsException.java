package com.zack.friendshub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FriendshipRequestAlreadyExistsException extends RuntimeException {
    public FriendshipRequestAlreadyExistsException(String message) {
        super(message);
    }
}
