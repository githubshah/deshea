package com.roava;

public class Client {
    String email;
    String event;
    String connectTo;

    public Client(String email, String event) {
        this.email = email;
        this.event = event;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getConnectTo() {
        return connectTo;
    }

    public void setConnectTo(String connectTo) {
        this.connectTo = connectTo;
    }
}
