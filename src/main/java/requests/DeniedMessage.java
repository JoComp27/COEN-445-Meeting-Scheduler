package requests;

public class DeniedMessage extends Message {

    private int requestQueryNumber;

    public DeniedMessage(RequestType requestType, int requestQueryNumber) {
        super(requestType);
        this.requestQueryNumber = requestQueryNumber;
    }

    public int getRequestQueryNumber() {
        return requestQueryNumber;
    }

    @Override
    public String toString() {
        return requestType.name() + "_" + requestQueryNumber + "_UNAVAILABLE";
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
