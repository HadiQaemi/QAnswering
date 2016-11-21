package qclassifier;


import edu.stanford.nlp.ling.TaggedWord;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegEx {

	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		
                //List<TaggedWord> tagQuestions = PosTagger.tagQuestion("What was the name of the first Russian astronaut to do a spacewalk?");
                
		//System.out.println("collines: " + tagQuestions.toString());
                BufferedReader br = null;
                String sCurrentLine;
                String question_taged = "";
                List<TaggedWord> sss = PosTagger.tagQuestion("Loading default properties from trained tagger.");
                System.out.println(((TaggedWord)sss.get(2) ).tag());
                
                
//                Pattern p = Pattern.compile("\\[(\\w{2}) (((\\w)+ ?)*)*\\]");
                Pattern p1 = Pattern.compile( "^((\\[[a-zA-Z&&[^vV]]\\w+ [a-zA-Z ]+\\] ?)+)"
                        + "((\\[[vV]\\w+ [a-zA-Z ]+\\] ?)+)"
                        + "((\\[[a-zA-Z&&[^vV]]+ [a-zA-Z ]+\\] ?)+)"
                        + "((\\[[vV]\\w+ [a-zA-Z ]+\\] ?)+)"
                        + "(\\[[a-zA-Z&&[^vV]]+ [a-zA-Z ]+\\] ?)*$");
                Pattern p2 = Pattern.compile( "^((\\[[a-zA-Z&&[^vV]]\\w+ [a-zA-Z ]+\\] ?)+)"
                        + "((\\[[vV]\\w+ [a-zA-Z ]+\\] ?)+)"
                        + "((\\[[a-zA-Z&&[^vV]]+ [a-zA-Z ]+\\] ?)+)$");
//                        + "(\\[[a-zA-Z&&[^vV]]\\w ((\\w+ ?)*)\\] ?)*");
                //Matcher m = p.matcher("[NP What] [VP was] [NP the nationality] [PP of] [NP Jackson Pollock]");   
                //String st = "[NP What fsdf] [NP Whatt fsdft] [VP is] [VP istwo] [NP the primary language] [PP of] [NP the Philippines]";
                String st = "[ADVP When] [VP was] [NP the first flush toilet] [VP invented]";
                Matcher m1 = p1.matcher(st);
                Matcher m2 = p2.matcher(st);
                
                if(m1.find()){
                    int start = 0;
                    while (m1.find(start)) {
                        for (int group = 0; group <= m1.groupCount(); ++group) {
                            System.out.println(group+ "=" + m1.group(group));
                        }
                        System.out.println(m1.group(5)+" "+ m1.group(7));
                        start = m1.toMatchResult().end(1);
                    }
                    
                    System.out.println("m1 is ok");
                }
                if(m2.find()){
                    int start = 0;
                    while (m2.find(start)) {
                        for (int group = 0; group <= m2.groupCount(); ++group) {
                            System.out.println(group+ "=" + m2.group(group));
                        }
                        start = m2.toMatchResult().end(1);
                        //System.out.println("whq =" + m2.group("whq"));
                    }
                    System.out.println("m2 is ok");
                    
                }
                
	}

}
