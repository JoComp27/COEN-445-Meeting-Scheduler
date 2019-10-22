package requests;

public class ScheduledMessage extends Message{

    int requestNumber;
    int meetingNumber;
    int roomNumber;
    String[] listOfConfirmedParticipants;

    public ScheduledMessage(int requestNumber, int meetingNumber, int roomNumber, String[] listOfConfirmedParticipants) {
        super(RequestType.Scheduled);
        this.requestNumber = requestNumber;
        this.meetingNumber = meetingNumber;
        this.roomNumber = roomNumber;
        this.listOfConfirmedParticipants = listOfConfirmedParticipants;
    }

    public int getRequestNumber() {
        return requestNumber;
    }

    public int getMeetingNumber() {
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
    public Message deserialize(String message) {

        String[] splitMessage = message.split("_");

        String[] participants = splitMessage[4].split(",");

        return new ScheduledMessage(Integer.parseInt(splitMessage[1]), Integer.parseInt(splitMessage[2]), Integer.parseInt(splitMessage[3]), participants);
    }
}
