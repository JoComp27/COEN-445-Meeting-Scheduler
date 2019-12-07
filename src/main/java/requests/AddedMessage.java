package requests;

public class AddedMessage extends Message {

    private Integer meetingNumber;
    private String socketAddress;

    public AddedMessage(){
        super(RequestType.Added);
        this.meetingNumber = null;
        this.socketAddress = null;
    }

    public AddedMessage(Integer meetingNumber, String ipAddress) {
        super(RequestType.Added);
        this.meetingNumber = meetingNumber;
        this.socketAddress = ipAddress;
    }

    public Integer getMeetingNumber() {
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

        this.meetingNumber = Integer.parseInt(msg[1]);
        this.socketAddress = msg[2];

    }
}
