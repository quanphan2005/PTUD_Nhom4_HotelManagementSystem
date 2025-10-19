package vn.iuh.dto.response;

import vn.iuh.constraint.ResponseType;

public class EventResponse {
    private ResponseType type;
    private String message;

    public EventResponse(ResponseType type, String message) {
        this.type = type;
        this.message = message;
    }

    public ResponseType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}


