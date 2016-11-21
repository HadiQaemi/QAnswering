package qclassifier.features;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.TaggedWord;

import Wordnet.Disambiguator;
import Wordnet.WordSimilarity;
import Wordnet.Wordnet;
import edu.stanford.nlp.trees.SemanticHeadFinder;

import qclassifier.Question;
import qclassifier.QuestionClassifier.ClassCategory;

public class QuestionCategory extends FeatureBuilder {

    private WordSimilarity _ws;
    private List<String> _fineCategories;
    private List<String> _coarseCategories;
    private Hashtable<IWord, String> _categorySenses;
    private Dictionary _dict;

    public QuestionCategory(double featureWeight, ClassCategory category) throws IOException {
        _featureWeight = featureWeight;
        _ws = new WordSimilarity();
        _categorySenses = new Hashtable<IWord, String>();
        _dict = Wordnet.getDictionary();

        /*
         * Question Categories: ENTY:Other and HUM:Description is removed
         * LOC:Other is replaced with location and NUM:other is replaced with
         * number
         */
        _fineCategories = Arrays.asList("abbreviation", "expansion",
                "definition", "description", "manner", "reason",
                "animal", "body", "color", "creation", "currency", "disease", "event", "food", "instrument", "language", "letter",
                "plant", "product", "religion", "sport", "substance", "symbol", "technique", "term", "vehicle", "word",
                "group", "individual", "title", "construct",
                "city", "country", "mountain", "location", "state",
                "code", "count", "date", "distance", "money", "order", "number", "percent", "period", "speed", "temperature", "size", "weight");

        _coarseCategories = Arrays.asList("abbreviation", "description", /*"entity",*/ "human", "location", "number");

        if (category == ClassCategory.Coarse) {
            loadCategorySenses(_coarseCategories);
        } else {
            loadCategorySenses(_fineCategories);
        }
    }

    @Override
    public void addFeatures(Question question) throws IOException, ClassNotFoundException {
        TaggedWord headWord = null;
        String semanticHeadWord = null;
        headWord = question.getHeadWord();
        
        if(headWord==null){
            SemanticHeadFinder shf = new SemanticHeadFinder();
            semanticHeadWord = question.getParseTree().headTerminal(shf).toString();
        }else{
            semanticHeadWord = headWord.word().toString();
        }
        
        
        for (TaggedWord taggedWord : question.getTaggedQuestion()) {
            if (taggedWord.word().equals(semanticHeadWord)) {
                headWord = taggedWord;
            }
        }
        
        if (headWord == null) {
            return;
        }

        Disambiguator d = new Disambiguator(question.getTaggedQuestion());
        IWord dword = d.disambiguateSense(headWord);

        if (dword == null) {
            return;
        }

        double maxSimilarity = 0, preMaxSimilarity = 0;
        String mostSimilar = "", preMostSimilar = "";

        for (Entry<IWord, String> categorySence : _categorySenses.entrySet()) {
            double sim = _ws.getSimilarity(categorySence.getKey(), dword);
            //System.out.println(categorySence.getValue() + "\t" + sim);

            if (sim > maxSimilarity) {
                preMaxSimilarity = maxSimilarity;
                preMostSimilar = mostSimilar;

                maxSimilarity = sim;
                mostSimilar = categorySence.getValue();
            }
        }

        if (!mostSimilar.equals("")) {
            question.addFeature(mostSimilar, _featureWeight * maxSimilarity);
            //System.out.println(question.getFeatures().size());
        }

        //if (!preMostSimilar.equals(""))
        //	question.addFeature(preMostSimilar, _featureWeight * preMaxSimilarity * 0.2);
    }

    private void loadCategorySenses(List<String> categories) {
        for (String category : categories) {
            IIndexWord indexCategory = _dict.getIndexWord(category, POS.NOUN);

            if (indexCategory == null) {
                System.out.println("Category " + category + " can not be disambiguated in WordNet!");
                continue;
            }

            /*
             * All senses of a given category will be mapped to that category
             */
            for (IWordID wordId : indexCategory.getWordIDs()) {
                _categorySenses.put(_dict.getWord(wordId), category);
            }
        }
    }
}
