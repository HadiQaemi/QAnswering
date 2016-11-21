package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import qclassifier.Question;
import qclassifier.QuestionClassifier;
import qclassifier.QuestionClassifier.ClassCategory;
import qclassifier.features.FeatureBuilder;

public class FeatureAnalysis 
{
	protected QuestionClassifier _qc;
	
	public FeatureAnalysis(QuestionClassifier testedClassifier)
	{
		_qc = testedClassifier;
	}
	
	public void writeFeatures(String outputFile) throws Exception
	{
		System.out.println("Writing features...");
		
		BufferedReader testFileReader = new BufferedReader(new FileReader(_qc.getTestFile()));
		BufferedReader testOutputReader = new BufferedReader(new FileReader(_qc.getTestOutputFile()));
		BufferedWriter analyzeWriter = new BufferedWriter(new FileWriter(outputFile));
		
		writeHeader(analyzeWriter);
		
		String line;
		while((line = testFileReader.readLine()) != null)
		{
			String[] parts = line.trim().split(" ", 2);
			String actaulLabel = (_qc.getClassCategory() == ClassCategory.Coarse) ? parts[0].split(":")[0] : parts[0];
			String question = parts[1];
			String predictedLabel = _qc.getLabelIds().getKeyByValue(Double.valueOf(testOutputReader.readLine()).intValue());
			
			String result = actaulLabel.equals(predictedLabel) ? "1" : "0";
			result += "\t" + predictedLabel + "\t" + actaulLabel + "\t" + question;
			
			Question q = new Question(question);
			for (FeatureBuilder builder : _qc.getFeatureBuilders())
				result += "\t" + buildFeatures(builder, q);
			
			analyzeWriter.write(result + "\n");
		}
		
		analyzeWriter.close();
	}
	
	private String buildFeatures(FeatureBuilder builder, Question question) throws IOException, ClassNotFoundException
	{
		builder.addFeatures(question);

		String result = "";
		
		for (Entry<String, Double> feature : question.getFeatures().entrySet())
			result += "(" + feature.getKey() + ", " + doubleToStr(feature.getValue()) + ") ";
		
		question.removeFeatures();
	
		return result;
	}
	
	private void writeHeader(BufferedWriter writer) throws IOException
	{
		String header = "isCorrect\tPredicted Label\tActual Label\tQuestion";
		
		for(FeatureBuilder builder : _qc.getFeatureBuilders())
			header += "\t" + builder.getClass().getSimpleName();
		
		writer.write(header + "\n");
	}
	
	private String doubleToStr(double value)
	{
		DecimalFormat dFormat = new DecimalFormat("#.##");
		return dFormat.format(value);	
	}
	
	/**
	 * Sort a map based on its value in descending order
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return
	 */
	private <K,V extends Comparable<? super V>>	SortedSet<Map.Entry<K,V>> sortedByValues(Map<K,V> map) 
	{
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() 
	        {
	            @Override 
	            public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) 
	            {
	                int res = e1.getValue().compareTo(e2.getValue());
	                /* Descending sort*/
	                res *= -1;	
	                return res != 0 ? res : 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
}
