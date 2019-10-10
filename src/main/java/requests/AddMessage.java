package requests;

public class AddMessage extends Message {

    public AddMessage(RequestType requestType) {
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
