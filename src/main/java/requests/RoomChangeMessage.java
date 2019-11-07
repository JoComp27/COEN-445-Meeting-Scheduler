package requests;

public class RoomChangeMessage extends Message {

    private Integer meetingNumber;
    private int newRoomNumber;

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

        return requestType.ordinal() + "_" + meetingNumber + "_" + newRoomNumber;
    }

    @Override
    public void deserialize(String message) {
        String[] msg = message.split("_");
        this.meetingNumber = Integer.parseInt(msg[1]);
        this.newRoomNumber = Integer.parseInt(msg[2]);

    }
}
