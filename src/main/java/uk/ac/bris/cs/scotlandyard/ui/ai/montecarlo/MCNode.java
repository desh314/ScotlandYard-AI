package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCNodeVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

/**
 * Class representing an abstract node in the Monte Carlo tree
 */
public abstract class MCNode {
    public SimulationGameState currGameState;
    public double numWins;
    public double numVisits;

    abstract public void accept(MCNodeVisitor visitor);

    public void update(double outcome) {
        numVisits += 1;
        numWins += outcome;
    }
}