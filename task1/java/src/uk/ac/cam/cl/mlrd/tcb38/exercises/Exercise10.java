package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class Exercise10 implements IExercise10{
    @Override
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
        return null;
    }

    @Override
    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
        return null;
    }

    @Override
    public int getDiameter(Map<Integer, Set<Integer>> graph) {
        return 0;
    }
}
