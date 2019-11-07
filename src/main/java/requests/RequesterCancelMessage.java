package requests;

public class RequesterCancelMessage extends Message{

    Integer meetingNumber;

    public RequesterCancelMessage() {
        super(RequestType.RequesterCancel);
        this.meetingNumber = null;
    }

    public RequesterCancelMessage(Integer meetingNumber) {
        super(RequestType.RequesterCancel);
        this.meetingNumber = meetingNumber;
    }

    public Integer getMeetingNumber() {
        return meetingNumber;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "_" + meetingNumber;
    }

    @Override
    public void deserialize(String message) {

        String[] splitMessage = message.split("_");

        this.meetingNumber = Integer.parseInt(splitMessage[1]);

    }
}
