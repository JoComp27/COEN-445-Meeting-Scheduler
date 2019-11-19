package requests;

import Tools.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RequestMessage extends Message {

    protected Integer requestNumber;
    protected Calendar calendar;
    protected int minimum;
    protected List<String> participants;
    protected String topic;

    public RequestMessage() {
        super(RequestType.Request);
        this.requestNumber = null;
        this.calendar = null;
        this.minimum = 0;
        this.participants = null;
        this.topic = null;
    }

    public RequestMessage(Integer requestNumber, Calendar calendar, int minimum, List<String> participants, String topic) {
        super(RequestType.Request);
        this.requestNumber = requestNumber;
        this.calendar = calendar;
        this.minimum = minimum;
        this.participants = participants;
        this.topic = topic;
    }

    public Integer getRequestNumber() {
        return requestNumber;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public int getMinimum() {
        return minimum;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String serialize() {
        String stringMessage = "";

        stringMessage += getRequestType().ordinal() + "_"; //Message ID
        stringMessage += requestNumber + "_";

        stringMessage += CalendarUtil.calendarToString(calendar) + "_";  // DATE & TIME

        stringMessage += minimum + "_";  // MINIMUM

        for(int i = 0; i < participants.size(); i++){ // LIST_OF_PARTICIPANTS
            if(i==participants.size()-1){
                stringMessage += participants.get(i);
            }
            else{
                stringMessage += participants.get(i) + ",";
            }


        }


        stringMessage += "_" +  topic; // TOPIC

        return stringMessage;
    }

    @Override
    public void deserialize(String message) {

        String[] subMessages = message.split("_");

        Calendar c = CalendarUtil.stringToCalendar(subMessages[2]);

        List<String> participants = new ArrayList<>();
        String[] users = subMessages[4].split(",");

        for(String user : users){
            participants.add(user);
        }
        this.requestNumber = Integer.parseInt(subMessages[1]);
        this.calendar = c;
        this.minimum = Integer.parseInt(subMessages[3]);
        this.participants = participants;
        this.topic = subMessages[5];

    }
}