package vn.iuh.dto.response;

public class AccountResponse {
    private boolean success;
    private Object data;

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public AccountResponse(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }
}

