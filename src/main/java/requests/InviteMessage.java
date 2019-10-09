package requests;

import java.util.Date;

public class InviteMessage extends Message {

    private int meetingNumber;
    private Date date;
    private String topic;
    private String requester;

    public InviteMessage(RequestType requestType, int meetingNumber, Date date, String topic, String requester) {
        super(requestType);
        this.meetingNumber = meetingNumber;
        this.date = date;
        this.topic = topic;
        this.requester = requester;
    }

    public int getMeetingNumber() {
        return meetingNumber;
    }

    public Date getDate() {
        return date;
    }

    public String getTopic() {
        return topic;
    }

    public String getRequester() {
        return requester;
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
