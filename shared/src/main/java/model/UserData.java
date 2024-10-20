package model;

public record UserData(String username, String password, String email) {
    public boolean initialized() {
        return this.username != null && this.password != null && this.email != null;
    }
}
