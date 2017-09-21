package server;

public class Auth {
    private String login;
    private String password;
    private boolean isOnline = false;


    public Auth(String login, String password) {
        this.login = login;
        this.password = password;
        isOnline = true;
    }


}
