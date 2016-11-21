package classifier;

import java.util.Hashtable;

public class Sample 
{
	private int _classNo = -1;
	private Hashtable<Integer, Double> _features;
	
	
	public Sample(String features, int classNo)
	{
		this(features);
		_classNo = classNo;
	}
	
	public Sample(String features)
	{
		_features = new Hashtable<Integer, Double>();
		String[] pairs = features.trim().split(" ");
		
		for(String pair : pairs)
		{
			String[] parts = pair.trim().split(":");
			_features.put(Integer.valueOf(parts[0]), Double.valueOf(parts[1]));
		}
	}
	
	public int getClassNo()
	{
		return _classNo;
	}
	
	public Hashtable<Integer, Double> getFeatures()
	{
		return _features;
	}
}
