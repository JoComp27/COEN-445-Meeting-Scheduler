package requests;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RegisterMessage extends Message {

    InetSocketAddress clientSocketAddress;
    String clientName;

    public RegisterMessage() {
        super(RequestType.Register);
    }

    public RegisterMessage(String clientName, InetSocketAddress clientSocketAddress) {
        super(RequestType.Register);
        this.clientSocketAddress = clientSocketAddress;
        this.clientName = clientName;
    }

    public InetSocketAddress getClientSocketAddress() {
        return clientSocketAddress;
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public String serialize() {

        String result = requestType.ordinal() + "$" + clientName + "$" + clientSocketAddress.getPort();

        return result;
    }

    @Override
    public void deserialize(String message) {

        String[] resultMsg = message.split("\\$");

        this.clientName = resultMsg[1];

        try {
            this.clientSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), Integer.parseInt(resultMsg[2]));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
