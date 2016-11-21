package Wordnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class WordSimilarity 
{
	private static Dictionary _dict;
	private HashMap<String, Integer> _hyponymsCount;
	private HashMap<String, Double> _ICs;
	private HypernymsTree _ht;
	
	public WordSimilarity() throws IOException
	{
		_dict = Wordnet.getDictionary();
		_hyponymsCount = new HashMap<String, Integer>();
		_ICs = new HashMap<String, Double>();
		_ht = HypernymsTree.getInstance();
		
		loadICs();
	}
	
	public double getSimilarity(IWord word1, IWord word2)
	{
		if (!_ICs.containsKey(word1.getID().toString()) || !_ICs.containsKey(word2.getID().toString()))
			return 0;
		
		double temp = _ICs.get(word1.getID().toString()) + _ICs.get(word2.getID().toString()) - 2 * resnikSimilarity(word1, word2);
		return 1 - temp / 2;
	}
	
	private double resnikSimilarity(IWord word1, IWord word2)
	{
		List<IWord> hypernyms1 = _ht.getHypernyms(word1);
		List<IWord> hypernyms2 = _ht.getHypernyms(word2);
		hypernyms1.retainAll(hypernyms2);
		
		double maxIC = 0;
		
		for (IWord common : hypernyms1)
		{
			double ic = _ICs.get(common.getID().toString());
			if (ic > maxIC)
				maxIC = ic;
		}
		
		return maxIC;
	}
	
	private void loadICs() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("QC/informatinContents.txt"));
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			String[] parts = line.split("\t");
			_ICs.put(parts[0], Double.valueOf(parts[1]));
		}
		
		reader.close();
	}
	
	public void writeICs() throws IOException
	{
		loadHyponymCounts();
		
		IIndexWord topIndexWord = _dict.getIndexWord("entity", POS.NOUN);
		IWord topWord = _dict.getWord(topIndexWord.getWordIDs().get(0));
		
		int maxhCount = _hyponymsCount.get(topWord.getID().toString());
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("QC/informatinContents.txt"));
		
		for (Entry<String, Integer> item : _hyponymsCount.entrySet())
		{
			double ic = 1 - Math.log(item.getValue() + 1) / Math.log(maxhCount);
			writer.write(item.getKey() + "\t" + ic + "\n");
		}
		
		writer.close();
	}
	
	
	private void loadHyponymCounts() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("QC/hyponyms.txt"));
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			String[] parts = line.split("\t");
			_hyponymsCount.put(parts[0], Integer.valueOf(parts[1]));
		}
		
		reader.close();
	}
		
	public void printHyponyms(IWord word)
	{
		printHyponyms(word, new ArrayList<IWord>(), 0);
	}
	
	private void printHyponyms(IWord word, List<IWord> heirarchy, int dept)
	{
		if (heirarchy.contains(word))
			return;
		
		heirarchy.add(word);

		ISynset synset = word.getSynset();
		List<ISynsetID> hyponyms = synset.getRelatedSynsets(Pointer.HYPONYM);
		
		/* In case there was no hypernyms check if the word has Hypernym_Instance synset */
		if (hyponyms.isEmpty())
			hyponyms = synset.getRelatedSynsets(Pointer.HYPONYM_INSTANCE);
		
		List<IWord> words;
		for (ISynsetID sid : hyponyms)
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
			
			printHyponyms(words.get(0), heirarchy, dept + 1);
		}
		
	}
	
	private void printTab(int nbTab)
	{
		for (int i = 0; i < nbTab; i++)
			System.out.print("\t");
	}
	
	public void writeHyponymsCount() throws IOException
	{
		IIndexWord topIndexWord = _dict.getIndexWord("entity", POS.NOUN);
		IWord topWord = _dict.getWord(topIndexWord.getWordIDs().get(0));
		
		_hyponymsCount.put(topWord.getID().toString(), getHyponymsCount(topWord));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("QC/hyponyms.txt"));
		
		System.out.println("Writing hyponym counts to file...");
		for (Entry<String, Integer> item : _hyponymsCount.entrySet())
			writer.write(item.getKey() + "\t" + item.getValue() + "\n");
		
		writer.close();
	}
	
	private int getHyponymsCount(IWord word)
	{
		if (_hyponymsCount.containsKey(word.getID().toString()))
			return _hyponymsCount.get(word.getID().toString());
		
		ISynset synset = word.getSynset();
		List<ISynsetID> hyponyms = synset.getRelatedSynsets(Pointer.HYPONYM);		
		List<ISynsetID> insHyponyms = synset.getRelatedSynsets(Pointer.HYPONYM_INSTANCE);
		
		int totalHyponyms = 0;
		int hCount = 0;
		
		List<IWord> words;
		
		/* normal hyponyms counting */
		for (ISynsetID sid : hyponyms)
		{
			words = _dict.getSynset(sid).getWords();
			hCount = getHyponymsCount(words.get(0));
			
			/* for all words in same synset the no. of hyponyms is same */
			for (IWord w : words)
				_hyponymsCount.put(w.getID().toString(), hCount);
			
			totalHyponyms += hCount + 1;
		}
		
		/* adding instance hyponyms to normal hyponyms */
		for (ISynsetID sid : insHyponyms)
		{
			words = _dict.getSynset(sid).getWords();
			hCount = getHyponymsCount(words.get(0));
			
			/* for all words in same synset the no. of hyponyms is same */
			for (IWord w : words)
				_hyponymsCount.put(w.getID().toString(), hCount);
			
			totalHyponyms += hCount + 1;
		}
		
		return totalHyponyms;
	}
	
	

}
