package qclassifier;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import edu.smu.tspell.wordnet.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.ModCollinsHeadFinder;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import qclassifier.QuestionClassifier.ClassCategory;
import qclassifier.features.BerkeleyHeadWord;
import qclassifier.features.Ngram;
import qclassifier.features.QuestionCategory;

//for wordnet

public class QuestionAnswering {
    
    /**
     *
     * @param SubQuestion
     * @param Tage
     * @return
     */
    protected StanfordCoreNLP pipeline;

    public List<String> lemmatize(String documentText)
    {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        this.pipeline = new StanfordCoreNLP(props);
        List<String> lemmas = new LinkedList<String>();
        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);
        // run all Annotators on this text
        this.pipeline.annotate(document);
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }
        return lemmas;
    }
    public static final List<String> STRINGS = Arrays.asList("is", "was", "were");
    public static final List<String> NUMBER_TYPE = Arrays.asList("value","far","much","rate","many","cost","population","tall");
    public static final List<String> PERSON_TYPE = Arrays.asList("name","write");
    public static final List<String> PERSON1_TYPE = Arrays.asList("two");
    public static final List<String> ENTITY_TYPE = Arrays.asList("visible","company","acronym","cancer");
    public static final List<String> LOCATION_TYPE = Arrays.asList("city","river","building","country","capital","nationality","large");
    public static final List<String> TIME_TYPE = Arrays.asList("year","time");
    public static final List<String> A1_TYPE = Arrays.asList("mean");
    public static final List<String> Q_TYPE = Arrays.asList("when", "where", "who");
    private static int j;
    
    public static String FilterClause(Scanner SubQuestion,String Tage) {
        String FilterClause = " ";
        SubQuestion.useDelimiter(", ");  // whitespace(s) or -
        System.setProperty("wordnet.database.dir", "models/wn3.1.dict/dict");
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        
        while (SubQuestion.hasNext()) {
            String sw = SubQuestion.next();
            
            String Tage1 = Tage;
            if(Tage=="N")
                Tage1 = "(N|J)";
            Pattern Q1 = Pattern.compile("([a-zA-Z]+)*/"+Tage1+"\\w+");           
            Matcher QQ1 = Q1.matcher(sw);
            if(QQ1.find()){
                for (int group1 = 1; group1 <= QQ1.groupCount(); ++group1) {
                    String str = QQ1.group(group1);
                    //StanfordLemmatizer slem = new StanfordLemmatizer();
                    //str = slem.lemmatize(str).toString();
                    if("V".equals(Tage)){
                        
                        if("was".equals(str) ||"Was".equals(str)){
                            str = "is";
                        }
                        if("were".equals(str) ||"Were".equals(str)){
                            str = "are";
                        }
                    }
                    
                    if(STRINGS.contains(str)){
                        FilterClause += " ".equals(FilterClause) ?  "regex(str(?"+Tage+"),'"+str+"')" : " || "+"regex(str(?"+Tage+"),'"+str+"')";
                    }else{
                        Synset[] synsets;
                        if(str==null || str.length()==1){
                            continue;
                        }
                        if(Tage=="V")
                            synsets = database.getSynsets(str,SynsetType.VERB, true);
                        else 
                            synsets = database.getSynsets(str,SynsetType.NOUN, true);
                        if(synsets.length==0){
                            FilterClause += " ".equals(FilterClause) ?  "regex(str(?"+Tage+"),'"+str+"')" : " || "+"regex(str(?"+Tage+"),'"+str+"')";
                            continue;
                        }
                        String[] wordForms = synsets[0].getWordForms();
                        for (int j = 0; j < wordForms.length; j++){
                            FilterClause += " ".equals(FilterClause) ?  "regex(str(?"+Tage+"),'"+wordForms[j].replace("'", "")+"')" : " || "+"regex(str(?"+Tage+"),'"+wordForms[j].replace("'", "")+"')";
                        }
                        if(str.equals("has")){
                            FilterClause += " ".equals(FilterClause) ?  "regex(str(?"+Tage+"),'"+str.replace("'", "")+"')" : " || "+"regex(str(?"+Tage+"),'"+str.replace("'", "")+"')";
                        }
                        
                    }
                }
            }
        }
        return FilterClause;
    }

    /**
     *
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Question question1 = new Question("date location human");
        String Ques = "How long does it take to travel from Tokyo to Niigata";
        Question question = new Question(Ques);
        
        String tag = question.getTaggedQuestion().toString();
        String collinsHeadWord = null;
        String semanticHeadWord = null;
        TaggedWord headWord = null;
        
        String qtype = question.getQuestionType().toString().toLowerCase();
        
        Pattern Head = Pattern.compile( "^\\[(\\w+)/W\\w+, (((?!\\w+/V\\w+).)+) ((\\w+/V\\w+.)+) .*\\]$");
        Matcher m_Head = Head.matcher(tag);
        if(Q_TYPE.contains(qtype)){
            switch (qtype) {
                case "when":
                    semanticHeadWord = "temporal";
                    break;
                case "where":
                    semanticHeadWord = "location";
                    break;
                case "who":
                    semanticHeadWord = "human";
                    break;
            }
            for (TaggedWord taggedWord : question1.getTaggedQuestion()) 
                if (taggedWord.word().equals(semanticHeadWord)) 
                    headWord = taggedWord;
        }else
        if(m_Head.find()){
            Pattern pattern = Pattern.compile("([a-zA-Z]*)/(\\w+),");
            Matcher matcher = pattern.matcher(m_Head.group(2).toString());
            if(matcher.find())
                semanticHeadWord = matcher.group(1);
            else
                semanticHeadWord = m_Head.group(2);
            for (TaggedWord taggedWord : question.getTaggedQuestion()) 
                if (taggedWord.word().equals(semanticHeadWord)) 
                    headWord = taggedWord;
        }else{
            ModCollinsHeadFinder hf = new ModCollinsHeadFinder();
            collinsHeadWord = question.getParseTree().headTerminal(hf).toString();
            SemanticHeadFinder shf = new SemanticHeadFinder();
            semanticHeadWord = question.getParseTree().headTerminal(shf).toString();
        }
        
        String C = "";
        if(NUMBER_TYPE.contains(semanticHeadWord)){
            C = "FILTER (regex(str(?c),'number')) ";
        }else if(PERSON_TYPE.contains(semanticHeadWord)){
            C = "FILTER (regex(str(?c),'PERSON') ||regex(str(?c),'Entity')) ";    
        }else if(ENTITY_TYPE.contains(semanticHeadWord)){
            C = "FILTER (regex(str(?c),'Entity')) ";    
        }else if(LOCATION_TYPE.contains(semanticHeadWord)){
            C = "FILTER (regex(str(?c),'LOC')) ";    
        }else if(A1_TYPE.contains(semanticHeadWord)){
            C = "FILTER (regex(str(?c),'A1')) ";    
        }else if(TIME_TYPE.contains(semanticHeadWord)){
            C = "FILTER (regex(str(?c),'TMP')) ";    
        }else if(PERSON1_TYPE.contains(semanticHeadWord)){
            C = "FILTER (regex(str(?c),'PERSON')) ";    
        }else 
            C = "FILTER (regex(str(?c),'Entity')) ";    
        
        QuestionCategory cat = new QuestionCategory(1, ClassCategory.Coarse);
        cat.addFeatures(question);
        String type = "";
        Set<Entry<String, Double>> features = question.getFeatures().entrySet();
        for (Entry<String, Double> entry : features) {
            type = entry.toString();
        }
                
        Pattern p1 = Pattern.compile( "^\\[(\\w+)/W\\w+, (((?!\\w+/V\\w+).)+) ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+)\\]$");
        Pattern p2 = Pattern.compile( "^\\[(\\w+)/W\\w+, (((?!\\w+/V\\w+).)+) ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+) ((\\w+/V\\w+.)+)\\]$");
        Pattern p3 = Pattern.compile( "^\\[(\\w+)/W\\w+, ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+)\\]$");
        Pattern p4 = Pattern.compile( "^\\[(\\w+)/W\\w+, ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+) ((\\w+/V\\w+.)+)\\]$");
        Pattern p5 = Pattern.compile( "^\\[(\\w+)/W\\w+, ((\\w+/V\\w+.)+) ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+)\\]$");
        Pattern p6 = Pattern.compile( "^\\[(\\w+)/W\\w+, ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+) ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+)\\]$");
        
        Pattern p7 = Pattern.compile( "^\\[(\\w+)/W\\w+, ((\\w+/V\\w+.)+) (((?![a-zA-Z1-9]+/V[a-zA-Z1-9]+).)+) ((\\w+/V\\w+.)+ ?)+ (((?!\\w+/V\\w+).)+)\\]$");
        Pattern p8 = Pattern.compile( "^\\[(\\w+)/W\\w+, (((?!\\w+/V\\w+).)+) ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+) ((\\w+/V\\w+.)+) (((?!\\w+/V\\w+).)+)\\]$");
        String OFilterClause = " ";
        String OFilterClause_2 = " ";
        String SFilterClause = " ";
        String VFilterClause = " ";
        Matcher m1 = p1.matcher(tag);
        Matcher m2 = p2.matcher(tag);
        Matcher m3 = p3.matcher(tag);
        Matcher m4 = p4.matcher(tag);
        Matcher m5 = p5.matcher(tag);
        Matcher m6 = p6.matcher(tag);
        Matcher m7 = p7.matcher(tag);
        Matcher m8 = p8.matcher(tag);
        
        int q_part = 2;
        if(m8.find()){
            Scanner in = new Scanner(m8.group(6));
            OFilterClause = FilterClause(in,"N");
            
            Scanner in_2 = new Scanner(m8.group(10));
            OFilterClause_2 = FilterClause(in_2,"N");
            
            if(OFilterClause!=" ")
                OFilterClause = OFilterClause + (OFilterClause_2 != " " ? " || "+OFilterClause_2 : "");

            Scanner V_in = new Scanner(m8.group(8));
            VFilterClause = FilterClause(V_in,"V");
            q_part = 3;
        }else
        if(m7.find()){
            Scanner in = new Scanner(m7.group(4));
            OFilterClause = FilterClause(in,"N");
            
            Scanner in_2 = new Scanner(m7.group(8));
            OFilterClause_2 = FilterClause(in_2,"N");
            if(OFilterClause!=" ")
                OFilterClause = OFilterClause + (OFilterClause_2 != " " ? " || "+OFilterClause_2 : "");
            
            Scanner V_in = new Scanner(m7.group(6));
            VFilterClause = FilterClause(V_in,"V");
            q_part = 2;
        }else if(m1.find()){
            Scanner in = new Scanner(m1.group(6));
            OFilterClause = FilterClause(in,"N");

            Scanner V_in = new Scanner(m1.group(4));
            VFilterClause = FilterClause(V_in,"V");            
        }else if(m2.find()){
            Scanner in = new Scanner(m2.group(6));
            OFilterClause = FilterClause(in,"N");

            Scanner V_in = new Scanner(m2.group(4));
            VFilterClause = FilterClause(V_in,"V");            
        }else if(m3.find()){
            Scanner in = new Scanner(m3.group(4));
            OFilterClause = FilterClause(in,"N");
            
            Scanner V_in = new Scanner(m3.group(2));
            VFilterClause = FilterClause(V_in,"V");
        }else if(m4.find()){
            Scanner in = new Scanner(m4.group(4));
            OFilterClause = FilterClause(in,"N");
            Scanner V_in = new Scanner(m4.group(6));
            VFilterClause = FilterClause(V_in,"V");
        }else if(m5.find()){
            Scanner V_in = new Scanner(m5.group(5));
            VFilterClause = FilterClause(V_in,"V");
            Scanner in = new Scanner(m5.group(6));
            OFilterClause = FilterClause(in,"N");
        }
        if("when".equals(qtype))
            C = "FILTER (regex(str(?c),'TMP')) ";
        else if("where".equals(qtype))
            C = "FILTER (regex(str(?c),'LOC')) ";
        else if("who".equals(qtype))
            C = "FILTER (regex(str(?c),'PERSON')) ";
        String queryString = "";
        if(q_part==2){
            String s1 = OFilterClause == " " ? "" : "FILTER ("+OFilterClause+") ";
            String s2 = VFilterClause == " " ? "" : "FILTER ("+VFilterClause+") } ";
            queryString = "SELECT ?q ?r ?c ?N WHERE { "
                + "?q ?e ?N ."
                + "?q ?w ?V ."
                + "?q ?c ?r "
                + s1
                + "FILTER (regex(str(?w),'V')) "
                + C    
                + s2;
        }else{
            String s1 = OFilterClause ==" " ? "" : "FILTER ("+OFilterClause+") ";
            String s2 = OFilterClause_2 ==" " ? "" : "FILTER ("+OFilterClause_2+") ";
            String s3 = VFilterClause ==" " ? "" : "FILTER ("+VFilterClause+") } ";
            
            queryString = "SELECT ?q ?r ?c WHERE { "
                + "?q ?e ?N ."
                + "?q ?w ?V ."
                + "?q ?c ?r "
                + s1
                + s2
                + "FILTER (regex(str(?w),'V')) "
                + C    
                + s3;
        }        
        FileManager.get().addLocatorClassLoader(sample.class.getClassLoader());
        Model model = FileManager.get().loadModel("models/triples.nt");
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        
        double max = 0;
        double Sim = 0;
        String ans = "";
        String ans_s = "";
        String ans_n = "";
        Ngram ngram = new Ngram();

        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("r");
                Literal N = soln.getLiteral("N");
                if (N == null){
                    ans_n = name.toString();
                }else
                    Sim = (ngram.getSimilarity(N.toString(),Ques, 1)+ LevenshteinDistance.similarity(Ques, N.toString())*5)/2;
                System.out.println(name+"--"+Sim);
                Resource Q = soln.getResource("q");
                if(max<Sim){
                    max = Sim;
                    ans = name.toString();
                    ans_s = Q.toString();
                }
            }
			System.out.println(ans);
            System.out.println(ans_s);
            System.out.println(max);
        } finally {
            qexec.close();
        }
    }
}