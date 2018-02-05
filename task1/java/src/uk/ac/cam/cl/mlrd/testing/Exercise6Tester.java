package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TODO: Replace with your package.
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise1;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise2;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise6;
import uk.ac.cam.cl.mlrd.utils.DataSplit3Way;

public class Exercise6Tester {

	static final Path dataDirectory = Paths.get("data/nuanced_sentiment_dataset");

    public static List<Map<Path, NuancedSentiment>> splitCVStratifiedRandom(Map<Path, NuancedSentiment> dataSet, int seed) {
        List<Map<Path, NuancedSentiment>> folds = new ArrayList<>();
        List<Path> randomPositive = new ArrayList<>();
        List<Path> randomNegative = new ArrayList<>();
        List<Path> randomNeutral = new ArrayList<>();
        Random rand = new Random(seed);

        //Perform a split into positive / negative
        for(Path p : dataSet.keySet()){
            if(dataSet.get(p).equals(NuancedSentiment.POSITIVE)){
                randomPositive.add(p);
            }else if(dataSet.get(p).equals(NuancedSentiment.NEGATIVE)){
                randomNegative.add(p);
            }else{
                randomNeutral.add(p);
            }
        }

        Collections.shuffle(randomPositive, rand);
        Collections.shuffle(randomNegative, rand);
        Collections.shuffle(randomNeutral, rand);

        int min = Math.min(randomPositive.size(), randomNegative.size());
        for(int i = 0; i < min; i++){
            Path pp = randomPositive.get(i);
            Path pn = randomNegative.get(i);
            Path pnt = randomNeutral.get(i);

            if(i < 10){
                //Initialise List
                Map<Path, NuancedSentiment> h = new HashMap<>();
                h.put(pp, NuancedSentiment.POSITIVE);
                h.put(pn, NuancedSentiment.NEGATIVE);
                h.put(pnt, NuancedSentiment.NEUTRAL);

                folds.add(h);
            }else{
                //Add to list
                folds.get(i % 10).put(pp, NuancedSentiment.POSITIVE);
                folds.get(i % 10).put(pn, NuancedSentiment.NEGATIVE);
                folds.get(i % 10).put(pnt, NuancedSentiment.NEUTRAL);
            }
        }

        return folds;
    }

    public static double[] crossValidate(List<Map<Path, NuancedSentiment>> folds, IExercise6 naiveBayes) throws IOException {
        double[] scores = new double[10];

        for(int i = 0; i < scores.length; i++){
            Map<Path, NuancedSentiment> dataSet = new HashMap<>();
            Map<Path, NuancedSentiment> testSet = new HashMap<>();

            //Add all except those from i to testSet
            for(int j = 0; j < scores.length; j++){
                if(i != j){
                    dataSet.putAll(folds.get(j));
                }else{
                    testSet = folds.get(j);
                }
            }

            //Perform naivebayes
            Map<String, Map<NuancedSentiment, Double>> probs = naiveBayes.calculateNuancedLogProbs(dataSet);
            Map<NuancedSentiment, Double> classProbs = naiveBayes.calculateClassProbabilities(dataSet);
            Map<Path, NuancedSentiment> testPredictions = naiveBayes.nuancedClassifier(testSet.keySet(), probs, classProbs);

            //Calculate accuracy and store in scores array
            scores[i] = naiveBayes.nuancedAccuracy(testSet, testPredictions);
        }

        return scores;
    }
	public static void main(String[] args) throws IOException {

		Path sentimentFile = dataDirectory.resolve("review_sentiment");
		Map<Path, NuancedSentiment> dataSet = DataPreparation6.loadNuancedDataset(dataDirectory.resolve("reviews"),
				sentimentFile);
		DataSplit3Way<NuancedSentiment> split = new DataSplit3Way<>(dataSet, 0);

		IExercise6 implementation = (IExercise6) new Exercise6();

		Map<NuancedSentiment, Double> classProbabilities = implementation.calculateClassProbabilities(split.trainingSet);
		Map<String, Map<NuancedSentiment, Double>> logProbs = implementation.calculateNuancedLogProbs(split.trainingSet);
		Map<Path, NuancedSentiment> predictions = implementation.nuancedClassifier(split.validationSet.keySet(), logProbs, classProbabilities);
		System.out.println("Multiclass predictions:");
		System.out.println(predictions);
		System.out.println();

		double accuracy = implementation.nuancedAccuracy(split.validationSet, predictions);
		System.out.println("Multiclass prediction accuracy:");
		System.out.println(accuracy);
		System.out.println();

        //10-fold cross validation
        List<Map<Path, NuancedSentiment>> folds = splitCVStratifiedRandom(dataSet, 0);
        double[] scores = crossValidate(folds, implementation);
        double cvAccuracy = Arrays.stream(scores).sum() / scores.length;
        double cvVariance = Arrays.stream(scores).map(s -> (s - cvAccuracy)*(s - cvAccuracy)).sum() / scores.length;

        System.out.println("Cross validation accuracy:");
        System.out.println(cvAccuracy);
        System.out.println("Cross validation variance:");
        System.out.println(cvVariance);
        System.out.println();

		
		Path classPredictionsFile = Paths.get("data/class_predictions.csv");
		List<Map<Integer, Sentiment>> classPredictions = DataPreparation6.loadClassPredictions(classPredictionsFile);
		
		Map<Integer, Map<Sentiment, Integer>> agreementTable = implementation.agreementTable(classPredictions);
		System.out.println("Agreement table:");
		System.out.println(agreementTable);
		System.out.println();

		double kappaAll = implementation.kappa(agreementTable);
		System.out.println("Overall kappa value:");
		System.out.println(kappaAll);
		System.out.println();

		Map<Integer, Map<Sentiment, Integer>> table12 = new HashMap<Integer, Map<Sentiment, Integer>>();
		table12.put(1, agreementTable.get(1));
		table12.put(2, agreementTable.get(2));
		double kappa12 = implementation.kappa(table12);
		System.out.println("Kappa value for reviews 1 and 2:");
		System.out.println(kappa12);
		System.out.println();

		Map<Integer, Map<Sentiment, Integer>> table34 = new HashMap<Integer, Map<Sentiment, Integer>>();
		table34.put(3, agreementTable.get(3));
		table34.put(4, agreementTable.get(4));
		double kappa34 = implementation.kappa(table34);
		System.out.println("Kappa value for reviews 3 and 4:");
		System.out.println(kappa34);
		System.out.println();
	}
}
