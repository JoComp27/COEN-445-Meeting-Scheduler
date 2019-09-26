package requests;

import java.util.Date;

public class InviteRequest extends Request{

    private int meetingNumber;
    private Date date;
    private String topic;
    private String requester;

    public InviteRequest(RequestType requestType, int meetingNumber, Date date, String topic, String requester) {
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
}
