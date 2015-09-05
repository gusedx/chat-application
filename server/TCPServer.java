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
	
	public static void main (String args[])
	{
		ChatRoom MainHall = new ChatRoom("MainHall"); //TODO: MAYBE CHANGE THIS TO CREATE MainHall CHATROOM VIA MESSAGE CALL
		
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
				
				MainHall.addGuestToChatRoom(guest); //add new guest to default chat room 
				//TODO: SEND ROOMCHANGE MESSAGE TO ALL CLIENTS IN THE MAINHALL
				System.out.println("Sending MainHall room contents message to " + guest.guestId);
				sendRoomContentsMessage(clientSocket, MainHall);
				System.out.println("Room contents message sent to " + guest.guestId);
				//TODO: SEND ROOMLIST MESSAGE TO CLIENT
				
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
	
	public static void sendMessage(Socket aClientSocket, String message)
	{
		DataOutputStream out;
		
		try {
			out = new DataOutputStream(aClientSocket.getOutputStream());
			out.writeUTF(message);
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
	
	public static String encodeJsonRoomListMessage(List rooms)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "roomlist");
		obj.put("rooms", rooms);
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
		
		ChatRoom room;
		Guest receivingGuest;
		
		//echo the received message to each guest (other than the sender) that is a member of a room that the sender is a member of:
		Iterator<ChatRoom> roomListIterator = senderGuest.getRoomMemberships().iterator();
		while (roomListIterator.hasNext())
		{
			room = roomListIterator.next();
			System.out.println("Relaying message to members of room " + room.roomName);
			Iterator<Guest> guestListIterator = room.getRoomGuestList().iterator();
			while (guestListIterator.hasNext())
			{
				receivingGuest = guestListIterator.next();
				if (receivingGuest.guestId != senderGuest.guestId)
				{
					System.out.println("Relaying message to " + receivingGuest.guestId);
					sendMessage(receivingGuest.guestSocket, message);
				}
			}
		}
	}
}

class Connection extends Thread
{
	DataInputStream in;
	DataOutputStream out;
	Guest clientGuest;
	
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
				processJsonMessage(in.readUTF());
				
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

					if ((isRequestedIdValid(value)) && (!isRequestedIdInUse(value)))
					{
						System.out.println("Identity change from " + clientGuest.guestId + " to " + value + " allowed. clientList size: " + TCPServer.clientList.size());
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
				default:
					System.out.println("Invalid message type " + type);
					//TODO: ADD ERROR HANDLING
					break;
			}
			value = json.getOrDefault(key, null).toString();

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
	
	public boolean isRequestedIdValid(String id)
	{
		String pattern= "^[a-zA-Z0-9]*$";
		if (!id.matches(pattern))
		{
			return false;
		}
		if (id.length() < 3 || id.length() > 16)
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
}