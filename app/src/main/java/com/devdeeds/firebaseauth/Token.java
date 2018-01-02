package com.devdeeds.firebaseauth;


public class Token {

    private String email,token;

    public Token(){}

    public Token(String gonderen, String token) {
        this.email = gonderen;
        this.token = token;
    }

    public String getEmail() {return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}