package vn.iuh.dto.event.create;

public class LoginEvent {
    private final String userName;
    private final String passWord;

    public LoginEvent(String userName, String passWord){
        this.userName = userName;
        this.passWord = passWord;
    }
    public String getUserName(){
        return userName;
    }

    public String getPassWord(){
        return passWord;
    }
}

