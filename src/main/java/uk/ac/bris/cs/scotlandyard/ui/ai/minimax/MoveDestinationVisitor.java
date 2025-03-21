package uk.ac.bris.cs.scotlandyard.ui.ai.minimax;

import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 * Visitor used to find the final destination of a Move without using reflection
 */
public class MoveDestinationVisitor implements Move.Visitor<Integer> {
    @Override
    public Integer visit(Move.SingleMove move) {
        return move.destination;
    }

    @Override
    public Integer visit(Move.DoubleMove move) {
        return move.destination2;
    }
}
