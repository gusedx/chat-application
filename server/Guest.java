package comp90015.project1.gustavo.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Guest {
	String guestId;
	Socket guestSocket;
	ChatRoom memberRoom;
	
	Guest(String guestID, Socket clientSocket)
	{
		guestId = guestID;
		guestSocket = clientSocket;
	}
	
	public void setRoomMembership(ChatRoom room)
	{
		memberRoom = room;
	}
	
	public ChatRoom getRoomMembership()
	{
		return memberRoom;
	}
}
