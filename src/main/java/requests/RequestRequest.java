package requests;

import java.util.Date;
import java.util.List;

public class RequestRequest extends Request{

    private int requestQueryNumber;
    private Date date;
    private int minimum;
    private List<String> participants;
    private String topic;

    public RequestRequest(RequestType requestType, int requestQueryNumber, Date date, int minimum, List<String> participants, String topic) {
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
    public String toString() {
        return requestQueryNumber +
                "_" + date.get +
                ", minimum=" + minimum +
                ", participants=" + participants +
                ", topic='" + topic + '\'' +
                ", requestType=" + requestType +
                '}';
    }
}
