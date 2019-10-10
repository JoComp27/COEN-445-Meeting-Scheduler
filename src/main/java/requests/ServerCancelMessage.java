package requests;

public class ServerCancelMessage extends Message {

    public ServerCancelMessage(RequestType requestType) {
        super(requestType);
    }

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public Message deserialize(String message) {
        return null;
    }
}
