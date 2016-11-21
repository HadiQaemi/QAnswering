package qclassifier.features;

import java.io.IOException;

import qclassifier.Question;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * Word shape according to 'Question Classification using HeadWords and their Hypernyms'.
 */
public class WordShape extends FeatureBuilder 
{
	public WordShape(double featureWeight)
	{
		_featureWeight = featureWeight;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		for (TaggedWord word : question.getTaggedQuestion()) 
		{
			question.addFeature("wordShape:" + getShapeIndex(word.word()), _featureWeight);
		}
	}
	
	private int getShapeIndex(String word) {
		// "We use five word shape features, namely
		//  all upper case, all lower case, mixed case, all digits,
		//  and other."
		char[] chars = word.toCharArray();
		
		int upper = 0, lower = 0, digit = 0, other = 0;
		
		for (int i=chars.length-1; i>=0; i--) {
			switch (Character.getType(chars[i])) {
			case Character.UPPERCASE_LETTER: upper++; break;
			case Character.LOWERCASE_LETTER: lower++; break;
			case Character.DECIMAL_DIGIT_NUMBER: digit++; break;
			default: other++;
			}
		}
		
		if (upper==chars.length) {
			return 0;
		} else if (lower==chars.length) {
			return 1;
		} else if (upper+lower==chars.length) {
			return 2; // mixed
		} else if (digit==chars.length) {
			return 3;
		} else {
			return 4;
		}
	}


}
