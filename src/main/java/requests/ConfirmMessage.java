package requests;

public class ConfirmMessage extends Message {

    private int meetingNumber;
    private int roomNumber;

    public ConfirmMessage(int meetingNumber, int roomNumber) {
        super(RequestType.Confirm);
        this.meetingNumber = meetingNumber;
        this.roomNumber = roomNumber;
    }

    public int getMeetingNumber(){
        return  meetingNumber;
    }

    public  int getRoomNumber(){
        return roomNumber;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "_" + meetingNumber + "_" + roomNumber;
    }

    @Override
    public Message deserialize(String message) {
        String[] msg = message.split("_");

        return new ConfirmMessage(Integer.parseInt(msg[1]), Integer.parseInt(msg[2]));
    }
}
