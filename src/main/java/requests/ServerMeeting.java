package requests;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerMeeting {
    //RequestType requestType;
    private static final AtomicInteger countID = new AtomicInteger(0);  //Thread safe auto increment
    private int meetingNumber;
    private RequestMessage requestMessage;
    private String state;
    private int maxParticipants;
    private int acceptedParticipants;
    //Key=port, bool=accepted
    private HashMap<Integer, Boolean> acceptedMap;
    private int organizer;

    public ServerMeeting(RequestMessage requestMessage, String state, int maxParticipants, int acceptedParticipants, HashMap acceptedMap, int organizer) {
        this.meetingNumber = countID.incrementAndGet();
        this.requestMessage = requestMessage;
        this.state = state;
        this.maxParticipants = maxParticipants;
        this.acceptedParticipants = acceptedParticipants;
        this.acceptedMap = acceptedMap;
        this.organizer = organizer;
    }

    public ServerMeeting()

    public int getId() {
        return id;
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
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
