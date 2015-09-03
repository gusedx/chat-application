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
	public static void main(String args[])
	{	
		Socket socket = null;
		try
		{
			int serverPort = 4444;
			if (args.length > 1)
			{
				serverPort = Integer.parseInt(args[1]);
			}
			
			System.out.println("Attempting to connect to host " + args[0] + " on port " + serverPort);
			
			//socket = new Socket(args[0], serverPort);
			socket = new Socket();
//			System.out.println("Client local port: " + socket.getLocalPort());
			socket.connect(new InetSocketAddress(args[0], serverPort), 10000);
			System.out.println("Connection Established");
			
			ReceiveMessage receivedMessage = new ReceiveMessage(socket);
//			receivedMessage.start();
			
			while (true)
			{
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
													
				System.out.println("Sending data...");	
				BufferedReader consoleInput = new BufferedReader( new InputStreamReader(System.in));
				out.writeUTF(encodeJsonMessage(consoleInput.readLine()));
				out.flush();
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
		System.out.println(jsonText);
		
		return jsonText;
	}
	
	public static void SetPrompt(String guestId)
	{
		
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
		System.out.println("Starting new client receive message thread...");
		start();
		System.out.println("New client receive message thread started.");
	}
		
	public void run()
	{
		System.out.println("In run method...");
		while (true)
		{
			System.out.println("Waiting to receive message...");
			try {
				in = new DataInputStream(socket.getInputStream());
				String jsonString = in.readUTF();
				String decodedMessage = decodeJsonMessage(jsonString);
				System.out.println(decodedMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //TODO: SET THE PROMPT INSTEAD OF JUST DISPLAYING MESSAGE IN SCREEN (DO THIS FROM DECODE FUNCTION?)
			//SetPrompt(decodedMessage); //TODO: CHECK TYPE OF MESSAGE AND CALL SETPROMPT IF APPROPRIATE
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

			String key = "";
			String type = json.getOrDefault("type", null).toString();
			
			switch(type)
			{
				case "newidentity":
					key = "identity";
					return json.getOrDefault(key, null).toString();
					
				case "roomcontents":
					key = "roomid";
					value = json.getOrDefault(key, null).toString();
					myRoom = value;
					
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
						if (guest == owner)
						{
							guests = guests + "*";
						}
					}
					return (myRoom + " contains " + guests);

				case "message":
					key = "identity";
					value = json.getOrDefault(key, null).toString();
					key = "content";
					String message = value + ": " + json.getOrDefault(key, null).toString();
					return message; 
				default:
					return "Invalid message type " + type;
					//TODO: ADD ERROR HANDLING
			}
		}
		catch (ParseException pe)
		{
			System.out.println("Parser Exception: " + pe);
		}
		return ("Invalid message type.");
	}
}
