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
		System.out.println("Adding " + guest.guestId + " to " + this.roomName);
		roomGuests.add(guest);
		roomGuestIds.add(guest.guestId);
		guest.setRoomMembership(this);
	}
	
	public void removeGuestFromChatRoom(Guest guest)
	{
		System.out.println("Removing " + guest.guestId + " from " + this.roomName);
		if (guest.guestId.equals(owner))
		{
			setRoomOwner("");
		}
		roomGuests.remove(guest);
		roomGuestIds.remove(guest.guestId);
		guest.setRoomMembership(null);
	}
	
	public List getRoomGuestList()
	{
		return roomGuests;
	}
	
	public int getNumberOfGuests()
	{
		return roomGuests.size();
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
