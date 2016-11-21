package qclassifier.features;

import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;

/**
 * The 'W words' from Question Classification using HeadWords and their Hypernyms.
 */
public enum QuestionType {
	What, Which, When, Where, Who, How, Why, Rest;

	public static QuestionType fromSentence(List<TaggedWord> sentence) {
		for (TaggedWord word : sentence) {
			for (QuestionType type : values()) {
				if (type.toString().equalsIgnoreCase(word.word())) {
					return type;
				}
			}
		}
		return Rest;
	}
}
