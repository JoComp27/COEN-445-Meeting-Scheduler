package requests;

import java.util.Calendar;
import java.util.Date;

public class InviteMessage extends Message {

    private int meetingNumber;
    private Calendar calendar;
    private String topic;
    private String requester;

    public InviteMessage(int meetingNumber, Calendar calendar, String topic, String requester) {
        super(RequestType.Invite);
        this.meetingNumber = meetingNumber;
        this.calendar = calendar;
        this.topic = topic;
        this.requester = requester;
    }

    public int getMeetingNumber() {
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
    public Message deserialize(String message) {

        String[] arrMsg = message.split("_");

        String[] cal = new String[1];

        for(int i = 0; i < 4; i++){
            cal = arrMsg[2].split(",");
        }

        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(cal[0]), Integer.parseInt(cal[1]), Integer.parseInt(cal[2]), Integer.parseInt(cal[3]), 0);


        return new InviteMessage(Integer.parseInt(arrMsg[1]), c, arrMsg[3], arrMsg[4]);
    }
}
