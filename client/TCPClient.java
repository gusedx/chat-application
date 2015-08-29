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
	static String myRoom = "";
	public static void main(String args[])
	{	
		Socket socket = null;
		try
		{
			int serverPort = 4444;
			socket = new Socket(args[1], serverPort);
			System.out.println("Connection Established");
			
			while (true)
			{
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				//String data = in.readUTF(); //read line of data from the stream
				//System.out.println("Received: " + data); 
				
				//TODO: SEE WHY I'M NOT GETTING ROOMCONTENTS MESSAGE FROM SERVER (ONLY GETTING NEWID MESSAGE). NEED THREADING AND/OR LOOP IN CLIENT?
				
				String decodedMessage = decodeJsonMessage(in.readUTF()); //TODO: SET THE PROMPT INSTEAD OF JUST DISPLAYING MESSAGE IN SCREEN (DO THIS FROM DECODE FUNCTION?)
				SetPrompt(decodedMessage);
				System.out.println("Decoded Message received: " + decodedMessage);
							
				System.out.println("Sending data...");
				out.writeUTF(encodeJsonMessage(args[0])); //TODO: GET DATA FROM STDIO INSTEAD OF PASSING DATA FROM PROGRAM CALL
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
					break;
					
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
					System.out.println(myRoom + " contains " + guests);
					
					break;
					
				default:
					System.out.println("Invalid message type " + type);
					//TODO: ADD ERROR HANDLING
					break;
			}
			value = json.getOrDefault(key, null).toString();
		}
		catch (ParseException pe)
		{
			System.out.println("Parser Exception: " + pe);
		}
		
		return value;
	}
	
	public static void SetPrompt(String guestId)
	{
		
	}
}

class ReceiveMessage extends Thread
{
	ReceiveMessage(Socket aClientSocket)
	{
		this.start();
	}
}
