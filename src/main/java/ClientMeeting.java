import requests.ConfirmMessage;
import requests.InviteMessage;
import requests.RequestMessage;
import requests.ScheduledMessage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMeeting {

    //Common Arguments
    private Calendar calendar;
    private String meetingType;
    private String state;
    private boolean currentAnswer;
    private int roomNumber;

    //Requester Arguments
    private int requestNumber;
                //Key=port, bool=approved
    private HashMap<Integer, Boolean> acceptedMap;

    //Invitee Arguments
    private int meetingNumber;

    public ClientMeeting(InviteMessage inviteMessage){ //Invitee Meeting
        this.meetingNumber = inviteMessage.getMeetingNumber();
        this.calendar = inviteMessage.getCalendar();
        this.state = "Standby";
        this.meetingType = "Invitee";

    }

    public ClientMeeting(RequestMessage requestMessage){ //Requester Meeting
        this.requestNumber = requestMessage.getRequestNumber();
        this.calendar = requestMessage.getCalendar();
        this.state = "Standby";
        this.meetingType = "Requester";
        this.currentAnswer = true;
    }

    public void receiveConfirmMessage(ConfirmMessage confirmMessage){

        this.state = "Complete";
        this.roomNumber = confirmMessage.getRoomNumber();

    }

    public void receiveScheduledMessage(ScheduledMessage scheduledMessage){

        this.state = "Complete";
        this.meetingNumber = scheduledMessage.getMeetingNumber();
        this.roomNumber = scheduledMessage.getRoomNumber();
        this.acceptedMap = new HashMap<>();

        for(String participant : scheduledMessage.getListOfConfirmedParticipants()){
            acceptedMap.put(Integer.parseInt(participant), true);
        }

    }

    public String getMeetingType() {
        return meetingType;
    }

    public String getState() {
        return state;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public int getRequestNumber() {
        return requestNumber;
    }

    public HashMap<Integer, Boolean> getAcceptedMap() {
        return acceptedMap;
    }

    public int getMeetingNumber() {
        return meetingNumber;
    }

    public boolean isCurrentAnswer() {
        return currentAnswer;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setCurrentAnswer(boolean currentAnswer) {
        this.currentAnswer = currentAnswer;
    }
}
