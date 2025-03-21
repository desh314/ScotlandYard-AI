package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import static uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationUtilityFunctions.isMrXTurn;

/**
 * Uses a {@link MoveSelectingStrategy} to choose a move
 */
public class MoveSelector {

    public Move chooseMove(MoveSelectingStrategy strategy, Board board) {

        if (isMrXTurn(board.getAvailableMoves())) {
            System.out.println(" -- MRX TURN --");
            return strategy.selectForMrX(board);
        }
        else {
            System.out.println(" -- DET TURN --");
            return strategy.selectForDetective(board);
        }

    }

}
