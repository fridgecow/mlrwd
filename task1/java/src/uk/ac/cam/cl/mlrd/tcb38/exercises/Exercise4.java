package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Strength;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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

                try {
                    String word = tokens[0].split("=")[1].trim();
                    Sentiment sentiment = tokens[2].split("=")[1].contains("positive") ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
                    Strength strength = tokens[1].split("=")[1].contains("strong") ? Strength.STRONG : Strength.WEAK;
                    sentiments.put(word, sentiment);
                    strengths.put(word, strength);
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Could not tokenize "+line);
                }
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

    private static Map<BigInteger, BigInteger> factorialCache = new HashMap<>();
    private static BigInteger factorial(BigInteger n){
        //Naive factorial - no memoisation
        BigInteger result = BigInteger.ONE;

        //0! = 1
        if(n.equals(BigInteger.ZERO)){
            return BigInteger.ONE;
        }

        for(BigInteger i = n; i.compareTo(BigInteger.ZERO) > 0; i = i.subtract(BigInteger.ONE)){
            if(factorialCache.containsKey(i)){
                result = result.multiply(factorialCache.get(i));
                break;
            }else{
                result = result.multiply(i);
            }
        }

        factorialCache.put(n, result);
        return result;
    }

    private static BigInteger choose(BigInteger n, BigInteger k) {
        //Naive choose function
        return factorial(n).divide( factorial(k).multiply(factorial(n.subtract(k))) );
    }

    @Override
    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA, Map<Path, Sentiment> classificationB) {
        BigInteger Plus = new BigInteger("0"); /*Counts the times classification A is better than classification B */
        BigInteger Minus = new BigInteger("0"); /* Counts the times class A is worse than class B */
        BigInteger Null = new BigInteger("0"); /* Counts the times class A is the same as class B */

        for(Path review : actualSentiments.keySet()){
            if(classificationA.containsKey(review) && classificationB.containsKey(review)) {
                Sentiment actual = actualSentiments.get(review);
                Sentiment classA = classificationA.get(review);
                Sentiment classB = classificationB.get(review);

                if(classA.equals(classB)){ //A and B performed equally as well
                    Null = Null.add(BigInteger.ONE);
                }else{ //A and B performed differently
                    if(classA.equals(actual)){ //A performed better
                        Plus = Plus.add(BigInteger.ONE);
                    }else{
                        Minus = Minus.add(BigInteger.ONE);
                    }
                }
            }
        }

        //Calculate N and K for the sign test
        final BigInteger Two = new BigInteger("2");
        BigInteger NullDiv2 = (Null.add(Two).subtract(BigInteger.ONE)).divide(Two);
        BigInteger N = (NullDiv2.multiply(Two)).add(Plus).add(Minus);
        BigInteger K = NullDiv2.add(Plus.min(Minus));

        //Do sign test
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal Half = new BigDecimal("0.5");
        //since q=0.5, 1-q = q so the power is just q^n - then we can just factor this out
        // and multiply at the end
        BigDecimal QtotheN = Half.pow(N.intValue());

        for(BigInteger i = BigInteger.ZERO; i.compareTo(K) <= 0; i = i.add(BigInteger.ONE)){
            sum = sum.add( new BigDecimal(choose(N, i)) );
        }

        sum = sum.multiply(QtotheN).multiply(new BigDecimal(Two));
        return sum.doubleValue();
    }
}
