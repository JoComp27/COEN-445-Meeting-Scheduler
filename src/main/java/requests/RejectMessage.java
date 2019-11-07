package requests;

public class RejectMessage extends Message{

    private Integer meetingNumber;

    public RejectMessage() {
        super(RequestType.Reject);
        this.meetingNumber = null;
    }

    public RejectMessage(Integer meetingNumber) {
        super(RequestType.Reject);
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

        String[] splitString = message.split("_");

        this.meetingNumber = Integer.parseInt(splitString[1]);
    }
}
