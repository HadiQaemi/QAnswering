/*
 * Question Classification Library
 * Author: Babak Loni, email: babak.loni@gmail.com
 * Last Modification: 31/08/2011
 * 
 * Question Classification using Back-propagation Neural Network 
 */


package qclassifier.nnclassifier;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.util.TransferFunctionType;

import qclassifier.nnclassifier.QuestionSet.ClassCategory;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	static int nbFeatures = 400;
	static int nbClasses = 6;
	
	public static void main(String[] args) throws IOException 
	{
		System.out.println(trainAndTest(nbFeatures, "Test2/matrix24_train_400.csv", "Test2/matrix24_test_400.csv"));
		System.out.println("Finished!");
	}
	
	public static void findOptimalTransferFunction() throws IOException
	{
		TransferFunctionType[] kernels = new TransferFunctionType[]{TransferFunctionType.SIGMOID 
				};
		
		for (TransferFunctionType kernel : kernels)
		{
			for (int j = 0; j < 5; j++)
				System.out.println(kernel.toString() + "\t" + trainAndTest(nbFeatures, "test5/matrix20_train_100.csv", "test5/matrix20_test_100.csv", kernel));
		}

	}
	
	public static double trainAndTest(int nbFeatures, String trainFile, String testFile) throws IOException
	{
		return trainAndTest(nbFeatures, trainFile, testFile, TransferFunctionType.SIGMOID);
	}
	
	public static double trainAndTest(int nbFeatures, String trainFile, String testFile, TransferFunctionType kernel) throws IOException
	{
		QuestionSet qs;
		
		// For original feature space
		if (trainFile.equals(""))
			qs = new QuestionSet(nbFeatures, nbClasses);
		else  // For the reduced space
			qs = new QuestionSet(nbFeatures, nbClasses, ClassCategory.Coarse, trainFile, testFile);
		
		//System.out.println("Making dataset...");
		
		TrainingSet trainingSet = qs.getTrainingSet();
		TrainingSet testSet = qs.getTestSet();
		
        int maxIterations = 10;
        
        NeuralNetwork neuralNet = new MultiLayerPerceptron(nbFeatures, nbClasses, nbClasses);
        neuralNet.setLearningRule(new BackPropagation());
        
        ((LMS) neuralNet.getLearningRule()).setMaxError(0.01);//0-1
        ((LMS) neuralNet.getLearningRule()).setLearningRate(0.7);//0-1
        ((LMS) neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1

        //System.out.println("Learning...");
        neuralNet.learnInSameThread(trainingSet);
        
        int correctCount = 0;
        int totalCount = 0;
        
        //System.out.println("Testing...");
        
        String outputFile = "test/output.csv";
        BufferedWriter outputClasses = new BufferedWriter(new FileWriter(outputFile));
        outputClasses.write("Actual\tPredicted\tIsTrue\n");
        
        for (TrainingElement testElement : testSet.trainingElements()) {
            neuralNet.setInput(testElement.getInput());
            neuralNet.calculate();
            
            double[] networkOutput = neuralNet.getOutput();
            
            int actualLabel = getClassNo(((SupervisedTrainingElement) testElement).getDesiredOutput());
            int predictedLabel = getClassNo(networkOutput);
            
            boolean isCorrect = false;
            
            if (actualLabel == predictedLabel)
            {
            	isCorrect = true;
            	correctCount++;
            }
            
            outputClasses.write(String.valueOf(actualLabel) + "\t" + String.valueOf(predictedLabel) + "\t" + String.valueOf(isCorrect) + "\n");
            totalCount++;
        }
        
        outputClasses.close();
        return ((double) correctCount / totalCount);
	}
	
	public static int getClassNo(double[] t)
	{
		return (maxIndex(t) + 1);
	}
	
	public static void trainAndTestAll() throws IOException
	{
		String[] nbFeatures = new String[]{"50", "100", "150", "200", "250", "300", "350", "400", "450", "500"};
		
		String[] trainFiles = new String[]{"matrix4_train_50.csv", "matrix4_train_100.csv", "matrix4_train_150.csv", "matrix4_train_200.csv",
				"matrix4_train_250.csv", "matrix4_train_300.csv", "matrix4_train_350.csv", "matrix4_train_400.csv", "matrix4_train_450.csv",
				"matrix4_train_500.csv"};
		
		String[] testFiles = new String[]{"matrix4_test_50.csv", "matrix4_test_100.csv", "matrix4_test_150.csv", "matrix4_test_200.csv",
				"matrix4_test_250.csv", "matrix4_test_300.csv", "matrix4_test_350.csv", "matrix4_test_400.csv", "matrix4_test_450.csv",
				"matrix4_test_500.csv"};
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("h:\\desktop\\NNQC\\test4\\result1.txt"));
		writer.append("nbFeatures\tmean\tmax\n");
		
		System.out.println("nbFeatures\tAccuracy");
		for (int i = 0; i < trainFiles.length; i++)
		{
			double max = 0;
			double sum = 0, mean;
			for (int j = 0; j < 20; j++)
			{
				double accuracy = trainAndTest(Integer.valueOf(nbFeatures[i]) ,"test4/" + trainFiles[i], "test4/" + testFiles[i]);
				sum += accuracy;
				if (accuracy > max)
					max = accuracy;
				System.out.println(nbFeatures[i] + "\t" + accuracy);
			}
			mean = sum / 20;
			writer.append(nbFeatures[i] + "\t" + mean + "\t" + max + "\n");
		}
		writer.close();
	}
	
	
	public static void writeMatrixToFile(String fileName) throws IOException
	{
		QuestionSet.writeTrainingMatrix(fileName);
		QuestionSet.writeTestMatrix(fileName);
	}
	
	public static boolean isSameOutput(double[] actualOutput, double[] predictedOutput)
	{
		int actualMax = maxIndex(actualOutput);
		int predictedMax = maxIndex(predictedOutput);
		
		return (actualMax == predictedMax);
	}
	
	public static int maxIndex(double[] t) 
	{
	    double maximum = t[0];   
	    int maxIndex = 0;
	    
	    for (int i = 1; i < t.length; i++) 
	    {
	        if (t[i] > maximum) {
	            maximum = t[i];   
	            maxIndex = i;
	        }
	    }
	    return maxIndex;
	}
	
	public static void print(double[] a)
	{
		System.out.print("[");
		for (double item : a)
			System.out.print(item + ", ");
		
		System.out.print("]");
	}
	
}
