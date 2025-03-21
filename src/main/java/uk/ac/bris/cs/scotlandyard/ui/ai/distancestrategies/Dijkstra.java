package uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/*
References implementation from: https://www.redblobgames.com/pathfinding/a-star/implementation.html
 */

/*
Evaluation of using Dijkstra:
(+) considers all possible paths and is not greedy (no early stopping)
(+) not using a heuristic means the search is not biased

(-) much slower than other methods
(-) assume the cost from going from one node to the next node is 1 ie. equal distance between adjacent nodes
-> we get no information about tickets/connectivity or the kind of transport needed to get to the edge
 */

/**
 * Class that calculates the distance between nodes using Dijkstra's Algorithm. Implements {@link DistanceStrategy}.
 */
public class Dijkstra implements DistanceStrategy {

    /**
     * Constructor to call Dijkstra from
     */
    public Dijkstra() {};

    /**
     * Runs Dijkstra algorithm to find the short number of edges between any two nodes on the game graph
     * @param source
     * @param destination
     * @param simGameState
     * @return
     */
    @Override
    public double findDistance(int source, int destination, SimulationGameState simGameState) {
        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = simGameState.getSetup().graph;

        //queue to indicate which node to explore next based on current distance
        //elements stored as: Pair(Node number, currentDistance)
        PriorityQueue<Pair<Integer, Integer>> remainingQueue = new PriorityQueue<>(Comparator.comparingInt(Pair::right));
        remainingQueue.add(new Pair<>(source, 0));

        //hashmap to check if a node is looked up
        //elements stored as:  Node number -> is node visited?
        HashMap<Integer, Boolean> visited = new HashMap<>();

        //hashmap for fast lookup of the distance from the source to any given node on the graph.
        //elements stored as: Node number -> current distance
        HashMap<Integer, Integer> currentDistance = new HashMap<>();

        //initialize the hashmaps
        for (int node : graph.nodes()) {
            currentDistance.put(node, Integer.MAX_VALUE);
            visited.put(node, Boolean.FALSE);
        }

        visited.put(source, Boolean.TRUE);
        currentDistance.put(source, 0);

        //perform BFS but only consider neighbour nodes if the added distance is lower than the current distance

        while (!remainingQueue.isEmpty()) {
            //pop out the top of the priority queue which should have the smallest score
            Pair<Integer, Integer> currentNode = remainingQueue.poll();
            //find all the adjacent/neighbouring nodes
            for (Integer neighbour : graph.adjacentNodes(currentNode.left())) {
                //add one to the cost as we have traversed an edge to get to the neighbour
                int newCost = currentDistance.get(currentNode.left()) + 1;

                //if the neighbour is not visited or the new distance travelled is smaller than the current smallest path
                if (!visited.get(neighbour) || newCost < currentDistance.get(neighbour)) {
                    //update the hashmap that stores the distances
                    currentDistance.put(neighbour, newCost);
                    //put the neighbour onto the priority queue to continue the path
                    remainingQueue.add(new Pair<>(neighbour, newCost));
                    //mark the neighbour as being visited
                    visited.put(neighbour, Boolean.TRUE);
                }
            }
        }

        //get the distance from the distance hashmap
        return currentDistance.get(destination);
    }
}
