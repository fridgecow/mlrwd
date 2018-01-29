package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Strength;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Exercise4 implements IExercise4 {
    @Override
    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        //Do sentiment classification, taking into account token strengths
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
            int sentimentCount = 0;
            for(String token : tokens){
                if(sentiments.containsKey(token)) {
                    //Weight strong words by 2x
                    int weight = 1;
                    if(strengths.get(token).equals(Strength.STRONG)){
                        weight = 2;
                    }

                    if (sentiments.get(token).equals(Sentiment.POSITIVE)) {
                        sentimentCount += weight;
                    } else {
                        sentimentCount -= weight;
                    }
                }
            }

            if(sentimentCount >= 0){
                results.put(test, Sentiment.POSITIVE);
            }else{
                results.put(test, Sentiment.NEGATIVE);
            }
        }

        return results;
    }

    @Override
    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA, Map<Path, Sentiment> classificationB) {
        return 0;
    }
}
