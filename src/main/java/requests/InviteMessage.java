package requests;

import java.util.Calendar;
import java.util.Date;

public class InviteMessage extends Message {

    private Integer meetingNumber;
    private Calendar calendar;
    private String topic;
    private String requester;

    public InviteMessage() {
        super(RequestType.Invite);
        this.meetingNumber = null;
        this.calendar = null;
        this.topic = null;
        this.requester = null;
    }

    public InviteMessage(Integer meetingNumber, Calendar calendar, String topic, String requester) {
        super(RequestType.Invite);
        this.meetingNumber = meetingNumber;
        this.calendar = calendar;
        this.topic = topic;
        this.requester = requester;
    }

    public Integer getMeetingNumber() {
        return meetingNumber;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public String getTopic() {
        return topic;
    }

    public String getRequester() {
        return requester;
    }

    public void setMeetingNumber(Integer meetingNumber) {
        this.meetingNumber = meetingNumber;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    @Override
    public String serialize() {

        String msg = "";
        msg += requestType.ordinal() + "$";
        msg += meetingNumber + "$";
        msg += calendar.get(Calendar.YEAR) + ":" + calendar.get(Calendar.MONTH) + ":" + calendar.get(Calendar.DAY_OF_MONTH) + ":" + calendar.get(Calendar.HOUR_OF_DAY) + "$";
        msg += topic + "$";
        msg += requester;

        return msg;
    }

    @Override
    public void deserialize(String message) {

        String[] arrMsg = message.split("\\$");

        String[] cal = new String[1];

        for(int i = 0; i < 4; i++){
            cal = arrMsg[2].split(":");
        }

        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(cal[0]), Integer.parseInt(cal[1]), Integer.parseInt(cal[2]), Integer.parseInt(cal[3]), 0);

        this.meetingNumber = Integer.parseInt(arrMsg[1]);
        this.calendar = c;
        this.topic = arrMsg[3];
        this.requester = arrMsg[4];

    }
}
