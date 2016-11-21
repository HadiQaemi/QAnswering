package classifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

public class NaiveClassifier 
{
	private Hashtable<String, Double> _featuresConfident;		/* Each entry specifies how confident feature i is about class j */
	private Hashtable<Integer, Double> _featuresSum;			/* Each entry is the sum of the value of a feature in all samples */
	private List<Integer> _classes;
	
	public NaiveClassifier(String trainFilePath) throws IOException
	{
		train(trainFilePath);
	}
	
	private void train(String trainFilePath) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(trainFilePath));
		
		_featuresConfident = new Hashtable<String, Double>();
		_featuresSum = new Hashtable<Integer, Double>();
		_classes = new ArrayList<Integer>();
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			String[] parts = line.split(" ", 2);
			int classNo = Integer.valueOf(parts[0]);
			Sample s = new Sample(parts[1], classNo);
			updateConfidents(s);
			
			if (!_classes.contains(classNo))
				_classes.add(classNo);
		}
		
		Hashtable<Integer, Double> test = new Hashtable<Integer, Double>();
		
		int size = _featuresConfident.size();
		int count = 0;
		for (Entry<String, Double> item : _featuresConfident.entrySet())
		{
			String key = item.getKey();
			double value = item.getValue();
			int featureNo = Integer.valueOf(key.split("/")[0]);
			double featureSum = _featuresSum.get(featureNo);
			_featuresConfident.put(key, value / featureSum);
			count++;
			
			double old = 0;
			if (test.containsKey(featureNo))
				old = test.get(featureNo);
			
			test.put(featureNo, old + value / featureSum);
			
		}
		
		System.out.println("Original: " + size + " counted: " + count);
	}
	
	private void updateConfidents(Sample s)
	{
		int classNo = s.getClassNo();
		
		for (Entry<Integer, Double> feature : s.getFeatures().entrySet())
		{
			int featureNo = feature.getKey();
			double value = feature.getValue();
			double oldSum = 0;
			double newSum;
			
			if (_featuresSum.containsKey(featureNo))
				oldSum = _featuresSum.get(featureNo);
			
			newSum = oldSum + value;
			_featuresSum.put(featureNo, newSum);
			
			double oldConfident = 0;
			String key = String.valueOf(featureNo) + "/" + String.valueOf(classNo);
			
			if (_featuresConfident.containsKey(key))
				oldConfident = _featuresConfident.get(key);
			
			double newConfident = oldConfident + value; //(oldConfident * oldSum + value) / newSum;
			
			_featuresConfident.put(key, newConfident);
		}
	}
	
	public int classify(Sample s)
	{
		int bestClass = -1;
		
		double maxLikelihood = 0;
		for (int classNo : _classes)
		{
			double likelihood = 0;
			for (Entry<Integer, Double> feature : s.getFeatures().entrySet())
			{
				String key = String.valueOf(feature.getKey()) + "/" + String.valueOf(classNo);
				double confident = 0;
				
				if (_featuresConfident.containsKey(key))
					confident = _featuresConfident.get(key);
				
				likelihood += feature.getValue() * confident;
			}
			
			if (likelihood > maxLikelihood)
			{
				maxLikelihood = likelihood;
				bestClass = classNo;
			}
		}
		
		return bestClass;
	}
	
	public double test(String testFilePath) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(testFilePath));
		
		int nbSamples = 0;
		int nbCorrect = 0;
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			String[] parts = line.split(" ", 2);
			Sample s = new Sample(parts[1]);
			int actualClass = Integer.valueOf(parts[0]);
			int predictedClass = classify(s);
			
			if (predictedClass == actualClass)
				nbCorrect++;
			
			nbSamples++;
		}
		
		return (double) nbCorrect / nbSamples;
	}
}
