package qclassifier;

import qclassifier.features.*;

import java.io.IOException;
import java.util.*;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;

public class Question 
{
	private String _question;
	private Map<String, Double> _features;
	private List<TaggedWord> _taggedQuestion;
	private QuestionType _questionType;
	private TaggedWord _headWord;
	private Tree _parseTree;
	
	public Question(String question)
	{
		_question = question;
		_features = new LinkedHashMap<String, Double>();	/* LinkeHashMap is used to maintain the order of insertion */
	}
	
	public void addFeature(String feature, double weight)
	{
		if (_features.containsKey(feature))
			_features.put(feature, _features.get(feature) + weight);
		else
			_features.put(feature, weight);
	}
	
	public String getQuestion()
	{
		return _question;
	}
		
	public Map<String, Double> getFeatures()
	{
		return _features;
	}
	
	 public void setHeadWord(TaggedWord headWord)
	 {
		 _headWord = headWord;
	 }
	 
	 public TaggedWord getHeadWord()
	 {
		 return _headWord;
	 }
	 
	 public List<TaggedWord> getTaggedQuestion() throws IOException, ClassNotFoundException
	 {
		 if (_taggedQuestion == null)
			 _taggedQuestion = PosTagger.tagQuestion(_question);
		 
		 return _taggedQuestion;
	 }
	 
	 public Tree getParseTree() throws IOException, ClassNotFoundException
	 {
		 if (_parseTree == null)
			 _parseTree = Parser.parseQuestion(getTaggedQuestion());
		 
		 return _parseTree;
	 }
	 	 
	 public QuestionType getQuestionType() throws IOException, ClassNotFoundException
	 {
		 if (_questionType == null)
			 _questionType = QuestionType.fromSentence(getTaggedQuestion());
		 
		 return _questionType;
	 }
	 
	 public void removeFeatures()
	 {
		 _features = new LinkedHashMap<String, Double>();
	 }
}
