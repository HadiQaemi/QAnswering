package qclassifier.features;

import java.io.IOException;

import qclassifier.*;

public abstract class FeatureBuilder 
{
	protected double _featureWeight;
	public abstract void addFeatures(Question question) throws IOException, ClassNotFoundException;
	
	public double getFeatureWeight()
	{
		return _featureWeight;
	}
	
}
