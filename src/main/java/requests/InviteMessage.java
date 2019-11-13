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

    @Override
    public String serialize() {

        String msg = "";
        msg += requestType.ordinal() + "_";
        msg += meetingNumber + "_";
        msg += calendar.get(Calendar.DAY_OF_YEAR) + "," + calendar.get(Calendar.MONTH) + "," + calendar.get(Calendar.YEAR) + "," + calendar.get(Calendar.HOUR_OF_DAY) + "_";
        msg += topic + "_";
        msg += requester;

        return msg;
    }

    @Override
    public void deserialize(String message) {

        String[] arrMsg = message.split("_");

        String[] cal = new String[1];

        for(int i = 0; i < 4; i++){
            cal = arrMsg[2].split(",");
        }

        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(cal[0]), Integer.parseInt(cal[1]), Integer.parseInt(cal[2]), Integer.parseInt(cal[3]), 0);

        this.meetingNumber = Integer.parseInt(arrMsg[1]);
        this.calendar = c;
        this.topic = arrMsg[3];
        this.requester = arrMsg[4];

    }
}