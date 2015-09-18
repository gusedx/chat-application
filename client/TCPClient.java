package comp90015.project1.gustavo.client;

import java.net.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TCPClient
{
	public static String clientId = "";
	public static String hostname = "";
	public static Socket socket = null;
	public static String newRoomName = "";
	public static String myRoom = "";

	public static void main(String args[])
	{	
		String userInput;
		String[] commandArguments;
		String firstInputChar;
		String roomName = "";
		
		try
		{
			int serverPort = 4444;
			hostname = args[0];
			if (args.length > 1)
			{
				if (args[1].equals("-p"))
				{
					if (args.length > 2)
					{
						serverPort = Integer.parseInt(args[2]);
					}
					else
					{
						System.out.println("Usage: java -jar chatclient.jar hostname [-p port]");
						System.exit(0);
					}
				}
				else
				{
					System.out.println("Usage: java -jar chatclient.jar hostname [-p port]");
					System.exit(0);
				}
			}
			
			System.out.println("Attempting to connect to host " + hostname + " on port " + serverPort);
			
			socket = new Socket();
			socket.connect(new InetSocketAddress(hostname, serverPort), 10000);
			
			ReceiveMessage receivedMessage = new ReceiveMessage(socket);
			
			sendRequestList(socket);
			
			while (true)
			{
				if (!myRoom.equals(""))
				{
					System.out.print("[" + myRoom + "] " + clientId + "> ");
				}	
				BufferedReader consoleInput = new BufferedReader( new InputStreamReader(System.in, "UTF-8"));
				userInput = consoleInput.readLine();
				firstInputChar = String.valueOf(userInput.charAt(0));
							
				if (firstInputChar.equals("#")) //if the user input is a command
				{
					if (Debugger.isEnabled())
						System.out.println("Input was a command...");
					userInput = userInput.replace(firstInputChar, "");
					commandArguments = userInput.split(" ");
					switch(commandArguments[0].toLowerCase())
					{
						case "identitychange":
							sendIdentityChange(socket, commandArguments[1]);
							break;
							
						case "createroom":
							newRoomName = commandArguments[1];
							sendCreateRoom(socket, newRoomName);
							break;
							
						case "list":
							sendRequestList(socket);
							break;
							
						case "join":
							roomName = commandArguments[1];
							sendJoin(socket, roomName);
							break;
							
						case "who":
							sendWho(socket, commandArguments[1]);
							break;
							
						case "delete":
							sendDeleteRoom(socket, commandArguments[1]);
							break;
							
						case "quit":
							sendQuit(socket);
							break;
							
						case "kick":
							sendKick(socket, commandArguments[1], commandArguments[2], Integer.parseInt(commandArguments[3]));
							break;
							
						default:
							System.out.println("Invalid command " + commandArguments[0]);
							break;
						}
				}
				else //message from user to other guests
				{
					if (Debugger.isEnabled())
						System.out.println("Input was a user message...");
					sendUserMessage(socket, userInput);
				}
			}
		}
		catch (UnknownHostException e)
		{
			System.out.println("Socket: " + e.getMessage());
		}
		catch (EOFException e)
		{
			System.out.println("EOF:" + e.getMessage());
		}
		catch (IOException e)
		{
			System.out.println("readline:" + e.getMessage());
		}
		catch (Exception e)
		{
			TCPClient.closeConnection();
			System.exit(0);
		}
		finally
		{
			if (socket != null)
				try
				{
					socket.close();
				}
				catch (IOException e)
				{
					System.out.println("Close: " + e.getMessage());
				}
		}
	}
	
	public static void closeConnection()
	{
		try {
			System.out.println("Closing connection.");
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String encodeJsonMessage(String message)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "message");
		obj.put("content", message);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonIdentityChange(String identity)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "identitychange");
		obj.put("identity", identity);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonCreateRoom(String roomName)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "createroom");
		obj.put("roomid", roomName);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonJoin(String roomName)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "join");
		obj.put("roomid", roomName);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonWho(String roomName)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "who");
		obj.put("roomid", roomName);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonRequestList()
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "list");
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}

	public static String encodeJsonDeleteRoom(String roomName)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "delete");
		obj.put("roomid", roomName);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonQuit()
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "quit");
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static String encodeJsonKick(String roomid, int timeout, String username)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "kick");
		obj.put("roomid", roomid);
		obj.put("time", new Integer(timeout));
		obj.put("identity", username);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		if (Debugger.isEnabled())
			System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static void sendMessage(Socket aClientSocket, String message)
	{
		DataOutputStream out;
		
		try {
//			out = new DataOutputStream(aClientSocket.getOutputStream());
			OutputStreamWriter out1 = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
			out1.write(message + "\n");
			out1.flush();
			
		} catch (IOException e) {
			System.out.println("readline: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void sendUserMessage(Socket aClientSocket, String userMessage)
	{
		String message = encodeJsonMessage(userMessage);
		sendMessage(aClientSocket, message);
	}
	
	public static void sendIdentityChange(Socket aClientSocket, String identity)
	{
		String message = encodeJsonIdentityChange(identity);
		if (Debugger.isEnabled())
			System.out.println("new JSON encoded identity to be sent: " + message);
		sendMessage(aClientSocket, message);
	}
	
	public static void sendCreateRoom(Socket aClientSocket, String roomName)
	{
		String message = encodeJsonCreateRoom(roomName);
		sendMessage(aClientSocket, message);
	}
	
	public static void sendRequestList(Socket aClientSocket)
	{
		String message = encodeJsonRequestList();
		sendMessage(aClientSocket, message);
	}
	
	public static void sendJoin(Socket aClientSocket, String roomName)
	{
		String message = encodeJsonJoin(roomName);
		sendMessage(aClientSocket, message);
	}
	
	public static void sendWho(Socket aClientSocket, String roomName)
	{
		String message = encodeJsonWho(roomName);
		sendMessage(aClientSocket, message);
	}
	
	public static void sendDeleteRoom(Socket aClientSocket, String roomName)
	{
		String message = encodeJsonDeleteRoom(roomName);
		sendMessage(aClientSocket, message);
	}
	
	public static void sendQuit(Socket aClientSocket)
	{
		String message = encodeJsonQuit();
		sendMessage(aClientSocket, message);
	}
	
	public static void sendKick(Socket aClientSocket, String username, String roomid, int timeout)
	{
		String message = encodeJsonKick(roomid, timeout, username);
		sendMessage(aClientSocket, message);
	}
}

class ReceiveMessage extends Thread
{
	static String myRoom = "";
	Socket socket = null;
	DataInputStream in;
	
	ReceiveMessage(Socket aClientSocket) throws IOException
	{
		socket = aClientSocket;
		if (Debugger.isEnabled())
			System.out.println("Starting new client receive message thread...");
		start();
		if (Debugger.isEnabled())
			System.out.println("New client receive message thread started.");
	}
		
	public void run()
	{
		try {
			if (Debugger.isEnabled())
				System.out.println("In run method...");
			BufferedReader in;
	
				in = new BufferedReader( new InputStreamReader(socket.getInputStream(), "UTF-8"));
			
			String jsonString;
			
			while((jsonString = in.readLine()) != null)
			{
				if (Debugger.isEnabled())
					System.out.println("Waiting to receive message...");
				processJsonMessage(jsonString);
				Thread.sleep(3000);
			}
		}
		catch (EOFException e)
		{
			try {
				System.out.println("Closing connection to " + TCPClient.hostname);
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Thread.currentThread().interrupt();
		} 
		catch (InterruptedException e)
		{
			return;
		}
		catch (IOException e1) {
			TCPClient.closeConnection();
			System.exit(0);
		}
	}
	
	public static void processJsonMessage(String jsonString)
	{	
		String value = null;
		
		JSONParser parser = new JSONParser();

		try
		{		
			JSONObject json = (JSONObject) parser.parse(jsonString);

			String key = "";
			String type = json.getOrDefault("type", null).toString();
			
			switch(type)
			{
				case "newidentity":
					String formerId = "";
					key = "identity";
					value = json.getOrDefault(key, null).toString();
					key = "former";
					formerId = json.getOrDefault(key, null).toString();
					
					if (formerId.equals(TCPClient.clientId) || TCPClient.clientId.equals("")) //new id for this client (client does not have an id or former id in message is client's current id:
					{
						if (formerId.equals(value))
						{
							System.out.println("Requested identity invalid or in use");
						}
						else
						{
							TCPClient.clientId = value;
							
							if (formerId.equals(""))
							{
								System.out.println("Connected to " + TCPClient.hostname + " as " + TCPClient.clientId);
							}
							else
							{
								System.out.println(formerId + " is now " + TCPClient.clientId);
							}
						}
					}
					else //new id is not for this client
					{
						System.out.println(formerId + " is now " + value);
					}
					break;
					
				case "roomcontents":
					key = "roomid";
					value = json.getOrDefault(key, null).toString();
					myRoom = value;
					TCPClient.myRoom = myRoom;
					
					key = "identities";
					String guests = "";
					String guest;
					String owner = "";
					List guestList = (List) json.getOrDefault(key, null);
					key = "owner";
					owner = json.getOrDefault(key, null).toString();
					for (int i = 0; i < guestList.size(); i++)
					{
						guest = guestList.get(i).toString();
						guests = guests + " " + guest;
						if (guest.equals(owner))
						{
							guests = guests + "*";
						}
					}
					System.out.println(myRoom + " contains " + guests);
					break;
					
				case "roomlist":
					String roomId;
					Boolean roomCreated = false;
									
					JSONArray roomsInfoJsonArray = (JSONArray) json.get("rooms");
					for (int i = 0; i < roomsInfoJsonArray.size(); i++)
					{
						JSONObject roomsInfoJsonArrayElement = (JSONObject) roomsInfoJsonArray.get(i);
						roomId = roomsInfoJsonArrayElement.getOrDefault("roomid", null).toString();
						if (roomId.equals(TCPClient.newRoomName))
						{
							roomCreated = true;
						}
						System.out.println(roomId + ": " + roomsInfoJsonArrayElement.getOrDefault("count", null) + " guests");
					}
					
					if (roomCreated)
					{
						System.out.println("Room " + TCPClient.newRoomName + " created.");
						TCPClient.newRoomName = "";
					}
					
					if ((!TCPClient.newRoomName.equals("")) && (!roomCreated))
					{
						System.out.println("Room " + TCPClient.newRoomName + " is invalid or already in use.");
					}
					break;

				case "message":
					key = "identity";
					value = json.getOrDefault(key, null).toString();
					key = "content";
					String message = value + ": " + json.getOrDefault(key, null).toString();
					System.out.println(message);
					break;
					
				case "roomchange":
					String formerRoom = "";
					String newRoom = "";
					String identity = "";
					key = "former";
					formerRoom = json.getOrDefault(key, null).toString();
					key = "roomid";
					newRoom = json.getOrDefault(key, null).toString();
					TCPClient.myRoom = newRoom;
					key = "identity";
					identity = json.getOrDefault(key, null).toString();
					
					if (formerRoom.equals("") && !newRoom.equals(""))
					{
						System.out.println(identity + " moves to " + newRoom);
					}
					else
					{
						if ((!identity.equals(TCPClient.clientId)) || (!newRoom.equals(formerRoom))) //request was for another client or was successful
						{
							if ((identity.equals(TCPClient.clientId)) && (newRoom.equals("")))
							{
								System.out.println(identity + " leaves " + formerRoom);
								System.out.println("Disconnected from " + TCPClient.hostname);
								TCPClient.closeConnection();
								System.exit(0);
							}
							else
							{
								if (!newRoom.equals(""))
								{
									System.out.println(identity + " moved from " + formerRoom + " to " + newRoom);
								}
							}
						}
						else //request was not successful
						{
							System.out.println("The requested room is invalid or non existent.");
						}
					}
					
					break;
				default:
					System.out.println("Invalid message type " + type);
					//TODO: ADD ERROR HANDLING
					break;
			}
		}
		catch (ParseException pe)
		{
			System.out.println("Parser Exception: " + pe);
		}
	}
}
