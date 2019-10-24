package requests;

public class AcceptMessage extends Message{

    private int meetingNumber;

    public AcceptMessage(int meetingNumber) {
        super(RequestType.Accept);
        this.meetingNumber = meetingNumber;
    }

    public int getMeetingNumber(){
        return this.meetingNumber;
    }

    @Override
    public String serialize() {

        return requestType.ordinal() + "_" + meetingNumber;
    }

    @Override
    public Message deserialize(String message) {
        String[] msg = message.split("_");

        return new AcceptMessage(Integer.parseInt(msg[1]));
    }
}
