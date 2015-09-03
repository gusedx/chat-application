package comp90015.project1.gustavo.server;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
	
	List roomGuests = new ArrayList();
	List roomGuestIds = new ArrayList();
	String roomName = "";
	String owner = "";
	
	public ChatRoom(String chatRoomName)
	{
		roomName = chatRoomName;
	}
	
	public void addGuestToChatRoom(Guest guest)
	{
		roomGuests.add(guest);
		roomGuestIds.add(guest.guestId);
		guest.addRoomMembership(this);
	}
	
	public List getRoomGuestList()
	{
		return roomGuests;
	}
	
	public List getRoomGuestIdList()
	{
		return roomGuestIds;
	}
	
	public void setRoomOwner(String roomOwner)
	{
		owner = roomOwner;
	}

}
