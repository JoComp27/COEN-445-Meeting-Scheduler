package requests;

public class RequesterCancelMessage extends Message{

    int meetingNumber;

    public RequesterCancelMessage(int meetingNumber) {
        super(RequestType.RequesterCancel);
        this.meetingNumber = meetingNumber;
    }

    public int getMeetingNumber() {
        return meetingNumber;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "_" + meetingNumber;
    }

    @Override
    public Message deserialize(String message) {

        String[] splitMessage = message.split("_");

        return new RequesterCancelMessage(Integer.parseInt(splitMessage[1]));
    }
}
