package vn.iuh.entity;

public class Account {
    private String id;
    private String userName;
    private String userPassword;
    private String userRole;
    private String employeeId;

    public Account() {
    }

    public Account(String id, String userName, String userPassword, String userRole, String employeeId) {
        this.id = id;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userRole = userRole;
        this.employeeId = employeeId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String role) {
        this.userRole = role;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
