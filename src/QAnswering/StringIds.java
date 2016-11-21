package qclassifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Mapping strings to a unique number.
 */
public class StringIds implements Iterable<Entry<String, Integer>> {
	private Map<String, Integer> ids;
	
	public StringIds() {
		ids = new HashMap<String, Integer>();
	}
	
	public int toId(String key) {
		Integer id = ids.get(key);
		if (id==null) {
			id = ids.size() + 1;
			ids.put(key, id);
		}
		return id;
	}
	
	public Iterator<Entry<String, Integer>> iterator() {
		return ids.entrySet().iterator();
	}
	
	public Map<String, Integer> getMap()
	{
		return ids;
	}
	
	public void put(String key, Integer value)
	{
		ids.put(key, value);
	}
	
	public String getKeyByValue(Integer value)
	{
		for (Entry<String, Integer> entry : ids.entrySet())
		{
			if (entry.getValue() == value)
				return entry.getKey();
		}
		
		return null;
	}
	
	public int getSize()
	{
		return ids.size();
	}
}