package requests;

public class RejectMessage extends Message{

    int meetingNumber;

    public RejectMessage(int meetingNumber) {
        super(RequestType.Reject);
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

        String[] splitString = message.split("_");

        return new RejectMessage(Integer.parseInt(splitString[1]));
    }
}
