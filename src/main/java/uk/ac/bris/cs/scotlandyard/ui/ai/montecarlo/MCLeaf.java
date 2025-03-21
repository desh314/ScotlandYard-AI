package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.LinkedList;

/**
 * Class representing a monte carlo node with a parent but no children; this represents a terminal state
 */
public class MCLeaf extends MCChild {
    public LinkedList<Move> unexploredMoves;

    public MCLeaf(SimulationGameState currGameState, MCFork parent, Move parentAction) {
        this.numVisits = 0;
        this.numWins = 0;
        this.currGameState = currGameState;
        this.parent = parent;
        this.parentAction = parentAction;
    }

    public MCLeaf(SimulationGameState currGameState, MCRoot parent, Move parentAction) {
        this.numVisits = 0;
        this.numWins = 0;
        this.currGameState = currGameState;
        this.parent = parent;
        this.parentAction = parentAction;
    }

    @Override
    public void accept(MCNodeVisitor visitor) {
        visitor.visit(this);
    }
}