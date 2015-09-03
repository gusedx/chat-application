package comp90015.project1.gustavo.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Guest {
	String guestId;
	Socket guestSocket;
	List<ChatRoom> roomMemberships;
	
	Guest(String guestID, Socket clientSocket)
	{
		guestId = guestID;
		guestSocket = clientSocket;
		roomMemberships = new ArrayList<ChatRoom>();
	}
	
	public void addRoomMembership(ChatRoom room)
	{
		roomMemberships.add(room);
	}
	
	public List getRoomMemberships()
	{
		return roomMemberships;
	}
}
