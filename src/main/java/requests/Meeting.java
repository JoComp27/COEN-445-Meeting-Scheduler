package requests;

import Tools.CalendarUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Meeting {
    //RequestType requestType;
    private static final AtomicInteger countID = new AtomicInteger(0);  //Thread safe auto increment
    private int id;
    private RequestMessage requestMessage;

    private int acceptedParticipants;
    //Key=port, bool=accepted
    private HashMap<String, Boolean> acceptedMap;
    private int roomNumber;
    private String organizer;


    public Meeting(String message){ //Constructor used to deserialize elements
        deserialize(message);
    }


    public Meeting(RequestMessage requestMessage, int acceptedParticipants, HashMap acceptedMap, int roomNumber, String organizer) {

        this.id = countID.incrementAndGet();
        this.requestMessage = requestMessage;

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


    public int getAcceptedParticipants() {
        return acceptedParticipants;
    }

    public void incrementAcceptedParticipants(){
        acceptedParticipants++;
    }

    public HashMap<String, Boolean> getAcceptedMap() {
        return acceptedMap;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setAcceptedMap(){
        for(int i = 0; i<this.requestMessage.getParticipants().size(); i++) {
            this.acceptedMap.put(this.requestMessage.getParticipants().get(i), false);
        }
    }
    public void setRoomNumber(int roomNumber){
        this.roomNumber = roomNumber;
    }

    public String serialize(){
        String result = "";

        result += this.id + ",";
        result += this.requestMessage.serialize() + ",";
        result += this.acceptedParticipants + ",";
        result += this.roomNumber + ",";
        result += this.organizer + ",";

        for(Map.Entry<String, Boolean> entry :  acceptedMap.entrySet()){
            result += entry.getKey() + "!" + entry.getValue() + "@";
        }

        return result;
    }

    public void deserialize(String message){

        String[] subMessages = message.split(",");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.deserialize(subMessages[1]);

        this.id = Integer.parseInt(subMessages[0]);
        this.requestMessage = requestMessage;

        this.acceptedParticipants = Integer.parseInt(subMessages[2]);
        this.roomNumber = Integer.parseInt(subMessages[4]);
        this.organizer = subMessages[5];

        String[] acceptedMap = subMessages[6].split("@");

        for(String accMsg : acceptedMap){

            if(accMsg.isEmpty()){
                continue;
            }

            String[] entry = accMsg.split("!");
            this.acceptedMap.put(entry[0], Boolean.parseBoolean(entry[1]));
        }

    }

}
