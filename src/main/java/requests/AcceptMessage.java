package requests;

public class AcceptMessage extends Message{

    private Integer meetingNumber;

    public AcceptMessage(){
       super(RequestType.Accept);
       this.meetingNumber = null;
    }

    public AcceptMessage(Integer meetingNumber) {
        super(RequestType.Accept);
        this.meetingNumber = meetingNumber;
    }

    public Integer getMeetingNumber(){
        return this.meetingNumber;
    }

    @Override
    public String serialize() {

        return requestType.ordinal() + "_" + meetingNumber;
    }

    @Override
    public void deserialize(String message) {
        String[] msg = message.trim().split("_");


        this.meetingNumber = Integer.parseInt(msg[1]);
    }
}
