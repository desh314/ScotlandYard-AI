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
Evaluation of using A*:
(+) heuristic means it is potentially faster

(-) heuristic is not very accurate so may introduce high error
(-) assume the cost from going from one node to the next node is 1 ie. equal distance between adjacent nodes
-> we get no information about tickets/connectivity or the kind of transport needed to get to the edge
 */


/**
 * Class that uses A Star to calculate shortest distances. Implements {@link DistanceStrategy}.
 */
public class AStar implements DistanceStrategy{
    /**
     * Constructor for AStar
     */
    public AStar() {};

    //uses the fact that similar numbered nodes are in a similar neighbourhood in the graph
    //fairly not precise heuristic but captures the notion of "closeness"

    /**
     * Heuristic for A*
     * @param source node number from which the distance is calculated
     * @param dest node number to which the distance is calculated
     * @return absolute distance between node numbers
     */
    public double heuristic(int source, int dest) {
        return Math.abs(source - dest);
    }

    /**
     * Uses search heuristic to perform A* search in order to find shortest distance between nodes on the game graph
     * @param source node number of the source
     * @param destination node number of the destination
     * @param simGameState simulation game state on which to run the search
     * @return shortest distance to the destination from the source
     */
    @Override
    public double findDistance(int source, int destination, SimulationGameState simGameState) {
        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = simGameState.getSetup().graph;

        PriorityQueue<Pair<Integer, Double>> remainingQueue = new PriorityQueue<>(Comparator.comparingDouble(Pair::right));
        remainingQueue.add(new Pair<>(source, 0.0));

        HashMap<Integer, Boolean> visited = new HashMap<>();

        HashMap<Integer, Integer> currentDistance = new HashMap<>();

        for (int node : graph.nodes()) {
            currentDistance.put(node, Integer.MAX_VALUE);
            visited.put(node, Boolean.FALSE);
        }

        visited.put(source, Boolean.TRUE);
        currentDistance.put(source, 0);

        while (!remainingQueue.isEmpty()) {
            Pair<Integer, Double> currentNode = remainingQueue.poll();

            for (Integer neighbour : graph.adjacentNodes(currentNode.left())) {
                int newCost = currentDistance.get(currentNode.left()) + 1;

                if (!visited.get(neighbour) || newCost < currentDistance.get(neighbour)) {

                    currentDistance.put(neighbour, newCost);
                    //adds the difference of node numbers to introduce a heuristic in the priority queue
                    remainingQueue.add(new Pair<>(neighbour, newCost+heuristic(neighbour, destination)));
                    visited.put(neighbour, Boolean.TRUE);
                }
            }
        }

        return currentDistance.get(destination);
    }
}
