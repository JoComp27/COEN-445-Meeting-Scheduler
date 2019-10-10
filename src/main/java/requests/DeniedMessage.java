package requests;

public class DeniedMessage extends Message {

    private int requestQueryNumber;

    public DeniedMessage(int requestQueryNumber) {
        super(RequestType.Denied);
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
    public String serialize() {
        return null;
    }

    @Override
    public Message deserialize(String message) {
        return null;
    }
}
