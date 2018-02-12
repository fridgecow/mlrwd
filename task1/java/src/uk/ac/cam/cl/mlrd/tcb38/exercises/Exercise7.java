package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exercise7 implements IExercise7 {
    @Override
    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
        Map<DiceType, Map<DiceType, Double>> transitionMatrix = new HashMap<>();
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = new HashMap<>();

        List<HMMDataStore<DiceRoll, DiceType>> dataStore = HMMDataStore.loadDiceFiles(sequenceFiles);

        //Calculate total transitions from each state, and the transition counts from one to the next
        Map<DiceType, Double> transitionTotals = new HashMap<>();
        Map<DiceType, Double> emissionTotals = new HashMap<>();
        for (HMMDataStore<DiceRoll, DiceType> d : dataStore) {
            for (int i = 0; i < d.hiddenSequence.size(); i++) {
                DiceType current = d.hiddenSequence.get(i);
                DiceRoll currentRoll = d.observedSequence.get(i);

                //Emission counts
                emissionTotals.put(current, emissionTotals.getOrDefault(current, 0.0)+1);
                Map<DiceRoll, Double> currentEmissions = emissionMatrix.getOrDefault(current, new HashMap<>());
                currentEmissions.put(currentRoll, currentEmissions.getOrDefault(currentRoll, 0.0)+1);
                emissionMatrix.put(current, currentEmissions);

                //Transition counts
                if(i < d.hiddenSequence.size() - 1) {
                    DiceType next = d.hiddenSequence.get(i + 1);
                    transitionTotals.put(current, transitionTotals.getOrDefault(current, 0.0) + 1);

                    Map<DiceType, Double> currentMap = transitionMatrix.getOrDefault(current, new HashMap<>());
                    currentMap.put(next, currentMap.getOrDefault(next, 0.0) + 1);
                    transitionMatrix.put(current, currentMap);
                }
            }
        }

        //Divide the transition and emission counts by the total transitions and emissions
        for (DiceType type : DiceType.values()) {
            for(DiceType nextType : DiceType.values()){
                if(transitionTotals.containsKey(type)) {
                    transitionMatrix.get(type).put(
                            nextType,
                            transitionMatrix.get(type).getOrDefault(nextType, 0.0) / transitionTotals.get(type)
                    );
                }else{
                    Map<DiceType, Double> transitionMap = transitionMatrix.getOrDefault(type, new HashMap<>());
                    transitionMap.put(nextType, 0.0);
                    transitionMatrix.put(type, transitionMap);
                }
            }

            for(DiceRoll roll : DiceRoll.values()){
                emissionMatrix.get(type).put(
                        roll,
                        emissionMatrix.get(type).getOrDefault(roll, 0.0)/emissionTotals.get(type)
                );
            }
        }

        //System.out.println(transitionMatrix);
        //System.out.println(emissionMatrix);

        HiddenMarkovModel<DiceRoll, DiceType> markovModel = new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);
        return markovModel;
    }
}
