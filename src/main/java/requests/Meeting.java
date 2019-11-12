package requests;

import java.util.concurrent.atomic.AtomicInteger;

public class Meeting {
    //RequestType requestType;
    private static final AtomicInteger countID = new AtomicInteger(0);  //Thread safe auto increment
    private int id;
    private RequestMessage requestMessage;
    private String state;
    private int maxParticipants;
    private int acceptedParticipants;

    public Meeting(RequestMessage requestMessage, String state, int maxParticipants, int acceptedParticipants) {
        this.id = countID.incrementAndGet();
        this.requestMessage = requestMessage;
        this.state = state;
        this.maxParticipants = maxParticipants;
        this.acceptedParticipants = acceptedParticipants;
    }

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
}
