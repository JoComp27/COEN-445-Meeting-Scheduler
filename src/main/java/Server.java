import requests.RequestType;

import java.net.*;
import java.io.*;
import java.lang.*;

public class Server implements Runnable{

    public static void main(String[] args){
        System.out.println("SERVER LAUNCHED");
    	Server server = new Server();
        server.run();
    }

    @Override
    public void run() {
        /**Create new server and binds to a free port. From source of the internet
         * the range should be 49152 - 65535.*/

    	try {
			System.out.println("Server Address: " + InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
        /**The port address is chosen randomly*/
        try(DatagramSocket serverSocket = new DatagramSocket(9999)) {
            byte[] buffer = new byte[65535];
            /**Messages here and sends to client*/
            while(true){
            	System.out.println("SERVER STARTED TO LISTEN");
                DatagramPacket DpReceive = new DatagramPacket(buffer, buffer.length);   //Create Datapacket to receive the data
                serverSocket.receive(DpReceive);        //Receive Data in Buffer
                System.out.println(DpReceive.getData());
                System.out.println(DpReceive.getAddress());
                String message = new String(DpReceive.getData());
                System.out.println("Client says: " + message);

                /**NEED TO ADD IN TIMEOUT OPTIONS TO RESEND THE MESSAGE. HAVE YET TO
                 * COMPLETE THIS PORTION OF THE CODE
                 *
                 * Add in Thread and feed in the message*/

                ServerHandle serverHandle = new ServerHandle(message);
                new Thread(serverHandle).start();

                if(message.equals("Bye")){
                    System.out.println("Client says bye. Exiting");
                    break;
                }
            }

        }catch (SocketException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public class ServerHandle implements Runnable{

        String message;

        public ServerHandle(String message){
            this.message = message;
        }

        /**Takes the message received from the datagramPacket and separate the message using the "_"*/
        @Override
        public void run() {
            String[] receivedMessage = message.split("_");

            //Gets the request type to treat the message.
            int messageType = Integer.parseInt(receivedMessage[0]);
            RequestType receivedRequestType = RequestType.values()[messageType];

            /**Cases to how to treat each of the requestTypes.*/
            switch(receivedRequestType){
                case RoomChange:
                    //Do something
                case Added:
                    //Do something
                case Denied:
                    //Do something
                case Invite:
                    //Do something
                case Confirm:
                    //Do something
                case Scheduled:
                    //Do something
                case NotScheduled:
                    //Do something
                case ServerCancel:
                    //Do something
                default:
                    System.out.println("Request type does not correspond. Exiting.");
                    break;

            }

        }
    }

}
