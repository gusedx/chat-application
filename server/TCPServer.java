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
				
				sendNewClientId(clientSocket);
				
				MainHall.addGuestToChatRoom(guestId); //add new guest to default chat room 
				//TODO: SEND ROOMCHANGE MESSAGE TO ALL CLIENTS IN THE MAINHALL
				System.out.println("Sending MainHall room contents message to " + guestId);
				sendRoomContentsMessage(clientSocket, MainHall);
				System.out.println("Room contents message sent to " + guestId);
				//TODO: SEND ROOMLIST MESSAGE TO CLIENT
				
				new Connection(clientSocket);
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
	
	public static void sendNewClientId(Socket aClientSocket)
	{
		DataOutputStream out;
		
		try {
			out = new DataOutputStream(aClientSocket.getOutputStream());
			out.writeUTF(encodeJsonNewIdentityMessage());
			
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void sendRoomContentsMessage(Socket aClientSocket, ChatRoom room)
	{
		DataOutputStream out;
		
		try {
			out = new DataOutputStream(aClientSocket.getOutputStream());
			out.writeUTF(encodeJsonRoomContentsMessage(room.roomName, room.getRoomGuestList(), room.owner));
			
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static String encodeJsonNewIdentityMessage()
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "newidentity");
		obj.put("former", "");
		obj.put("identity", guestId);
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
}

class Connection extends Thread
{
	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;
	
	public Connection(Socket aClientSocket)
	{
		try
		{
			clientSocket = aClientSocket;
//			in = new DataInputStream(clientSocket.getInputStream());
//			out = new DataOutputStream(clientSocket.getOutputStream());
			
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
				in = new DataInputStream(clientSocket.getInputStream());
				decodedMessage = decodeJsonMessage(in.readUTF());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Decoded Message received: " + decodedMessage);
		}
	}
	
	public static String decodeJsonMessage(String jsonString)
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

			//TODO: THIS COULD BE A GENERIC JSON DECODER WITH THE KEY SPECIFIED BY THE CALLING FUNCTION (MESSAGE TYPE AND KEY TO GET A CERTAIN VALUE, SUCH AS MESSAGE CONTENT)
			String key = "";
			String type = json.getOrDefault("type", null).toString();
			
			switch(type)
			{
				case "message":
					key = "content";
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
		
		return value;
	}
}