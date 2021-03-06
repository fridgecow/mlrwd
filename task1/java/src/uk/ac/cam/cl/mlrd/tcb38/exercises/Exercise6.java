package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise6 implements IExercise6 {
    @Override
    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<NuancedSentiment, Double> probabilities = new HashMap<>();

        double positive = 0;
        double neutral = 0;
        double negative = 0;

        //For each piece of training data
        for(NuancedSentiment s : trainingSet.values()){
            if(s.equals(NuancedSentiment.POSITIVE)){
                positive += 1;
            }else if(s.equals(NuancedSentiment.NEUTRAL)){
                neutral += 1;
            }else if(s.equals(NuancedSentiment.NEGATIVE)){
                negative += 1;
            }
        }

        probabilities.put(NuancedSentiment.POSITIVE, positive/trainingSet.size());
        probabilities.put(NuancedSentiment.NEUTRAL, neutral/trainingSet.size());
        probabilities.put(NuancedSentiment.NEGATIVE, negative/trainingSet.size());

        return probabilities;
    }

    private Map<String, Map<NuancedSentiment, Double>> calculateWordCounts(Map<Path, NuancedSentiment> trainingSet) throws IOException{
        Map<String, Map<NuancedSentiment, Double>> wordCounts = new HashMap<>();

        //For each piece of training data
        for(Path review : trainingSet.keySet()){
            NuancedSentiment sentiment = trainingSet.get(review);

            //For each token in review
            for(String token : Tokenizer.tokenize(review)){
                //Add to this word's count for this sentiment
                Map<NuancedSentiment, Double> countMap = wordCounts.getOrDefault(token, new HashMap<>());
                countMap.put(
                        sentiment,
                        countMap.getOrDefault(sentiment, 0.0) + 1
                );
                wordCounts.put(token, countMap);
            }
        }

        return wordCounts;
    }

    private Map<NuancedSentiment, Double> calculateTotalCounts(Map<String, Map<NuancedSentiment, Double>> wordCounts){
        //Get total counts for positive/negative
        double totalPos = 0;
        double totalNeg = 0;
        double totalNeut = 0;

        for(String token : wordCounts.keySet()){
            Map<NuancedSentiment, Double> counts = wordCounts.get(token);
            totalPos += counts.getOrDefault(NuancedSentiment.POSITIVE, 0.0) + 1;
            totalNeg += counts.getOrDefault(NuancedSentiment.NEGATIVE, 0.0) + 1;
            totalNeut += counts.getOrDefault(NuancedSentiment.NEUTRAL, 0.0) + 1;
        }

        Map<NuancedSentiment, Double> totals = new HashMap<>();
        totals.put(NuancedSentiment.POSITIVE, totalPos);
        totals.put(NuancedSentiment.NEGATIVE, totalNeg);
        totals.put(NuancedSentiment.NEUTRAL, totalNeut);
        return totals;
    }

    @Override
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<String, Map<NuancedSentiment, Double>> wordLogProbabilities = new HashMap<>();
        Map<String, Map<NuancedSentiment, Double>> wordCounts = calculateWordCounts(trainingSet);
        Map<NuancedSentiment, Double> totalCounts = calculateTotalCounts(wordCounts);

        //For each counted word, get the log probabilities (+1)
        for(String token : wordCounts.keySet()){
            Map<NuancedSentiment, Double> counts = wordCounts.get(token);
            double pos = counts.getOrDefault(NuancedSentiment.POSITIVE, 0.0) + 1;
            double neg = counts.getOrDefault(NuancedSentiment.NEGATIVE, 0.0) + 1;
            double neut = counts.getOrDefault(NuancedSentiment.NEUTRAL, 0.0) + 1;

            Map<NuancedSentiment, Double> logProbs = new HashMap<>();
            logProbs.put(NuancedSentiment.POSITIVE, Math.log(pos/totalCounts.get(NuancedSentiment.POSITIVE)));
            logProbs.put(NuancedSentiment.NEGATIVE, Math.log(neg/totalCounts.get(NuancedSentiment.NEGATIVE)));
            logProbs.put(NuancedSentiment.NEUTRAL, Math.log(neut/totalCounts.get(NuancedSentiment.NEUTRAL)));

            wordLogProbabilities.put(token, logProbs);
        }

        return wordLogProbabilities;
    }

    @Override
    public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {
        Map<Path, NuancedSentiment> predictions = new HashMap<>();

        if(tokenLogProbs == null){
            return null;
        }

        //For each path in our test set
        for(Path review : testSet){
            //Tokenize review
            List<String> tokens = Tokenizer.tokenize(review);

            //Start values for argmax
            double positiveScore = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE));
            double negativeScore = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE));
            double neutralScore = Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL));

            //sum in to {positive,negative}score for each token in review
            for(String token : tokens){
                if(tokenLogProbs.containsKey(token)) {
                    positiveScore += tokenLogProbs.get(token).get(NuancedSentiment.POSITIVE);
                    negativeScore += tokenLogProbs.get(token).get(NuancedSentiment.NEGATIVE);
                    neutralScore += tokenLogProbs.get(token).get(NuancedSentiment.NEUTRAL);
                }
            }

            if(positiveScore >= negativeScore && positiveScore >= neutralScore){
                predictions.put(review, NuancedSentiment.POSITIVE);
            }else if(negativeScore >= neutralScore){
                predictions.put(review, NuancedSentiment.NEGATIVE);
            }else{
                predictions.put(review, NuancedSentiment.NEUTRAL);
            }
        }

        return predictions;
    }

    @Override
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments, Map<Path, NuancedSentiment> predictedSentiments) {
        double correctCount = 0;
        double totalCount = 0;

        if(predictedSentiments != null) {
            for (Path p : predictedSentiments.keySet()) {
                if(trueSentiments.containsKey(p)) {
                    if (trueSentiments.get(p).equals(predictedSentiments.get(p))) {
                        correctCount++;
                    }
                    totalCount++;
                }
            }

            return correctCount / totalCount;
        }else{
            return 0;
        }
    }

    @Override
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {
        Map<Integer, Map<Sentiment, Integer>> table = new HashMap<>();

        for(Map<Integer, Sentiment> group : predictedSentiments){
            for(Integer review : group.keySet()){
                Sentiment sentiment = group.get(review);

                Map<Sentiment, Integer> counts = table.getOrDefault(review, new HashMap<>());
                counts.put(sentiment, counts.getOrDefault(sentiment, 0) +1);
                table.put(review, counts);
            }
        }

        return table;
    }

    @Override
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
        double N = agreementTable.size();

        //Calculate n(i,j)
        Map<Integer, Map<Sentiment, Double>> nij = new HashMap<>();
        for(Integer review : agreementTable.keySet()){
            Map<Sentiment, Double> values = new HashMap<>();

            values.put(Sentiment.POSITIVE, (double) agreementTable.get(review).getOrDefault(Sentiment.POSITIVE, 0));
            values.put(Sentiment.NEGATIVE, (double) agreementTable.get(review).getOrDefault(Sentiment.NEGATIVE, 0));

            nij.put(review, values);
        }
        System.out.println(nij);

        //Calculate n(i)
        Map<Integer, Double> ni = new HashMap<>();
        for(Integer review : agreementTable.keySet()){
            ni.put(review, nij.get(review).get(Sentiment.POSITIVE) + nij.get(review).get(Sentiment.NEGATIVE));
        }
        System.out.println(ni);

        //Calculate Pa
        double Pa = 0;
        for(Integer i : agreementTable.keySet()){
            double innerSum = 0;
            for(Sentiment j : nij.get(i).keySet()) {
                innerSum += nij.get(i).get(j)*(nij.get(i).get(j) - 1);
            }
            Pa += innerSum/(ni.get(i)*(ni.get(i) - 1));
        }

        Pa *= 1/N;

        //Calculate Pe
        double Pe = 0;
        for(Sentiment j : Sentiment.values()){
            double innerSum = 0;
            for(Integer review : agreementTable.keySet()){
                innerSum += nij.get(review).get(j)/ni.get(review);
            }
            Pe += (innerSum/N)*(innerSum/N);
        }

        double kappa = (Pa - Pe)/(1- Pe);
        return kappa;
    }
}
