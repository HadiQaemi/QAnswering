package qclassifier.features;

import java.io.IOException;

import qclassifier.Question;

public class WhWord extends FeatureBuilder 
{
	public WhWord(double featureWeeight)
	{
		_featureWeight = featureWeeight;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		question.addFeature("whWord:" + question.getQuestionType().toString(), _featureWeight);
	}
}

