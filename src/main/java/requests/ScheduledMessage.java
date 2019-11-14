package requests;

public class ScheduledMessage extends Message{

    Integer requestNumber;
    Integer meetingNumber;
    Integer roomNumber;
    String[] listOfConfirmedParticipants;

    public ScheduledMessage(){
        super(RequestType.Scheduled);
        this.requestNumber = null;
        this.meetingNumber = null;
        this.roomNumber = null;
        this.listOfConfirmedParticipants = null;
    }

    public ScheduledMessage(Integer requestNumber, Integer meetingNumber, Integer roomNumber, String[] listOfConfirmedParticipants) {
        super(RequestType.Scheduled);
        this.requestNumber = requestNumber;
        this.meetingNumber = meetingNumber;
        this.roomNumber = roomNumber;
        this.listOfConfirmedParticipants = listOfConfirmedParticipants;
    }

    public Integer getRequestNumber() {
        return requestNumber;
    }

    public Integer getMeetingNumber() {
        return meetingNumber;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String[] getListOfConfirmedParticipants() {
        return listOfConfirmedParticipants;
    }

    @Override
    public String serialize() {
        String answer = "";

        answer += requestType.ordinal() + "_";
        answer += requestNumber + "_";
        answer += roomNumber + "_";

        for(int i = 0; i < listOfConfirmedParticipants.length ; i++) {
            if (i == listOfConfirmedParticipants.length - 1) {
                answer += listOfConfirmedParticipants[i];
                break;
            }
            answer += listOfConfirmedParticipants[i] + ",";
        }

        return answer;
    }

    @Override
    public void deserialize(String message) {

        String[] splitMessage = message.split("_");

        String[] participants = splitMessage[4].split(",");

        this.requestNumber = Integer.parseInt(splitMessage[1]);
        this.meetingNumber = Integer.parseInt(splitMessage[2]);
        this.roomNumber = Integer.parseInt(splitMessage[3]);
        this.listOfConfirmedParticipants = participants;

    }
}
