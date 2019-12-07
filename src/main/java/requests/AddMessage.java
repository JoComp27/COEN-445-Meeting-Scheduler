package requests;

public class AddMessage extends Message {

    private String meetingNumber;

    public AddMessage(){
        super(RequestType.Add);
        this.meetingNumber = null;
    }

    public AddMessage(String meetingNumber) {
        super(RequestType.Add);
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
        String[] msg = message.split("\\$");
        this.meetingNumber = msg[1];
        //this.meetingNumber = Integer.parseInt(msg[1]);
    }
}
