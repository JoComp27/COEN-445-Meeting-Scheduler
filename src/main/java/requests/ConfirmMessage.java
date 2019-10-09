package requests;

public class ConfirmMessage extends Message {

    public ConfirmMessage(RequestType requestType) {
        super(requestType);
    }

    @Override
    public String serialize(Message message) {
        return null;
    }

    @Override
    public Message deserialize(String message) {
        return null;
    }
}
