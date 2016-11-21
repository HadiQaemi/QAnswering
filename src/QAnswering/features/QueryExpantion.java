package qclassifier.features;

import java.io.IOException;
import java.util.List;

import Wordnet.Disambiguator;
import Wordnet.HypernymsTree;
import edu.mit.jwi.item.IWord;
import edu.stanford.nlp.ling.TaggedWord;

import qclassifier.Question;

public class QueryExpantion extends FeatureBuilder 
{
	private double _gama;
	private Expansion _expantion;
	private HypernymsTree _ht;
	
	public QueryExpantion(double featureWeight, double gama, Expansion expansionType) throws IOException
	{
		_featureWeight = featureWeight;
		_gama = gama;
		_expantion = expansionType;
		_ht = HypernymsTree.getInstance();
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		List<TaggedWord> sentence = question.getTaggedQuestion();
		
		Disambiguator d = new Disambiguator(sentence);
		
		if (_expantion == Expansion.Sentence)
		{
			for (TaggedWord tword : sentence)
			{
				IWord dword = d.disambiguateSense(tword);
				addHypernyms(question, dword);
			}
		}
		else if (question.getHeadWord() != null)
		{
			IWord dword = d.disambiguateSense(question.getHeadWord());
			addHypernyms(question, dword);
		}
	}
	
	private void addHypernyms(Question question, IWord disambgWord)
	{
		if (disambgWord == null)
			return;

		List<IWord> hypernyms = _ht.getHypernyms(disambgWord);
		for (int i = 0; i < hypernyms.size(); i++)
			question.addFeature("hy:" + hypernyms.get(i).getLemma(), _featureWeight * Math.pow(_gama, i));
	}
	
	public enum Expansion
	{
		Sentence, HeadWord
	}
}
