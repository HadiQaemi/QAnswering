package qclassifier;


import edu.stanford.nlp.ling.TaggedWord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.neuroph.util.FileUtils;

public class Test {

	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		String original_answers = "PATH"
		BufferedReader br = null;
		try {
			String sCurrentLine;
			String question_taged = "";
			br = new BufferedReader(new FileReader());
			while ((sCurrentLine = br.readLine()) != null) {
				String ss = sCurrentLine;
				Pattern p = Pattern.compile(".*Wh(.*)?.*");
				Matcher m = p.matcher(sCurrentLine);   
				if(m.find()){
					List<TaggedWord> tagQuestions = PosTagger.tagQuestion(m.group());
					question_taged +=m.group()+"\n";
				}
			}
			System.out.println("" + question_taged);
			FileUtils.writeStringToFile(new File(original_answers), question_taged);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

                
	}

}
