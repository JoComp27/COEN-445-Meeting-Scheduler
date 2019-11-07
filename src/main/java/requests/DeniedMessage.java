package requests;

public class DeniedMessage extends Message {

    private Integer requestNumber;
    private String unavailable;

    public DeniedMessage() {
        super(RequestType.Denied);
        this.requestNumber = null;
        this.unavailable = null;
    }

    public DeniedMessage(Integer requestNumber, String unavailable) {
        super(RequestType.Denied);
        this.requestNumber = requestNumber;
        this.unavailable = unavailable;
    }

    public Integer getRequestNumber() {
        return requestNumber;
    }

    public String getUnavailable() {
        return unavailable;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "_" + requestNumber + "_" + unavailable;
    }

    @Override
    public void deserialize(String message) {

        String[] array = message.split("_");

        this.requestNumber = Integer.parseInt(array[1]);
        this.unavailable = array[2];
    }
}
