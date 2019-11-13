package requests;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMeeting {
    //RequestType requestType;
    private static final AtomicInteger countID = new AtomicInteger(0);  //Thread safe auto increment for RequestNumber


    private String state;

    //Common Arguments
    private Calendar calendar;

    //Requester Arguments
    private int requestNumber;
                //Key=port, bool=approved
    private HashMap<Integer, Boolean> acceptedMap;

    //Invitee Arguments
    private int meetingNumber;
    private String topic;
    private int requester;

    public ClientMeeting(InviteMessage inviteMessage){ //Invitee Meeting
        this.meetingNumber = inviteMessage.getMeetingNumber();
        this.calendar = inviteMessage.getCalendar();
        this.topic = inviteMessage.getTopic();
        this.requester = inviteMessage.getRequester();
    }

    public ClientMeeting(RequestMessage requestMessage){ //Requester Meeting
        this.requestMessage
    }


    private int maxParticipants;
    private int acceptedParticipants;



    public ClientMeeting()

    public int getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public int getAcceptedParticipants() {
        return acceptedParticipants;
    }

    public void incrementAcceptedParticipants(){
        acceptedParticipants++;
    }

    public HashMap<Integer, Boolean> getAcceptedMap() {
        return acceptedMap;
    }

    public int getOrganizer() {
        return organizer;
    }

    public void setAceeptedMap(){
        for(int i = 0; i<this.requestMessage.getParticipants().size(); i++) {
            this.acceptedMap.put(Integer.parseInt(this.requestMessage.getParticipants().get(i)), false);
        }
    }
}
