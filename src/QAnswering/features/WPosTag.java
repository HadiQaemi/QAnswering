package qclassifier.features;

import java.io.IOException;
import qclassifier.Question;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * Adds a feature for each POS tag starting with W.
 * (for What, Where, How etc.)
 */

public class WPosTag extends FeatureBuilder {
	
	public WPosTag(double featureWeight)
	{
		_featureWeight = featureWeight;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		for (TaggedWord token : question.getTaggedQuestion()) 
		{
			if (token.tag().startsWith("W")) 
			{
				question.addFeature(token.tag(), _featureWeight);
			}
		}
	}
}
