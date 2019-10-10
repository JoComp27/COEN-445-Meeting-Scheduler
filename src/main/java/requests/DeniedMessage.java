package requests;

public class DeniedMessage extends Message {

    private int requestQueryNumber;
    private String unavailable;

    public DeniedMessage(int requestQueryNumber, String unavailable) {
        super(RequestType.Denied);
        this.requestQueryNumber = requestQueryNumber;
        this.unavailable = unavailable;
    }

    public int getRequestQueryNumber() {
        return requestQueryNumber;
    }

    public String getUnavailable() {
        return unavailable;
    }

    @Override
    public String serialize() {
        return requestType.ordinal() + "_" + requestQueryNumber + "_" + unavailable;
    }

    @Override
    public Message deserialize(String message) {

        String[] array = message.split("_");

        DeniedMessage msg = new DeniedMessage(Integer.parseInt(array[1]), array[2]);

        return msg;
    }
}
