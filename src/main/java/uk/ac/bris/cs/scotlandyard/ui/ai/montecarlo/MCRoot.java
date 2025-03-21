package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.LinkedList;

/**
 * Class representing a node that is a root in the monte carlo tree; it has no parents, it is at the very top.
 * We have a special case for the root so that we are able to tell when we have reached it when back propagating.
 */
public class MCRoot extends MCNode {
    public LinkedList<MCChild> children;
    public SimulationGameState currGameState;
    public LinkedList<Move> unexploredMoves;

    public MCRoot(SimulationGameState currGameState, LinkedList<MCChild> children) {
        this.unexploredMoves = new LinkedList<>(currGameState.getAvailableMoves());
        this.currGameState = currGameState;
        this.numWins = 0;
        this.numVisits = 0;
        this.children = children;
    }

    @Override
    public void accept(MCNodeVisitor visitor) {
        visitor.visit(this);
    }
}