package uk.ac.cam.cl.mlrd.tcb38.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Exercise10 implements IExercise10{
    @Override
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
        Map<Integer, Set<Integer>> graph = new HashMap<>();

        BufferedReader reader = Files.newBufferedReader(graphFile);
        reader.lines().forEach(line -> {
            String[] nodes = line.split(" ");
            Integer u = Integer.valueOf(nodes[0]);
            Integer v = Integer.valueOf(nodes[1]);

            if(!graph.containsKey(u)){
                graph.put(u, new HashSet<>());
            }

            graph.get(u).add(v);

            //Undirected graph
            if(!graph.containsKey(v)){
                graph.put(v, new HashSet<>());
            }

            graph.get(v).add(u);
        });

        return graph;
    }

    @Override
    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
        Map<Integer, Integer> connectivities = new HashMap<>();
        for(Integer node : graph.keySet()){
            connectivities.put(node, graph.get(node).size());
        }

        return connectivities;
    }

    @Override
    public int getDiameter(Map<Integer, Set<Integer>> graph) {
        int longestShortPath = 0;

        for(Integer startNode : graph.keySet()){
            //BFS from this node
            Queue<Integer> queue = new LinkedList<>();
            Map<Integer,Integer> visitedDistance = new HashMap<>();
            int shortestPath = 0;

            visitedDistance.put(startNode, 0);
            queue.add(startNode);

            Integer node;
            while ((node = queue.poll()) != null ){
                int nodeDistance = visitedDistance.get(node);
                if(nodeDistance > shortestPath){
                    shortestPath = nodeDistance;
                }

                for(Integer neighbour : graph.get(node)){ //For each neighbour
                    if(!visitedDistance.containsKey(neighbour)){ //That hasn't been visited
                        visitedDistance.put(neighbour, nodeDistance + 1); //Set its distance to this+1
                        queue.add(neighbour); //And visit it next
                    }
                }
            }

            if(shortestPath > longestShortPath){
                longestShortPath = shortestPath;
            }
        }

        return longestShortPath;
    }
}
