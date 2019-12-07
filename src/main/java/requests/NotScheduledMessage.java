package requests;

import Tools.CalendarUtil;

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

    @Override
    public String serialize() {
        String stringMessage = "";

        stringMessage += getRequestType().ordinal() + "$"; //Message ID
        stringMessage += requestNumber + "$";
        stringMessage += CalendarUtil.calendarToString(calendar) + "$";
        stringMessage += minimum + "$";  // MINIMUM

        for(int i = 0; i < participants.size(); i++){ // LIST_OF_PARTICIPANTS
            stringMessage += participants.get(i) + ",";
        }

        stringMessage += "$" +  topic; // TOPIC

        return stringMessage;
    }

    @Override
    public void deserialize(String message) {

        String[] subMessages = message.split("\\$");
        Calendar c = CalendarUtil.stringToCalendar(subMessages[2]);

        List<String> participants = new ArrayList<>();
        String[] users = subMessages[4].split(",");

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
