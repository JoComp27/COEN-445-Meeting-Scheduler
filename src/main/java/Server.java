import Tools.FileReaderWriter;
import requests.Meeting;
import requests.Message;
import requests.RequestMessage;
import requests.RequestType;

import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;


public class Server implements Runnable{

    HashMap<String, String> requestMap;
    HashMap<String, ArrayList<RequestMessage>> scheduleMap;

    public Server (){
        this.requestMap = new HashMap<>();
        this.scheduleMap = new HashMap<>();
    }

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
        try(DatagramSocket serverSocket = new DatagramSocket(9997)) {
            byte[] buffer = new byte[100];
            /**Messages here and sends to client*/
            while(true){
            	System.out.println("SERVER STARTED TO LISTEN");
                DatagramPacket DpReceive = new DatagramPacket(buffer, buffer.length);   //Create Datapacket to receive the data
                serverSocket.receive(DpReceive);        //Receive Data in Buffer

                //System.out.println(DpReceive.getData());
                //System.out.println(DpReceive.getAddress());
                String message = new String(DpReceive.getData());

                System.out.println("DpReceive getAddress" + DpReceive.getAddress());
                System.out.println("DpReceive socket address" + DpReceive.getSocketAddress());

                System.out.println("Client says: " + message);



                /**NEED TO ADD IN TIMEOUT OPTIONS TO RESEND THE MESSAGE. HAVE YET TO
                 * COMPLETE THIS PORTION OF THE CODE
                 *
                 * Add in Thread and feed in the message*/
                String sendToClient = "From the server";
                byte[] bufferSend =  sendToClient.getBytes();
                DatagramPacket DpSend = new DatagramPacket(bufferSend, bufferSend.length);
                int port = DpReceive.getPort();
                /**Creating a new thread of each new request*/
                ServerHandle serverHandle = new ServerHandle(message, port);
                new Thread(serverHandle).start();


                System.out.println("DpReceive Port " + DpReceive.getPort());
                //DpSend.setPort(DpReceive.getPort());
                System.out.println("DpReceive socket address" + DpReceive.getSocketAddress());
                DpSend.setSocketAddress(DpReceive.getSocketAddress());
                serverSocket.send(DpSend);

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
        int port;

        public ServerHandle(String message, int port){
            this.message = message;

        }

        /**Takes the message received from the datagramPacket and separate the message using the "_"*/
        @Override
        public void run() {
            String[] receivedMessage = message.split("_");
            List<Meeting> listMeeting = new ArrayList<>();


            //Gets the request type to treat the message.
            System.out.println("receivedMessage: " + receivedMessage[0]);
            int messageType = Integer.parseInt(receivedMessage[0]);
            RequestType receivedRequestType = RequestType.values()[messageType];

            FileReaderWriter file = new FileReaderWriter();
            String currentDir = System.getProperty("user.dir");
            String filePath = currentDir + "log.txt";

            ArrayList<Message> room = new ArrayList<>();

            /**Cases to how to treat each of the requestTypes.*/
            switch(receivedRequestType){
                case Request:
                    //Time should be the 3rd + 4th element in the array
                    String time = receivedMessage[2] + "_" + receivedMessage[3];
                    //Message theMessage = new RequestMessage(message);
                    if(!requestMap.containsKey(time)){
                        requestMap.put(time, message);

                        /** Testing meeting **/
                        Calendar calendar = Calendar.getInstance();
                        List<String> list = new ArrayList<>();
                        list.add("ASDADS");
                        RequestMessage requestMessage = new RequestMessage(1, calendar, 5, list, "asdfa");
                        Meeting meeting = new Meeting(requestMessage, "First", 10, 1);      //Accepted participants should always initialize as 1 for organizer
                        /** End of testing**/

                        /**Writes the message in the log file.*/
                        try {
                            file.WriteFile(filePath, message, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        break;
                    }
                    /**Put the message inside the requestMap Hashmap.
                     * Key is the IP, and stores the received message.*/



                    if(room.size() < 2){

                    }

                    //scheduleMap.put(receivedMessage[2] + "_" + receivedMessage[3],room);


                    break;
                case Accept:
                    Meeting meeting = null;
                    //Go through all existing meetings
                    for(int i = 0; i < listMeeting.size(); i++) {
                        //Go through all the participants in the existing meetings
                        for(int j = 0; j < listMeeting.get(i).getRequestMessage().getParticipants().size(); j++) {
                            //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                            if (listMeeting.get(i).getRequestMessage().getParticipants().get(j) == Integer.toString(port)){
                                meeting = listMeeting.get(i);
                            }
                        }
                    }
                    //Go through the request and the valid participants
                    for(int i = 0; i < meeting.getRequestMessage().getParticipants().size(); i++) {
                        //If valid increment the accepted count
                        if (meeting.getRequestMessage().getParticipants().get(i) == Integer.toString(port)) {
                            meeting.incrementAcceptedParticipants();
                        }
                    }

                    break;
                case Reject:
                    //Do something
                    break;
                case Withdraw:
                    //Do something
                    break;
                case Add:
                    //Do something
                    break;
                case RequesterCancel:
                    //Do something
                    break;
                default:
                    System.out.println("Request type does not correspond. Exiting.");
                    break;

            }

        }
    }

}
