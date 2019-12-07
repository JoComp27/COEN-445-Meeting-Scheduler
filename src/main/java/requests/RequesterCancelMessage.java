package requests;

public class RequesterCancelMessage extends Message{

    String meetingNumber;

    public RequesterCancelMessage() {
        super(RequestType.RequesterCancel);
        this.meetingNumber = null;
    }

    public RequesterCancelMessage(String meetingNumber) {
        super(RequestType.RequesterCancel);
        this.meetingNumber = meetingNumber;
    }

    public String getMeetingNumber() {
        return meetingNumber;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "$" + meetingNumber;
    }

    @Override
    public void deserialize(String message) {

        String[] splitMessage = message.split("\\$");

        this.meetingNumber = splitMessage[1];

    }
}
