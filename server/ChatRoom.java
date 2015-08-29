package comp90015.project1.gustavo.server;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
	
	List roomGuests = new ArrayList();
	String roomName = "";
	String owner = "";
	
	public ChatRoom(String chatRoomName)
	{
		roomName = chatRoomName;
	}
	
	public void addGuestToChatRoom(String guestId)
	{
		roomGuests.add(guestId);
	}
	
	public List getRoomGuestList()
	{
		return roomGuests;
	}
	
	public void setRoomOwner(String roomOwner)
	{
		owner = roomOwner;
	}

}
