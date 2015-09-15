package comp90015.project1.gustavo.server;

import java.net.*;
import java.io.*;

import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.*;
import org.json.simple.*;

public class TCPServer
{

	static int guestCount = 0; //total number of guests connected to the server
	static String guestId;
	static List clientList = new ArrayList();
	static List roomList = new ArrayList();
	
	public static void main (String args[])
	{
		ChatRoom room = createNewRoom("MainHall");
		
		try{
			int serverPort = 4444; //default server port
			
			if (args.length > 0)
			{
				serverPort = Integer.parseInt(args[0]);
			}

			ServerSocket listenSocket = new ServerSocket(serverPort); 
			
			while (true)
			{
				System.out.println("Server listening for a connection on port " + serverPort); //TODO: DEBUG ONLY

				Socket clientSocket = listenSocket.accept();

				guestCount++;
				guestId = "guest" + guestCount;
				System.out.println("Received connection from " + guestId);
				Guest guest = new Guest(guestId, clientSocket);
				
				sendNewClientId(guest, guest.guestId, "");
				clientList.add(guest);
				//System.out.println("client list size: " + clientList.size());
				
				room.addGuestToChatRoom(guest); //add new guest to default chat room 
				//TODO: SEND ROOMCHANGE MESSAGE TO ALL CLIENTS IN THE MAINHALL
				System.out.println("Sending MainHall room contents message to " + guest.guestId);
				sendRoomContentsMessage(clientSocket, room);
				System.out.println("Room contents message sent to " + guest.guestId);
				
				//USING RUNNABLE:
				//Thread t = new Thread(new Connection(socket));
				//t.start();
				
				new Connection(guest);
				System.out.println("New connection thread started, will start listening loop again...");
			}
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
		DataOutputStream out;
		
		try {
			out = new DataOutputStream(aClientSocket.getOutputStream());
			out.writeUTF(message + "\n");
			out.flush();
			
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void sendNewClientId(Guest guest, String newId, String formerId)
	{
		String message = encodeJsonNewIdentityMessage(newId, formerId);
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
			//String jsonEncodedRoomInformation = encodeJsonRoomInformation(room.roomName, room.getNumberOfGuests());
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
		
//		ChatRoom room;
		Guest receivingGuest;
		
//		//echo the received message to each guest (other than the sender) that is a member of a room that the sender is a member of:
//		Iterator<ChatRoom> roomListIterator = senderGuest.getRoomMemberships().iterator();
//		while (roomListIterator.hasNext())
//		{
//			room = roomListIterator.next();
			System.out.println("Relaying message to members of room " + senderGuest.memberRoom.roomName);
			Iterator<Guest> guestListIterator = senderGuest.memberRoom.getRoomGuestList().iterator();
			while (guestListIterator.hasNext())
			{
				receivingGuest = guestListIterator.next();
				if (receivingGuest.guestId != senderGuest.guestId)
				{
					System.out.println("Relaying message to " + receivingGuest.guestId);
					sendMessage(receivingGuest.guestSocket, message);
				}
			}
//		}
	}
}

class Connection extends Thread
{
	DataInputStream in;
	DataOutputStream out;
	Guest clientGuest;
	ChatRoom room;
	
	public Connection(Guest guest)
	{
		try
		{
//			in = new DataInputStream(clientSocket.getInputStream());
//			out = new DataOutputStream(clientSocket.getOutputStream());
			clientGuest = guest;
			
			System.out.println("Starting new server thread...");
			this.start();
			System.out.println("New server thread started.");
		}
		finally
		{
//			try
//			{
//				System.out.println("Closing socket for " + aClientSocket.getInetAddress().toString());
//				clientSocket.close();
//			}
//			catch (IOException e)
//			{
//				
//			}
		}
	}
	
	public void run()
	{
		System.out.println("In run method...");
		while (true)
		{
			String decodedMessage = null;
			
			System.out.println("Waiting for encoded JSON message...");
			
			try {
				in = new DataInputStream(clientGuest.guestSocket.getInputStream());
				try
				{
					processJsonMessage(in.readUTF());
					Thread.sleep(3000);
				}
				catch (InterruptedException e)
				{
//					Thread.currentThread().interrupt();
					return;
				}
				
				System.out.println("Thread Stopped.");
				
				//if (msg.equals("end")) //TODO: THIS IS WHERE WE RECEIVE THE CLIENT DISCONNECT MESSAGE
				//	break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Decoded Message received: " + decodedMessage);
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
					
					moveRooms(clientGuest, formerRoomName, value);
//					Guest receivingGuest;
//					String formerRoomName = clientGuest.getRoomMembership().roomName; 
//					ChatRoom[] roomArray = new ChatRoom[2]; //stores the former and new rooms
//					
//					if (isRoomNameInUse(value))
//					{
//						roomArray[0] = clientGuest.memberRoom; //former room
//						roomArray[1] = getRoomByName(value); //new room
//						
//						roomArray[0].removeGuestFromChatRoom(clientGuest);
//						roomArray[1].addGuestToChatRoom(clientGuest);
//						
//						//send RoomChange message to all clients currently in the requesting client's current room and requesting client's requested room:
//						for (int i = 0; i < roomArray.length; i++)
//						{
//							Iterator<Guest> guestListIterator = roomArray[i].getRoomGuestList().iterator();
//							while (guestListIterator.hasNext())
//							{
//								receivingGuest = guestListIterator.next();
//								TCPServer.sendRoomChange(receivingGuest.guestSocket, clientGuest.guestId, formerRoomName, value);
//							}
//						}
//						
//						if (value.equals("MainHall"))
//						{
//							TCPServer.sendRoomContentsMessage(clientGuest.guestSocket, roomArray[1]);
//							TCPServer.sendRoomList(clientGuest.guestSocket);
//						}
//					}
//					else
//					{
//						//send RoomChange message only to the requesting client:
//						TCPServer.sendRoomChange(clientGuest.guestSocket, clientGuest.guestId, formerRoomName, formerRoomName);
//					}
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
						//TODO: SEND ROOMCONTENTSMESSAGE WITH EMPTY LIST AND EMPTY OWNER (HOW TO PASS ROOM NAME TO SEND FUNCTION?)
						//TCPServer.sendRoomContentsMessage(clientGuest.guestSocket, null);
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
					
//					if (guest.guestId.equals(id))
//					{
//						guestListIterator.remove();
						room.removeGuestFromChatRoom(clientGuest);
						
						try {
							System.out.println("Closing connection to " + clientGuest.guestId);
							clientGuest.guestSocket.close();
							
							Thread.currentThread().interrupt();
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//					}
					break;
					
				case "kick":
					key = "roomid";
					value = json.getOrDefault(key, null).toString();
					key = "identity";
					String identity = json.getOrDefault(key, null).toString();
					
					if (clientGuest.guestId.equals(getRoomByName(value).owner)) //if the client is the owner of the room:
					{
						moveRooms(getGuestByName(identity), value, "MainHall");
					}
					break;
				default:
					System.out.println("Invalid message type " + type);
					//TODO: ADD ERROR HANDLING
					break;
			}
			//value = json.getOrDefault(key, null).toString();

			//DEBUG ONLY:
			/*		    System.out.println("==iterate result==");
		    while(iter.hasNext()){
		      Map.Entry entry = (Map.Entry)iter.next();
		      System.out.println(entry.getKey() + "=>" + entry.getValue());
		    }*/

			/*		    System.out.println("==toJSONString()==");
		    System.out.println(JSONValue.toJSONString(json));*/

		}
		catch (ParseException pe)
		{
			System.out.println("Parser Exception: " + pe);
		}
	}
	
	public void moveRooms(Guest targetGuest, String formerRoom, String newRoom)
	{
		Guest receivingGuest;
//		String formerRoomName = clientGuest.getRoomMembership().roomName; 
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
			//send RoomChange message only to the requesting client:
			TCPServer.sendRoomChange(clientGuest.guestSocket, targetGuest.guestId, formerRoom, formerRoom);
		}
	}
	
	public boolean isRequestedIdValid(String id, int minLength, int maxLength)
	{
		String pattern= "^[a-zA-Z0-9]*$";
		if (!id.matches(pattern))
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