package requests;

public class AddMessage extends Message {

    private int meetingNumber;

    public AddMessage(int meetingNumber) {
        super(requestType.Add);
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
        String[] msg = message.split("_");

        return new AddMessage(Integer.parseInt(msg[1]));
    }
}
