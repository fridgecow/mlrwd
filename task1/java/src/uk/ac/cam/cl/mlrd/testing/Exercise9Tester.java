package uk.ac.cam.cl.mlrd.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise5;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise9;
import uk.ac.cam.cl.mlrd.exercises.markov_models.AminoAcid;
import uk.ac.cam.cl.mlrd.exercises.markov_models.Feature;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise9;

public class Exercise9Tester {

	static final Path dataFile = Paths.get("data/bio_dataset.txt");

	public static List<List<HMMDataStore<AminoAcid, Feature>>> stratifiedRandomSplit(List<HMMDataStore<AminoAcid, Feature>> dataSet, int seed){
		List<List<HMMDataStore<AminoAcid, Feature>>> folds = new ArrayList<>();
		Random rand = new Random(seed);

		Collections.shuffle(dataSet, rand);

		int i = 0;
		for(HMMDataStore<AminoAcid, Feature> p: dataSet){
			if(i < 10){
				//Initialise List
				List<HMMDataStore<AminoAcid, Feature>> l = new ArrayList<>();
				l.add(p);
				folds.add(l);
			}else{
				//Add to list
				folds.get(i % 10).add(p);
			}
			i++;
		}

		return folds;
	}

	public static void main(String[] args) throws IOException {

		List<HMMDataStore<AminoAcid, Feature>> sequencePairs = HMMDataStore.loadBioFile(dataFile);

		List<List<HMMDataStore<AminoAcid, Feature>>> folds = stratifiedRandomSplit(sequencePairs, 0);

		IExercise9 implementation = new Exercise9();
		IExercise5 implementation5 = (IExercise5) new Exercise5();

		double[] precisions = new double[10];
		double[] recalls = new double[10];
		double[] fOneMeasure = new double[10];
		for(int i = 0; i < 10; i++){
			List<HMMDataStore<AminoAcid, Feature>> testSet = new ArrayList<>();
			List<HMMDataStore<AminoAcid, Feature>> trainingSet = new ArrayList<>();
			for(int j = 0; j < 10; j++){
				if(i != j){
					testSet.addAll(folds.get(j));
				}else{
					trainingSet.addAll(folds.get(j));
				}
			}

			HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(trainingSet);
			Map<List<Feature>, List<Feature>> true2PredictedMap = implementation.predictAll(model, testSet);

			precisions[i] = implementation.precision(true2PredictedMap);
			recalls[i] = implementation.recall(true2PredictedMap);
			fOneMeasure[i] = implementation.fOneMeasure(true2PredictedMap);
		}

		System.out.println("10-fold cross-validation\n");

		System.out.println("Precisions:");
		System.out.println(Arrays.toString(precisions));
		System.out.println("Mean: "+implementation5.cvAccuracy(precisions));
		System.out.println("Variance: "+implementation5.cvVariance(precisions));
		System.out.println("");

		System.out.println("Recalls:");
		System.out.println(Arrays.toString(recalls));
		System.out.println("Mean: "+implementation5.cvAccuracy(recalls));
		System.out.println("Variance: "+implementation5.cvVariance(recalls));
		System.out.println();

		System.out.println("fOneMeasure:");
		System.out.println(Arrays.toString(fOneMeasure));
		System.out.println("Mean: "+implementation5.cvAccuracy(fOneMeasure));
		System.out.println("Variance: "+implementation5.cvVariance(fOneMeasure));
	}
}
