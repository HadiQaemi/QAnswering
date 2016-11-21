package qclassifier.features;

import java.io.IOException;

import qclassifier.Question;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * A feature for each POS-tagged word.
 */
public class TaggedUnigrams extends FeatureBuilder 
{
	public TaggedUnigrams(double featureWight)
	{
		_featureWeight = featureWight;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		for (TaggedWord token : question.getTaggedQuestion()) 
		{
			question.addFeature(token.toString("_"), _featureWeight);
		}
	}

}
