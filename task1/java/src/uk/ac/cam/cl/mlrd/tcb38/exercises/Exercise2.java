package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class Exercise2 implements IExercise2{
    @Override
    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {
        return null;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        return null;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        return null;
    }

    @Override
    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {
        return null;
    }
}
