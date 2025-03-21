package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 * Strategy for selecting a move
 */
public interface MoveSelectingStrategy {

    /**
     * Find the optimal move given that the initial player is MrX
     * @param board the initial state of the game
     * @return optimal move for the AI to execute
     */
    Move selectForMrX(Board board);

    /**
     * Find the optimal move given that the initial player is a detective
     * @param board the initial state of the game
     * @return optimal move for the AI to execute
     */
    Move selectForDetective(Board board);

}
