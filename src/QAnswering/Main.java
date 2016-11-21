/*
 * Question Classification Library
 * Authors: Babak Loni, email: babak.loni@gmail.com
 * 	    Gijs van Tulder
 * Last Modification: 31/08/2011
 * 
 * Question Classification using Support Vector Classifier 
 */


package qclassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import analysis.FeatureAnalysis;

import classifier.NaiveClassifier;

import Wordnet.Disambiguator;
import Wordnet.HypernymsTree;
import Wordnet.WordSimilarity;

import qclassifier.QuestionClassifier.ClassCategory;
import qclassifier.features.*;
import qclassifier.features.QueryExpantion.Expansion;

import edu.mit.jwi.item.IWord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;

public class Main 
{
	static QuestionClassifier qc;

	public static void main(String[] args) throws Exception 
	{
		trainAndTest(ClassCategory.Coarse);
		System.out.println("Finished!");
	}
	
	private static void printSilvaHead(String question) throws IOException, ClassNotFoundException
	{
		Question q = new Question(question);
		
		SilvaHeadwords sh = new SilvaHeadwords();
		sh.findHeadword(q);
	}
	
	private static void testAndWriteFeatures(String outputFile, ClassCategory category) throws Exception
	{
		trainAndTest(category);
		
		FeatureAnalysis fa = new FeatureAnalysis(qc);
		fa.writeFeatures(outputFile);
	}
	
	private static void testHypernyms(String question) throws IOException, ClassNotFoundException
	{
		Question q = new Question(question);
		Disambiguator d = new Disambiguator(q.getTaggedQuestion());
		HypernymsTree ht = HypernymsTree.getInstance();
		
		List<IWord> wordList = d.getDisambiguatedSentence();
		
		for (IWord w : wordList)
		{
			System.out.println(w.getLemma() + " height: " + ht.getWordHeight(w));
			
			for (IWord h : ht.getHypernyms(w))
			{
				System.out.println(h.getLemma());
			}
			System.out.println();
		}		
	}
	
	private static void printHypernyms(String contextQuestion) throws IOException, ClassNotFoundException
	{
		Question q = new Question(contextQuestion);
		Disambiguator d = new Disambiguator(q.getTaggedQuestion());
		HypernymsTree ht = HypernymsTree.getInstance();
		
		List<IWord> wordList = d.getDisambiguatedSentence();
		
		for (IWord w : wordList)
		{
			System.out.println(w.getLemma() + " height: " + ht.getWordHeight(w));
			ht.printHypernyms(w);
			System.out.println();
		}
	}
	
	private static void printHyponyms(String contextQuestion) throws IOException, ClassNotFoundException
	{
		Question q = new Question(contextQuestion);
		Disambiguator d = new Disambiguator(q.getTaggedQuestion());
		HypernymsTree ht = HypernymsTree.getInstance();
		WordSimilarity ws = new WordSimilarity();
		
		List<IWord> wordList = d.getDisambiguatedSentence();
		
		for (IWord w : wordList)
		{
			System.out.println(w.getID().toString());
			ws.printHyponyms(w);
			System.out.println();
		}
	}
	
	private static void printBerekelyHeadWord(String question) throws IOException, ClassNotFoundException
	{
		BerkeleyHeadWord.getHeadWord(question);
	}
	
	private static void printHeadWord(String question) throws IOException, ClassNotFoundException
	{
		Question q = new Question(question);
		
		HeadFinder hf = new SemanticHeadFinder();
		Tree parseTree = q.getParseTree();
		
		// bring head node label to top of tree, retrieve
		parseTree.percolateHeads(hf);
		CoreLabel headLabel = (CoreLabel)parseTree.label();
		
		TaggedWord candidate = new TaggedWord(headLabel.word(), headLabel.tag());
		if (candidate.tag().startsWith("NN")) {
			System.out.println("1 " + candidate.toString());
			return;
		}
		
		// return the first word whose tag starts with NN
		List<TaggedWord> sentence = q.getTaggedQuestion();
		for (int i = 0; i < sentence.size(); i++) 
		{
			if (sentence.get(i).tag().startsWith("NN")) {
				System.out.println("2 " + sentence.get(i).toString());
				return;
			}
		}
	}
	
	private static void naiveTest() throws IOException
	{
		NaiveClassifier nc = new NaiveClassifier("Test/train_5500.feat");
		double accuracy = nc.test("Test/test_500.feat");
		System.out.println("accuracy: " + accuracy);
	}
	
	private static double trainAndTest(ClassCategory category) throws Exception 
	{
		return trainAndTest(category, 1, 0.1);
	}

	private static double trainAndTest(ClassCategory category, double slack) throws Exception 
	{
		return trainAndTest(category, slack, 0.1);
	}
	
	private static double trainAndTest(ClassCategory category, double slack, double gamma) throws Exception 
	{
		qc = new QuestionClassifier(category);
		
		addFeatureBuilders(category);
		
		//System.out.println("Training...");
		qc.train("Test/train_5500", slack, gamma);
		//qc.saveModel("Test/train_5500");
		//qc.loadModel("Test/train_5500");
		
		double accuracy = qc.test("Test/test_500");
		System.out.println(accuracy);
		System.out.println("nbFeatures: " + qc.getNbFeatures());
		
		return accuracy;
	}
	
	private static void findOptinalGamma() throws Exception
	{
		//double[] gammaArray = new double[]{0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.2};
		double[] gammaArray = new double[]{0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		//double[] gammaArray = new double[]{1.2, 1.5, 2, 3};
		
		for (double gamma : gammaArray)
		{
			System.out.println(gamma + "\t" + trainAndTest(ClassCategory.Coarse, 1, gamma));
		}
	}
	
	private static void findOptinalSlack() throws Exception
	{
		double[] slackArray = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 1.2, 1.5, 2};
		
		for (double slack : slackArray)
		{
			System.out.println(slack + "\t" + trainAndTest(ClassCategory.Coarse, slack));
		}
	}
	
	private static void createClassFile(String featureFile, String classFile) throws IOException
	{
		BufferedReader classReader = new BufferedReader(new FileReader(featureFile));
		BufferedWriter classWriter = new BufferedWriter(new FileWriter(classFile));
		
		String line = classReader.readLine();
		
		while (line != null)
		{
			String classNo = line.split(" ")[0];
			classWriter.append(classNo + "\n");
			
			line = classReader.readLine();
		}
		
		classReader.close();
		classWriter.close();
	}
	
	public static void createClassFiles(String trainFile, String testFile, ClassCategory category) throws IOException
	{
		if (category == ClassCategory.Coarse)
		{
			createClassFile(trainFile, "Test/train.coarse");
			createClassFile(testFile, "Test/test.coarse");
		}
		else 
		{
			createClassFile(trainFile, "Test/train.fine");
			createClassFile(testFile, "Test/test.fine");			
		}
	}
	
	private static void trainAndTestAll(String sourceTrainFile, String sourceTestFile, ClassCategory category) throws Exception
	{
		createClassFiles(sourceTrainFile, sourceTestFile, category);
		
		String[] nbFeatures = new String[]{"50", "100", "150", "200", "250", "300", "350", "400"};
		
		String[] trainFiles = new String[]{"matrix39_train_50.csv", "matrix39_train_100.csv", "matrix39_train_150.csv", "matrix39_train_200.csv",
				"matrix39_train_250.csv", "matrix39_train_300.csv", "matrix39_train_350.csv", "matrix39_train_400.csv"};
		
		String[] testFiles = new String[]{"matrix39_test_50.csv", "matrix39_test_100.csv", "matrix39_test_150.csv", "matrix39_test_200.csv",
				"matrix39_test_250.csv", "matrix39_test_300.csv", "matrix39_test_350.csv", "matrix39_test_400.csv"};
		
		System.out.println("nbFeatures\tAccuracy");
		for (int i = 0; i < trainFiles.length; i++)
		{
			System.out.println(nbFeatures[i] + "\t" + trainAndTestReduced("Test6/" + trainFiles[i], "Test6/" + testFiles[i], category));
		}
	}
	
	private static double trainAndTestReduced(String trainFile, String testFile, ClassCategory category) throws Exception
	{
		String trainFileClasses;
		String testFileClasses;
		
		if (category == ClassCategory.Coarse)
		{
			trainFileClasses = "Test/train.coarse";
			testFileClasses = "Test/test.coarse";
		} 
		else
		{
			trainFileClasses = "Test/train.fine";
			testFileClasses = "Test/test.fine";
		}
		
		String svmTrainFile = trainFile + ".svm";
		String svmTestFile = testFile + ".svm";
		
		csvToSvm(trainFile, trainFileClasses, svmTrainFile);
		csvToSvm(testFile, testFileClasses, svmTestFile);
		
		qc = new QuestionClassifier(category);
		qc.trainWithFeatureFile(svmTrainFile, 1);
		
		return qc.testWithFeatureFile(svmTestFile);
	}
	
	private static void printTree(String question) throws IOException, ClassNotFoundException
	{
		Question q = new Question(question);
		Tree parsTree = q.getParseTree();
		
		//HeadFinder hf = new SemanticHeadFinder();
		
		TreePrint tp = new TreePrint("penn");
		
		//tp.setHeadFinder(hf);
		tp.printTree(parsTree);		
	}
	
	
	private static void crossValidation(ClassCategory category) throws Exception
	{
		createFolds("test/train_5500", "test/test_500");
		File foldsDir = new File("crossvalidation");

		String[] folds = foldsDir.list();
		
		for (int i = 0; i < folds.length; i++)
		{
			folds[i] = "crossvalidation/" + folds[i];
		}
		
		String testFile, trainFile;
		String[] trainingFolds = new String[folds.length - 1];
		
		System.out.println("Fold\tAccuracy");
		for (int i = 0; i < folds.length; i++)
		{
			testFile = folds[i];
			
			int k = 0;
			for (int j = 0; j < folds.length; j++)
			{
				if (j != i)
					trainingFolds[k++] = folds[j]; 
			}
			
			trainFile = "crossvalidation/training" + String.valueOf(i + 1);
			mergFiles(trainingFolds, trainFile);
			
			qc = new QuestionClassifier(category);
			addFeatureBuilders(category);

			qc.train(trainFile);
			System.out.println(String.valueOf(i + 1) + "\t" + qc.test(testFile));
		}
		
	}
	
	private static void mergFiles(String[] files, String resultFile) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
		
		for (int i = 0; i < files.length; i++)
		{
			BufferedReader reader = new BufferedReader(new FileReader(files[i]));
			
			String line = reader.readLine();
			
			while (line != null)
			{
				writer.write(line + "\n");
				line = reader.readLine();
			}
			
			reader.close();
		}
		
		writer.close();
	}
	
	private static void createFolds(String trainFile, String testFile) throws IOException
	{
		BufferedReader trainReader = new BufferedReader(new FileReader(trainFile));
		BufferedReader testReader = new BufferedReader(new FileReader(testFile));
		
		List<String> allSamples = new ArrayList<String>();
		List<String> folds = new ArrayList<String>();
		
		int nbSamples = 0;
		String sample = trainReader.readLine();
		
		while (sample != null)
		{
			allSamples.add(sample);
			sample = trainReader.readLine();
			nbSamples++;
		}
		
		sample = testReader.readLine();
		
		while (sample != null)
		{
			allSamples.add(sample);
			sample = testReader.readLine();
			nbSamples++;
		}
		
		int maxFoldSamples = 1000;
		int foldNo = 1;
		
		Random r = new Random(System.currentTimeMillis());
		
		while (nbSamples != 0 && allSamples.size() != 0)
		{
			BufferedWriter foldWriter = new BufferedWriter(new FileWriter("Crossvalidation/fold." + String.valueOf(foldNo)));
			
			for (int i = 0; i < maxFoldSamples; i++)
			{
				if (nbSamples == 0 || allSamples.size() == 0)
					break;
				
				int rand = (int) (r.nextDouble() * nbSamples);
				foldWriter.write(allSamples.remove(rand) + "\n");
				nbSamples--;
			}
			
			foldNo++;
			foldWriter.close();
		}
		
	}
	
	private static void addFeatureBuilders(ClassCategory category) throws IOException
	{
		qc.addFeatureBuilder(new Unigram(1));
		//qc.addFeatureBuilder(new WordShape(1));
		//qc.addFeatureBuilder(new Bigram(1, true));
		//qc.addFeatureBuilder(new WhWord(1));
		//qc.addFeatureBuilder(new SilvaHeadwords(1, "Test/trainAndTest.shead", true));
		//qc.addFeatureBuilder(new RelatedWordsGroup(1, Expansion.Sentence, 2));		
		//qc.addFeatureBuilder(new HeadRules(1));
		//qc.addFeatureBuilder(new HeadWord(1, "Test/trainAndTest.head", true));
		//qc.addFeatureBuilder(new WPosTag(1));
		//qc.addFeatureBuilder(new TaggedUnigrams(1));
		//qc.addFeatureBuilder(new CollinsHeadWord(1));
		//qc.addFeatureBuilder(new SemanticHeadWord(1));   
		//qc.addFeatureBuilder(new Hypernyms(1, Expansion.HeadWord));
		//qc.addFeatureBuilder(new WordsHeight(1));
		//qc.addFeatureBuilder(new QueryExpantion(1, 0.6, Expansion.HeadWord));
		//qc.addFeatureBuilder(new QueryExpansion2(1, 0.6, Expansion.HeadWord));
		//qc.addFeatureBuilder(new StemmedUnigram(1));
		//qc.addFeatureBuilder(new QuestionCategory(1, category));
	}
	
	
	private static void csvToSvm(String featureFile, String classFile, String svmFile) throws IOException
	{
		BufferedReader featReader = new BufferedReader(new FileReader(featureFile));
		BufferedReader classReader = new BufferedReader(new FileReader(classFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(svmFile));
		
		String featLine = featReader.readLine();
		String classLine = classReader.readLine();
		
		while (featLine != null)
		{
			String result = classLine; 

			String[] feats = featLine.trim().split(",");
			for (int j = 1; j <= feats.length; j++)
			{
				result += " " + String.valueOf(j) + ":" + feats[j - 1];
			}
			
			result += "\n";
			writer.append(result);

			featLine = featReader.readLine();
			classLine = classReader.readLine();
		}
		
		writer.close();		
	}
	
	private static void findOptimalC() throws Exception 
	{
		qc = new QuestionClassifier(ClassCategory.Fine);
		addFeatureBuilders(ClassCategory.Fine);
		
		double a = 0.1;
		double accuracy;
		for (double c = 0.1; c < 10; c += a)
		{
			qc.train("Test/train_5500", c, 0.1);
			accuracy = qc.test("Test/test_500");
			System.out.println(c + "\t" + accuracy);			
			if (c >= 1)
				a = 1;
		}
	}
	
	private static void findOptimalWeight() throws Exception
	{
		qc = new QuestionClassifier(ClassCategory.Fine);
		
		double accuracy;
		double[] weights = new double[]{0.2, 0.3, 0.4, 0.5, 0.6, 0.8, 1, 1.1, 1.3, 1.5, 2, 2.5, 3, 4, 5};
		
		for (double w : weights)
		{
			qc.addFeatureBuilder(new Unigram(1));
			//qc.addFeatureBuilder(new SilvaHeadwords(1, "Test/trainAndTest.shead", true));
			qc.addFeatureBuilder(new RelatedWordsGroup(w, Expansion.Sentence));
			//qc.addFeatureBuilder(new Bigram(w));
			//qc.addFeatureBuilder(new WordShape(w));
			//qc.addFeatureBuilder(new WhWord(w));
			//qc.addFeatureBuilder(new Hypernyms(w, Expansion.HeadWord));
			qc.train("Test/train_5500", 1, 0.1);
			
			accuracy = qc.test("Test/test_500");
			System.out.println(w + "\t" + accuracy);
			
			qc.unloadModel();
		}
	}

}
