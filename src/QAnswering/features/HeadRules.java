package qclassifier.features;

import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import qclassifier.Question;

public class HeadRules extends FeatureBuilder 
{
	public HeadRules(double featureWeight)
	{
		_featureWeight = featureWeight;
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		TaggedWord headRule = getHeadRule(question);
		
		if (headRule != null)
			question.addFeature(headRule.word(), _featureWeight);
	}
	
	private TaggedWord getHeadRule(Question question) throws IOException, ClassNotFoundException
	{
		QuestionType questionType = question.getQuestionType();
		List<TaggedWord> sentence = question.getTaggedQuestion();
		
		switch (questionType) {
		case How:
			// return the word following "how"
			for (int i=0; i < sentence.size() - 1; i++) {
				if (sentence.get(i).word().equalsIgnoreCase("how")) {
					return sentence.get(i+1);
				}
			}
			break;
			
		case What:
			// check the regular expression patterns
			// (regexp faked here)
			if (matchesDescDefPattern1a(sentence) || matchesDescDefPattern1b(sentence)) {
				return new TaggedWord("DESC:def pattern 1", "");
			} else if (matchesDescDefPattern2(sentence)) {
				return new TaggedWord("DESC:def pattern 2", "");
			} else if (matchesEntySubstancePattern1(sentence) || matchesEntySubstancePattern2(sentence)) {
				return new TaggedWord("ENTY:substance pattern", "");
			} else if (matchesDescDescPattern(sentence)) {
				return new TaggedWord("DESC:desc pattern", "");
			} else if (matchesEntyTermPattern(sentence)) {
				return new TaggedWord("ENTY:term pattern", "");
			} else if (matchesDescReasonPattern1(sentence)) {
				return new TaggedWord("DESC:reason pattern 1", "");
			} else if (matchesDescReasonPattern2(sentence)) {
				return new TaggedWord("DESC:reason pattern 2", "");
			} else if (matchesAbbrExpPattern(sentence)) {
				return new TaggedWord("ABBR:exp pattern", "");
			} else if (matchesHumDescPattern(sentence)) {
				return new TaggedWord("HUM:desc pattern", "");
			}
			break;
			
		case Who:
			if (matchesHumDescPattern(sentence)) {
				return new TaggedWord("HUM:desc pattern", "");
			}
			break;
		};
		
		return null;
	}
	
	private boolean matchesHumDescPattern(List<TaggedWord> sentence) {
		if (sentence.size() < 3) return false;
		return sentence.get(0).word().equalsIgnoreCase("who") &&
		       (sentence.get(1).word().equalsIgnoreCase("is") || sentence.get(1).word().equalsIgnoreCase("are")) &&
		       Character.isUpperCase(sentence.get(2).word().charAt(0));
	}
	
	private boolean matchesAbbrExpPattern(List<TaggedWord> sentence) {
		if (sentence.size() < 5) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       (sentence.get(1).word().equalsIgnoreCase("does") || sentence.get(1).word().equalsIgnoreCase("do")) &&
		       sentence.get(sentence.size()-3).word().equalsIgnoreCase("stand") &&
		       sentence.get(sentence.size()-2).word().equalsIgnoreCase("for");
		// last word is ?
	}
	
	private boolean matchesDescReasonPattern2(List<TaggedWord> sentence) {
		if (sentence.size() < 5) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       (sentence.get(1).word().equalsIgnoreCase("is") || sentence.get(1).word().equalsIgnoreCase("are")) &&
		       sentence.get(sentence.size()-3).word().equalsIgnoreCase("used") &&
		       sentence.get(sentence.size()-2).word().equalsIgnoreCase("for");
		// last word is ?
	}
	
	private boolean matchesDescReasonPattern1(List<TaggedWord> sentence) {
		if (sentence.size() < 2) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       (sentence.get(1).word().equalsIgnoreCase("causes") || sentence.get(1).word().equalsIgnoreCase("cause"));
	}
	
	private boolean matchesEntyTermPattern(List<TaggedWord> sentence) {
		if (sentence.size() < 4) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
	           sentence.get(1).word().equalsIgnoreCase("do") &&
	           sentence.get(2).word().equalsIgnoreCase("you") &&
		       sentence.get(3).word().equalsIgnoreCase("call");
	}
	
	private boolean matchesDescDescPattern(List<TaggedWord> sentence) {
		if (sentence.size() < 3) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       sentence.get(1).word().equalsIgnoreCase("does") &&
		       sentence.get(sentence.size()-2).word().equalsIgnoreCase("do");
		// last word is ?
	}
	
	private boolean matchesEntySubstancePattern2(List<TaggedWord> sentence) {
		if (sentence.size() < 5) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       (sentence.get(1).word().equalsIgnoreCase("is") || sentence.get(1).word().equalsIgnoreCase("are")) &&
		       (sentence.get(sentence.size()-3).word().equalsIgnoreCase("composed") || sentence.get(sentence.size()-3).word().equalsIgnoreCase("made")) &&
		       sentence.get(sentence.size()-2).word().equalsIgnoreCase("of");
		// last word is ?
	}
	
	private boolean matchesEntySubstancePattern1(List<TaggedWord> sentence) {
		if (sentence.size() < 4) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       (sentence.get(1).word().equalsIgnoreCase("is") || sentence.get(1).word().equalsIgnoreCase("are")) &&
		       sentence.get(sentence.size()-4).word().equalsIgnoreCase("made") &&
		       sentence.get(sentence.size()-3).word().equalsIgnoreCase("out") &&
		       sentence.get(sentence.size()-2).word().equalsIgnoreCase("of");
		// last word is ?
	}
	
	private boolean matchesDescDefPattern2(List<TaggedWord> sentence) {
		if (sentence.size() < 5) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       (sentence.get(1).word().equalsIgnoreCase("does") || sentence.get(1).word().equalsIgnoreCase("do")) &&
		       sentence.get(sentence.size()-2).word().equalsIgnoreCase("mean");
		// last word is ?
	}
	
	private boolean matchesDescDefPattern1b(List<TaggedWord> sentence) {
		if (sentence.size() >= 5 && sentence.size() <= 6) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       (sentence.get(1).word().equalsIgnoreCase("is") || sentence.get(1).word().equalsIgnoreCase("are")) &&
		       (sentence.get(2).word().equalsIgnoreCase("the") || sentence.get(2).word().equalsIgnoreCase("a") || sentence.get(2).word().equalsIgnoreCase("an"));
		// last word is ?
	}
	
	private boolean matchesDescDefPattern1a(List<TaggedWord> sentence) {
		if (sentence.size() >= 4 && sentence.size() <= 5) return false;
		return sentence.get(0).word().equalsIgnoreCase("what") &&
		       (sentence.get(1).word().equalsIgnoreCase("is") || sentence.get(1).word().equalsIgnoreCase("are"));
		// last word is ?
	}

}
