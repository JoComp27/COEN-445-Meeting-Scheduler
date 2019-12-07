package requests;

public class ConfirmMessage extends Message {

    private Integer meetingNumber;
    private Integer roomNumber;

    public ConfirmMessage(){
        super(RequestType.Confirm);
        this.meetingNumber = null;
        this.roomNumber = null;
    }

    public ConfirmMessage(Integer meetingNumber, int roomNumber) {
        super(RequestType.Confirm);
        this.meetingNumber = meetingNumber;
        this.roomNumber = roomNumber;
    }

    public Integer getMeetingNumber(){
        return  meetingNumber;
    }

    public Integer getRoomNumber(){
        return roomNumber;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "$" + meetingNumber + "$" + roomNumber;
    }

    @Override
    public void deserialize(String message) {
        String[] msg = message.split("\\$");

        this.meetingNumber = Integer.parseInt(msg[1]);
        this.roomNumber = Integer.parseInt(msg[2]);
    }
}
