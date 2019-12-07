package requests;

public class WithdrawMessage extends Message {

    String meetingNumber;

    public WithdrawMessage() {
        super(RequestType.Withdraw);
        this.meetingNumber = null;
    }

    public WithdrawMessage(String meetingNumber) {
        super(RequestType.Withdraw);
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
        this.meetingNumber = (message.split("\\$")[1]);
    }
}
