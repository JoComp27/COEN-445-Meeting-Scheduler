package requests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RequestMessage extends Message {

    private int requestQueryNumber;
    private Calendar calendar;
    private int minimum;
    private List<String> participants;
    private String topic;

    public RequestMessage(int requestQueryNumber, Calendar calendar, int minimum, List<String> participants, String topic) {
        super(RequestType.Request);
        this.requestQueryNumber = requestQueryNumber;
        this.calendar = calendar;
        this.minimum = minimum;
        this.participants = participants;
        this.topic = topic;
    }

    public int getRequestQueryNumber() {
        return requestQueryNumber;
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
        stringMessage += requestQueryNumber + "_";
        stringMessage += calendar.get(Calendar.DAY_OF_YEAR) + "," + calendar.get(Calendar.MONTH) + "," + calendar.get(Calendar.YEAR) + "," + calendar.get(Calendar.HOUR_OF_DAY) + "_";  // DATE & TIME
        stringMessage += minimum + "_";  // MINIMUM

        for(int i = 0; i < participants.size(); i++){ // LIST_OF_PARTICIPANTS
            stringMessage += participants.get(i) + ",";
        }


        stringMessage += "_" +  topic + "_"; // TOPIC

        return stringMessage;
    }

    @Override
    public Message deserialize(String message) {

        String[] subMessages = message.split("_");

        String[] cal = new String[1];

        for(int i = 0; i < 4; i++){
            cal = subMessages[2].split(",");
        }

        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(cal[0]), Integer.parseInt(cal[1]), Integer.parseInt(cal[2]), Integer.parseInt(cal[3]), 0);

        List<String> participants = new ArrayList<>();
        String[] users = subMessages[4].split(",");

        for(String user : users){
            participants.add(user);
        }

        RequestMessage objMessage = new RequestMessage(
                Integer.parseInt(subMessages[1]),
                c,
                Integer.parseInt(subMessages[3]),
                participants,
                subMessages[5]
                );

        return objMessage;
    }
}
