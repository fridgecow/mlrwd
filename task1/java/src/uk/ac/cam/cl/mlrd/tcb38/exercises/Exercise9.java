package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.util.*;

public class Exercise9 implements IExercise9 {

    @Override
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException {
        Map<Feature, Map<Feature, Double>> transitionMatrix = new HashMap<>();
        Map<Feature, Map<AminoAcid, Double>> emissionMatrix = new HashMap<>();

        //Calculate total transitions from each state, and the transition counts from one to the next
        Map<Feature, Double> transitionTotals = new HashMap<>();
        Map<Feature, Double> emissionTotals = new HashMap<>();
        for (HMMDataStore<AminoAcid, Feature> d : sequencePairs) {
            for (int i = 0; i < d.hiddenSequence.size(); i++) {
                Feature current = d.hiddenSequence.get(i);
                AminoAcid currentRoll = d.observedSequence.get(i);

                //Emission counts
                emissionTotals.put(current, emissionTotals.getOrDefault(current, 0.0)+1);
                Map<AminoAcid, Double> currentEmissions = emissionMatrix.getOrDefault(current, new HashMap<>());
                currentEmissions.put(currentRoll, currentEmissions.getOrDefault(currentRoll, 0.0)+1);
                emissionMatrix.put(current, currentEmissions);

                //Transition counts
                if(i < d.hiddenSequence.size() - 1) {
                    Feature next = d.hiddenSequence.get(i + 1);
                    transitionTotals.put(current, transitionTotals.getOrDefault(current, 0.0) + 1);

                    Map<Feature, Double> currentMap = transitionMatrix.getOrDefault(current, new HashMap<>());
                    currentMap.put(next, currentMap.getOrDefault(next, 0.0) + 1);
                    transitionMatrix.put(current, currentMap);
                }
            }
        }

        //Divide the transition and emission counts by the total transitions and emissions
        for (Feature type : Feature.values()) {
            for(Feature nextType : Feature.values()){
                if(transitionTotals.containsKey(type)) {
                    transitionMatrix.get(type).put(
                            nextType,
                            transitionMatrix.get(type).getOrDefault(nextType, 0.0) / transitionTotals.get(type)
                    );
                }else{
                    Map<Feature, Double> transitionMap = transitionMatrix.getOrDefault(type, new HashMap<>());
                    transitionMap.put(nextType, 0.0);
                    transitionMatrix.put(type, transitionMap);
                }
            }

            for(AminoAcid roll : AminoAcid.values()){
                emissionMatrix.get(type).put(
                        roll,
                        emissionMatrix.get(type).getOrDefault(roll, 0.0)/emissionTotals.get(type)
                );
            }
        }

        return new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);
    }

    @Override
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
        List<Map<Feature, Feature>> mostProbablePreviousState = new ArrayList<>();
        List<Map<Feature, Double>> logPathProbabilities = new ArrayList<>();

        for(int t = 0; t < observedSequence.size(); t++){
            AminoAcid roll = observedSequence.get(t);

            //System.out.println(logPathProbabilities.get(t-1));

            logPathProbabilities.add(new HashMap<>());
            mostProbablePreviousState.add(new HashMap<>());
            for(Feature state : Feature.values()){
                //Compute max(delta(t-1)*...), but log'd
                Double max = Double.NEGATIVE_INFINITY;
                double rollProb = Math.log(model.getEmissionMatrix().get(state).get(roll));
                Feature bestState = null;

                //System.out.println("Roll Probability of "+observedSequence.get(t)+": "+rollProb);

                if(t == 0){
                    max = rollProb;
                }else {
                    for (Feature i : Feature.values()) {
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
        List<Feature> bestSequence = new ArrayList<>();

        //Find max probability final dice
        Feature state = Feature.END;
        bestSequence.add(state);
        for(int i = mostProbablePreviousState.size() - 1; i > 0; i--){
            state = mostProbablePreviousState.get(i).get(state);
            bestSequence.add(state);
        }
        Collections.reverse(bestSequence);
        return bestSequence;
    }

    @Override
    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        Map<List<Feature>, List<Feature>> predictions = new HashMap<>();

        for(HMMDataStore<AminoAcid, Feature> data : testSequencePairs){
            predictions.put(data.hiddenSequence, viterbi(model, data.observedSequence));
        }

        return predictions;
    }

    @Override
    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double correctCount = 0;
        double allCount = 0;

        for(List<Feature> trueHidden : true2PredictedMap.keySet()){
            List<Feature> predictedHidden = true2PredictedMap.get(trueHidden);

            for(int i = 0; i < trueHidden.size(); i++){
                if(predictedHidden.get(i).equals(Feature.MEMBRANE)) {
                    allCount++;

                    if (trueHidden.get(i).equals(Feature.MEMBRANE)) {
                        correctCount++;
                    }
                }
            }
        }

        return correctCount / allCount;
    }

    @Override
    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double correctCount = 0;
        double allCount = 0;

        for(List<Feature> trueHidden : true2PredictedMap.keySet()){
            List<Feature> predictedHidden = true2PredictedMap.get(trueHidden);

            for(int i = 0; i < trueHidden.size(); i++){
                if(predictedHidden.get(i).equals(Feature.MEMBRANE)) {
                    if (trueHidden.get(i).equals(Feature.MEMBRANE)) {
                        correctCount++;
                    }
                }

                if(trueHidden.get(i).equals(Feature.MEMBRANE)){
                    allCount++;
                }
            }
        }

        return correctCount / allCount;
    }

    @Override
    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);

        return (2*precision*recall)/(precision + recall);
    }
}
