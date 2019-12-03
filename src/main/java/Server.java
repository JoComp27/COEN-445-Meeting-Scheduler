import Tools.CalendarUtil;
import Tools.FileReaderWriter;
import Tools.UdpSend;
import requests.*;

import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


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

        //Checking if previous
        File saveFile = new File("server.txt");

        if(saveFile.exists()){

            //Add CLI to check if user wants to restore user or not.

            System.out.println("It seems that a restore file is available and could be loaded onto the" +
                    "server\n Do you wish to restore it?");

            String answer = "";

            Scanner scanner = new Scanner(System.in);

            while(!answer.equals("y") || !answer.equals("n")){

                answer = scanner.nextLine().trim();
                switch (answer) {
                    case "y":
                        System.out.println("Save will be restored for server");
                        server.loadServer();
                        break;
                    case "n":
                        System.out.println("Save will not be restored for client");
                        break;
                    default:
                        System.out.println("INVALID SAVE RESTORE ANSWER");
                }
            }


        }

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

            ServerCommand serverCommand = new ServerCommand();
            Thread threadServerCommand = new Thread(serverCommand);
            threadServerCommand.start();

            while(true){

            	System.out.println("-------------- SERVER STARTED TO LISTEN --------------");
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

                //Create server command thread



                //If we type "RoomChange_MT#_Room#" ex. "RoomChange_3_2"
                //Set the message to that
                System.out.println("Right here");
//                String[] s = serverCommand.getCommandMessage().split("_");
//                if(s[0].equals("RoomChange")){
//                    System.out.println("In room change if statement");
//
//                    //threadServerHandle.start();
//                }
                //String serverMessage = serverCommand.getCommandMessage();
                //serverHandle = new ServerHandle(serverMessage,port,DpReceive.getSocketAddress());

                //threadServerHandle.start();

                ServerHandle serverHandle = new ServerHandle(message, port, DpReceive.getSocketAddress());
                Thread threadServerHandle = new Thread(serverHandle);
                //= new ServerHandle(message, port);
                //threadServerHandle = new Thread(serverHandle);
                threadServerHandle.start();
                //threadServerHandle.join();

                //Get the message from handler
//                String messageToClient = serverHandle.getMessageToClient();
//                byte[] bufferSend =  messageToClient.getBytes();
//                DatagramPacket DpSend = new DatagramPacket(bufferSend, bufferSend.length);
//
//                System.out.println("DpReceive Port " + DpReceive.getPort());
//                //DpSend.setPort(DpReceive.getPort());
//                //System.out.println("DpReceive socket address" + DpReceive.getSocketAddress());
//                DpSend.setSocketAddress(DpReceive.getSocketAddress());
//                //Send to client
//
//                serverSocket.send(DpSend);

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
        SocketAddress socketAddress;
        String messageToClient = "Initial value";

        public ServerHandle(String message, int port, SocketAddress socketAddress){
            this.message = message;
            this.port = port;
            this.socketAddress = socketAddress;
        }

        /**Takes the message received from the datagramPacket and separate the message using the "_"*/
        @Override
        public void run() {
            String[] receivedMessage = message.split("_");


            //Gets the request type to treat the message.
            System.out.println("receivedMessage: " + receivedMessage[0]);
            System.out.println("receivedMessage: " + receivedMessage[1]);
            System.out.println("receivedMessage: " + receivedMessage[2]);
            System.out.println("receivedMessage: " + receivedMessage[3]);
            System.out.println("receivedMessage: " + receivedMessage[4]);
            System.out.println("receivedMessage: " + receivedMessage[5]);

            //System.out.println("receivedMessage Value of: " + RequestType.valueOf(receivedMessage[0]));
//            if(receivedMessage[0] == "9" || receivedMessage[0].equals("9")){
//                receivedMessage[0] = "Request";
//            }
            //int messageType = Integer.parseInt(receivedMessage[0]);
            //System.out.println("Message type: " + messageType);
            //RequestType receivedRequestType = RequestType.values()[messageType];

            RequestType receivedRequestType = RequestType.valueOf(receivedMessage[0]);

            FileReaderWriter file = new FileReaderWriter();
            String currentDir = System.getProperty("user.dir");
            String filePath = currentDir + "log.txt";

            /**Cases to how to treat each of the requestTypes.*/
            switch(receivedRequestType){
                case Request:

                    System.out.println(" Receiving Port: " + port);

                    RequestMessage requestMessage = new RequestMessage();

                    requestMessage.deserialize(message);


                    String time = CalendarUtil.calendarToString(requestMessage.getCalendar());
                    //System.out.println("TIME: " + time);
                    //Make meeting object
                    //Accepted participants should always initialize as 1 for organizer
                    Meeting meeting = new Meeting(requestMessage, null, requestMessage.getParticipants().size(), 1, new HashMap<Integer, Boolean>(), 0, port);
                    System.out.println("Meeting Number: " + meeting.getId());

                    //If this meeting does not exist yet
                    if(!scheduleMap.containsKey(time)){
                        //Make first room taken
                        scheduleMap.put(time, new Boolean[]{true, false});
                        UdpSend.sendMessage(requestMessage.serialize(), socketAddress);
                        messageToClient = "Room 1 is available";

                        System.out.println("In server: " + messageToClient);


                        meeting.setRoomNumber(1);
                        //Set all participants accepted value to false (none have accepted in this stage)
                        meeting.setAcceptedMap();
                        //Add meeting to hashmap that lists all existing meetings
                        meetingMap.put(Integer.toString(meeting.getId()), meeting);


                        /**Writes the message in the log file.*/
                        file.WriteFile(filePath, message, true);
                    }
                    else if(scheduleMap.containsKey(time)){
                        //If first room not taken
                        if(!scheduleMap.get(time)[0]) {
                            //Set room number to 1
                            meeting.setRoomNumber(1);
                            meetingMap.put(Integer.toString(meeting.getId()), meeting);
                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[0] = true;
                            scheduleMap.put(time, roomArray);
                            messageToClient = "Room 1 is available";
                            System.out.println("In server: " + messageToClient);
                            UdpSend.sendMessage(requestMessage.serialize(), socketAddress);
                            //Create meeting
                            //Add meeting to meetingMap
                        }
                        else if(!scheduleMap.get(time)[1]){
                            //Set room number to 2
                            meeting.setRoomNumber(2);
                            meetingMap.put(Integer.toString(meeting.getId()), meeting);
                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[1] = true;
                            scheduleMap.put(time, roomArray);
                            messageToClient = "Room 2 is available";
                            System.out.println("In server: " + messageToClient);
                            UdpSend.sendMessage(requestMessage.serialize(), socketAddress);
                            //Create meeting
                            //Add meeting to meetingMap
                        }
                        else{
                            messageToClient = "Room is not available at this time. Choose another time";
                            UdpSend.sendMessage(requestMessage.serialize(), socketAddress);
                            System.out.println("In server: " + messageToClient);
                            break;
                        }
                    }
                    else{
                        messageToClient = "Room is not available at this time. Choose another time";
                        UdpSend.sendMessage(requestMessage.serialize(), socketAddress);
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
                    AcceptMessage acceptMessage = new AcceptMessage();
                    acceptMessage.deserialize(message);
                    String meetingNumberAccept = Integer.toString(acceptMessage.getMeetingNumber());

                    Meeting acceptMeeting = null;
                    boolean foundMatchAccept = false;
                    for(String typeKey : meetingMap.keySet()){
                        String key = typeKey.toString();
                        String value = meetingMap.get(typeKey).getRequestMessage().getParticipants().toString();
                        System.out.println("Hashmap");
                        System.out.println(key + ": " + value);
                    }

                    //Go through all the participants in the existing meetings
                    for (int j = 0; j < meetingMap.size(); j++) {
                        //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                        if (meetingMap.containsKey(meetingNumberAccept) && meetingMap.get(meetingNumberAccept).getRequestMessage().getParticipants().get(j).equals(Integer.toString(port))) {
                            acceptMeeting = meetingMap.get(meetingNumberAccept);
                            foundMatchAccept = true;
                        }
                    }


                    if(!foundMatchAccept){
                        messageToClient = "You are not in a scheduled meeting";
                        break;
                    }

                    //Check if client is in the meeting AND if they already accepted the meeting
                    if(acceptMeeting.getAcceptedMap().containsKey(port) && acceptMeeting.getAcceptedMap().get(port) == false){
                        //Increment accepted count
                        acceptMeeting.incrementAcceptedParticipants();
                        //Make accepted boolean true
                        acceptMeeting.getAcceptedMap().replace(port, true);
                        messageToClient = "You have been added to the scheduled meeting";
                        UdpSend.sendMessage(acceptMessage.serialize(), socketAddress);
                    }

                    break;
                case Reject:
                    RejectMessage rejectMessage = new RejectMessage();
                    rejectMessage.deserialize(message);
                    String meetingNumberReject = Integer.toString(rejectMessage.getMeetingNumber());

                    Meeting rejectMeeting = null;
                    boolean foundMatchReject = false;


                        //Go through all the participants in the existing meetings
                        for (int j = 0; j < meetingMap.size(); j++) {
                            //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                            if (meetingMap.containsKey(meetingNumberReject) && meetingMap.get(meetingNumberReject).getRequestMessage().getParticipants().get(j).equals(Integer.toString(port))) {
                                rejectMeeting = meetingMap.get(meetingNumberReject);
                                foundMatchReject = true;
                            }
                        }


                    if(!foundMatchReject){
                        messageToClient = "You are not in a scheduled meeting";
                        break;
                    }

                    //Check if client is in the meeting AND if they already accepted the meeting
                    if(rejectMeeting.getAcceptedMap().containsKey(port) && rejectMeeting.getAcceptedMap().get(port) == false){
                        messageToClient = "You have rejected the meeting";
                        UdpSend.sendMessage(rejectMessage.serialize(), socketAddress);
                    }
                    else{
                        //If client has already accepted, they cannot Reject
                        messageToClient = "You cannot send this message";
                    }
                    break;
                case Withdraw:
                    WithdrawMessage withdrawMessage = new WithdrawMessage();
                    withdrawMessage.deserialize(message);
                    String withdrawMeetingNumber = Integer.toString(withdrawMessage.getMeetingNumber());
                    int withdrawMeetingNumberINT = withdrawMessage.getMeetingNumber();
                    List<Integer> NotAcceptedParticipants = new ArrayList<>();
                    List<Integer> acceptedParticipants = new ArrayList<>();

                    if (meetingMap.containsKey(withdrawMeetingNumber)){
                        Meeting withdrawMeeting = meetingMap.get(withdrawMeetingNumber);
                        if (withdrawMeeting.getAcceptedMap().containsKey(port)){
                            /**Withdraws the client that sent the withdraw command from the meeting
                             * CHECK IF IT SENDS TO ALL OTHER CLIENTS */
                            withdrawMeeting.getAcceptedMap().remove(port);

                            int requesterPort = withdrawMeeting.getOrganizer();
                            try {
                                SocketAddress requesterSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), requesterPort);
                                ServerWidthdrawMessage serverWidthdrawMessage = new ServerWidthdrawMessage(Integer.valueOf(withdrawMeetingNumber), Integer.toString(port));
                                UdpSend.sendMessage(serverWidthdrawMessage.serialize(), requesterSocketAddress);


                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }

                            /**Adding all participants that has yet to accepted the invite*/
                            Set<Integer> portNumber = withdrawMeeting.getAcceptedMap().keySet();
                            for (Integer port : portNumber){
                                if (withdrawMeeting.getAcceptedMap().get(port) == false){
                                    NotAcceptedParticipants.add(port);
                                }
                            }

                            /**Sending invite message to those not who have not accepted
                             * CHECK IF IT SENDS TO ALL OTHER CLIENTS*/
                            RequestMessage requestMessageFromMeeting = withdrawMeeting.getRequestMessage();
                            InviteMessage inviteMessage = new InviteMessage(Integer.valueOf(withdrawMeetingNumber),
                                    requestMessageFromMeeting.getCalendar(),
                                    requestMessageFromMeeting.getTopic(),
                                    Integer.toString(withdrawMeeting.getOrganizer()));
                            for (int i = 0; i < NotAcceptedParticipants.size(); i++){
                                try {
                                    SocketAddress participantSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), Integer.valueOf(NotAcceptedParticipants.get(i)));
                                    UdpSend.sendMessage(inviteMessage.serialize(), participantSocketAddress);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                            }

                            try {
                                TimeUnit.MINUTES.sleep(1);
                                /**Save all the participants who have accepted after timeout*/
                                for (Integer port : portNumber){
                                    if (withdrawMeeting.getAcceptedMap().get(port) == true){
                                        acceptedParticipants.add(port);
                                    }
                                }

                                int minimumParticipants = withdrawMeeting.getRequestMessage().getMinimum();

                                if (acceptedParticipants.size() < minimumParticipants){
                                    List<Integer> participants = new ArrayList<>();

                                    for (Integer port : portNumber){
                                        participants.add(port);
                                    }

                                    /**NOT SURE IF IT WORKS FOR SENDING THE MESSAGE TO ALL*/
                                    for (int i = 0; i < participants.size(); i++){
                                        SocketAddress participantsSocket = new InetSocketAddress(InetAddress.getLocalHost(), Integer.valueOf(participants.get(i)));
                                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage(Integer.valueOf(withdrawMeetingNumber), "Not enough participants for meeting #" + withdrawMeetingNumber);
                                        UdpSend.sendMessage(serverCancelMessage.serialize(), participantsSocket);
                                    }

                                    String date = CalendarUtil.calendarToString(withdrawMeeting.getRequestMessage().getCalendar());
                                    int roomNumber = withdrawMeeting.getRoomNumber();
                                    Boolean[] rooms = scheduleMap.get(date);

                                    if(roomNumber == 1){
                                        rooms[roomNumber] = false;
                                        scheduleMap.replace(date, rooms);
                                        meetingMap.remove(withdrawMeetingNumber);
                                    }
                                    else if (roomNumber == 2){
                                        rooms[roomNumber] = false;
                                        scheduleMap.replace(date, rooms);
                                        meetingMap.remove(withdrawMeetingNumber);
                                    }

                                }
                            } catch (InterruptedException | UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            messageToClient = "You were never invited";
                            DeniedMessage deniedMessage = new DeniedMessage(withdrawMessage.getMeetingNumber(), messageToClient);
                            UdpSend.sendMessage(deniedMessage.serialize(), socketAddress);
                        }
                    }
                    else{
                        messageToClient = "Meeting does not exist.";
                        DeniedMessage deniedMessage = new DeniedMessage(withdrawMessage.getMeetingNumber(), messageToClient);
                        UdpSend.sendMessage(deniedMessage.serialize(), socketAddress);
                    }
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

                    AddMessage addMessage = new AddMessage();
                    addMessage.deserialize(message);
                    String meetingNumber = Integer.valueOf(addMessage.getMeetingNumber()).toString();
                    //String meetingNumber = receivedMessage[1];
                    System.out.println(meetingNumber);
                    System.out.println(" Receiving Port: " + port);
                    /*for (int i = 0; i < meetingMap.get(meetingNumber).getAcceptedMap().size(); i++){
                        System.out.println(meetingMap.get(meetingNumber).getAcceptedMap().keySet());
                    }*/
                    /**If your meeting number does not exist*/
                    if (!meetingMap.containsKey(meetingNumber)){
                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage(addMessage.getMeetingNumber(), "The meeting number you provided does not exist");
                        //messageToClient = "The meeting number you provided does not exist";
                        UdpSend.sendMessage(serverCancelMessage.serialize(), socketAddress);
                    }
                    else if(!meetingMap.get(meetingNumber).getAcceptedMap().containsKey(port)){
                        System.out.print("AcceptedMap: ");
                        System.out.print(meetingMap.get(meetingNumber).getAcceptedMap());
                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage(addMessage.getMeetingNumber(), "You are not invited in the meeting.");
                        messageToClient = "You are not invited in the meeting.";
                        UdpSend.sendMessage(serverCancelMessage.serialize(), socketAddress);
                    }
                    else{
                        Meeting theMeeting = meetingMap.get(meetingNumber);
                        if (theMeeting.getAcceptedMap().get(port) == true){
                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(addMessage.getMeetingNumber(), "You have already accepted the meeting");
                            messageToClient = "You have already accepted the meeting";
                            UdpSend.sendMessage(serverCancelMessage.serialize(), socketAddress);
                        }else{

                            theMeeting.getAcceptedMap().put(port, true);
                            SocketAddress hostSocketAddress = null;
                            try {
                                hostSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), theMeeting.getOrganizer());
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }

                            /*if (socketAddress == hostSocketAddress){
                                AddedMessage addedMessage = new AddedMessage(addMessage.getMeetingNumber(), socketAddress.toString());
                                ConfirmMessage confirmMessage = new ConfirmMessage(addedMessage.getMeetingNumber(), theMeeting.getRoomNumber());
                                UdpSend.sendMessage(confirmMessage.serialize(), socketAddress);
                            }
                            else{
                                AddedMessage addedMessage = new AddedMessage(addMessage.getMeetingNumber(), socketAddress.toString());
                                UdpSend.sendMessage(addedMessage.serialize(), hostSocketAddress);
                                messageToClient = "You are added to the meeting " + meetingNumber;
                                ConfirmMessage confirmMessage = new ConfirmMessage(addedMessage.getMeetingNumber(), theMeeting.getRoomNumber());
                                UdpSend.sendMessage(confirmMessage.serialize(), socketAddress);
                            }*/

                            AddedMessage addedMessage = new AddedMessage(addMessage.getMeetingNumber(), socketAddress.toString());
                            UdpSend.sendMessage(addedMessage.serialize(), hostSocketAddress);
                            messageToClient = "You are added to the meeting " + meetingNumber;
                            ConfirmMessage confirmMessage = new ConfirmMessage(addedMessage.getMeetingNumber(), theMeeting.getRoomNumber());
                            UdpSend.sendMessage(confirmMessage.serialize(), socketAddress);
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

                    RequesterCancelMessage requesterCancelMessage = new RequesterCancelMessage();
                    requesterCancelMessage.deserialize(message);
                    String mNumber = Integer.toString(requesterCancelMessage.getMeetingNumber());
                    //String mNumber = receivedMessage[1];
                    System.out.println("Meeting number " + mNumber);
                    System.out.println("Meeting Map: " + meetingMap);
                    if(meetingMap.containsKey(mNumber)){
                        Meeting theMeeting = meetingMap.get(mNumber);
                        System.out.println(theMeeting.getRoomNumber());
                        if(port == theMeeting.getOrganizer()){
                            System.out.println("Organizer" + theMeeting.getOrganizer() + " Receiving Port: " + port);
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
                            SocketAddress nonHostSocketAddress = null;

                            if(roomNumber == 1){
                                /**CHECK THE INDEX WELL. MIGHT HAVE TO SUBTRACT 1*/
                                Set<Integer> portNumber = theMeeting.getAcceptedMap().keySet();
                                for (Integer port : portNumber){
                                    participants.add(port);
                                }
                                for (int i = 0; i < participants.size(); i++){
                                    System.out.println(participants.get(i));
                                }

                                /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.
                                try {
                                    socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), participants.get(0));
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                                ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                UdpSend.sendMessage(serverCancelMessage.serialize(), socketAddress);*/

                               /**Loop through the save ports and send message to them*/
                                for (int i = 0; i < participants.size(); i++){
                                     /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.*/
                                    try {
                                        nonHostSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), Integer.valueOf(participants.get(i)));
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }

                                    /**THINKING OF CREATING THREAD TO SEND MESSAGE TO ALL INVITED PEOPLE*/
                                    /*SocketAddress finalSocketAddress = nonHostSocketAddress;
                                    Thread thread = new Thread(){
                                        public void run(){
                                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                            UdpSend.sendMessage(serverCancelMessage.serialize(), finalSocketAddress);
                                        }
                                    };
                                    thread.start();*/

                                    ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                    UdpSend.sendMessage(serverCancelMessage.serialize(), nonHostSocketAddress);
                                }

                                rooms[roomNumber - 1] = false;
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
                                    /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.*/
                                    try {
                                        nonHostSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), Integer.valueOf(participants.get(i)));
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }

                                    /**THINKING OF CREATING THREAD TO SEND MESSAGE TO ALL INVITED PEOPLE*/
                                    /*SocketAddress finalSocketAddress = nonHostSocketAddress;
                                    Thread thread = new Thread(){
                                        public void run(){
                                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                            UdpSend.sendMessage(serverCancelMessage.serialize(), finalSocketAddress);
                                        }
                                    };
                                    thread.start();*/

                                    ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                    UdpSend.sendMessage(serverCancelMessage.serialize(), nonHostSocketAddress);
                                }

                                rooms[roomNumber - 1] = false;
                                scheduleMap.replace(date, rooms);
                                meetingMap.remove(mNumber);

                            }
                            System.out.println("meetingMap: " + meetingMap);
                            System.out.println("scheduleMap" + scheduleMap);

                        }
                        /**MIGHT HAVE TO CHANGE THE SERVERCANCELMESSAGE TO DENIEDMESSAGE TYPE.
                         * WILL HAVE TO TEST IT THOROUGHLY.*/
                        else{
                            messageToClient = "Not requestor, cannot cancel meeting";
                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), messageToClient);
                            UdpSend.sendMessage(serverCancelMessage.serialize(), socketAddress);
                        }
                    }
                    else{
                        messageToClient = "Meeting does not exist";
                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), messageToClient);
                        UdpSend.sendMessage(serverCancelMessage.serialize(), socketAddress);
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

    public class ServerCommand implements Runnable {

        String message = "";
        //String messageToServer = "Set Server Value";

        public ServerCommand() {

        }


        @Override
        public void run() {

            while(true) {
                Scanner scanner = new Scanner(System.in);
                message = scanner.nextLine();

                //RoomChangeMessage roomChangeMessage = new RoomChangeMessage();


                String[] commandMessage = message.split("_");


                if (commandMessage[0].equals("RoomChange")) {
                    System.out.println("Server Command");

                    RoomChangeMessage roomChangeMessage = new RoomChangeMessage();
                    roomChangeMessage.deserialize(message);
                    String meetingNumberRC = Integer.toString(roomChangeMessage.getMeetingNumber());
                    int newRoomNumber = roomChangeMessage.getNewRoomNumber() - 1;

                    if (newRoomNumber != 0 && newRoomNumber != 1) {
                        System.out.println("Choose a room number of 1 or 2");
                        //break;
                    }


                    //If meeting number exists
                    if (meetingMap.containsKey(meetingNumberRC)) {
                        System.out.println("Has meeting number");
                        System.out.println(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()));
                        //If the new room number found at the same time as the meeting number is false (FREE)
                        if (scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber] == false) {
                            //Make the meeting's room number to the new one
                            meetingMap.get(meetingNumberRC).setRoomNumber(newRoomNumber);
                            scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber] = true;
                            System.out.println("We are changing rooms!");
                        } else if (scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber] == true) {
                            System.out.println("They are already in that room");
                        }

                    } else {
                        System.out.println("Meeting number does not exist");
                    }
                }
                else {
                    //messageToServer = "Not a room change command";
                }

            }
        }

    }

    public String getCommandMessage(){
        return null;
    }

    private void saveServer(){

        FileReaderWriter.WriteFile("server", getServerState(), false);

    }

    private String getServerState(){
        String result = "";

        for(Map.Entry<String, Boolean[]> entry :  scheduleMap.entrySet()){
            result += entry.getKey() + "!" + entry.getValue()[0] + "!" + entry.getValue()[1] + ";";
        }

        result += "_";

        for(Map.Entry<String, Meeting> entry :  meetingMap.entrySet()){
            result += entry.getValue().serialize() + ";";
        }

        return result;
    }

    private void loadServer(){

        ArrayList<String> fileString = FileReaderWriter.ReadFile("server");

        String message = "";

        for(int i = 0; i < fileString.size(); i++){
            message += fileString.get(i);
        }

        String[] subMessage = message.split("_");

        String[] scheduleMapMessages = subMessage[0].split(";");
        String[] meetingMapString = subMessage[1].split(";");

        for(int i = 0; i < scheduleMapMessages.length ; i++){
            String[] sMMSplit = scheduleMapMessages[i].split("!");

            Boolean[] availability = {Boolean.parseBoolean(sMMSplit[1]), Boolean.parseBoolean(sMMSplit[2])};

            scheduleMap.put(sMMSplit[0], availability);
        }

        for(int i = 0 ; i < meetingMapString.length ; i++){

            Meeting meeting = new Meeting(meetingMapString[i]);

            meetingMap.put(Integer.toString(meeting.getId()), meeting);

        }

    }

}
