package uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies;

import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

public interface DistanceStrategy {

    public double findDistance(int source, int destination, SimulationGameState simGameState);

}