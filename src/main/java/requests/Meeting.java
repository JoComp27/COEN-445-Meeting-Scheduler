package requests;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Meeting {
    //RequestType requestType;
    private static final AtomicInteger countID = new AtomicInteger(0);  //Thread safe auto increment
    private int id;
    private RequestMessage requestMessage;
    private String state;
    private int maxParticipants;
    private int acceptedParticipants;
    //Key=port, bool=accepted
    private HashMap<Integer, Boolean> acceptedMap;
    private int roomNumber;
    private int organizer;

    public Meeting(RequestMessage requestMessage, String state, int maxParticipants, int acceptedParticipants, HashMap acceptedMap, int roomNumber, int organizer) {
        this.id = countID.incrementAndGet();
        this.requestMessage = requestMessage;
        this.state = state;
        this.maxParticipants = maxParticipants;
        this.acceptedParticipants = acceptedParticipants;
        this.acceptedMap = acceptedMap;
        this.roomNumber = roomNumber;
        this.organizer = organizer;
    }

    public int getId() {
        return id;
    }

    //This contains the list of participants because of the request message.
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

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getOrganizer() {
        return organizer;
    }

    public void setAcceptedMap(){
        for(int i = 0; i<this.requestMessage.getParticipants().size(); i++) {
            this.acceptedMap.put(Integer.parseInt(this.requestMessage.getParticipants().get(i)), false);
        }
    }
    public void setRoomNumber(int roomNumber){
        this.roomNumber = roomNumber;
    }
}
