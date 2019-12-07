package requests;

public class ServerCancelMessage extends Message {

    String meetingNumber;
    String reason;

    public ServerCancelMessage() {
        super(RequestType.ServerCancel);
        this.meetingNumber = null;
        this.reason = null;
    }

    public ServerCancelMessage(String meetingNumber, String reason) {
        super(RequestType.ServerCancel);
        this.meetingNumber = meetingNumber;
        this.reason = reason;

    }

    public String getMeetingNumber() {
        return meetingNumber;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "$" + meetingNumber + "$" + reason;
    }

    @Override
    public void deserialize(String message) {
        String[] stringArrayMessage = message.split("\\$");

        this.meetingNumber = stringArrayMessage[1];
        this.reason = stringArrayMessage[2];

    }
}
