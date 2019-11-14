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

    private HashMap<String, Boolean[]> scheduleMap;     //String Date and Time, Boolean Array of size 2: True = Booked, False = Not Booked.
    private HashMap<String, Meeting> meetingMap;        //String MeetingNumber, Meeting Class

    public Server (){
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

                int port = DpReceive.getPort();
                /**Creating a new thread of each new request*/
                ServerHandle serverHandle = new ServerHandle(message, port);
                Thread threadServerHandle = new Thread(serverHandle);
                threadServerHandle.start();
                threadServerHandle.join();

                //Get the message from handler
                String messageToClient = serverHandle.getMessageToClient();
                System.out.println("Message to client1: " + serverHandle.getMessageToClient());
                byte[] bufferSend =  messageToClient.getBytes();
                DatagramPacket DpSend = new DatagramPacket(bufferSend, bufferSend.length);

                System.out.println("DpReceive Port " + DpReceive.getPort());
                //DpSend.setPort(DpReceive.getPort());
                System.out.println("DpReceive socket address" + DpReceive.getSocketAddress());
                DpSend.setSocketAddress(DpReceive.getSocketAddress());
                //Send to client
                System.out.println("Message to client2: " + serverHandle.getMessageToClient());
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public class ServerHandle implements Runnable{
        String message;
        int port;
        String messageToClient = "Initial value";

        public ServerHandle(String message, int port){
            this.message = message;
            this.port = port;
        }

        /**Takes the message received from the datagramPacket and separate the message using the "_"*/
        @Override
        public void run() {
            String[] receivedMessage = message.split("_");
            List<Meeting> listMeeting = new ArrayList<>();



            //Gets the request type to treat the message.
            System.out.println("receivedMessage: " + receivedMessage[0]);
            //int messageType = Integer.parseInt(receivedMessage[0]);
            //RequestType receivedRequestType = RequestType.values()[messageType];
            System.out.println("receivedMessage Value of: " + RequestType.valueOf(receivedMessage[0]));
            RequestType receivedRequestType = RequestType.valueOf(receivedMessage[0]);

            FileReaderWriter file = new FileReaderWriter();
            String currentDir = System.getProperty("user.dir");
            String filePath = currentDir + "log.txt";
            ArrayList<Message> room = new ArrayList<>();

            /**Cases to how to treat each of the requestTypes.*/
            switch(receivedRequestType){
                case Request:

                    //Time should be the 3rd + 4th element in the array
//                    if(receivedMessage.length > 2) {
//                        time = receivedMessage[2] + "_" + receivedMessage[3];
//                    }

                    /** Testing meeting **/
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(2019,10,9,15,0, 0);
                    String time = calendar.getTime().toString();
                    List<String> list = new ArrayList<>();
                    list.add("5984");
                    list.add("3434");
                    RequestMessage requestMessage = new RequestMessage(1, calendar, 1, list, "asdfa");
                    Meeting aMeeting = new Meeting(requestMessage, "state", 1,1, meetingMap, 1, 3434);
//                    aMeeting.getAcceptedMap().put(Integer.valueOf(list.get(0)), false);
//                    aMeeting.getAcceptedMap().put(Integer.valueOf(list.get(1)), true);
//                    meetingMap.put(list.get(0), aMeeting);
//                    meetingMap.put(list.get(1), aMeeting);
                    /** End of testing**/
                    //Message theMessage = new RequestMessage(message);

                    //If hashmap does not already have this time scheduled, add new key
                    if(!scheduleMap.containsKey(time)){
                        //Make first room taken
                        scheduleMap.put(time, new Boolean[]{true, false});
                        messageToClient = "Room 1 is available";
                        System.out.println("In server: " + messageToClient);

                        Meeting meeting = new Meeting(requestMessage, "First", 10, 1, null, 1, port);      //Accepted participants should always initialize as 1 for organizer





                        //Add new RequestMessage to list of meeting
                        //listMeeting.add(meeting);

                        /**Writes the message in the log file.*/
                        try {
                            file.WriteFile(filePath, message, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(scheduleMap.containsKey(time)){
                        //If first room not taken
                        if(!scheduleMap.get(time)[0]) {

                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[0] = true;
                            scheduleMap.put(time, roomArray);
                            messageToClient = "Room 1 is available";
                            System.out.println("In server: " + messageToClient);
                        }
                        else if(!scheduleMap.get(time)[1]){
                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[1] = true;
                            scheduleMap.put(time, roomArray);
                            messageToClient = "Room 2 is available";
                            System.out.println("In server: " + messageToClient);
                        }
                        else{
                            messageToClient = "Room is not available at this time. Choose another time";
                            System.out.println("In server: " + messageToClient);
                            break;
                        }
                    }
                    else{
                        messageToClient = "Room is not available at this time. Choose another time";
                        System.out.println("In server: " + messageToClient);
                        break;
                    }
                    /**Put the message inside the requestMap Hashmap.
                     * Key is the IP, and stores the received message.*/

                    for(String typeKey : scheduleMap.keySet()){
                        String key = typeKey.toString();
                        String value = Arrays.toString(scheduleMap.get(typeKey));
                        System.out.println("Hashmap");
                        System.out.println(key + ": " + value);
                    }


                    break;
                case Accept:
                    Meeting acceptMeeting = null;

                    //Go through all existing meetings
                    for(String typeKey : meetingMap.keySet()) {
                        //Go through all the participants in the existing meetings
                        for (int j = 0; j < meetingMap.size(); j++) {
                            //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                            if (meetingMap.get(typeKey).getRequestMessage().getParticipants().get(j) == Integer.toString(port)) {
                                acceptMeeting = meetingMap.get(typeKey);
                            }
                            else{
                                messageToClient = "You are not in a scheduled meeting";
                                break;
                            }
                        }
                    }

                    //Check if client is in the meeting AND if they already accepted the meeting
                    if(acceptMeeting.getAcceptedMap().containsKey(port) && acceptMeeting.getAcceptedMap().get(port) == false){
                        //Increment accepted count
                        acceptMeeting.incrementAcceptedParticipants();
                        //Make accepted boolean true
                        acceptMeeting.getAcceptedMap().replace(port, true);
                        messageToClient = "You have been added to the scheduled meeting";
                    }

                    break;
                case Reject:
                    Meeting rejectMeeting = null;

                    //Go through all existing meetings
                    for(String typeKey : meetingMap.keySet()) {
                        //Go through all the participants in the existing meetings
                        for (int j = 0; j < meetingMap.size(); j++) {
                            //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                            if (meetingMap.get(typeKey).getRequestMessage().getParticipants().get(j) == Integer.toString(port)) {
                                rejectMeeting = meetingMap.get(typeKey);
                            }
                            else{
                                messageToClient = "You are not in a scheduled meeting";
                                break;
                            }
                        }
                    }

                    //Check if client is in the meeting AND if they already accepted the meeting
                    if(rejectMeeting.getAcceptedMap().containsKey(port) && rejectMeeting.getAcceptedMap().get(port) == false){
                        messageToClient = "You have rejected the meeting";
                    }
                    break;
                case Withdraw:
                    //Do something
                    break;
                case Add:

                    /**receivedMessage[] -> 0 is Add, 1 Meeting Number
                     * Take meeting number, go to meetingMap search for that key.
                     * if exist, get the Meeting object, use method getAcceptedMap with "port".
                     *      if port does not exist, exit and return message "not invited"
                     *      else fetch the status of the requestor.
                     *          if true, return "already accepted"
                     *          else, change to "true". Return "Updated".
                     * */

                    /** Testing meeting *
                 Calendar calendar1 = Calendar.getInstance();
                 calendar1.set(2019,10,9,15,0, 0);
                 String time1 = calendar1.getTime().toString();
                 List<String> list1 = new ArrayList<>();     //Only for testing
                 list1.add("4545")
                 RequestMessage requestMessage1 = new RequestMessage(1, calendar1, 1, list1, "asdfa");
                 Meeting meeting1 = new Meeting(requestMessage1, "random", 2, 1,);
                 String portString = Integer.valueOf(port).toString();
                 meetingMap.put(portString, );
                 * End of testing**/

                    String meetingNumber = receivedMessage[1];
                    if (!meetingMap.containsKey(meetingNumber)){
                        messageToClient = "You are not invited for this meeting";
                    }else{
                        Meeting theMeeting = meetingMap.get(meetingNumber);
                        if (theMeeting.getAcceptedMap().get(port) == true){
                            messageToClient = "You have already accepted the meeting";
                        }else{
                            theMeeting.getAcceptedMap().put(port, true);
                            messageToClient = "You are added to the meeting " + meetingNumber;
                        }
                    }

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

        public String getMessageToClient(){
            return messageToClient;
        }
    }

}
