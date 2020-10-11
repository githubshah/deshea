package com.roava;

public class MessagePacket {
    String email;
    String event;

    String type;

    String connectTo;
    public MessagePacket(String email, String event, String type) {
        this.email = email;
        this.event = event;
        this.type = type;
    }

    public MessagePacket(String email, String event, String type, String connectTo) {
        this.email = email;
        this.event = event;
        this.type = type;
        this.connectTo = connectTo;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
