package requests;

public class RoomChangeMessage extends Message {

    public RoomChangeMessage(RequestType requestType) {
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
