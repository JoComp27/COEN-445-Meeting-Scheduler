package requests;

public class DeniedRequest extends Request {

    private int requestQueryNumber;

    public DeniedRequest(RequestType requestType, int requestQueryNumber) {
        super(requestType);
        this.requestQueryNumber = requestQueryNumber;
    }

    public int getRequestQueryNumber() {
        return requestQueryNumber;
    }

    @Override
    public String toString() {
        return requestType.name() + "_" + requestQueryNumber + "_UNAVAILABLE";
    }
}
