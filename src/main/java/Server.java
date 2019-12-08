import Tools.CalendarUtil;
import Tools.FileReaderWriter;
import Tools.UdpSend;
import requests.*;

import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;


public class Server implements Runnable{

    private HashMap<String, Boolean[]> scheduleMap;     //String Date and Time, Boolean Array of size 2: True = Booked, False = Not Booked.
    private HashMap<String, Meeting> meetingMap;        //String MeetingNumber, Meeting Class
    private HashMap<String, InetSocketAddress> clientAddressMap;         //String ClientName, InetSocketAddress client socket address

    private DatagramSocket serverSocket;

    public Server (){
        this.scheduleMap = new HashMap<>();
        this.meetingMap = new HashMap<>();
        this.clientAddressMap = new HashMap<>();

        try {
            this.serverSocket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 9997));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

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

            while(!answer.equals("y") && !answer.equals("n")){

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

        ServerSave serverSave = new ServerSave();
        Thread saveThread = new Thread(serverSave);
        saveThread.start();

    	try {
			System.out.println("Server Address: " + InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
        /**The port address is chosen randomly*/
        try {

            /**Messages here and sends to client*/

            ServerCommand serverCommand = new ServerCommand();
            Thread threadServerCommand = new Thread(serverCommand);
            threadServerCommand.start();

            while(true){
                byte[] buffer = new byte[100];
            	System.out.println("-------------- SERVER STARTED TO LISTEN --------------");
                DatagramPacket DpReceive = new DatagramPacket(buffer, buffer.length);   //Create Datapacket to receive the data
                serverSocket.receive(DpReceive);        //Receive Data in Buffer

                //System.out.println(DpReceive.getData());
                //System.out.println(DpReceive.getAddress());
                String message = new String(DpReceive.getData());


                //System.out.println("DpReceive getAddress" + DpReceive.getAddress());
                //System.out.println("DpReceive socket address" + DpReceive.getSocketAddress());

                System.out.println("Client says: " + message);



                /**NEED TO ADD IN TIMEOUT OPTIONS TO RESEND THE MESSAGE. HAVE YET TO
                 * COMPLETE THIS PORTION OF THE CODE
                 *
                 * Add in Thread and feed in the message*/
                int port = DpReceive.getPort();
                System.out.println("Port: " + port);
                /**Creating a new thread of each new request*/

                //Create server command thread



                //If we type "RoomChange_MT#_Room#" ex. "RoomChange_3_2"
                //Set the message to that
//                String[] s = serverCommand.getCommandMessage().split("_");
//                if(s[0].equals("RoomChange")){
//                    System.out.println("In room change if statement");
//
//                    //threadServerHandle.start();
//                }
                //String serverMessage = serverCommand.getCommandMessage();
                //serverHandle = new ServerHandle(serverMessage,port,DpReceive.getSocketAddress());

                //threadServerHandle.start();
                //System.out.println("DpReceive Socket Address: " + DpReceive.getSocketAddress());
                ServerHandle serverHandle = new ServerHandle(message, port, DpReceive.getSocketAddress());

                Thread threadServerHandle = new Thread(serverHandle);
                //= new ServerHandle(message, port);
                //threadServerHandle = new Thread(serverHandle);
                threadServerHandle.start();
                threadServerHandle.join();

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
        } catch (InterruptedException e) {
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
            String[] receivedMessage = message.split("\\$");
            System.out.println("The received message: " + message);


            //Gets the request type to treat the message.
            System.out.println("receivedMessage: " + receivedMessage[0]);
            //System.out.println("receivedMessage Value of: " + RequestType.valueOf(receivedMessage[0]));
//            if(receivedMessage[0] == "9" || receivedMessage[0].equals("9")){
//                receivedMessage[0] = "Request";
//            }
            int messageType = Integer.parseInt(receivedMessage[0]);
            System.out.println("Message type: " + messageType);
            RequestType receivedRequestType = RequestType.values()[messageType];
//            if(receivedMessage[0] == "10" || receivedMessage[0].equals("10")){
//                System.out.println("Got Accept");
//                receivedMessage[0] = "Accept";
//            }
            //RequestType receivedRequestType = RequestType.valueOf(receivedMessage[0]);


            FileReaderWriter file = new FileReaderWriter();
            String currentDir = System.getProperty("user.dir");
            String filePath = currentDir + "log.txt";
            Calendar calendar = Calendar.getInstance();
            String currentTime = "Server[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                    + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";

            /**Cases to how to treat each of the requestTypes.*/
            switch(receivedRequestType){
                case Register:
                    RegisterMessage registerMessage = new RegisterMessage();
                    registerMessage.deserialize(message);
                    clientAddressMap.put(registerMessage.getClientName(), registerMessage.getClientSocketAddress());
                    FileReaderWriter.WriteFile("log", currentTime + "Registered " + registerMessage.getClientName() + "\n", true);

                    break;
                case Request:
                    RequestMessage requestMessage = new RequestMessage();
                    requestMessage.deserialize(message);

                    String time = CalendarUtil.calendarToString(requestMessage.getCalendar());

                    String name = "";
                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                name = entry.getKey();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    }

                    //Accepted participants should always initialize as 1 for organizer
                    Meeting meeting = new Meeting(requestMessage, 1, new HashMap<Integer, Boolean>(), 0, name, 1);

                    //If this meeting does not exist yet
                    if(!scheduleMap.containsKey(time)){


                        //Make first room taken
                        synchronized(scheduleMap) {
                            scheduleMap.put(time, new Boolean[]{true, false});
                        }

                        messageToClient = "Room 1 is available";

                        System.out.println("In server: " + messageToClient);

                        meeting.setRoomNumber(1);
                        //Set all participants accepted value to false (none have accepted in this stage)
                        meeting.setAcceptedMap();
                        //Add meeting to hashmap that lists all existing meetings
                        synchronized(meetingMap) {
                            meetingMap.put(Integer.toString(meeting.getId()), meeting);
                        }

                        InviteMessage inviteMessage = new InviteMessage();
                        inviteMessage.setMeetingNumber(meeting.getId());
                        inviteMessage.setCalendar(meeting.getRequestMessage().getCalendar());
                        inviteMessage.setTopic(meeting.getRequestMessage().getTopic());
                        inviteMessage.setRequester(meeting.getOrganizer().trim());


                        for(String s: meeting.getRequestMessage().getParticipants()){
                            socketAddress = clientAddressMap.get(s);

                            //If you add participant in list that does not have a running client
                            if(socketAddress == null){
                                continue;
                            }
                            UdpSend.sendMessage(inviteMessage.serialize(), serverSocket, socketAddress);

                            FileReaderWriter.WriteFile("log", currentTime + "Invited " + s + "\n", true);
                        }


                        /**Writes the message in the log file.*/
                        file.WriteFile(filePath, message, true);
                    }
                    else if(scheduleMap.containsKey(time)){
                        //If first room not taken
                        if(!scheduleMap.get(time)[0]) {
                            //Set room number to 1
                            meeting.setRoomNumber(1);
                            //Set all participants accepted value to false (none have accepted in this stage)
                            meeting.setAcceptedMap();
                            synchronized (meetingMap) {
                                meetingMap.put(Integer.toString(meeting.getId()), meeting);
                            }
                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[0] = true;

                            synchronized(scheduleMap) {
                                scheduleMap.put(time, roomArray);
                            }
                            messageToClient = "Room 1 is available";
                            System.out.println("In server: " + messageToClient);

                            InviteMessage inviteMessage = new InviteMessage();
                            inviteMessage.setMeetingNumber(meeting.getId());
                            inviteMessage.setCalendar(meeting.getRequestMessage().getCalendar());
                            inviteMessage.setTopic(meeting.getRequestMessage().getTopic().trim());
                            inviteMessage.setRequester(meeting.getOrganizer().trim());


                            for(String s: meeting.getRequestMessage().getParticipants()){
                                socketAddress = clientAddressMap.get(s);

                                //If you add participant in list that does not have a running client
                                if(socketAddress == null){
                                    continue;
                                }
                                UdpSend.sendMessage(inviteMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Invited " + s + "\n", true);

                            }
                            //Create meeting
                            //Add meeting to meetingMap
                        }
                        else if(!scheduleMap.get(time)[1]){
                            //Set room number to 2
                            meeting.setRoomNumber(2);
                            //Set all participants accepted value to false (none have accepted in this stage)
                            meeting.setAcceptedMap();

                            synchronized(meetingMap) {
                                meetingMap.put(Integer.toString(meeting.getId()), meeting);
                            }
                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[1] = true;
                            synchronized(scheduleMap) {
                                scheduleMap.put(time, roomArray);
                            }
                            messageToClient = "Room 2 is available";
                            System.out.println("In server: " + messageToClient);

                            InviteMessage inviteMessage = new InviteMessage();
                            inviteMessage.setMeetingNumber(meeting.getId());
                            inviteMessage.setCalendar(meeting.getRequestMessage().getCalendar());
                            inviteMessage.setTopic(meeting.getRequestMessage().getTopic().trim());
                            inviteMessage.setRequester(meeting.getOrganizer().trim());

                            for(String s: meeting.getRequestMessage().getParticipants()){
                                socketAddress = clientAddressMap.get(s);

                                //If you add participant in list that does not have a running client
                                if(socketAddress == null){
                                    continue;
                                }
                                UdpSend.sendMessage(inviteMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Invited " + s + "\n", true);

                            }

                        }
                        else{
                            DeniedMessage deniedMessage = new DeniedMessage();
                            deniedMessage.setRequestNumber(meeting.getRequestMessage().getRequestNumber());
                            deniedMessage.setUnavailable("Unavailable");

                            UdpSend.sendMessage(deniedMessage.serialize(), serverSocket, socketAddress);
                            FileReaderWriter.WriteFile("log", currentTime + deniedMessage.getUnavailable() + " " + deniedMessage.getRequestNumber() + "\n", true);

                            messageToClient = "Room is not available at this time. Choose another time";
                            System.out.println("In server: " + messageToClient);
                            break;
                        }
                    }
                    else{
                        DeniedMessage deniedMessage = new DeniedMessage();
                        deniedMessage.setRequestNumber(meeting.getRequestMessage().getRequestNumber());
                        deniedMessage.setUnavailable("Unavailable");

                        UdpSend.sendMessage(deniedMessage.serialize(), serverSocket, socketAddress);
                        FileReaderWriter.WriteFile("log", currentTime + deniedMessage.getUnavailable() + " " + deniedMessage.getRequestNumber() + "\n", true);


                        messageToClient = "Room is not available at this time. Choose another time";
                        System.out.println("In server: " + messageToClient);
                        break;
                    }
                    /**Put the message inside the requestMap Hashmap.
                     * Key is the IP, and stores the received message.*/

                    for(String typeKey : scheduleMap.keySet()){
                        String key = typeKey.toString();
                        String value = Arrays.toString(scheduleMap.get(typeKey));
                        System.out.println("Hashmap for scheduled");
                        System.out.println(key + ": " + value);
                    }


                    break;
                case Accept:
                    System.out.println("Accept from Client");
                    AcceptMessage acceptMessage = new AcceptMessage();
                    acceptMessage.deserialize(message);
                    String meetingNumberAccept = Integer.toString(acceptMessage.getMeetingNumber());

                    Meeting acceptMeeting = null;
                    boolean foundMatchAccept = false;
                    for(String typeKey : meetingMap.keySet()){
                        String key = typeKey.toString();
                        String value = meetingMap.get(typeKey).getRequestMessage().getParticipants().toString();
                        System.out.println("Hashmap for meetings");
                        System.out.println(key + ": " + value);
                    }

                    String participantName = "";

                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                participantName = entry.getKey();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    }
                    //System.out.println("Meeting number is: " + meetingNumberAccept);

                    //Go through all the participants in the existing meetings
                    if(meetingMap.containsKey(meetingNumberAccept)) {
                        //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                        for (int i = 0; i < meetingMap.get(meetingNumberAccept).getRequestMessage().getParticipants().size(); i++) {
                            if (meetingMap.get(meetingNumberAccept).getRequestMessage().getParticipants().get(i).equals(participantName)) {
                                //System.out.println("In here: " + participantName);
                                acceptMeeting = meetingMap.get(meetingNumberAccept);
                                foundMatchAccept = true;
                            }
                        }
                    }

                    if(!foundMatchAccept){
                        messageToClient = "You are not in a scheduled meeting";
                        break;
                    }


                    //Check if client is in the meeting AND if they already accepted the meeting
                    //System.out.println(participantName + " This participant says: " + acceptMeeting.getAcceptedMap().get(participantName));
                    if(acceptMeeting.getAcceptedMap().containsKey(participantName) && !acceptMeeting.getAcceptedMap().get(participantName)){
                        synchronized(acceptMeeting) {
                            //Increment accepted count
                            acceptMeeting.incrementAcceptedParticipants();
                            acceptMeeting.incrementAnsweredNumber();
                            //Make accepted boolean true
                            //System.out.println("You are now true: " + participantName);
                            acceptMeeting.getAcceptedMap().replace(participantName, true);
                        }
                        System.out.println("You have been added to the scheduled meeting");
                        messageToClient = "You have been added to the scheduled meeting";
                        //UdpSend.sendMessage(acceptMessage.serialize(), serverSocket, socketAddress);
                        FileReaderWriter.WriteFile("log", currentTime + "Accepted " + participantName + "\n", true);

                    }

                    //If all participants answer, decide if Scheduled or Not Scheduled
                    if(acceptMeeting.getAnsweredNumber() >= acceptMeeting.getRequestMessage().getParticipants().size()+1){  //+1 because organizer should not be in list
                        //System.out.println("Answered number:" + acceptMeeting.getAnsweredNumber());
                        //System.out.println("Size: " + acceptMeeting.getRequestMessage().getParticipants().size());


                        //If the accepted numbers >= minimum
                        if(acceptMeeting.getAcceptedParticipants() >= acceptMeeting.getRequestMessage().getMinimum()){
                            //Send confirm messages to all participants
                            ConfirmMessage confirmMessage = new ConfirmMessage();
                            confirmMessage.setMeetingNumber(acceptMeeting.getId());
                            confirmMessage.setRoomNumber(acceptMeeting.getRoomNumber());

                            for(String s: acceptMeeting.getRequestMessage().getParticipants()){
                                socketAddress = clientAddressMap.get(s);

                                //If you add participant in list that does not have a running client
                                if(socketAddress == null){
                                    continue;
                                }
                                System.out.println("Confirm " + confirmMessage.serialize());
                                UdpSend.sendMessage(confirmMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Confirm message sent to " + s + "\n", true);
                            }

                            //Send to organizer of Scheduled meeting
                            ScheduledMessage scheduledMessage = new ScheduledMessage();
                            scheduledMessage.setRequestNumber(acceptMeeting.getRequestMessage().getRequestNumber());
                            scheduledMessage.setMeetingNumber(acceptMeeting.getId());
                            scheduledMessage.setRoomNumber(acceptMeeting.getRoomNumber());
                            String person = "";
                            List<String> listAccepted = new ArrayList<>();
                            for (Map.Entry<String, Boolean> entry : acceptMeeting.getAcceptedMap().entrySet()) {
                                Boolean value = entry.getValue();
                                if (value == true && entry.getValue().equals(value)) {
                                    person = entry.getKey();

                                    listAccepted.add(person);

                                }
                            }
                            String[] arrayAccepted = new String[listAccepted.size()];
                            for (int i = 0; i < listAccepted.size(); i++) {
                                arrayAccepted[i] = listAccepted.get(i);
                            }
                            scheduledMessage.setListOfConfirmedParticipants(arrayAccepted);
                            for (String s : clientAddressMap.keySet()) {
                                if (acceptMeeting.getOrganizer().equals(s)){
                                    socketAddress = clientAddressMap.get(s);
                                    UdpSend.sendMessage(scheduledMessage.serialize(), serverSocket, socketAddress);
                                }
                            }


                        }
                        //If accepted numbers < minimum
                        else{
                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage();
                            serverCancelMessage.setMeetingNumber(acceptMeeting.getId());
                            serverCancelMessage.setReason("Number lower than minimum");
                            String person = "";

                            //Find the name if they accepted (true)
                            for (Map.Entry<String, Boolean> entry : acceptMeeting.getAcceptedMap().entrySet()) {
                                Boolean value = entry.getValue();
                                if (value == true && entry.getValue().equals(value)) {
                                    person = entry.getKey();

                                    socketAddress = clientAddressMap.get(person);

                                    //If you add participant in list that does not have a running client
                                    if (socketAddress == null) {
                                        continue;
                                    }
                                    UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);
                                    FileReaderWriter.WriteFile("log", currentTime + "Cancel message sent to " + person + "\n", true);
                                }
                            }

                            //Remove that meeting from the meetingMap
                            synchronized (meetingMap) {
                                meetingMap.remove(Integer.toString(acceptMeeting.getId()));
                            }

                            //Send to organizer of Not scheduled meeting
                            NotScheduledMessage notScheduledMessage = new NotScheduledMessage();
                            notScheduledMessage.setRequestNumber(acceptMeeting.getRequestMessage().getRequestNumber());
                            notScheduledMessage.setCalendar(acceptMeeting.getRequestMessage().getCalendar());
                            notScheduledMessage.setMinimum(acceptMeeting.getRequestMessage().getMinimum());

                            String person2 = "";
                            List<String> listAccepted = new ArrayList<>();
                            for (Map.Entry<String, Boolean> entry : acceptMeeting.getAcceptedMap().entrySet()) {
                                Boolean value = entry.getValue();
                                if (value == true && entry.getValue().equals(value)) {
                                    person2 = entry.getKey();

                                    listAccepted.add(person2);

                                }
                            }

                            notScheduledMessage.setParticipants(listAccepted);
                            notScheduledMessage.setTopic(acceptMeeting.getRequestMessage().getTopic());
                            for (String s : clientAddressMap.keySet()) {
                                if (acceptMeeting.getOrganizer().equals(s)){
                                    socketAddress = clientAddressMap.get(s);
                                    UdpSend.sendMessage(notScheduledMessage.serialize(), serverSocket, socketAddress);
                                }
                            }


                        }
                    }



                    break;
                case Reject:
                    RejectMessage rejectMessage = new RejectMessage();
                    rejectMessage.deserialize(message);
                    String meetingNumberReject = Integer.toString(rejectMessage.getMeetingNumber());

                    Meeting rejectMeeting = null;
                    boolean foundMatchReject = false;

                    String participantName2 = "";

                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                participantName2 = entry.getKey();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    }


                        //Go through all the participants in the existing meetings
                        for (int j = 0; j < meetingMap.size(); j++) {
                            //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                            for(int i = 0; i < meetingMap.get(meetingNumberReject).getRequestMessage().getParticipants().size(); i++) {
                                if (meetingMap.containsKey(meetingNumberReject) && meetingMap.get(meetingNumberReject).getRequestMessage().getParticipants().get(i).equals(participantName2)) {
                                    rejectMeeting = meetingMap.get(meetingNumberReject);
                                    foundMatchReject = true;
                                }
                            }
                        }


                    if(!foundMatchReject){
                        messageToClient = "You are not in a scheduled meeting";
                        break;
                    }

                    //Check if client is in the meeting AND if they already accepted the meeting
                    if(rejectMeeting.getAcceptedMap().containsKey(participantName2) && !rejectMeeting.getAcceptedMap().get(participantName2)){
                        messageToClient = "You have rejected the meeting";
                        rejectMeeting.incrementAnsweredNumber();
                        //UdpSend.sendMessage(rejectMessage.serialize(), serverSocket, socketAddress);
                        FileReaderWriter.WriteFile("log", currentTime + "Rejected " + participantName2 + "\n", true);

                    }
                    else{
                        //If client has already accepted, they cannot Reject
                        messageToClient = "You cannot send this message";
                    }

                    //If all participants answer, decide if Scheduled or Not Scheduled
                    if(rejectMeeting.getAnsweredNumber() >= rejectMeeting.getRequestMessage().getParticipants().size() + 1){    //+1 because organizer should not be in list

                        //If the accepted numbers >= minimum
                        if(rejectMeeting.getAcceptedParticipants() >= rejectMeeting.getRequestMessage().getMinimum()) {
                            //Send confirm messages to all participants
                            ConfirmMessage confirmMessage = new ConfirmMessage();
                            confirmMessage.setMeetingNumber(rejectMeeting.getId());
                            confirmMessage.setRoomNumber(rejectMeeting.getRoomNumber());

                            for (String s : rejectMeeting.getRequestMessage().getParticipants()) {
                                socketAddress = clientAddressMap.get(s);

                                //If you add participant in list that does not have a running client
                                if (socketAddress == null) {
                                    continue;
                                }
                                System.out.println("Confirm " + confirmMessage.serialize());
                                UdpSend.sendMessage(confirmMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Confirm message sent to " + s + "\n", true);
                            }
                            //Send to organizer of Scheduled meeting
                            ScheduledMessage scheduledMessage = new ScheduledMessage();
                            scheduledMessage.setRequestNumber(rejectMeeting.getRequestMessage().getRequestNumber());
                            scheduledMessage.setMeetingNumber(rejectMeeting.getId());
                            scheduledMessage.setRoomNumber(rejectMeeting.getRoomNumber());
                            String person = "";
                            List<String> listAccepted = new ArrayList<>();
                            for (Map.Entry<String, Boolean> entry : rejectMeeting.getAcceptedMap().entrySet()) {
                                Boolean value = entry.getValue();
                                if (value == true && entry.getValue().equals(value)) {
                                    person = entry.getKey();

                                    listAccepted.add(person);

                                }
                            }
                            String[] arrayAccepted = new String[listAccepted.size()];
                            for (int i = 0; i < listAccepted.size(); i++) {
                                arrayAccepted[i] = listAccepted.get(i);
                            }
                            scheduledMessage.setListOfConfirmedParticipants(arrayAccepted);
                            for (String s : clientAddressMap.keySet()) {
                                if (rejectMeeting.getOrganizer().equals(s)){
                                    socketAddress = clientAddressMap.get(s);
                                    UdpSend.sendMessage(scheduledMessage.serialize(), serverSocket, socketAddress);
                                }
                            }

                        }
                        //If accepted numbers < minimum
                        else{
                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage();
                            serverCancelMessage.setMeetingNumber(rejectMeeting.getId());
                            serverCancelMessage.setReason("Number lower than minimum");

                            String person = "";

                            //Find the name if they accepted (true)
                            for (Map.Entry<String, Boolean> entry : rejectMeeting.getAcceptedMap().entrySet()) {
                                Boolean value = entry.getValue();
                                if (value == true && entry.getValue().equals(value)) {
                                    person = entry.getKey();

                                    socketAddress = clientAddressMap.get(person);

                                    //If you add participant in list that does not have a running client
                                    if (socketAddress == null) {
                                        continue;
                                    }
                                    UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);
                                    FileReaderWriter.WriteFile("log", currentTime + "Cancel message sent to " + person + "\n", true);
                                }
                            }

                            //Send to organizer of Not scheduled meeting
                            NotScheduledMessage notScheduledMessage = new NotScheduledMessage();
                            notScheduledMessage.setRequestNumber(rejectMeeting.getRequestMessage().getRequestNumber());
                            notScheduledMessage.setCalendar(rejectMeeting.getRequestMessage().getCalendar());
                            notScheduledMessage.setMinimum(rejectMeeting.getRequestMessage().getMinimum());

                            String person2 = "";
                            List<String> listAccepted = new ArrayList<>();
                            for (Map.Entry<String, Boolean> entry : rejectMeeting.getAcceptedMap().entrySet()) {
                                Boolean value = entry.getValue();
                                if (value == true && entry.getValue().equals(value)) {
                                    person2 = entry.getKey();

                                    listAccepted.add(person2);

                                }
                            }

                            notScheduledMessage.setParticipants(listAccepted);
                            notScheduledMessage.setTopic(rejectMeeting.getRequestMessage().getTopic());
                            for (String s : clientAddressMap.keySet()) {
                                if (rejectMeeting.getOrganizer().equals(s)){
                                    socketAddress = clientAddressMap.get(s);
                                    UdpSend.sendMessage(notScheduledMessage.serialize(), serverSocket, socketAddress);
                                }
                            }

                        }
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
                        if (theMeeting.getAcceptedMap().get(port)){
                            messageToClient = "You have already accepted the meeting";
                        }else{
                            //theMeeting.getAcceptedMap().put(port, true);
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


//                    String mNumber = receivedMessage[1];
//                    System.out.println("Meeting number " + mNumber);
//                    System.out.println("Meeting Map: " + meetingMap);
//                    if(meetingMap.containsKey(mNumber)){
//                        Meeting theMeeting = meetingMap.get(mNumber);
//                        System.out.println(theMeeting.getRoomNumber());
//                        if(port == theMeeting.getOrganizer()){
//                            String date = CalendarUtil.calendarToString(theMeeting.getRequestMessage().getCalendar());
//                            System.out.println(date);
//                            int roomNumber = theMeeting.getRoomNumber();
//                            Boolean[] rooms = scheduleMap.get(date);
//                            System.out.println("Boolean Rooms: " + rooms);
//
//                            /**NEED TO SEND MESSAGE TO ALL INVITED PARTICIPANTS. SENDING THE SAME
//                             * MESSAGE TO DIFFERENT PORT NUMBER.
//                             *
//                             * Create array, store all port number getAcceptedMap.
//                             * Loop through the array
//                             *      Create ServerCancelMessage object to send message
//                             *      Call the serialize function to send.
//                             * */
//
//                            List<String> participants = new ArrayList<>();
//
//                            if(roomNumber == 1){
//                                /**CHECK THE INDEX WELL. MIGHT HAVE TO SUBTRACT 1*/
//
//                                Set<String> portNumber = theMeeting.getAcceptedMap().keySet();
//                                for (String port : portNumber){
//                                    participants.add(port);
//                                }
//                                for (int i = 0; i < participants.size(); i++){
//                                    System.out.println(participants.get(i));
//                                }
//
//                                /**Loop through the save ports and send message to them*/
//
//                                for (int i = 0; i < participants.size(); i++){
//                                    //ServerCancelMessage serverCancelMessage = new ServerCancelMessage(participants.get(i), "ServerCancel_Requestor_Cancelled_Meeting");
//                                    /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.*/
//                                }
//
//                                rooms[roomNumber] = false;
//                                scheduleMap.replace(date, rooms);
//                                meetingMap.remove(mNumber);
//
//                            }
//                            else if(roomNumber == 2){
//                                /**CHECK THE INDEX WELL. MIGHT HAVE TO SUBTRACT 1*/
//
//                                Set<String> portNumber = theMeeting.getAcceptedMap().keySet();
//                                for (String port : portNumber){
//                                    participants.add(port);
//                                }
//                                for (int i = 0; i < participants.size(); i++){
//                                    System.out.println(participants.get(i));
//                                }
//
//                                /**Loop through the save ports and send message to them*/
//
//                                for (int i = 0; i < participants.size(); i++){
//                                    //ServerCancelMessage serverCancelMessage = new ServerCancelMessage(participants.get(i), "ServerCancel_Requestor_Cancelled_Meeting");
//                                    /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.*/
//                                }
//
//                                rooms[roomNumber] = false;
//                                scheduleMap.replace(date, rooms);
//                                meetingMap.remove(mNumber);
//
//                            }
//                            System.out.println("meetingMap: " + meetingMap);
//                            System.out.println("scheduleMap" + scheduleMap);
//
//
//                        }
//                        else{
//                            messageToClient = "Not requestor, cannot cancel meeting";
//                        }
//                    }
//                    else{
//                        messageToClient = "Meeting does not exist";
//                    }
//
//                    break;
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
                        if (!scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber]) {
                            //Make the meeting's room number to the new one
                            meetingMap.get(meetingNumberRC).setRoomNumber(newRoomNumber);
                            scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber] = true;
                            System.out.println("We are changing rooms!");
                        } else if (scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber]) {
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

    public String getCommandMessage() {
        return null;
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

        if(subMessage.length > 0 && !subMessage[0].isEmpty()){
            String[] scheduleMapMessages = subMessage[0].split(";");

            for(int i = 0; i < scheduleMapMessages.length ; i++){
                String[] sMMSplit = scheduleMapMessages[i].split("!");

                Boolean[] availability = {Boolean.parseBoolean(sMMSplit[1]), Boolean.parseBoolean(sMMSplit[2])};

                scheduleMap.put(sMMSplit[0], availability);
            }

        }

        if(subMessage.length > 0 && !subMessage[1].isEmpty()){
            String[] meetingMapString = subMessage[1].split(";");

            for(int i = 0 ; i < meetingMapString.length ; i++) {

                Meeting meeting = new Meeting(meetingMapString[i]);

                meetingMap.put(Integer.toString(meeting.getId()), meeting);

            }
        }
    }

    public class ServerSave implements Runnable {
        @Override
        public void run(){

            while(true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }

                FileReaderWriter.WriteFile("server", getServerState(), false);

            }

        }
    }

}
