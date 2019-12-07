package requests;

public class WithdrawMessage extends Message {

    Integer meetingNumber;

    public WithdrawMessage() {
        super(RequestType.Withdraw);
        this.meetingNumber = null;
    }

    public WithdrawMessage(Integer meetingNumber) {
        super(RequestType.Withdraw);
        this.meetingNumber = meetingNumber;
    }

    public Integer getMeetingNumber() {
        return meetingNumber;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "$" + meetingNumber;
    }

    @Override
    public void deserialize(String message) {
        this.meetingNumber = Integer.parseInt(message.split("\\$")[1]);
    }
}
