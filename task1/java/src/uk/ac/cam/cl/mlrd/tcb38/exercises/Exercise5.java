package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise5 implements IExercise5 {
    @Override
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Map<Path, Sentiment>> folds = new ArrayList<>();
        Random rand = new Random(seed);

        List<Path> randomKeys = new ArrayList<>(dataSet.keySet());
        Collections.shuffle(randomKeys, rand);

        for(int i = 0; i < randomKeys.size(); i++){
            Path p = randomKeys.get(i);
            Sentiment s = dataSet.get(p);

            if(i < 10){
                //Initialise List
                Map<Path, Sentiment> h = new HashMap<>();
                h.put(p, s);

                folds.add(h);
            }else{
                //Add to list
                folds.get(i % 10).put(p, s);
            }
        }

        return folds;
    }

    @Override
    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Map<Path, Sentiment>> folds = new ArrayList<>();
        List<Path> randomPositive = new ArrayList<>();
        List<Path> randomNegative = new ArrayList<>();
        Random rand = new Random(seed);

        //Perform a split into positive / negative
        for(Path p : dataSet.keySet()){
            if(dataSet.get(p).equals(Sentiment.POSITIVE)){
                randomPositive.add(p);
            }else{
                randomNegative.add(p);
            }
        }

        Collections.shuffle(randomPositive, rand);
        Collections.shuffle(randomNegative, rand);

        int min = Math.min(randomPositive.size(), randomNegative.size());
        for(int i = 0; i < min; i++){
            Path pp = randomPositive.get(i);
            Path pn = randomNegative.get(i);

            if(i < 10){
                //Initialise List
                Map<Path, Sentiment> h = new HashMap<>();
                h.put(pp, Sentiment.POSITIVE);
                h.put(pn, Sentiment.NEGATIVE);

                folds.add(h);
            }else{
                //Add to list
                folds.get(i % 10).put(pp, Sentiment.POSITIVE);
                folds.get(i % 10).put(pn, Sentiment.NEGATIVE);
            }
        }

        return folds;
    }

    @Override
    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
        double[] scores = new double[10];
        IExercise2 naiveBayes = new Exercise2();

        for(int i = 0; i < scores.length; i++){
            Map<Path,Sentiment> dataSet = new HashMap<>();
            Map<Path, Sentiment> testSet = new HashMap<>();

            //Add all except those from i to testSet
            for(int j = 0; j < scores.length; j++){
                if(i != j){
                    dataSet.putAll(folds.get(j));
                }else{
                    testSet = folds.get(j);
                }
            }

            //Perform naivebayes
            Map<String, Map<Sentiment, Double>> probs = naiveBayes.calculateSmoothedLogProbs(dataSet);
            Map<Sentiment, Double> classProbs = naiveBayes.calculateClassProbabilities(dataSet);
            Map<Path, Sentiment> testPredictions = naiveBayes.naiveBayes(testSet.keySet(), probs, classProbs);

            //Calculate accuracy and store in scores array
            IExercise1 impl1 = new Exercise1();
            scores[i] = impl1.calculateAccuracy(testSet, testPredictions);
        }

        return scores;
    }

    @Override
    public double cvAccuracy(double[] scores) {
        double total = 0;
        for(double s : scores){
            total += s;
        }
        return total / scores.length;
    }

    @Override
    public double cvVariance(double[] scores) {
        double mu = cvAccuracy(scores);
        double n = scores.length;
        double var = 0;

        for(double s : scores){
            var += (s - mu)*(s - mu);
        }

        return var / n;
    }
}
