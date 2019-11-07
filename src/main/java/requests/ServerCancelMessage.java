package requests;

public class ServerCancelMessage extends Message {

    Integer requestNumber;
    String reason;

    public ServerCancelMessage() {
        super(RequestType.ServerCancel);
        this.requestNumber = null;
        this.reason = null;
    }

    public ServerCancelMessage(Integer requestNumber, String reason) {
        super(RequestType.ServerCancel);
        this.requestNumber = requestNumber;
        this.reason = reason;

    }

    public Integer getRequestNumber() {
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
    public void deserialize(String message) {
        String[] stringArrayMessage = message.split("_");

        this.requestNumber = Integer.parseInt(stringArrayMessage[1]);
        this.reason = stringArrayMessage[2];

    }
}
