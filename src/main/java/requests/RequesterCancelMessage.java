package requests;

public class RequesterCancelMessage extends Message{

    public RequesterCancelMessage(RequestType requestType) {
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
