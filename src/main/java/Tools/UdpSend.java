package Tools;

import java.io.IOException;
import java.net.*;

public class UdpSend {

    public static void sendMessage(String message, DatagramSocket sourceDatagramSocket, SocketAddress destinationSocketAddress) {

        // convert the String input into the byte array.
        byte buf[] = message.getBytes();

        DatagramPacket DpSend = new DatagramPacket(buf, buf.length);
        DpSend.setSocketAddress(destinationSocketAddress);

        try {
            sourceDatagramSocket.send(DpSend);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("SERVER SENT");
    }

}
