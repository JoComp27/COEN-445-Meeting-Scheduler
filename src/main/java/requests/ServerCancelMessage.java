package requests;

public class ServerCancelMessage extends Message {

    int requestNumber;
    String reason;

    public ServerCancelMessage(int requestNumber, String reason) {
        super(RequestType.ServerCancel);
        this.requestNumber = requestNumber;
        this.reason = reason;

    }

    public int getRequestNumber() {
        return requestNumber;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "_" + requestNumber + "_" + reason;
    }

    @Override
    public Message deserialize(String message) {
        String[] stringArrayMessage = message.split("_");

        return new ServerCancelMessage(Integer.parseInt(stringArrayMessage[1]), stringArrayMessage[2]);
    }
}
