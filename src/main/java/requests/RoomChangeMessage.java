package requests;

public class RoomChangeMessage extends Message {

    private Integer meetingNumber;
    private Integer newRoomNumber;


    public RoomChangeMessage(){
        super(RequestType.RoomChange);
        this.meetingNumber = null;
        this.newRoomNumber = null;
    }


    public RoomChangeMessage(Integer meetingNumber, int newRoomNumber) {
        super(RequestType.RoomChange);
        this.meetingNumber = meetingNumber;
        this.newRoomNumber = newRoomNumber;
    }

    public Integer getMeetingNumber() {
        return meetingNumber;
    }

    public int getNewRoomNumber() {
        return newRoomNumber;
    }

    @Override
    public String serialize() {

        return requestType.ordinal() + "$" + meetingNumber + "$" + newRoomNumber;
    }

    @Override
    public void deserialize(String message) {
        String[] msg = message.split("\\$");
        this.meetingNumber = Integer.parseInt(msg[1]);
        this.newRoomNumber = Integer.parseInt(msg[2]);

    }
}
