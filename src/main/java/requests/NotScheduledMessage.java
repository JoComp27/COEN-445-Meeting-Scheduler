package requests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotScheduledMessage extends Message {

    private Integer requestNumber;
    private Calendar calendar;
    private Integer minimum;
    private List<String> participants;
    private String topic;

    public NotScheduledMessage() {
        super(RequestType.NotScheduled);
        this.requestNumber = null;
        this.calendar = null;
        this.minimum = null;
        this.participants = null;
        this.topic = null;
    }

    public NotScheduledMessage(Integer requestNumber, Calendar calendar, Integer minimum, List<String> participants, String topic) {
        super(RequestType.NotScheduled);
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

    public Integer getMinimum() {
        return minimum;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getTopic() {
        return topic;
    }

    public void setRequestNumber(Integer requestNumber) {
        this.requestNumber = requestNumber;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public void setMinimum(Integer minimum) {
        this.minimum = minimum;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String serialize() {
        String stringMessage = "";

        stringMessage += getRequestType().ordinal() + "$"; //Message ID
        stringMessage += requestNumber + "$";
        stringMessage += calendar.get(Calendar.YEAR) + ":" + calendar.get(Calendar.MONTH) + ":" + calendar.get(Calendar.DAY_OF_MONTH) + ":" + calendar.get(Calendar.HOUR_OF_DAY) + "$";
        stringMessage += minimum + "$";  // MINIMUM

        for(int i = 0; i < participants.size(); i++){ // LIST_OF_PARTICIPANTS
            stringMessage += participants.get(i) + "%";
        }


        stringMessage += "$" +  topic; // TOPIC

        return stringMessage;
    }

    @Override
    public void deserialize(String message) {

        String[] subMessages = message.split("\\$");

        String[] cal = new String[1];

        for(int i = 0; i < 4; i++){
            cal = subMessages[2].split(":");
        }

        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(cal[0]), Integer.parseInt(cal[1]), Integer.parseInt(cal[2]), Integer.parseInt(cal[3]), 0);

        List<String> participants = new ArrayList<>();
        String[] users = subMessages[4].split("%");

        for(String user : users) {
            participants.add(user);
        }

        this.requestNumber = Integer.parseInt(subMessages[1]);
        this.calendar = c;
        this.minimum = Integer.parseInt(subMessages[3]);
        this.participants = participants;
        this.topic = subMessages[5];

    }
}
