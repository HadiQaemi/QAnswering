/*
 * Question Classification Library
 * Author: Babak Loni, email: babak.loni@gmail.com
 * Last Modification: 31/08/2011
 * 
 * Detecting the headword of a question based on the algorithm represented in Silva et. al (2011) 
 */

package qclassifier.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;
import qclassifier.Question;

public class SilvaHeadwords extends FeatureBuilder 
{
	private Hashtable<String, List<String>> _priorityList;
	private Hashtable<String, ParsDirection> _direction;
	private List<String> _typeWords;

	private Hashtable<String, TaggedWord> _savedHeadWords;
	private boolean _loadFromFile;
	private BufferedWriter _headWordWriter; 
	
	public SilvaHeadwords(double feaureWeight, String headWordFile, boolean loadFromFile) throws IOException
	{
		_featureWeight = feaureWeight;
		makeRules();
		_loadFromFile = loadFromFile;
		
		//If loadFromFile is true then the class tries to load the pre-computed headwords from file,
		//otherwise it compute the headwords and write them in the file specified
		if (_loadFromFile)
			loadHeadWords(headWordFile);
		else
			_headWordWriter = new BufferedWriter(new FileWriter(headWordFile));

	}
	
	public SilvaHeadwords()
	{
		makeRules();
	}
			
	private void loadHeadWords(String filePath)
	{
		_savedHeadWords = new Hashtable<String, TaggedWord>();
		BufferedReader headWordReader;
		
		try {
			headWordReader = new BufferedReader(new FileReader(filePath));
			String line = headWordReader.readLine();
			
			while (line != null)
			{
				String[] parts = line.split("\t", 2);
				if (parts.length > 1)
				{
					String question = parts[0];
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
			headWord = findHeadword(question);
		
		if (headWord != null)
		{
			question.setHeadWord(headWord);
			question.addFeature(headWord.word(), _featureWeight);
		}		
		//else
		//{
		//	question.setHeadWord(new TaggedWord("headword:null", ""));
		//	question.addFeature("headWord:" + "headword:null", _featureWeight);
		//}
		
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
	
	private void makeRules()
	{
		_priorityList = new Hashtable<String, List<String>>();
		_direction = new Hashtable<String, ParsDirection>();
		
		_priorityList.put("S", Arrays.asList("VP", "S", "FRAG", "SBAR", "ADJP"));
		_priorityList.put("SBARQ", Arrays.asList("SQ", "S", "SINV", "SBARQ", "FRAG"));
		_priorityList.put("SQ", Arrays.asList("NP", "VP", "SQ"));
		_priorityList.put("NP", Arrays.asList("NP", "NN", "NNP", "NNPS", "NNS", "NX"));
		_priorityList.put("PP", Arrays.asList("WHNP","NP", "WHADVP", "SBAR"));
		_priorityList.put("WHNP", Arrays.asList("NP", "NN", "NNP", "NNPS", "NNS", "NX", "WHNP"));  //WHNP and rest is added
		_priorityList.put("WHADVP", Arrays.asList("NP", "NN", "NNP", "NNPS", "NNS", "NX", "WHNP"));
		_priorityList.put("WHADJP", Arrays.asList("NP", "NN", "NNP", "NNPS", "NNS", "NX", "WHNP"));
		_priorityList.put("WHPP", Arrays.asList("WHNP", "WHADVP", "NP", "SBAR"));
		_priorityList.put("ROOT", Arrays.asList("S", "SBARQ"));
		_priorityList.put("VP", Arrays.asList("NP", "NN", "NNP", "NNPS", "NNS", "NX", "SQ", "PP"));
		_priorityList.put("SINV", Arrays.asList("NP"));
		_priorityList.put("NX", Arrays.asList("NP", "NN", "NNP", "NNPS", "NNS", "NX", "S"));
		
		_direction.put("S", ParsDirection.LeftByCategory);
		_direction.put("SBARQ", ParsDirection.LeftByCategory);
		_direction.put("SQ", ParsDirection.LeftByCategory);
		_direction.put("NP", ParsDirection.RightByPosition);
		_direction.put("PP", ParsDirection.LeftByCategory);
		_direction.put("WHNP", ParsDirection.LeftByCategory);
		_direction.put("WHPP", ParsDirection.RightByCategory);
		_direction.put("WHADVP", ParsDirection.LeftByCategory);
		_direction.put("WHADJP", ParsDirection.LeftByCategory);
		_direction.put("ROOT", ParsDirection.LeftByCategory);
		_direction.put("VP", ParsDirection.RightByCategory);
		_direction.put("SINV", ParsDirection.LeftByCategory);
		_direction.put("NX", ParsDirection.LeftByCategory);
		
		_typeWords = Arrays.asList("kind", "name", "type", "part", "genre", "group");
	}
	
	
	public TaggedWord findHeadword(Question question) throws IOException, ClassNotFoundException
	{		
		if (question.getQuestionType() == QuestionType.When || 
				question.getQuestionType() == QuestionType.When || 
				question.getQuestionType() == QuestionType.Why)
			return null;
		else if (question.getQuestionType() == QuestionType.How)
		{
			List<TaggedWord> taggedQuestion = question.getTaggedQuestion();
			for (int i = 0; i < taggedQuestion.size() - 1; i++) 
			{
				if (taggedQuestion.get(i).word().equalsIgnoreCase("how")) 
				{
					return taggedQuestion.get(i + 1);
				}
			}
		}
		
		
		Tree parseTree = question.getParseTree();
		//parseTree.pennPrint();
		
		Tree head = extractHeadWord(parseTree, null);
		if (head != null)
		{	
			Tree terminal = head.children()[0];
			String POS = head.label().value();
			
			TaggedWord headWord = new TaggedWord(terminal.label().value(), POS);
			
			//System.out.println("Headword: " + headWord.toString());
			return headWord;
		}

		return null;
	}
	
	private Tree extractHeadWord(Tree tree, Tree parent)
	{
		if (tree == null)
			return null;
		else if (tree.isPrePreTerminal())
	    {
			if (containsTypeWords(tree))
			{
				Tree closestPP = getClosestPP(tree, parent);
				if (closestPP != null)
					return extractHeadWord(closestPP, null);
			}
			
		}
		else if (tree.isPreTerminal())
			return tree;

		Tree headChild = applyRules(tree);
		return extractHeadWord(headChild, tree);
	}
	
	/**
	 * find closest PP in a tree in which head word is one of the type, kind, ... words
	 * @param tree
	 * @param parent
	 * @return
	 */
	private Tree getClosestPP(Tree tree, Tree parent)
	{		
		
		for (Tree child : tree.children())
			if (child.label().value().equals("PP"))
				return child;
		
		if (parent == null)
			return null;

		for (Tree sib : tree.siblings(parent))
			if (sib.label().value().equals("PP"))
				return sib;
		
		return null;
	}
	
	private boolean containsTypeWords(Tree tree)
	{
		if (tree.isLeaf())
		{
			if (_typeWords.contains(tree.label().value()))
				return true;
		}
		else
		{
			for (Tree child : tree.children())
			{
				if (containsTypeWords(child))
					return true;
			}
		}
		
		return false;
	}
	
	private Tree applyRules(Tree tree)
	{
		Tree t = applyNontrivialRules(tree);
		
		if (t != null)
			return t;
		
		Tree[] childs = tree.children();
		String parentPOS = tree.label().value();
		
		List<String> priorityList = _priorityList.get(parentPOS);
		if (priorityList != null)
		{
			ParsDirection pd = _direction.get(parentPOS);
			
			switch (pd)
			{
			case RightByCategory:
				for (String pos : priorityList)
					for (int i = childs.length - 1; i >= 0; i--)
						if (childs[i].label().value().equals(pos))
							return childs[i];
			case LeftByCategory:
				for (String pos : priorityList)
					for (int i = 0; i < childs.length; i++)
						if (childs[i].label().value().equals(pos))
							return childs[i];
			case RightByPosition:
				for (int i = childs.length - 1; i >= 0; i--)
					for (String pos : priorityList)
						if (childs[i].label().value().equals(pos))
							return childs[i];
			case LeftByPosition:
				for (int i = 0; i < childs.length; i++)
					for (String pos : priorityList)
						if (childs[i].label().value().equals(pos))
							return childs[i];				
			}
		}
		else
		{
			System.out.println("Unknown POS\n" + tree.pennString());
		}
		
		return null;
	}
	
	private Tree applyNontrivialRules(Tree tree)
	{
		String parentPOS = tree.label().value();
		Tree[] childs = tree.children();
		
		if (parentPOS.equals("SBARQ"))
			for (Tree child : childs)
				if (child.label().value().startsWith("WH") && 
					child.label().value().endsWith("P") && child.children().length >= 2)
					return child;
		
		
		if (tree.label().value().startsWith("WH"))
		{
			Tree NPcontainingPOS = getNPContainingPOS(tree);
			if (NPcontainingPOS != null && NPcontainingPOS != tree)
				return NPcontainingPOS;
		}
		
		return null;			
	}
	
	
	private Tree getNPContainingPOS(Tree tree)
	{
		if (tree.isLeaf())
			return null;
				
		for (Tree child : tree.children())
			if (child.label().value().equals("POS"))
				return tree;
		
		for (Tree child : tree.children())
			if (getNPContainingPOS(child) != null)
				return child;
		
		return null;
	}
	
	public enum ParsDirection
	{
		LeftByCategory, RightByCategory, LeftByPosition, RightByPosition
	}
	
	
	
}
