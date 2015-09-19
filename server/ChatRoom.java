package comp90015.project1.gustavo.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatRoom {
	
	List roomGuests = new ArrayList();
	HashMap<Guest,Calendar> kickedUsers = new HashMap<Guest,Calendar>();
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
		guest.setRoomMembership(this);
	}
	
	public void removeGuestFromChatRoom(Guest guest)
	{
		if (roomGuests.contains(guest))
		{
			System.out.println("Removing " + guest.guestId + " from " + this.roomName);
			if (guest.guestId.equals(owner))
			{
				setRoomOwner("");
			}
			roomGuests.remove(guest);
			guest.setRoomMembership(null);
			
			//delete the room if it is not the Main Hall and it has no owner and no guests
			if ((!this.roomName.equals("MainHall")) && (owner.equals("")) && (roomGuests.size() == 0))
			{
				TCPServer.roomList.remove(this);
				System.gc();
			}	
		}
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
		List roomGuestIds = new ArrayList();
		Guest guest;
		
		for (int i = 0; i < roomGuests.size(); i++)
		{
			guest = (Guest) roomGuests.get(i);
			roomGuestIds.add(guest.guestId);
		}
		return roomGuestIds;
	}
	
	public void setRoomOwner(String roomOwner)
	{
		owner = roomOwner;
	}
	
	public void updateGuestId(Guest guest, String newId)
	{
		roomGuests.remove(guest);
		roomGuests.add(guest);
	}
	
	public void addKickedUser(Guest guest, Calendar dateCanJoinAgain)
	{
		kickedUsers.put(guest, dateCanJoinAgain);
	}
	
	public void clearKickedUser(Guest guest)
	{
		//remove user from the list of kicked out users
		kickedUsers.remove(guest);
	}

}
