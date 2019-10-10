package requests;

public class AddedMessage extends Message {

    public AddedMessage(RequestType requestType) {
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
