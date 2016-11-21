package qclassifier.features;

import qclassifier.Question;

public class Unigram extends FeatureBuilder
{
	public Unigram(double featureWeight)
	{
		_featureWeight = featureWeight;
	}
	
	@Override
	public void addFeatures(Question question) 
	{
		for(String word : question.getQuestion().split(" "))
		{
			question.addFeature(word, _featureWeight);
		}			
	}
}
