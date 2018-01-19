package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
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
import java.util.function.Consumer;

public class Exercise1 implements IExercise1 {
    public enum Strength {
        STRONG, WEAK;
    }
    @Override
    public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<Path, Sentiment> results = new HashMap<>();

        //Load in lexicon file as a "word"->sentiment map
        Map<String, Sentiment> sentiments = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(lexiconFile)) {
            reader.lines().forEach(line -> {
                String[] tokens = line.split(" ");

                String word = tokens[0].split("=")[1].trim();
                Sentiment sentiment = tokens[2].split("=")[1].contains("positive") ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
                sentiments.put(word, sentiment);
            });
        }

        for(Path test : testSet){
            //Tokenize each test item
            List<String> tokens = Tokenizer.tokenize(test);

            //Classify by incrementing for positive and decrementing for negative
            int sentimentCount = 0;
            for(String token : tokens){
                if(sentiments.containsKey(token)) {
                    if (sentiments.get(token).equals(Sentiment.POSITIVE)) {
                        sentimentCount++;
                    } else {
                        sentimentCount--;
                    }
                }
            }

            if(sentimentCount > 0){
                results.put(test, Sentiment.POSITIVE);
            }else{
                results.put(test, Sentiment.NEGATIVE);
            }
        }

        return results;
    }

    @Override
    public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {
        double correctCount = 0;

        if(predictedSentiments != null) {
            for (Path p : predictedSentiments.keySet()) {
                if (trueSentiments.get(p).equals(predictedSentiments.get(p))) {
                    correctCount++;
                }
            }

            return correctCount / predictedSentiments.size();
        }else{
            return 0;
        }
    }

    @Override
    public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<Path, Sentiment> results = new HashMap<>();

        //Load in lexicon file as a "word"->sentiment map
        Map<String, Sentiment> sentiments = new HashMap<>();
        Map<String, Strength> strengths = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(lexiconFile)) {
            reader.lines().forEach(line -> {
                String[] tokens = line.split(" ");

                String word = tokens[0].split("=")[1].trim();
                Sentiment sentiment = tokens[2].split("=")[1].contains("positive") ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
                Strength strength = tokens[1].split("=")[1].contains("strong") ? Strength.STRONG : Strength.WEAK;
                sentiments.put(word, sentiment);
                strengths.put(word, strength);
            });
        }

        for(Path test : testSet){
            //Tokenize each test item
            List<String> tokens = Tokenizer.tokenize(test);

            //Classify by incrementing for positive and decrementing for negative
            //Use a 2x weighting on strong words
            int sentimentCount = 0;
            for(String token : tokens){
                int multiplier = 0;
                if(strengths.containsKey(token) && strengths.get(token).equals(Strength.STRONG)){
                    multiplier = 1;
                }
                if(sentiments.containsKey(token)) {
                    if (sentiments.get(token).equals(Sentiment.POSITIVE)) {
                        sentimentCount += multiplier;
                    } else {
                        sentimentCount -= multiplier;
                    }
                }
            }

            if(sentimentCount > 0){
                results.put(test, Sentiment.POSITIVE);
            }else{
                results.put(test, Sentiment.NEGATIVE);
            }
        }

        return results;
    }
}
