package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise8 implements IExercise8 {
    @Override
    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {
        List<Map<DiceType, DiceType>> mostProbablePreviousState = new ArrayList<>();
        List<Map<DiceType, Double>> logPathProbabilities = new ArrayList<>();

        for(int t = 0; t < observedSequence.size(); t++){
            DiceRoll roll = observedSequence.get(t);

            //System.out.println(logPathProbabilities.get(t-1));

            logPathProbabilities.add(new HashMap<>());
            mostProbablePreviousState.add(new HashMap<>());
            for(DiceType state : DiceType.values()){
                //Compute max(delta(t-1)*...), but log'd
                Double max = Double.NEGATIVE_INFINITY;
                double rollProb = Math.log(model.getEmissionMatrix().get(state).get(roll));
                DiceType bestState = null;

                //System.out.println("Roll Probability of "+observedSequence.get(t)+": "+rollProb);

                if(t == 0){
                    max = rollProb;
                }else {
                    for (DiceType i : DiceType.values()) {
                        Double candidate = logPathProbabilities.get(t - 1).get(i);
                        candidate += Math.log(model.getTransitionMatrix().get(i).get(state));
                        candidate += rollProb;

                        //System.out.println(i.toString()+"->"+state.toString()+": "+Math.log(model.getTransitionMatrix().get(i).get(state)));
                        if (bestState == null || candidate > max) {
                            max = candidate;
                            bestState = i;
                        }
                    }

                    mostProbablePreviousState.get(t).put(state, bestState);
                }

                //System.out.println("Best: "+bestState+" @ "+max);
                logPathProbabilities.get(t).put(state, max);
            }
        }

        //Backtrack
        List<DiceType> bestSequence = new ArrayList<>();

        //Find max probability final dice
        DiceType state = DiceType.END;
        bestSequence.add(state);
        for(int i = mostProbablePreviousState.size() - 1; i > 0; i--){
            state = mostProbablePreviousState.get(i).get(state);
            bestSequence.add(state);
        }
        Collections.reverse(bestSequence);
        return bestSequence;
    }

    @Override
    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> testFiles) throws IOException {
        Map<List<DiceType>, List<DiceType>> predictions = new HashMap<>();

        for(HMMDataStore<DiceRoll, DiceType> data : HMMDataStore.loadDiceFiles(testFiles)){
            predictions.put(data.hiddenSequence, viterbi(model, data.observedSequence));
        }

        return predictions;
    }

    @Override
    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double correctCount = 0;
        double allCount = 0;

        for(List<DiceType> trueHidden : true2PredictedMap.keySet()){
            List<DiceType> predictedHidden = true2PredictedMap.get(trueHidden);

            for(int i = 0; i < trueHidden.size(); i++){
                if(predictedHidden.get(i).equals(DiceType.WEIGHTED)) {
                    allCount++;

                    if (trueHidden.get(i).equals(DiceType.WEIGHTED)) {
                        correctCount++;
                    }
                }
            }
        }

        return correctCount / allCount;
    }

    @Override
    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double correctCount = 0;
        double allCount = 0;

        for(List<DiceType> trueHidden : true2PredictedMap.keySet()){
            List<DiceType> predictedHidden = true2PredictedMap.get(trueHidden);

            for(int i = 0; i < trueHidden.size(); i++){
                if(predictedHidden.get(i).equals(DiceType.WEIGHTED)) {
                    if (trueHidden.get(i).equals(DiceType.WEIGHTED)) {
                        correctCount++;
                    }
                }

                if(trueHidden.get(i).equals(DiceType.WEIGHTED)){
                    allCount++;
                }
            }
        }

        return correctCount / allCount;
    }

    @Override
    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);

        return (2*precision*recall)/(precision + recall);
    }
}
