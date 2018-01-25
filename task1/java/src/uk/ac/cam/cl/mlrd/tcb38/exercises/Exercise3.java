package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.cl.mlrd.utils.BestFit;
import uk.ac.cam.cl.mlrd.utils.BestFit.Point;
import uk.ac.cam.cl.mlrd.utils.BestFit.Line;
import uk.ac.cam.cl.mlrd.utils.ChartPlotter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Exercise3 {
    static final Path dataDirectory = Paths.get("data/large_dataset");
    static final int numberToPlot = 10000;
    static final String[] task1tokenArray = {
            "recommend",
            "bland",
            "nice",
            "forgotten",
            "butt-jokes",
            "relax",
            "popcorn",
            "classic",
            "wonder",
            "motivations"
    };
    static final List<String> task1tokens = Arrays.asList(task1tokenArray);

    public static Double calculateY(Line line, Double x){
        return line.yIntercept + x*line.gradient;
    }

    public static List<Point> lineToPointList(Line line, Double start, Double end){
        List<Point> pointList = new ArrayList<>();

        pointList.add(new Point(start, calculateY(line, start)));
        pointList.add(new Point(end, calculateY(line, end)));

        return pointList;
    }

    public static void main(String[] args){
        //Step 1: Zipf
        Map<String, Integer> tokenFrequencies = new HashMap<>();

        //Load all of the dataset, tokenize them, then add to the frequency map
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
            System.out.println("Reading large dataset, this will take a while...");
            for(Path review : files){
                for(String token : Tokenizer.tokenize(review)){
                    tokenFrequencies.put(token, tokenFrequencies.getOrDefault(token, 0)+1);
                }
            }
        }catch(IOException e){
            System.err.println("Can't read the reviews");
        }

        //Rank them
        List<String> rankedTokens = new ArrayList<>(tokenFrequencies.keySet());
        rankedTokens.sort(Comparator.comparing(tokenFrequencies::get).reversed());

        //Print the first 10, just to see
        System.out.println("\nTop Ranked Tokens");
        for(int i = 0; i < 10; i++){
            System.out.println("#"+(i+1)+": '"+rankedTokens.get(i)+"' with "+tokenFrequencies.get(rankedTokens.get(i)));
        }

        List<Point> task1Plot = new ArrayList<>();
        List<Point> zipfPlot = new ArrayList<>();
        List<Point> loglogPlot = new ArrayList<>();
        Map<Point, Double> bestFitWeights = new HashMap<>();

        //Plot the first 10,000, and plot tokens from task 1.
        //In addition, calculate log-log points for plotting later
        //and create a best-fit weighting Map
        for(int i = 0; i < numberToPlot; i++){
            String token = rankedTokens.get(i);
            Integer freq = tokenFrequencies.get(token);
            Point p = new Point(i, freq);
            Point llp = new Point(Math.log(i+1), Math.log(freq));

            zipfPlot.add(p);
            loglogPlot.add(llp);
            bestFitWeights.put(llp, (double) freq);

            //Check for task 1 tokens
            if(task1tokens.contains(token)){
                task1Plot.add(p);
            }
        }
        ChartPlotter.plotLines(zipfPlot, task1Plot);

        //Print and plot tokens from task 1
        System.out.println("\nFrequency of tokens from Task 1");
        for(String token : task1tokens){
            System.out.println(token+": "+tokenFrequencies.get(token));
        }

        //Find a best fit for the log-log
        Line bestFitLine = BestFit.leastSquares(bestFitWeights);
        List<Point> bestFitPoints = lineToPointList(
                bestFitLine,
                loglogPlot.get(0).x,
                loglogPlot.get(loglogPlot.size() -1).x
        );

        //Plot the log-log chart, and its best fit
        ChartPlotter.plotLines(loglogPlot, bestFitPoints);
    }
}
