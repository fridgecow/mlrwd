package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import jdk.internal.dynalink.linker.LinkerServices;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise5;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise7;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise8;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise8;

public class Exercise8Tester {

	static final Path dataDirectory = Paths.get("data/dice_dataset");

	public static List<List<Path>> stratifiedRandomSplit(List<Path> dataSet, int seed){
		List<List<Path>> folds = new ArrayList<>();
		Random rand = new Random(seed);

		Collections.shuffle(dataSet, rand);

		int i = 0;
		for(Path p: dataSet){
			if(i < 10){
				//Initialise List
				List<Path> l = new ArrayList<>();
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

	public static void main(String[] args)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		List<Path> sequenceFiles = new ArrayList<>();
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
			for (Path item : files) {
				sequenceFiles.add(item);
			}
		} catch (IOException e) {
			throw new IOException("Cant access the dataset.", e);
		}

		//10-fold Cross-Verification
        IExercise8 implementation = (IExercise8) new Exercise8();
        IExercise7 implementation7 = (IExercise7) new Exercise7();
        IExercise5 implementation5 = (IExercise5) new Exercise5();

        double[] precisions = new double[10];
        double[] recalls = new double[10];
        double[] fOneMeasure = new double[10];

        List<List<Path>> folds = stratifiedRandomSplit(sequenceFiles, 0);
		for(int i = 0; i < 10; i++){
            List<Path> testSet = new ArrayList<>();
            List<Path> trainingSet = new ArrayList<>();
		    for(int j = 0; j < 10; j++){
		        if(i != j){
                    testSet.addAll(folds.get(j));
                }else{
                    trainingSet.addAll(folds.get(j));
                }
            }

            HiddenMarkovModel<DiceRoll, DiceType> model = implementation7.estimateHMM(trainingSet);
            Map<List<DiceType>, List<DiceType>> true2PredictedMap = implementation.predictAll(model, testSet);

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
