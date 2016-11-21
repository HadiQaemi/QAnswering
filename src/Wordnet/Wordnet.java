/*
 * Question Classification Library
 * Author: Babak Loni, email: babak.loni@gmail.com
 * Last Modification: 31/08/2011
 * 
 * Wordnet Interface 
 */

package Wordnet;

import java.io.File;
import java.net.MalformedURLException;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.morph.WordnetStemmer;

public class Wordnet
{
	private static Dictionary _dict;
	private static WordnetStemmer _stemmer;
	
	public static Dictionary getDictionary()
	{
		if (_dict == null)
		{
			try {
				_dict = new Dictionary(new File("models/dict").toURI().toURL());
				_dict.open();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return _dict;
	}
	
	public static WordnetStemmer getStemmer()
	{
		if (_stemmer == null)
			_stemmer = new WordnetStemmer(getDictionary());
		
		return _stemmer;
	}
}
