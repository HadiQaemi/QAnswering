package qclassifier.features;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import qclassifier.Question;

import edu.berkeley.nlp.PCFGLA.BerkeleyParser;
import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.Parser;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.ling.CollinsHeadFinder;
import edu.berkeley.nlp.ling.HeadFinder;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;
import edu.berkeley.nlp.util.Numberer;
import edu.stanford.nlp.ling.TaggedWord;

public class BerkeleyHeadWord 
{
	public static String getHeadWord(String question) throws IOException, ClassNotFoundException
	{
		ParserData pData = ParserData.Load("lib\\eng_sm6.gr");
	    
		if (pData == null) {
	      System.out.println("Failed to load grammar file");
	      System.exit(1);
	    }
	    
		Grammar grammar = pData.getGrammar();
	    Lexicon lexicon = pData.getLexicon();
	    Numberer.setNumberers(pData.getNumbs());
	    
		CoarseToFineMaxRuleParser parser = new CoarseToFineMaxRuleParser(
				grammar, lexicon, 1.0, -1, false, false, false, false, false, true, true);
		
		Question q = new Question(question);
		List<TaggedWord> taggedQuestion = q.getTaggedQuestion();
		
		List<String> sentence = new ArrayList<String>();
		List<String> posTags = new ArrayList<String>();
		for (TaggedWord tword : taggedQuestion)
		{
			sentence.add(tword.word());
			posTags.add(tword.tag());
		}
		
		Tree<String> parsedTree = parser.getBestConstrainedParse(sentence, posTags, null);
		
		/*
		Trees.PennTreeReader treeReader = new PennTreeReader(
				new StringReader("((S (NP (DT the) (JJ quick) (JJ (AA (BB (CC brown)))) (NN fox)) (VP (VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))"));		
		Tree<String> tree = treeReader.next();
		System.out.println("tree "+tree);
		*/
		
		CollinsHeadFinder headFinder = new CollinsHeadFinder();
		while (!parsedTree.isLeaf()) 
		{
			Tree<String> head = headFinder.determineHead(parsedTree);
			System.out.println("head " + head);
			parsedTree = head;
		}
		
		return "";
	}
}
