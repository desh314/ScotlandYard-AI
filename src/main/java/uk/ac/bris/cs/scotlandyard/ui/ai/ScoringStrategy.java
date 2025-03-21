package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.DistanceStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

/**
 * Strategy for scoring a move
 */

public interface ScoringStrategy {

    /**
     * This function is used by clients to use the scoring strategy to score a given board.
     * @param simGameState The current game state.
     * @return The score of the game state
     */
    public Double score(SimulationGameState simGameState);
}
