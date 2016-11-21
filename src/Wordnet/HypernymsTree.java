package Wordnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.Pointer;

public class HypernymsTree 
{
	private Dictionary _dict;
	private static HypernymsTree _instance;
	private HashMap<String, Integer> _wordsHeight; 
	private BufferedWriter _heightWriter;
	
	private HypernymsTree() throws IOException
	{
		_dict = Wordnet.getDictionary();
		
		File wordHeightFile = new File("Test//words.height");
		
		if (wordHeightFile.exists())
			loadWordsHeight(wordHeightFile);
		else
			_heightWriter = new BufferedWriter(new FileWriter(wordHeightFile));
			
	}
	
	private void loadWordsHeight(File wordHeightFile) throws NumberFormatException, IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(wordHeightFile));
		_wordsHeight = new HashMap<String, Integer>();
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			String[] parts = line.split("\t");
			_wordsHeight.put(parts[0], Integer.valueOf(parts[1]));
		}			
	}
	
	public static HypernymsTree getInstance() throws IOException
	{
		if (_instance == null)
			_instance = new HypernymsTree();
		
		return _instance;
	}
	
	
	public int getWordHeight(IWord word) throws IOException
	{
		if (_heightWriter != null)
		{
			int wordHeigh = getWordHeight(word, new ArrayList<IWord>());
			_heightWriter.write(word.getLemma() + "\t" + wordHeigh + "\n");
			_heightWriter.flush();
		}
		else
		{
			if (_wordsHeight.containsKey(word.getLemma()))
				return _wordsHeight.get(word.getLemma());
		}
		
		return -1;
	}
	
	private int getWordHeight(IWord word, List<IWord> heirarchy)
	{
		if (heirarchy.contains(word))
			return 0;
		
		heirarchy.add(word);
		
		ISynset synset = word.getSynset();
		List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
		
		/* In case there was no hypernyms check if the word has Hypernym_Instance synset */
		if (hypernyms.isEmpty())
			hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE);

		if (!hypernyms.isEmpty())
		{
			List<IWord> words = _dict.getSynset(hypernyms.get(0)).getWords();
			return getWordHeight(words.get(0), heirarchy) + 1;
		}
			
		return 0;
	}
	
	public List<IWord> getHypernyms(IWord word)
	{
		return getHypernyms(word, new ArrayList<IWord>());	
	}
	
	private List<IWord> getHypernyms(IWord word, List<IWord> heirarchy)
	{
		if (heirarchy.contains(word))
			return heirarchy;
		
		heirarchy.add(word);
		
		ISynset synset = word.getSynset();
		List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
		
		/* In case there was no hypernyms check if the word has Hypernym_Instance synset */
		if (hypernyms.isEmpty())
			hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE);

		if (!hypernyms.isEmpty())
		{
			for (ISynsetID hypernym : hypernyms)
			{
				List<IWord> words = _dict.getSynset(hypernym).getWords();
	
				for (IWord hword : words)
				{
					getHypernyms(hword, heirarchy);
				}
			}
			
			//return heirarchy;
		}
			
		return heirarchy;
	}
	
	public void printHypernyms(IWord word)
	{
		printHypernyms(word, new ArrayList<IWord>(), 0);
	}
	
	private void printHypernyms(IWord word, List<IWord> heirarchy, int dept)
	{
		if (heirarchy.contains(word))
			return;
		
		heirarchy.add(word);

		ISynset synset = word.getSynset();
		List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
		
		/* In case there was no hypernyms check if the word has Hypernym_Instance synset */
		if (hypernyms.isEmpty())
			hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE);
		
		List<IWord> words;
		for (ISynsetID sid : hypernyms)
		{
			words = _dict.getSynset(sid).getWords();
			printTab(dept);
			System.out.print("{");
			
			for (Iterator<IWord> i = words.iterator(); i.hasNext(); )
			{
				IWord w = i.next();
				System.out.print(w.getLemma());
				
				if (i.hasNext())
					System.out.print(", ");
			}
			System.out.println("}");
			
			printHypernyms(words.get(0), heirarchy, dept + 1);
		}
		
	}
	
	private void printTab(int nbTab)
	{
		for (int i = 0; i < nbTab; i++)
			System.out.print("\t");
	}
}
