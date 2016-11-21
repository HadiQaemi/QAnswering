package qclassifier.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import qclassifier.Question;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.trees.Tree;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HeadWord extends FeatureBuilder 
{
	private HeadFinder _headFinder;
	private Hashtable<String, TaggedWord> _savedHeadWords;
	private boolean _loadFromFile;
	private BufferedWriter _headWordWriter; 
	
	public HeadWord(double featureWeight, String headWordFile, boolean loadFromFile) throws IOException, SQLException
	{
		_headFinder = new SemanticHeadFinder();
		_featureWeight = featureWeight;
		_loadFromFile = loadFromFile;
		
		//If loadFromFile is true then the class tries to load the pre-computed headwords from file,
		//otherwise it compute the headwords and write them in the file specified
		if (_loadFromFile)
			loadHeadWords(headWordFile);
		else
			_headWordWriter = new BufferedWriter(new FileWriter(headWordFile));
	}
		
	private void loadHeadWords(String filePath) throws SQLException
	{
		System.out.println("Loading Headwords...");
		
		_savedHeadWords = new Hashtable<String, TaggedWord>();
		BufferedReader headWordReader;
		PreparedStatement preparedStatement = null;
                /*
                Connection connection = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    System.out.println(e);
                    return;
                } 
                try {
                    connection = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/qc","root", "");
                } catch (SQLException e) {
                    System.out.println(e);
                    return;
                }*/
                
		try {
			headWordReader = new BufferedReader(new FileReader(filePath));
			String line = headWordReader.readLine();
			int i =1;
			while (line != null)
			{
				String[] parts = line.split("\t", 2);
				if (parts.length > 1)
				{
					String question = parts[0];
					/*System.out.println(question);
                                        preparedStatement = connection.prepareStatement("INSERT INTO `qc`.`questions` (`question`) VALUES (?)");
                                        preparedStatement.setString(1, question);
                                        preparedStatement.executeUpdate();*/
                                        String[] taggedWord = parts[1].split("_", 2);
					TaggedWord headWord;
					
					if (taggedWord.length > 1)
						headWord = new TaggedWord(taggedWord[0], taggedWord[1]);
					else
						headWord = new TaggedWord(taggedWord[0], "");
					
					_savedHeadWords.put(question, headWord);
				}
				
				line = headWordReader.readLine(); 
			}

		} catch (FileNotFoundException e) {
			System.out.println("Head word file not found!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeToFile(TaggedWord headWord, String question)
	{
		if (headWord == null)
			headWord = new TaggedWord("null", "");
		
		try {
			_headWordWriter.append(question + "\t" + headWord.toString("_") + "\n");
			_headWordWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		TaggedWord headWord = null;
		
		if (_loadFromFile)
			headWord = getHeadwordFromFile(question.getQuestion());
		else
			headWord = findHeadword(question.getTaggedQuestion(), question.getParseTree(), question.getQuestionType());
		
		if (headWord != null)
		{
			question.setHeadWord(headWord);
			question.addFeature("headWord:" + headWord.toString(), _featureWeight);
		}
		else
		{
			question.setHeadWord(new TaggedWord("headword:null", ""));
			question.addFeature("headWord:" + "headword:null", _featureWeight);
		}
		
		if (!_loadFromFile)
			writeToFile(headWord, question.getQuestion());
	}
	
	private TaggedWord getHeadwordFromFile(String questionString)
	{
		TaggedWord headWord = null;
		
		if (_savedHeadWords.containsKey(questionString))
		{
			headWord = _savedHeadWords.get(questionString);
		
			if (headWord.toString("_").equals("null_"))
				return null;
		}
		
		return headWord;
	}
	
	private TaggedWord findHeadword(List<TaggedWord> sentence, Tree parseTree, QuestionType questionType) 
	{
		// following to the paper (algorithm 1)
		
		switch (questionType) {
		case When: case Where: case Why:
			// no head word
			return null;

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
		
		
		// bring head node label to top of tree, retrieve
		parseTree.percolateHeads(_headFinder);
		CoreLabel headLabel = (CoreLabel)parseTree.label();
		
		TaggedWord candidate = new TaggedWord(headLabel.word(), headLabel.tag());
		if (candidate.tag().startsWith("NN")) {
			return candidate;
		}
		
		// return the first word whose tag starts with NN
		/*
		for (int i = 0; i < sentence.size(); i++) 
		{
			if (sentence.get(i).tag().startsWith("NN")) {
				return sentence.get(i);
			}
		}
		*/
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
