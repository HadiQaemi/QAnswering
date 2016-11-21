package qclassifier;

import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.BinaryGrammar;
import edu.stanford.nlp.parser.lexparser.ExhaustivePCFGParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.parser.lexparser.UnaryGrammar;
import edu.stanford.nlp.trees.Tree;

public class Parser 
{
	private static LexicalizedParser _parser;
	
	public static Tree parseQuestion(List<TaggedWord> taggedQuestion)
	{
		if (_parser == null)
		{
			_parser = new LexicalizedParser("models/englishPCFG.ser.gz");
		}
		
		_parser.parse(taggedQuestion);
		
		return _parser.getBestParse();
	}
}
