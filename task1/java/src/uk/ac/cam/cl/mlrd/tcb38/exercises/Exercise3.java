package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Exercise3 {
    static final Path dataDirectory = Paths.get("data/large_dataset");

    public static void main(String[] args){
        //Step 1: Zipf
        Map<String, Long> tokenFrequencies = new HashMap<>();

        //Load all of the dataset, tokenize them, then add to the frequency map
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
            System.out.println("Reading large dataset, this will take a while...");
            for(Path review : files){
                for(String token : Tokenizer.tokenize(review)){
                    tokenFrequencies.put(token, tokenFrequencies.getOrDefault(token, 0L)+1);
                }
            }
        }catch(IOException e){
            System.err.println("Can't read the reviews");
        }

        //Rank them
        List<String> rankedTokens = new ArrayList<>(tokenFrequencies.keySet());
        rankedTokens.sort(Comparator.comparing(tokenFrequencies::get).reversed());
        //Print the first 10
        for(int i = 0; i < 10; i++){
            System.out.println("#"+(i+1)+": "+rankedTokens.get(i)+" with "+tokenFrequencies.get(rankedTokens.get(i)));
        }
    }
}
