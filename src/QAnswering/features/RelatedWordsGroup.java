package qclassifier.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import qclassifier.Question;
import qclassifier.features.QueryExpantion.Expansion;

import edu.stanford.nlp.ling.TaggedWord;

/**
 * Adds a feature for each related-words-group.
 * The word groups are defined in the files in QC/publish/lists
 * from http://cogcomp.cs.illinois.edu/Data/QA/QC/. 
 */
public class RelatedWordsGroup extends FeatureBuilder 
{
	private HashMap<String, List<String>> _relatedWords;
	private Expansion _expansion;
	private double _headAmpl;	/* Specifiy the amplifying coefficient of head word */
	
	/**
	 * Initialize the related word groups, load the lists.
	 * @param directory the directory with the list files ( QC/publish/lists )
	 * @throws IOException
	 */
	public RelatedWordsGroup(double featureWeight, Expansion expansion) throws IOException 
	{
		this(featureWeight, expansion, 1);
	}
	
	public RelatedWordsGroup(double featureWeight, Expansion expansion, double headAmpl) throws IOException 
	{
		_featureWeight = featureWeight;
		_expansion = expansion;
		_relatedWords = new HashMap<String, List<String>>();
		_headAmpl = headAmpl;
		
		File directory = new File("QC/publish/lists");
		File[] files = directory.listFiles();
		
		// each relation-group has its own file,
		// we transform it into an inverted index
		for (File file : files) {
			String rel = file.getName();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				// add relation name to this word
				String word = line.trim();
				if (!word.isEmpty()) {
					List<String> list = _relatedWords.get(word);
					if (list == null) {
						list = new ArrayList<String>();
						_relatedWords.put(word, list); 
					}
					list.add("rel:" + rel);
				}
				line = reader.readLine();
			}
			reader.close();
		}
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		if (_expansion == Expansion.Sentence)
			for (TaggedWord token : question.getTaggedQuestion()) 
				addRelatedWords(question, token);
		else if (_expansion == Expansion.HeadWord)
			if (question.getHeadWord() != null)
				addRelatedWords(question, question.getHeadWord());
	}
	
	private void addRelatedWords(Question question, TaggedWord taggedWord)
	{
		double featureWeight = _featureWeight;
		
		if (question.getHeadWord() != null)
			if (taggedWord.word().equals(question.getHeadWord().word()))
				featureWeight *= _headAmpl;
		
		List<String> relatedWords = _relatedWords.get(taggedWord.word());
		if (relatedWords != null)
		{
			for (String word : relatedWords)
			{
				question.addFeature(word, featureWeight);
			}
		}		
	}
}

