package requests;

public class ServerWidthdrawMessage extends Message{

    private Integer meetingNumber;
    private String ipAddress;

    public ServerWidthdrawMessage(){
        super(RequestType.ServerWidthdraw);
        this.meetingNumber = null;
        this.ipAddress = null;
    }

    public ServerWidthdrawMessage(Integer meetingNumber, String ipAddress) {
        super(RequestType.ServerWidthdraw);
        this.meetingNumber = meetingNumber;
        this.ipAddress = ipAddress;
    }

    public Integer getMeetingNumber() {
        return meetingNumber;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String serialize() {
        return RequestType.ServerWidthdraw.ordinal() + "_" + meetingNumber + "_" + ipAddress;
    }

    @Override
    public void deserialize(String message) {
        String[] splitMessage = message.split("_");

        this.meetingNumber = Integer.parseInt(splitMessage[1]);
        this.ipAddress = splitMessage[2];
    }
}
