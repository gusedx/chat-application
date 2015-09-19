package comp90015.project1.gustavo.server;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

public class GenerateUniqueId {
	
	int greatest = 1;

	static PriorityQueue<Integer> sortedPq = new PriorityQueue<Integer>();
	
	public int generateUniqueID()
	{		
		if (sortedPq.size() > 0)
		{
			return sortedPq.poll();
		}
		greatest++;
		return greatest - 1;
	}
}
