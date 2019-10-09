package requests;

public class AcceptMessage extends Message{

    public AcceptMessage(RequestType requestType) {
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
