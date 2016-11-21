package qclassifier.features;

import java.io.IOException;
import java.util.List;

import Wordnet.Disambiguator;
import Wordnet.HypernymsTree;
import edu.mit.jwi.item.IWord;
import edu.stanford.nlp.ling.TaggedWord;
import qclassifier.Question;

public class WordsHeight extends FeatureBuilder 
{
	public WordsHeight(double featureWeight)
	{
		_featureWeight = featureWeight;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		List<TaggedWord> sentence = question.getTaggedQuestion();
		
		Disambiguator d = new Disambiguator(sentence);
		HypernymsTree ht = HypernymsTree.getInstance();
		
		for (TaggedWord tword : sentence)
		{
			IWord dword = d.disambiguateSense(tword);
			
			if (dword != null)
			{
				int wordHeight = ht.getWordHeight(dword) + 1;
				question.addFeature(tword.word(),  _featureWeight * (double) wordHeight / 10);
			}
		}
	}
}
