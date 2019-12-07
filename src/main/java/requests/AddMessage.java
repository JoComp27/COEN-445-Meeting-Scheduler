package requests;

public class AddMessage extends Message {

    private Integer meetingNumber;

    public AddMessage(){
        super(RequestType.Add);
        this.meetingNumber = null;
    }

    public AddMessage(Integer meetingNumber) {
        super(RequestType.Add);
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
        String[] msg = message.split("\\$");

        this.meetingNumber = Integer.parseInt(msg[1]);
    }
}
