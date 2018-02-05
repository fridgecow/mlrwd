package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise6 implements IExercise6 {
    @Override
    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<NuancedSentiment, Double> probabilities = new HashMap<>();

        double positive = 0;
        double neutral = 0;

        //For each piece of training data
        for(NuancedSentiment s : trainingSet.values()){
            if(s.equals(NuancedSentiment.POSITIVE)){
                positive += 1;
            }else if(s.equals(NuancedSentiment.NEUTRAL)){
                neutral += 1;
            }
        }

        probabilities.put(NuancedSentiment.POSITIVE, positive/trainingSet.size());
        probabilities.put(NuancedSentiment.NEUTRAL, neutral/trainingSet.size());
        probabilities.put(NuancedSentiment.NEGATIVE, 1 - (neutral + positive)/trainingSet.size());

        return probabilities;
    }

    @Override
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        return null;
    }

    @Override
    public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {
        return null;
    }

    @Override
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments, Map<Path, NuancedSentiment> predictedSentiments) {
        return 0;
    }

    @Override
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {
        return null;
    }

    @Override
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
        return 0;
    }
}
