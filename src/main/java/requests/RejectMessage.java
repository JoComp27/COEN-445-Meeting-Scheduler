package requests;

public class RejectMessage extends Message{


    public RejectMessage(RequestType requestType) {
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
