package requests;

public class AcceptMessage extends Message{

    public AcceptMessage() {
        super(RequestType.Accept);
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
