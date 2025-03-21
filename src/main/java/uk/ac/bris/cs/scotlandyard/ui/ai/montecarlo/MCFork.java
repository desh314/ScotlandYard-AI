package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.LinkedList;

/**
 * Class representing a monte carlo node with a parent and children
 */
public class MCFork extends MCChild {
    public LinkedList<MCChild> children;
    public LinkedList<Move> unexploredMoves;

    public MCFork(MCFork parent, Move parentAction, LinkedList<MCChild> children, SimulationGameState currGameState) {
        this.children = children;
        this.numVisits = 0;
        this.numWins = 0;
        this.currGameState = currGameState;
        this.parent = parent;
        this.parentAction = parentAction;
        this.unexploredMoves = new LinkedList<>(currGameState.getAvailableMoves());
    }

    public MCFork(MCRoot parent, Move parentAction, LinkedList<MCChild> children, SimulationGameState currGameState) {
        this.children = children;
        this.numVisits = 0;
        this.numWins = 0;
        this.currGameState = currGameState;
        this.parent = parent;
        this.parentAction = parentAction;
        this.unexploredMoves = new LinkedList<>(currGameState.getAvailableMoves());
    }

    @Override
    public void accept(MCNodeVisitor visitor) {
        visitor.visit(this);
    }
}