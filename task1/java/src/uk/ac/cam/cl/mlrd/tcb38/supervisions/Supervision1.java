package uk.ac.cam.cl.mlrd.tcb38.supervisions;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise1;
import uk.ac.cam.cl.mlrd.tcb38.exercises.Exercise2;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Supervision1 {
    public static void main(String[] args){
        //Sentiment Lexicon exercise 1: Tokenize a random review.
        Path myOpinionPiece = Paths.get("data/supervisions/1/opinionpiece.txt");
        Path albertsPiece = Paths.get("data/supervisions/1/albertpiece.txt");
        Path lexiconFile = Paths.get("data/sentiment_lexicon");
        Path dataDirectory = Paths.get("data/sentiment_dataset");
        Path sentimentFile = dataDirectory.resolve("review_sentiment");

        Set<String> opinionTokens = new HashSet<>();

        try {
            opinionTokens = new TreeSet<>(Tokenizer.tokenize(myOpinionPiece));
        }catch(IOException e){
            System.out.println("Could not open opinion piece");
        }

        System.out.println("Tokens from my review:");
        System.out.println(String.join(
                "\n",
                opinionTokens
                        .stream()
                        .filter(s -> s.compareTo("a") >= 0)
                        .collect(Collectors.toList())
        ));

        //Do some classification
        IExercise1 task1 = new Exercise1();
        IExercise2 task2 = new Exercise2();
        Set<Path> testSet = new HashSet<>();
        Map<Path, Sentiment> task1pred = new HashMap<>();
        Map<Path, Sentiment> task2pred = new HashMap<>();

        testSet.add(myOpinionPiece);
        testSet.add(albertsPiece);

        try {
            //Task 1 predictions
            task1pred = task1.improvedClassifier(testSet, lexiconFile);

            //Task 2 training + predictions
            Map<Path, Sentiment> dataSet = DataPreparation1.loadSentimentDataset(dataDirectory.resolve("reviews"),
                    sentimentFile);
            Map<String, Map<Sentiment, Double>> smoothedLogProbs =
                    task2.calculateSmoothedLogProbs(dataSet);
            Map<Sentiment, Double> classProbs = task2.calculateClassProbabilities(dataSet);
            task2pred = task2.naiveBayes(testSet, smoothedLogProbs, classProbs);
        }catch(IOException e){
            System.out.println("Failed to classify files");
        }
        //Set<Path> task2pred = task2.

        System.out.println("\nSentiment of Albert's text:");
        System.out.println("Task 1 System: "+task1pred.get(albertsPiece));
        System.out.println("Task 2 System: "+task2pred.get(albertsPiece));

        System.out.println("\nSentiment of my text:");

        System.out.println("Task 1 System: "+task1pred.get(myOpinionPiece));
        System.out.println("Task 2 System: "+task2pred.get(myOpinionPiece));
        System.out.println("Actual/My opinion: Positive");
    }
}
