package requests;

public class RoomChangeMessage extends Message {

    private int meetingNumber;
    private int newRoomNumber;

    public RoomChangeMessage(int meetingNumber, int newRoomNumber) {
        super(requestType.RoomChange);
        this.meetingNumber = meetingNumber;
        this.newRoomNumber = newRoomNumber;
    }

    public int getMeetingNumber() {
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
    public Message deserialize(String message) {
        String[] msg = message.split("_");
        return new RoomChangeMessage(Integer.parseInt(msg[1]), Integer.parseInt(msg[2]));
    }
}
