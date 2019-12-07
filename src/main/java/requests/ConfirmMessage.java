package requests;

public class ConfirmMessage extends Message {

    private String meetingNumber;
    private String roomNumber;

    public ConfirmMessage(){
        super(RequestType.Confirm);
        this.meetingNumber = null;
        this.roomNumber = null;
    }

    public ConfirmMessage(String meetingNumber, String roomNumber) {
        super(RequestType.Confirm);
        this.meetingNumber = meetingNumber;
        this.roomNumber = roomNumber;
    }

    public String getMeetingNumber(){
        return  meetingNumber;
    }

    public String getRoomNumber(){
        return roomNumber;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "$" + meetingNumber + "$" + roomNumber;
    }

    @Override
    public void deserialize(String message) {
        String[] msg = message.split("\\$");

        this.meetingNumber = (msg[1]);
        this.roomNumber = (msg[2]);
    }
}
