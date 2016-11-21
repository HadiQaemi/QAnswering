package qclassifier;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import libsvm.*;
import qclassifier.features.*;

public class QuestionClassifier 
{
	private List<FeatureBuilder> _featureBuilders;
	private boolean _isTrained;
	private boolean _isTested;
	private svm_model _svmModel;
	private StringIds _featureIds;
	private StringIds _labelIds;
	private String _testFile;
	private String _testOutputFile;
	private ClassCategory _classCategory;
	
	public QuestionClassifier(ClassCategory classCategory)
	{
		_featureBuilders = new ArrayList<FeatureBuilder>();
		_featureIds = new StringIds();
		_labelIds = new StringIds();
		_isTrained = false;
		_classCategory = classCategory;
	}
	
	public void addFeatureBuilder(FeatureBuilder builder)
	{
		_featureBuilders.add(builder);
	}
	
	public List<FeatureBuilder> getFeatureBuilders()
	{
		return _featureBuilders;
	}
	
	public StringIds getFeatureIds()
	{
		return _featureIds;
	}
	
	public StringIds getLabelIds()
	{
		return _labelIds;
	}
	
	public boolean isTested()
	{
		return _isTested;
	}
	
	public ClassCategory getClassCategory()
	{
		return _classCategory;
	}
	
	/**
	 * 
	 * Generate the feature vector in svmlib format in an String
	 */
	private String generateFeatures(String questionString) throws IOException, ClassNotFoundException
	{
		Question question = new Question(questionString);
		
		//Each builder add its own features to the question
		for (FeatureBuilder builder : _featureBuilders) 
			builder.addFeatures(question);

		//Used to map the features IDs to a double value
		TreeMap<Integer, Double> featureValues = new TreeMap<Integer, Double>();

		//The feature entries are translated to Ids and added to the tree with thier weight 
		for (Entry<String, Double> feature : question.getFeatures().entrySet()) 
			featureValues.put(_featureIds.toId(feature.getKey()), feature.getValue());
		
		String featureVector = "";
		
		//Read features from tree and create feature string
		for (Entry<Integer, Double> featId : featureValues.entrySet()) 
			featureVector += featId.getKey() + ":" + featId.getValue() + " ";
		
		return featureVector;
	}
	
	//Read data set (training or testing), generate features and save the result to a file
	private void createFeatureFile(String dataSetPath) throws IOException, ClassNotFoundException
	{
		String featureFile = dataSetPath + ".feat";
		
		BufferedReader reader = new BufferedReader(new FileReader(dataSetPath));
		BufferedWriter writer = new BufferedWriter(new FileWriter(featureFile));

		String line = reader.readLine();

		while (line != null) 
		{
			String[] parts = line.trim().split(" ", 2);
			if (parts.length > 1)
			{
				String label = (_classCategory == ClassCategory.Coarse) ? parts[0].split(":")[0] : parts[0];
				String question = parts[1];
				
				writer.append(_labelIds.toId(label) + " " + generateFeatures(question) + "\n");
			}
			
			line = reader.readLine();
		}
		
		reader.close();
		writer.close();
	}
	
	public void train(String trainSetPath) throws IOException, ClassNotFoundException
	{
		train(trainSetPath, 1, 0.1);
	}

	public void train(String trainSetPath, double C, double gamma) throws IOException, ClassNotFoundException
	{		
		String svmModelFile = trainSetPath + ".model";
		String featureFile = trainSetPath + ".feat";
		
		createFeatureFile(trainSetPath);
		
		String kernelType = "0";	//0 indicate linear kernel
		String[] trainParameters = {"-t", kernelType, "-c", String.valueOf(C) , featureFile, svmModelFile};
		
		svm_train trainer = new svm_train();
		trainer.run(trainParameters);
		
		_svmModel = svm.svm_load_model(svmModelFile);
		_isTrained = true;
	}
	
	public void trainWithFeatureFile(String reducedTrainFile, double C) throws IOException
	{
		String svmModelFile = reducedTrainFile + ".model";
		
		String kernelType = "0";	//0 indicate linear kernel
		String[] trainParameters = {"-t", kernelType, "-c", String.valueOf(C), reducedTrainFile, svmModelFile};
		
		svm_train trainer = new svm_train();
		trainer.run(trainParameters);
		
		_svmModel = svm.svm_load_model(svmModelFile);
		_isTrained = true;
		
	}
	
	public double testWithFeatureFile(String reducedTestFile) throws Exception
	{
		if (!_isTrained)
			throw new Exception("The classifier is not trained\n");  

		String testOutputFile = reducedTestFile + ".out";
		
		BufferedReader input = new BufferedReader(new FileReader(reducedTestFile));
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(testOutputFile)));
		
		double accuracy = svm_predict.predict(input, output, _svmModel, 0);
		_isTested = true;
		
		return accuracy;		
	}
	
	public double test(String testSetPath) throws Exception
	{
		if (!_isTrained)
			throw new Exception("The classifier is not trained\n");  

		String testOutputFile = testSetPath + ".out";
		String testFeatureFile = testSetPath + ".feat";

		_testFile = testSetPath;
		_testOutputFile = testOutputFile;
		
		createFeatureFile(testSetPath);
		
		BufferedReader input = new BufferedReader(new FileReader(testFeatureFile));
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(testOutputFile)));
		
		double accuracy = svm_predict.predict(input, output, _svmModel, 0);
		_isTested = true;
		
		return accuracy;		
	}
	
	/**
	 *  To save the model 3 files need to be saved: 
	 *    1) svm model file which is already saved if the classifier is trained
	 *    2) stringIds
	 *    3) labelIds
	 * 	This method save the last object in separate files 
	 */
	public void saveModel(String trainSetPath) throws Exception
	{
		if (!_isTrained)
			throw new Exception("The classifier is not trained\n");  
		
		String stringIdFile = trainSetPath + ".stringIds";
		String labelIdFile = trainSetPath + ".labelIds";
		
		BufferedWriter stringIdWriter = new BufferedWriter(new FileWriter(stringIdFile));
		BufferedWriter labelIdWriter = new BufferedWriter(new FileWriter(labelIdFile));
		
		for (Entry<String, Integer> item : _featureIds) 
		{
			stringIdWriter.append(item.getKey() + "\t" + item.getValue() + "\n");
		}
		
		for (Entry<String, Integer> item : _labelIds) 
		{
			labelIdWriter.append(item.getKey() + "\t" + item.getValue() + "\n");
		}
		
		stringIdWriter.close();
		labelIdWriter.close();
	}

	public void loadModel(String trainSetPath) throws Exception
	{
		if (_featureIds.getMap().size() > 0 || _labelIds.getMap().size() > 0)
			throw new Exception("There is already a model in classifier. Use unloadModel() method to unload previous model\n");
		
		String stringIdFile = trainSetPath + ".stringIds";
		String labelIdFile = trainSetPath + ".labelIds";
		String svmModelFile = trainSetPath + ".model";
		
		BufferedReader stringIdReader = new BufferedReader(new FileReader(stringIdFile));
		BufferedReader labelIdReader = new BufferedReader(new FileReader(labelIdFile));
			
		String line = stringIdReader.readLine();
		String[] parts;
		
		//Loading featureIds
		while (line != null)
		{
			parts = line.split("\t");
			_featureIds.put(parts[0], Integer.valueOf(parts[1]));

			line = stringIdReader.readLine();
		}
				
		//Loading labelIds
		line = labelIdReader.readLine();
		while (line != null)
		{
			parts = line.split("\t");
			_labelIds.put(parts[0], Integer.valueOf(parts[1]));
			
			line = labelIdReader.readLine();
		}
		
		stringIdReader.close();
		labelIdReader.close();

		//Loading svm model file
		_svmModel = svm.svm_load_model(svmModelFile);
		
		_isTrained = true;
	}
	
	public void unloadModel()
	{
		_featureBuilders = new ArrayList<FeatureBuilder>();
		_featureIds = new StringIds();
		_labelIds = new StringIds();
		_svmModel = null;
		_isTrained = false;
		_isTested = false;
	}
	
	public String predictLabel(String question) throws Exception
	{
		if (!_isTrained)
			throw new Exception("The classifier is not trained\n");  

		String featVector = generateFeatures(question);
		StringTokenizer st = new StringTokenizer(featVector," \t\n\r\f:");

		int featCount = st.countTokens() / 2;
		svm_node[] svmNode = new svm_node[featCount];

		for(int j = 0; j < featCount; j++)
		{
			svmNode[j] = new svm_node();
			svmNode[j].index = Integer.valueOf(st.nextToken());
			svmNode[j].value = Double.valueOf(st.nextToken());
		}
		
		int labelId = (int) svm.svm_predict(_svmModel, svmNode);
		
		return _labelIds.getKeyByValue(labelId);
	}
	
	public String getTestFile() throws Exception
	{
		if (!_isTested)
			throw new Exception("Classifier is not tested!");
		
		return _testFile;
	}
	
	public String getTestOutputFile() throws Exception
	{
		if (!_isTested)
			throw new Exception("Classifier is not tested!");

		return _testOutputFile;
	}
	
	public int getNbFeatures()
	{
		return _featureIds.getSize();
	}
	
	public enum ClassCategory
	{
		Coarse, Fine
	}
}


