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
Evaluation of using greedy A*:
(+) faster due to early stopping and heuristic
(+) as we give edges scores, we are providing more information about the graph in our score so the cost is not assumed to be even
-> provides a better indication of the "distance"

(-) early stopping and heuristic exposes to chance of finding a local minimum or not the shortest path possible.
 */

/**
 * Class that uses a greedy version of A Star and outputs a score based on connectivity and neighbourhood proximity. Implements {@link DistanceStrategy}.
 */
public class GreedyAStar implements DistanceStrategy {

    /**
     * Constructor to call greedy A* from
     */
    public GreedyAStar () {};

    /**
     * Greedy A* Search that returns a heuristic score instead of the distance
     * Score is higher when a node has more tickets and the absolute difference of node locations is low (|node1 - node2|)
     * - the |node1 - node2| heuristic can be thought as whether the source is in approximately close or far neighbourhood to the destination
     * - the more connectivity in the path, the better score it should have as rerouting is easier
     * @param source source node location
     * @param destination destination node location
     * @param board the board on which to calculate the distance
     * @return integer that represents the distance between nodes
     */

    @Override
    public double findDistance(int source, int destination, SimulationGameState board) {

        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = board.getSetup().graph;
        int maxEdgeValue = 20;

        PriorityQueue<Pair<Integer, Integer>> q = new PriorityQueue<>(Comparator.comparingInt(Pair::right));
        HashMap<Integer, Boolean> visited = new HashMap<>();
        HashMap<Integer, Integer> currentCost = new HashMap<>();

        q.add(new Pair<>(source, 0));
        for (int node : graph.nodes()) {
            currentCost.put(node, Integer.MAX_VALUE);
            visited.put(node, Boolean.FALSE);
        }

        currentCost.put(source, 0);
        visited.put(source, Boolean.TRUE);

        while (!q.isEmpty()) {

            Pair<Integer, Integer> current = q.poll();

            //greedy due to early exit
            if (current.left() == destination) {
                break;
            }
            for (int next : graph.adjacentNodes(current.left())) {

                //calculates the cost using the absolute difference and number of node connections
                //ie. integrates the heuristic into A*
                int cost = currentCost.get(current.left()) + (maxEdgeValue - graph.edgeValue(current.left(), next).get().size()) + Math.abs(current.left() - next);

                if (!visited.get(next) || cost < currentCost.get(next)) {
                    currentCost.replace(next, cost);
                    q.add(new Pair<>(next, cost));
                    visited.put(next, Boolean.TRUE);
                }
            }
        }

        return currentCost.get(destination);
    }
}
