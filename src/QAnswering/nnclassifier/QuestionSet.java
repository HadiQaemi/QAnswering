package qclassifier.nnclassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;

public class QuestionSet 
{
	List<double[]> _trainigInput;
	List<double[]> _trainingOutput;
	List<double[]> _testInput;
	List<double[]> _testOutput;
	int _nbFeatures;
	int _nbClasses;
	ClassCategory _classCat;
	String _traningFile;
	String _testFile;
		
	public QuestionSet(int featureCount, int classCount) throws IOException
	{
		_trainigInput = new ArrayList<double[]>();
		_trainingOutput = new ArrayList<double[]>();
		_testInput = new ArrayList<double[]>();
		_testOutput = new ArrayList<double[]>();
		_nbFeatures = featureCount;
		_nbClasses = classCount;
		
		readTrainingset();
		readTestset();
	}

	public QuestionSet(int featureCount, int classCount, ClassCategory category, String trainingFile, String testFile) throws IOException
	{
		_trainigInput = new ArrayList<double[]>();
		_trainingOutput = new ArrayList<double[]>();
		_testInput = new ArrayList<double[]>();
		_testOutput = new ArrayList<double[]>();
		_nbFeatures = featureCount;
		_nbClasses = classCount;		
		_classCat = category;		
		_traningFile = trainingFile;
		_testFile = testFile;
		
		readReducedTrainingSet();
		readReducedTestSet();
	}
	
	public void readReducedTrainingSet() throws IOException
	{
		BufferedReader classReader;
		
		if (_classCat == ClassCategory.Coarse)
			classReader = new BufferedReader(new FileReader("test/train_5500.feat"));
		else
			classReader = new BufferedReader(new FileReader("test/train_5500.feat.fine"));
		
		BufferedReader featReader = new BufferedReader(new FileReader(_traningFile));
		
		String classLine = classReader.readLine();
		
		while (classLine != null) 
		{
			double[] questionOutput = new double[_nbClasses];

			String[] parts = classLine.trim().split(" ", 2);
			if (parts.length > 1)
			{
				int classNo = Integer.parseInt(parts[0]);
				questionOutput[classNo - 1] = 1;
			}
			_trainingOutput.add(questionOutput);
			
			classLine = classReader.readLine();
		}
		
		String featLine = featReader.readLine();

		while (featLine != null)
		{
			double[] questionInput = new double[_nbFeatures];		
			String[] features = featLine.split(",");
			
			for (int i = 0; i < _nbFeatures; i++)
			{
				questionInput[i] = Double.parseDouble(features[i]);
			}
			
			_trainigInput.add(questionInput);
			
			featLine = featReader.readLine();
		}
		
		classReader.close();
		featReader.close();

	}
	
	
	public void readReducedTestSet() throws IOException
	{
		BufferedReader classReader;
		
		if (_classCat == ClassCategory.Coarse)
			classReader = new BufferedReader(new FileReader("test/test_500.feat"));
		else
			classReader = new BufferedReader(new FileReader("test/test_500.feat.fine"));
		
		BufferedReader featReader = new BufferedReader(new FileReader(_testFile));
		
		String classLine = classReader.readLine();
		
		while (classLine != null) 
		{
			double[] questionOutput = new double[_nbClasses];

			String[] parts = classLine.trim().split(" ", 2);
			if (parts.length > 1)
			{
				int classNo = Integer.parseInt(parts[0]);
				questionOutput[classNo - 1] = 1;
			}
			_testOutput.add(questionOutput);
			
			classLine = classReader.readLine();
		}
		
		String featLine = featReader.readLine();

		while (featLine != null)
		{
			double[] questionInput = new double[_nbFeatures];		
			String[] features = featLine.split(",");
			
			for (int i = 0; i < _nbFeatures; i++)
			{
				questionInput[i] = Double.parseDouble(features[i]);
			}
			
			_testInput.add(questionInput);
			
			featLine = featReader.readLine();
		}
		
		classReader.close();
		featReader.close();

	}
	
	public void readTrainingset() throws IOException
	{
		BufferedReader trainReader = new BufferedReader(new FileReader("test/train_5500.feat"));

		String line = trainReader.readLine();
		
		while (line != null) 
		{
			double[] questionInput = new double[_nbFeatures];
			double[] questionOutput = new double[_nbClasses];

			String[] parts = line.trim().split(" ", 2);
			if (parts.length > 1)
			{
				int classNo = Integer.parseInt(parts[0]);
				questionOutput[classNo - 1] = 1;
				
				String[] features = parts[1].split(" ");
				
				for (String feature : features)
				{
					String[] featPair = feature.split(":");
					int featNo = Integer.parseInt(featPair[0]);
					questionInput[featNo - 1] = Double.parseDouble(featPair[1]);
				}
			}
			
			_trainigInput.add(questionInput);
			_trainingOutput.add(questionOutput);
			
			line = trainReader.readLine();
		}
		
		trainReader.close();
	}
	
	public void readTestset() throws IOException
	{
		BufferedReader testReader = new BufferedReader(new FileReader("test/test_500.feat"));
		
		String line = testReader.readLine();
		
		while (line != null) 
		{
			double[] questionInput = new double[_nbFeatures];
			double[] questionOutput = new double[_nbClasses];

			String[] parts = line.trim().split(" ", 2);
			if (parts.length > 1)
			{
				int classNo = Integer.parseInt(parts[0]);
				questionOutput[classNo - 1] = 1;
				
				String[] features = parts[1].split(" ");
				
				for (String feature : features)
				{
					String[] featPair = feature.split(":");
					int featNo = Integer.parseInt(featPair[0]);
					questionInput[featNo - 1] = Double.parseDouble(featPair[1]);
				}
			}
			
			_testInput.add(questionInput);
			_testOutput.add(questionOutput);
			
			line = testReader.readLine();
		}
		
		testReader.close();
	}

	public TrainingSet getTrainingSet()
	{
		TrainingSet ts = new TrainingSet();
		
		for (int i = 0; i < _trainigInput.size(); i++)
			ts.addElement(new SupervisedTrainingElement(_trainigInput.get(i), _trainingOutput.get(i)));
		
		return ts;
	}
	
	public TrainingSet getTestSet()
	{
		TrainingSet ts = new TrainingSet();
		
		for (int i = 0; i < _testInput.size(); i++)
			ts.addElement(new SupervisedTrainingElement(_testInput.get(i), _testOutput.get(i)));
		
		return ts;
	}
	
	public static void writeTrainingMatrix(String fileName) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("test/train_5500.feat"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("test/" + fileName + "_train.m"));
		
		List<Integer> rowIndeces = new ArrayList<Integer>();
		List<Integer> colIndeces = new ArrayList<Integer>();
		List<Double> values = new ArrayList<Double>();
		
		String line = reader.readLine();
		int questionIndex = 1;
		
		while(line != null)
		{
			String[] parts = line.trim().split(" ", 2);
			if (parts.length > 1)
			{
				String[] features = parts[1].split(" ");
				
				for (String feature : features)
				{
					String[] featPair = feature.split(":");
					int featNo = Integer.parseInt(featPair[0]);
					
					rowIndeces.add(featNo);
					colIndeces.add(questionIndex);
				values.add(Double.parseDouble(featPair[1]));
				}
			}
			
			questionIndex++;
			line = reader.readLine();
		}
		
		writer.append("i = [");
		
		for (int rowIndex : rowIndeces)
			writer.append(String.valueOf(rowIndex) + ";");

		writer.append("]\n");
		

		
		writer.append("j = [");
		
		for (int colIndex : colIndeces)
			writer.append(String.valueOf(colIndex) + ";");

		writer.append("]\n");

		
		writer.append("values = [");
		
		for (Double value : values)
			writer.append(String.valueOf(value) + ";");

		writer.append("]\n");

		writer.flush();
		writer.close();
	}

	
	public static void writeTestMatrix(String fileName) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("test/test_500.feat"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("test/" + fileName + "_test.m"));
		
		List<Integer> rowIndeces = new ArrayList<Integer>();
		List<Integer> colIndeces = new ArrayList<Integer>();
		List<Double> values = new ArrayList<Double>();
		
		String line = reader.readLine();
		int questionIndex = 1;
		
		while(line != null)
		{
			String[] parts = line.trim().split(" ", 2);
			if (parts.length > 1)
			{
				String[] features = parts[1].split(" ");
				
				for (String feature : features)
				{
					String[] featPair = feature.split(":");
					int featNo = Integer.parseInt(featPair[0]);
					
					rowIndeces.add(featNo);
					colIndeces.add(questionIndex);
				values.add(Double.parseDouble(featPair[1]));
				}
			}
			
			questionIndex++;
			line = reader.readLine();
		}
		
		writer.append("i = [");
		
		for (int rowIndex : rowIndeces)
			writer.append(String.valueOf(rowIndex) + ";");

		writer.append("]\n");
		

		
		writer.append("j = [");
		
		for (int colIndex : colIndeces)
			writer.append(String.valueOf(colIndex) + ";");

		writer.append("]\n");

		
		writer.append("values = [");
		
		for (Double value : values)
			writer.append(String.valueOf(value) + ";");

		writer.append("]\n");

		writer.flush();
		writer.close();
	}

	
	public enum ClassCategory
	{
		Coarse, Fine
	}
}
