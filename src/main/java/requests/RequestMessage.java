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

        stringMessage += getRequestType().ordinal() + "$"; //Message ID
        stringMessage += requestNumber + "$";

        stringMessage += CalendarUtil.calendarToString(calendar) + "$";  // DATE & TIME

        stringMessage += minimum + "$";  // MINIMUM

        for(int i = 0; i < participants.size(); i++){ // LIST_OF_PARTICIPANTS
            if(i==participants.size()-1){
                stringMessage += participants.get(i);
            }
            else{
                stringMessage += participants.get(i) + "%";
            }


        }


        stringMessage += "$" +  topic; // TOPIC

        return stringMessage;
    }

    @Override
    public void deserialize(String message) {

        String[] subMessages = message.split("\\$");


        String[] cal = new String[4];

        for(int i = 0; i < 4; i++){
            cal = subMessages[2].split(":");
        }
        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(cal[0]), Integer.parseInt(cal[1]), Integer.parseInt(cal[2]), Integer.parseInt(cal[3]), 0);

        List<String> participants = new ArrayList<>();
        String[] users = subMessages[4].split("%");

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