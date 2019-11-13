package Tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSend {

    public static void sendMessage(String message, int portNumber) throws IOException {

            // convert the String input into the byte array.
            byte buf[] = message.getBytes();

            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9997);

            DatagramSocket dp = new DatagramSocket();

            dp.send(DpSend);
            System.out.println("MESSAGE SENT");
    }

}
