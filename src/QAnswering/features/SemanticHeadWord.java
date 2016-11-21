package qclassifier.features;

import java.io.IOException;

import edu.stanford.nlp.trees.SemanticHeadFinder;

import qclassifier.Question;

public class SemanticHeadWord extends FeatureBuilder
{
	SemanticHeadFinder _semanticHeadFinder;
	
	public SemanticHeadWord(double featureWeight)
	{
		_featureWeight = featureWeight;
		_semanticHeadFinder = new SemanticHeadFinder();
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		String semanticHeadWord = question.getParseTree().headTerminal(_semanticHeadFinder).toString();
		question.addFeature("sHeadWord:" + semanticHeadWord, _featureWeight);
	}

}
