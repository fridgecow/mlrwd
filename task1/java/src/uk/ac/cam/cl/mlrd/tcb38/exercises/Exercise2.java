package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Exercise2 implements IExercise2{
    @Override
    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<Sentiment, Double> probabilities = new HashMap<>();
        double positive = 0;

        //For each piece of training data
        for(Sentiment s : trainingSet.values()){
            if(s.equals(Sentiment.POSITIVE)){
                positive += 1;
            }
        }

        probabilities.put(Sentiment.POSITIVE, positive/trainingSet.size());
        probabilities.put(Sentiment.NEGATIVE, 1 - positive/trainingSet.size());
        return probabilities;
    }

    private Map<String, Map<Sentiment, Double>> calculateWordCounts(Map<Path, Sentiment> trainingSet) throws IOException{
        Map<String, Map<Sentiment, Double>> wordCounts = new HashMap<>();

        //For each piece of training data
        for(Path review : trainingSet.keySet()){
            Sentiment sentiment = trainingSet.get(review);

            //For each token in review
            for(String token : Tokenizer.tokenize(review)){
                //Add to this word's count for this sentiment
                Map<Sentiment, Double> countMap = wordCounts.getOrDefault(token, new HashMap<>());
                countMap.put(
                        sentiment,
                        countMap.getOrDefault(sentiment, 0.0) + 1
                );
                wordCounts.put(token, countMap);
            }
        }

        return wordCounts;
    }

    private Map<Sentiment, Double> calculateTotalCounts(Map<String, Map<Sentiment, Double>> wordCounts, boolean smoothing){
        //Get total counts for positive/negative
        double totalPos = 0;
        double totalNeg = 0;

        for(String token : wordCounts.keySet()){
            Map<Sentiment, Double> counts = wordCounts.get(token);
            totalPos += counts.getOrDefault(Sentiment.POSITIVE, 0.0);
            totalNeg += counts.getOrDefault(Sentiment.NEGATIVE, 0.0);

            if(smoothing){
                totalPos += 1;
                totalNeg += 1;
            }
        }

        Map<Sentiment, Double> totals = new HashMap<>();
        totals.put(Sentiment.POSITIVE, totalPos);
        totals.put(Sentiment.NEGATIVE, totalNeg);
        return totals;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Map<Sentiment, Double>> wordLogProbabilities = new HashMap<>();
        Map<String, Map<Sentiment, Double>> wordCounts = calculateWordCounts(trainingSet); //Get counts for each token, by sentiment
        Map<Sentiment, Double> totalCounts = calculateTotalCounts(wordCounts, false); //Get total counts for positive/negative

        //For each counted word, get the (log) probabilities
        for(String token : wordCounts.keySet()){
            Map<Sentiment, Double> counts = wordCounts.get(token);
            double pos = counts.getOrDefault(Sentiment.POSITIVE, 0.0);
            double neg = counts.getOrDefault(Sentiment.NEGATIVE, 0.0);

            Map<Sentiment, Double> logProbs = new HashMap<>();
            logProbs.put(Sentiment.POSITIVE, Math.log(pos/totalCounts.get(Sentiment.POSITIVE)));
            logProbs.put(Sentiment.NEGATIVE, Math.log(neg/totalCounts.get(Sentiment.NEGATIVE)));

            wordLogProbabilities.put(token, logProbs);
        }

        return wordLogProbabilities;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Map<Sentiment, Double>> wordLogProbabilities = new HashMap<>();
        Map<String, Map<Sentiment, Double>> wordCounts = calculateWordCounts(trainingSet);
        Map<Sentiment, Double> totalCounts = calculateTotalCounts(wordCounts, true); //Get total counts for positive/negative

        //For each counted word, get the log probabilities (+1)
        for(String token : wordCounts.keySet()){
            Map<Sentiment, Double> counts = wordCounts.get(token);
            double pos = counts.getOrDefault(Sentiment.POSITIVE, 0.0) + 1;
            double neg = counts.getOrDefault(Sentiment.NEGATIVE, 0.0) + 1;

            Map<Sentiment, Double> logProbs = new HashMap<>();
            logProbs.put(Sentiment.POSITIVE, Math.log(pos/totalCounts.get(Sentiment.POSITIVE)));
            logProbs.put(Sentiment.NEGATIVE, Math.log(neg/totalCounts.get(Sentiment.NEGATIVE)));

            wordLogProbabilities.put(token, logProbs);
        }

        return wordLogProbabilities;
    }

    @Override
    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {
        Map<Path, Sentiment> predictions = new HashMap<>();

        if(tokenLogProbs == null){
            return null;
        }
        //For each path in our test set
        for(Path review : testSet){
            //Tokenize review
            List<String> tokens = Tokenizer.tokenize(review);

            //Start values for argmax
            double positiveScore = Math.log(classProbabilities.get(Sentiment.POSITIVE));
            double negativeScore = Math.log(classProbabilities.get(Sentiment.NEGATIVE));

            //sum in to {positive,negative}score for each token in review
            for(String token : tokens){
                if(tokenLogProbs.containsKey(token)) {
                    positiveScore += tokenLogProbs.get(token).get(Sentiment.POSITIVE);
                    negativeScore += tokenLogProbs.get(token).get(Sentiment.NEGATIVE);
                }
            }

            if(positiveScore >= negativeScore){
                predictions.put(review, Sentiment.POSITIVE);
            }else{
                predictions.put(review, Sentiment.NEGATIVE);
            }
        }

        return predictions;
    }
}
