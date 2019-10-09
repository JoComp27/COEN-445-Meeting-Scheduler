package requests;

public class NotScheduledMessage extends Message {

    public NotScheduledMessage(RequestType requestType) {
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
