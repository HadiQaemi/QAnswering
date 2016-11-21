/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Wordnet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import java.io.File;
import java.net.URL;
import qclassifier.QuestionClassifier;

/**
 *
 * @author haqa
 */
public class testWordnet {
    public static void main(String[] args) throws Exception {
        
        IDictionary dict = new Dictionary ( new File("models/dict").toURI().toURL());
        dict . open ();
        //Start
         // look up first sense of the word "dog "
        IIndexWord idxWord = dict . getIndexWord ("dog", POS.NOUN );
        System.out.println("ss"+idxWord.getWordIDs().toString());
        IWordID wordID = idxWord.getWordIDs().get(0) ;
        IWord word = dict . getWord ( wordID );
        System .out . println ("Id = " + wordID );
        System .out . println (" Lemma = " + word . getLemma ());
        System .out . println (" Gloss = " + word . getSynset (). getGloss ());
        
        //Synset of words
        IIndexWord idxWord2 = dict.getIndexWord ("dog", POS. NOUN );
        IWordID wordID2 = idxWord2 . getWordIDs ().get (0) ; // 1st meaning
        IWord word2 = dict . getWord ( wordID2 );
        ISynset synset = word2 . getSynset ();

        // iterate over words associated with the synset
        for( IWord w : synset . getWords ())
            System .out . println (w. getLemma ());
    }
}
