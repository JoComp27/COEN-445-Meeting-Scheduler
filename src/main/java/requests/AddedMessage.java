package requests;

public class AddedMessage extends Message {

    private String meetingNumber;
    private String socketAddress;

    public AddedMessage(){
        super(RequestType.Added);
        this.meetingNumber = null;
        this.socketAddress = null;
    }

    public AddedMessage(String meetingNumber, String ipAddress) {
        super(RequestType.Added);
        this.meetingNumber = meetingNumber;
        this.socketAddress = ipAddress;
    }

    public String getMeetingNumber() {
        return meetingNumber;
    }

    public String getSocketAddress() {
        return socketAddress;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "$" + meetingNumber + "$" + socketAddress;
    }

    @Override
    public void deserialize(String message) {

        String[] msg = message.split("\\$");

        this.meetingNumber = (msg[1]);
        this.socketAddress = msg[2];

    }
}
