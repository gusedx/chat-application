package comp90015.project1.gustavo.server;

import java.net.*;
import java.io.*;

import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import comp90015.project1.gustavo.client.Debugger;

import org.json.simple.parser.*;
import org.json.simple.*;

public class TCPServer
{
	static String guestId;
	static List clientList = new ArrayList();
	static List roomList = new ArrayList();
	
	public static void main (String args[])
	{
		ChatRoom room = createNewRoom("MainHall");
		Socket clientSocket = null;
		Guest guest = null;
		
		try{
			int serverPort = 4444; //default server port
			
			if (args.length > 0)
			{
				if (args[0].equals("-p"))
				{
					if (args.length > 1)
					{
						serverPort = Integer.parseInt(args[1]);
					}
					else
					{
						System.out.println("Usage: java -jar chatserver.jar [-p port]");
						System.exit(0);
					}
				}
				else
				{
					System.out.println("Usage: java -jar chatserver.jar [-p port]");
					System.exit(0);
				}
			}

			ServerSocket listenSocket = new ServerSocket(serverPort);
			GenerateUniqueId uniqueId = new GenerateUniqueId();
			
			while (true)
			{
				System.out.println("Server listening for a connection on port " + serverPort);

				clientSocket = listenSocket.accept();

				Integer guestNumber = uniqueId.generateUniqueID();
				
				guestId = "guest" + guestNumber;
				
				System.out.println("Received connection from " + guestId);
				guest = new Guest(guestId, clientSocket);
				guest.guestNumber = guestNumber;
				
				sendNewClientId(guest, guest.guestId, "");
				clientList.add(guest);
					
				room.addGuestToChatRoom(guest); //add new guest to default chat room 
				
				//SEND ROOMCHANGE MESSAGE TO ALL CLIENTS IN THE MAINHALL:
				Guest guestInMainHall;
				Iterator<Guest> guestListIterator = room.getRoomGuestList().iterator();
				while (guestListIterator.hasNext())
				{
					guestInMainHall = guestListIterator.next();
					TCPServer.sendRoomChange(guestInMainHall.guestSocket, guestId, "", "MainHall");
				}			
				
				System.out.println("Sending MainHall room contents message to " + guest.guestId);
				sendRoomContentsMessage(clientSocket, room);
				System.out.println("Room contents message sent to " + guest.guestId);
				
				new Connection(guest);
				System.out.println("New connection thread started, will start listening loop again...");
			}
		}
		catch (EOFException e)
		{
			if (Debugger.isEnabled())
				System.out.println("Closing connection to " + guest.guestId);
			try {
				clientSocket.close();
				GenerateUniqueId.sortedPq.offer(guest.guestNumber);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			room.removeGuestFromChatRoom(guest); //this is needed so that the server does not later try to send messages to the client that is gone
			Thread.currentThread().interrupt();
		}
		catch (IOException e)
		{
			System.out.println("Listen socket:" + e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public static ChatRoom createNewRoom(String roomName)
	{
		ChatRoom room = new ChatRoom(roomName);
		roomList.add(room);
		return room;
	}
	
	public static void sendMessage(Socket aClientSocket, String message)
	{	
		try {
			OutputStreamWriter out = new OutputStreamWriter(aClientSocket.getOutputStream(), "UTF-8");
			out.write(message + "\n");
			out.flush();
			
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void sendNewClientId(Guest guest, String newId, String formerId)
	{
		String message = encodeJsonNewIdentityMessage(newId, formerId);
		if (Debugger.isEnabled())
			System.out.println("Encoded new identity message: " + message);
		sendMessage(guest.guestSocket, message);
	}
	
	public static void sendRoomContentsMessage(Socket aClientSocket, ChatRoom room)
	{
		String message = encodeJsonRoomContentsMessage(room.roomName, room.getRoomGuestIdList(), room.owner);
		sendMessage(aClientSocket, message);
	}
	
	public static void sendRoomList(Socket aClientSocket)
	{	
		String message = encodeJsonRoomListMessage();
		sendMessage(aClientSocket, message);
	}
	
	public static void sendRoomChange(Socket aClientSocket, String identity, String formerRoom, String newRoom)
	{	
		String message = encodeJsonRoomChangeMessage(identity, formerRoom, newRoom);
		sendMessage(aClientSocket, message);
	}
	
	public static String encodeJsonNewIdentityMessage(String identity, String formerId)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "newidentity");
		obj.put("former", formerId);
		obj.put("identity", identity);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
		String jsonText = out.toString();
		//System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonRoomChangeMessage(String identity, String formerRoomId, String newRoomId)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "roomchange");
		obj.put("identity", identity);
		obj.put("former", formerRoomId);
		obj.put("roomid", newRoomId);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
		String jsonText = out.toString();
		//System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonRoomContentsMessage(String roomId, List identities, String owner)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "roomcontents");
		obj.put("roomid", roomId);
		obj.put("identities", identities);
		obj.put("owner", owner);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println("Encoded room contents message: " + jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonRoomInformation(String roomId, int numberOfGuests)
	{
		JSONObject obj = new JSONObject();
		obj.put("roomid", roomId);
		obj.put("count", new Integer(numberOfGuests));
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
		String jsonText = out.toString();
		//System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonRoomListMessage()
	{
		JSONArray roomInformationList = new JSONArray();
		JSONObject obj = new JSONObject();
		ChatRoom room;
		
		for (int i = 0; i < roomList.size(); i++)
		{
			room = (ChatRoom) roomList.get(i);
			
			JSONObject jsonEncodedRoomInformation = new JSONObject();
			jsonEncodedRoomInformation.put("roomid",room.roomName);
			jsonEncodedRoomInformation.put("count", new Integer(room.getNumberOfGuests()));
			roomInformationList.add(jsonEncodedRoomInformation);
		}
		
		obj.put("type", "roomlist");
		obj.put("rooms", roomInformationList);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
		String jsonText = out.toString();
		//System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonEchoMessageToClient(String guestId, String message)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "message");
		obj.put("identity", guestId);
		obj.put("content", message);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
		String jsonText = out.toString();
		//System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static void echoMessage(String decodedMessage, Guest senderGuest)
	{
		String message = encodeJsonEchoMessageToClient(senderGuest.guestId, decodedMessage);
		System.out.println("message to echo: " + message);

		Guest receivingGuest;
		
		System.out.println("Relaying message to members of room " + senderGuest.memberRoom.roomName);
		Iterator<Guest> guestListIterator = senderGuest.memberRoom.getRoomGuestList().iterator();
		while (guestListIterator.hasNext())
		{
			receivingGuest = guestListIterator.next();
			System.out.println("Relaying message to " + receivingGuest.guestId);
			sendMessage(receivingGuest.guestSocket, message);
		}
	}
}

class Connection extends Thread
{
	DataInputStream in;
	Guest clientGuest;
	ChatRoom room;
	
	public Connection(Guest guest)
	{
		try
		{
			clientGuest = guest;
			
			if (Debugger.isEnabled())
				System.out.println("Starting new server thread...");
			this.start();
			if (Debugger.isEnabled())
				System.out.println("New server thread started.");
		}
		finally
		{

		}
	}
	
	public void run()
	{			
		try
		{
			if (Debugger.isEnabled())
				System.out.println("In run method...");

			String decodedMessage = null;
			
			System.out.println("Waiting for encoded JSON message...");
			
			BufferedReader in;
			in = new BufferedReader(new InputStreamReader(clientGuest.guestSocket.getInputStream(), "UTF-8"));
			String jsonString;
			while ((jsonString = in.readLine()) != null)
			{
				processJsonMessage(jsonString);
				Thread.sleep(3000);
			}
		}
		catch (EOFException e)
		{
			ChatRoom room;
			if (Debugger.isEnabled())
				System.out.println("Closing connection to " + clientGuest.guestId);
			try {
				clientGuest.guestSocket.close();
				GenerateUniqueId.sortedPq.offer(clientGuest.guestNumber);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			room = clientGuest.getRoomMembership();
			room.removeGuestFromChatRoom(clientGuest); //this is needed so that the server does not later try to send messages to the client that is gone
			Thread.currentThread().interrupt();
		}
		catch (InterruptedException e)
		{
			return;
		} 
		catch (IOException e) {
			ChatRoom room;
			if (Debugger.isEnabled())
				System.out.println("Closing connection to " + clientGuest.guestId);
			try {
				clientGuest.guestSocket.close();
				GenerateUniqueId.sortedPq.offer(clientGuest.guestNumber);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			room = clientGuest.getRoomMembership();
			room.removeGuestFromChatRoom(clientGuest); //this is needed so that the server does not later try to send messages to the client that is gone
			Thread.currentThread().interrupt();
		}
		finally
		{
			if (clientGuest.guestSocket != null)
			{
				room = clientGuest.getRoomMembership();
				if (room != null)
				{
					room.removeGuestFromChatRoom(clientGuest); //this is needed so that the server does not later try to send messages to the client that is gone	
				}
				
				if (Debugger.isEnabled())
					System.out.println("Closing connection to " + clientGuest.guestId);
				try {
					clientGuest.guestSocket.close();
					GenerateUniqueId.sortedPq.offer(clientGuest.guestNumber);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (Debugger.isEnabled())
				System.out.println("Thread Stopped.");
		}
	}
	
	public void processJsonMessage(String jsonString)
	{	
		String value = null;
		
		JSONParser parser = new JSONParser();
		ContainerFactory containerFactory = new ContainerFactory()
		{
			public List creatArrayContainer()
			{
				return new LinkedList();
			}

			public Map createObjectContainer()
			{
				return new LinkedHashMap();
			}

		};

		try
		{
			Map json = (Map)parser.parse(jsonString, containerFactory);
			Iterator iter = json.entrySet().iterator();

			String key = "";
			String type = json.getOrDefault("type", null).toString();
			
			switch(type)
			{
				case "message":
					key = "content";
					value = json.getOrDefault(key, null).toString();
					TCPServer.echoMessage(value, clientGuest);
					break;
				case "identitychange":
					key = "identity";
					value = json.getOrDefault(key, null).toString();
					System.out.println("New identity value requested from client: " + value);

					if ((isRequestedIdValid(value, 3, 16)) && (!isRequestedIdInUse(value)))
					{
						System.out.println("Identity change from " + clientGuest.guestId + " to " + value + " allowed.");

						//update ownership of room(s) owned by the client with the changed id:
						Iterator<ChatRoom> roomListIterator = TCPServer.roomList.iterator();
						while (roomListIterator.hasNext())
						{
							room = roomListIterator.next();
							
							if (room.owner.equals(clientGuest.guestId))
							{
								room.setRoomOwner(value);
							}
						}
						
						room = clientGuest.getRoomMembership();
						room.updateGuestId(clientGuest, value);
						
						//send the new id to all the clients:
						Iterator<Guest> clientListIterator = TCPServer.clientList.iterator();
						while (clientListIterator.hasNext())
						{
							TCPServer.sendNewClientId(clientListIterator.next(), value, clientGuest.guestId);
						}
						clientGuest.guestId = value;
					}
					else
					{
						System.out.println("Identity change from " + clientGuest.guestId + " to " + value + " NOT allowed.");
						//send the current id to the requesting client only:
						TCPServer.sendNewClientId(clientGuest, clientGuest.guestId, clientGuest.guestId);
					}

					break;
				case "createroom":
					key = "roomid";
					value = json.getOrDefault(key, null).toString();
					
					if ((isRequestedIdValid(value, 3, 32)) && (!isRoomNameInUse(value)))
					{
						System.out.println("Creating room " + value);
						room = TCPServer.createNewRoom(value);
						room.setRoomOwner(clientGuest.guestId);
					}
					else
					{
						System.out.println("Room name " + value + " is invalid or is already in use.");
					}
					
					TCPServer.sendRoomList(clientGuest.guestSocket);
					
					break;
				case "list":
					TCPServer.sendRoomList(clientGuest.guestSocket);
					break;
				case "join":
					key = "roomid";
					value = json.getOrDefault(key, null).toString();
										
					String formerRoomName = clientGuest.getRoomMembership().roomName;
					
					if (!isRoomNameInUse(value)) //move not allowed as room id is invalid or non existent:
					{
						TCPServer.sendRoomChange(clientGuest.guestSocket, clientGuest.guestId, formerRoomName, formerRoomName);
					}
					
					else
					{
						Calendar calendarCurrentTime = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
						room = getRoomByName(value);
										
						//if client is in the list of kicked out users for the room and current time is less than time when user can re-join the room:
						if ((room.kickedUsers.containsKey(clientGuest)) && (calendarCurrentTime.getTime().before(room.kickedUsers.get(clientGuest).getTime())))
						{
							//Change not allowed (kick out time not yet elapsed): send RoomChange message only to the requesting client:
							TCPServer.sendRoomChange(clientGuest.guestSocket, clientGuest.guestId, formerRoomName, formerRoomName);
						}
						else
						{
							//move is allowed:
							moveRooms(clientGuest, formerRoomName, value);
							if (room.kickedUsers.containsKey(clientGuest))
							{
								room.clearKickedUser(clientGuest);
							}
						}	
					}
					
					break;
				case "who":
					key = "roomid";
					value = json.getOrDefault(key, null).toString();
					if (isRoomNameInUse(value))
					{
						TCPServer.sendRoomContentsMessage(clientGuest.guestSocket, getRoomByName(value));
					}
					else
					{
						System.out.println("Room name " + value + " does not exist.");
					}
					break;
					
				case "delete":
					key = "roomid";
					value = json.getOrDefault(key, null).toString();
					Guest guest;
					List<Guest> roomGuestList;
					
					if (isRoomNameInUse(value))
					{
						room = getRoomByName(value);
						if (room.owner.equals(clientGuest.guestId))
						{
							//move all users of the room to the MainHall:
							roomGuestList = room.getRoomGuestList();
							Iterator<Guest> guestListIterator = roomGuestList.iterator();
							while (guestListIterator.hasNext())
							{
								guest = guestListIterator.next();
								room = getRoomByName(value);
								System.out.println("Moving guest " + guest.guestId);
								guestListIterator.remove();
								room.removeGuestFromChatRoom(guest);
								room = getRoomByName("MainHall");
								room.addGuestToChatRoom(guest);
							}
							//delete the room:
							room = getRoomByName(value);
							TCPServer.roomList.remove(room);
							room = null;
							System.gc();
						}
						else
						{
							System.out.println("cannot delete room " + room + " as " + clientGuest.guestId + " is not its owner. Owner is " + room.owner);
						}
					}
					TCPServer.sendRoomList(clientGuest.guestSocket);
					break;
					
				case "quit":
					String id = clientGuest.guestId;
					room = clientGuest.getRoomMembership();
					roomGuestList = room.getRoomGuestList();
					Iterator<Guest> guestListIterator = roomGuestList.iterator();
					while (guestListIterator.hasNext())
					{
						guest = guestListIterator.next();
						TCPServer.sendRoomChange(guest.guestSocket, id, room.roomName, "");
					}
					
					room.removeGuestFromChatRoom(clientGuest);
					
					try {
						if (Debugger.isEnabled())
							System.out.println("Closing connection to " + clientGuest.guestId);
						clientGuest.guestSocket.close();
						GenerateUniqueId.sortedPq.offer(clientGuest.guestNumber);
						
						Thread.currentThread().interrupt();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
					
				case "kick":
					key = "roomid";
					value = json.getOrDefault(key, null).toString();
					key = "identity";
					String identity = json.getOrDefault(key, null).toString();
					
					if (clientGuest.guestId.equals(getRoomByName(value).owner)) //if the client is the owner of the room:
					{
						//set the time when the user is able to re-join the room that it is being kicked out of:
						key = "time";
						String timeOffRoom = json.getOrDefault(key, null).toString();
						Calendar calendarFutureTime = Calendar.getInstance();
						System.out.println(calendarFutureTime.getTime());
						calendarFutureTime.add(Calendar.SECOND, Integer.parseInt(timeOffRoom));
						System.out.println(calendarFutureTime.getTime());
						getRoomByName(value).addKickedUser(getGuestByName(identity), calendarFutureTime);
						
						moveRooms(getGuestByName(identity), value, "MainHall");
					}
					break;
				default:
					System.out.println("Invalid message type " + type);
					break;
			}
		}
		catch (ParseException pe)
		{
			System.out.println("Parser Exception: " + pe);
		}
	}
	
	public void moveRooms(Guest targetGuest, String formerRoom, String newRoom)
	{
		Guest receivingGuest;
		ChatRoom[] roomArray = new ChatRoom[2]; //stores the former and new rooms
		
		if (isRoomNameInUse(newRoom))
		{			
			roomArray[0] = targetGuest.memberRoom; //former room
			roomArray[1] = getRoomByName(newRoom); //new room

			roomArray[0].removeGuestFromChatRoom(targetGuest);
			roomArray[1].addGuestToChatRoom(targetGuest);
			
			//send RoomChange message to all clients currently in the requesting client's current room and requesting client's requested room:
			for (int i = 0; i < roomArray.length; i++)
			{
				Iterator<Guest> guestListIterator = roomArray[i].getRoomGuestList().iterator();
				while (guestListIterator.hasNext())
				{
					receivingGuest = guestListIterator.next();
					TCPServer.sendRoomChange(receivingGuest.guestSocket, targetGuest.guestId, formerRoom, newRoom);
				}
			}
			
			if (newRoom.equals("MainHall"))
			{
				TCPServer.sendRoomContentsMessage(clientGuest.guestSocket, roomArray[1]);
				TCPServer.sendRoomList(clientGuest.guestSocket);
			}
		}
		else
		{
			//Change not allowed: send RoomChange message only to the requesting client:
			TCPServer.sendRoomChange(clientGuest.guestSocket, targetGuest.guestId, formerRoom, formerRoom);
		}
	}
	
	public boolean isRequestedIdValid(String id, int minLength, int maxLength)
	{
		if (!String.valueOf(id.charAt(0)).matches("^[a-zA-Z]*$"))
		{
			return false;
		}
		
		if (!id.matches("^[a-zA-Z0-9]*$"))
		{
			return false;
		}
		
		if (id.length() < minLength || id.length() > maxLength)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean isRequestedIdInUse(String id)
	{
		Guest guest;
		for (int i = 0; i < TCPServer.clientList.size(); i++)
		{
			guest = (Guest) TCPServer.clientList.get(i);
			System.out.println("Checking requested id " + id + " against id " + guest.guestId);
			if(id.equals(guest.guestId))
			{
				System.out.println("ID " + id + " already in use.");
				return true;
			}
		}
		return false;
	}
	public boolean isRoomNameInUse(String name)
	{
		for (int i = 0; i < TCPServer.roomList.size(); i++)
		{
			room = (ChatRoom) TCPServer.roomList.get(i);
			System.out.println("Checking requested room name " + name + " against room " + room.roomName);
			if(name.equals(room.roomName))
			{
				System.out.println("Room name " + name + " is in use.");
				return true;
			}
		}
		return false;
	}
	
	public ChatRoom getRoomByName(String roomName)
	{
		for (int i = 0; i < TCPServer.roomList.size(); i++)
		{
			room = (ChatRoom) TCPServer.roomList.get(i);
			if (room.roomName.equals(roomName))
			{
				return room;
			}
		}
		
		return null;
	}
	
	public Guest getGuestByName(String guestName)
	{
		Guest guest;
		for (int i = 0; i < TCPServer.clientList.size(); i++)
		{
			guest = (Guest) TCPServer.clientList.get(i);
			if (guest.guestId.equals(guestName))
			{
				return guest;
			}
		}
		
		return null;
	}
}