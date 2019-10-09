package requests;

public abstract class Message {

    protected RequestType requestType;

    public Message(RequestType requestType) {
        this.requestType = requestType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public abstract String serialize(Message message);

    public abstract Message deserialize(String message);
}
