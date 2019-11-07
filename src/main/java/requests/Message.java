package requests;

public abstract class Message {

    protected RequestType requestType;

    public Message(RequestType requestType) {
        this.requestType = requestType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public abstract String serialize();

    public abstract void deserialize(String message);
}
