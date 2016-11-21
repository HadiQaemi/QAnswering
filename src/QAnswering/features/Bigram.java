package qclassifier.features;

import java.io.IOException;
import java.util.List;

import qclassifier.Question;

public class Bigram extends FeatureBuilder 
{
	private boolean _wholeQuestion;		/* If not true only the bigram of first two words will be considered */
	
	public Bigram(double featureWeight, boolean wholeQuestion)
	{
		_featureWeight = featureWeight;
		_wholeQuestion = wholeQuestion;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		
		if (!_wholeQuestion)
		{
			String[] questionWords = question.getQuestion().split(" ");
			
			if (questionWords.length > 1)
				question.addFeature("bigram:" + questionWords[0] + "->" + questionWords[1], _featureWeight);
			
			return;
		}
		
		for (int i = question.getTaggedQuestion().size(); i >= 0; i--) 
		{
			String a = (i > 0) ? question.getTaggedQuestion().get(i - 1).toString("_") : ""; 
			String b = (i < question.getTaggedQuestion().size()-1) ? question.getTaggedQuestion().get(i).toString("_") : ""; 
			question.addFeature("bigram:" + a + "->" + b, _featureWeight);
		}
	}

}
