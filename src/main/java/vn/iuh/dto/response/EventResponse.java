package vn.iuh.dto.response;

import vn.iuh.constraint.ResponseType;

public class EventResponse<T> {
    private ResponseType type;
    private String message;
    private T data;

    public EventResponse(ResponseType type, String message, T data) {
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public ResponseType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}


