package requests;

public class AcceptMessage extends Message{

    public AcceptMessage() {
        super(RequestType.Accept);
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
