package qclassifier.features;

import java.io.IOException;

import qclassifier.Question;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.ModCollinsHeadFinder;

public class CollinsHeadWord extends FeatureBuilder 
{
	private HeadFinder _headFinder;
	
	public CollinsHeadWord(double featurWeight) 
	{
		_headFinder = new ModCollinsHeadFinder();
		_featureWeight = featurWeight;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		String collinsHeadWord = question.getParseTree().headTerminal(_headFinder).toString();
		question.addFeature("cHeadWord:" + collinsHeadWord, _featureWeight);
	}
}
