package qclassifier.features;

import java.io.IOException;
import java.util.List;
import edu.stanford.nlp.ling.TaggedWord;
import qclassifier.Question;
import qclassifier.Stemmer;

public class StemmedUnigram extends FeatureBuilder 
{
	public StemmedUnigram(double featureWeight)
	{
		_featureWeight = featureWeight;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		List<TaggedWord> sentence = question.getTaggedQuestion();
		
		for (TaggedWord tword : sentence)
			question.addFeature(Stemmer.getRoot(tword), _featureWeight);
	}

}
