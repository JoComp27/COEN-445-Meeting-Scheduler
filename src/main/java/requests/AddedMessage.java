package requests;

public class AddedMessage extends Message {

    private int meetingNumber;
    private String ipAddress;

    public AddedMessage(int meetingNumber, String ipAddress) {
        super(RequestType.Added);
        this.meetingNumber = meetingNumber;
        this.ipAddress = ipAddress;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "_" + meetingNumber + "_" + ipAddress;
    }

    @Override
    public Message deserialize(String message) {

        String[] msg = message.split("_");

        return new AddedMessage(Integer.parseInt(msg[1]), msg[2]);
    }
}
