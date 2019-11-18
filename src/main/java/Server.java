import Tools.CalendarUtil;
import Tools.FileReaderWriter;
import Tools.UdpSend;
import requests.*;

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
        this.meetingMap = new HashMap<>();
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
                    System.out.println(time);
                    List<String> list = new ArrayList<>();
                    list.add("5984");
                    list.add(Integer.valueOf(port).toString());
                    RequestMessage requestMessage = new RequestMessage(1, calendar, 1, list, "asdfa");
                    HashMap<Integer, Boolean> getAcceptedMap = new HashMap<>();
                    getAcceptedMap.put(Integer.valueOf(list.get(0)), false);
                    getAcceptedMap.put(Integer.valueOf(list.get(1)), true);
                    Meeting aMeeting = new Meeting(requestMessage, "state", 1,1, getAcceptedMap, 1, port);
                    meetingMap.put(Integer.valueOf(aMeeting.getId()).toString(), aMeeting);
                    /** End of testing**/
                    //Message theMessage = new RequestMessage(message);

                    //If hashmap does not already have this time scheduled, add new key
                    if(!scheduleMap.containsKey(time)){
                        //Make first room taken
                        /**NEED TO STORE theTime BECAUSE THE FORMAT OF time AND theTime is different.
                         * Format of calendar is different from the format saved inside requestMessage.getCalendar*/
                        String theTime = CalendarUtil.calendarToString(requestMessage.getCalendar());
                        System.out.println(theTime);
                        scheduleMap.put(theTime, new Boolean[]{true, false});
                        messageToClient = "Room is available. Your meeting number is " + aMeeting.getId();
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
                            messageToClient = "Room is available";
                            System.out.println("In server: " + messageToClient);
                        }
                        else if(!scheduleMap.get(time)[1]){
                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[1] = true;
                            scheduleMap.put(time, roomArray);
                            messageToClient = "Room is available";
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
                            else{
                                messageToClient = "You are not in a scheduled meeting";
                            }
                        }
                    }
//                    //Go through the request and the valid participants
//                    for(int i = 0; i < meeting.getRequestMessage().getParticipants().size(); i++) {
//                        //If valid increment the accepted count
//                        if (meeting.getRequestMessage().getParticipants().get(i) == Integer.toString(port)) {
//                            meeting.incrementAcceptedParticipants();
//                        }
//                    }
                    //Check if client is in the meeting AND if they already accepted the meeting
                    if(meeting.getAcceptedMap().containsKey(port) && meeting.getAcceptedMap().get(port) == false){
                        //Increment accepted count
                        meeting.incrementAcceptedParticipants();
                        //Make accepted boolean true
                        meeting.getAcceptedMap().replace(port, true);
                        messageToClient = "You have been added to the scheduled meeting";
                    }

                    break;
                case Reject:
                    //Do something
                    break;
                case Withdraw:
                    //Do something
                    break;
                case Add:

                    /**receivedMessage[] -> 0 is Add, 1 Meeting Number
                     * Take meeting number, go to meetingMap search for that key.
                     * if exist, get the Meeting object, use method getAcceptedMap with "port".
                     *      if port does not exist, exit and return message "not invited"
                     *      else if check if the port number exists in getAcceptedMap.
                     *      else fetch the status of the requestor.
                     *          if true, return "already accepted"
                     *          else, change to "true". Return "Updated".
                     * */

                    String meetingNumber = receivedMessage[1];
                    System.out.println(meetingNumber);
                    /**If your meeting number does not exist*/
                    if (!meetingMap.containsKey(meetingNumber)){
                        messageToClient = "The meeting number you provided does not exist";
                    }
                    else if(!meetingMap.get(meetingNumber).getAcceptedMap().containsKey(port)){
                        messageToClient = "You are not invited in the meeting.";
                    }
                    else{
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

                    /**receivedMessage[] -> 0 is Cancel (or RequesterCancel in this case), 1 Meeting Number
                     * if(meeting number exist)
                     *      Get the meeting connected the meeting number.
                     *      if(port of received message == meeting.getOrganizer())
                     *          String date = meeting.getRequestMessage[2]
                     *          String roomNumber = meeting.getRoomNumber
                     *          scheduleMap(date, roomNumber) -> Change room number boolean into False.
                     *          meetingMap -> delete meeting with specified meeting number (key included)
                     *          return Cancel message to all clients.
                     *
                     *      else
                     *          Not requestor, cannot cancel meeting
                     * else
                     *      Meeting does not exist*/


                    String mNumber = receivedMessage[1];
                    System.out.println("Meeting number " + mNumber);
                    System.out.println("Meeting Map: " + meetingMap);
                    if(meetingMap.containsKey(mNumber)){
                        Meeting theMeeting = meetingMap.get(mNumber);
                        System.out.println(theMeeting.getRoomNumber());
                        if(port == theMeeting.getOrganizer()){
                            String date = CalendarUtil.calendarToString(theMeeting.getRequestMessage().getCalendar());
                            System.out.println(date);
                            int roomNumber = theMeeting.getRoomNumber();
                            Boolean[] rooms = scheduleMap.get(date);
                            System.out.println("Boolean Rooms: " + rooms);

                            /**NEED TO SEND MESSAGE TO ALL INVITED PARTICIPANTS. SENDING THE SAME
                             * MESSAGE TO DIFFERENT PORT NUMBER.
                             *
                             * Create array, store all port number getAcceptedMap.
                             * Loop through the array
                             *      Create ServerCancelMessage object to send message
                             *      Call the serialize function to send.
                             * */

                            List<Integer> participants = new ArrayList<>();

                            if(roomNumber == 1){
                                /**CHECK THE INDEX WELL. MIGHT HAVE TO SUBTRACT 1*/

                                Set<Integer> portNumber = theMeeting.getAcceptedMap().keySet();
                                for (Integer port : portNumber){
                                    participants.add(port);
                                }
                                for (int i = 0; i < participants.size(); i++){
                                    System.out.println(participants.get(i));
                                }

                                /**Loop through the save ports and send message to them*/

                                for (int i = 0; i < participants.size(); i++){
                                    ServerCancelMessage serverCancelMessage = new ServerCancelMessage(participants.get(i), "ServerCancel_Requestor_Cancelled_Meeting");
                                    /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.*/
                                }

                                rooms[roomNumber] = false;
                                scheduleMap.replace(date, rooms);
                                meetingMap.remove(mNumber);

                            }
                            else if(roomNumber == 2){
                                /**CHECK THE INDEX WELL. MIGHT HAVE TO SUBTRACT 1*/

                                Set<Integer> portNumber = theMeeting.getAcceptedMap().keySet();
                                for (Integer port : portNumber){
                                    participants.add(port);
                                }
                                for (int i = 0; i < participants.size(); i++){
                                    System.out.println(participants.get(i));
                                }

                                /**Loop through the save ports and send message to them*/

                                for (int i = 0; i < participants.size(); i++){
                                    ServerCancelMessage serverCancelMessage = new ServerCancelMessage(participants.get(i), "ServerCancel_Requestor_Cancelled_Meeting");
                                    /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.*/
                                }

                                rooms[roomNumber] = false;
                                scheduleMap.replace(date, rooms);
                                meetingMap.remove(mNumber);

                            }
                            System.out.println("meetingMap: " + meetingMap);
                            System.out.println("scheduleMap" + scheduleMap);


                        }
                        else{
                            messageToClient = "Not requestor, cannot cancel meeting";
                        }
                    }
                    else{
                        messageToClient = "Meeting does not exist";
                    }

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
