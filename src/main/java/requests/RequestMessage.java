package requests;

import java.util.Date;
import java.util.List;

public class RequestMessage extends Message {

    private int requestQueryNumber;
    private Date date;
    private int minimum;
    private List<String> participants;
    private String topic;

    public RequestMessage(RequestType requestType, int requestQueryNumber, Date date, int minimum, List<String> participants, String topic) {
        super(requestType);
        this.requestQueryNumber = requestQueryNumber;
        this.date = date;
        this.minimum = minimum;
        this.participants = participants;
        this.topic = topic;
    }

    public int getRequestQueryNumber() {
        return requestQueryNumber;
    }

    public Date getDate() {
        return date;
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
    public String serialize(Message message) {
        return null;
    }

    @Override
    public Message deserialize(String message) {
        return null;
    }
}
