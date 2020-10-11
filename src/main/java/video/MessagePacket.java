package video;

import java.util.List;
import java.util.Map;

public class MessagePacket {
    String email;
    String event;
    String connectTo;
    String type;
    List<String> emailPojos;
    Map<String, String> connection;

    public MessagePacket(String email, String event, String type) {
        this.email = email;
        this.event = event;
        this.type = type;
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

    public List<String> getEmailPojos() {
        return emailPojos;
    }

    public MessagePacket setEmailPojos(List<String> emailPojos) {
        this.emailPojos = emailPojos;
        return this;
    }

    public Map<String, String> getConnection() {
        return connection;
    }

    public MessagePacket setConnection(Map<String, String> connection) {
        this.connection = connection;
        return this;
    }
}