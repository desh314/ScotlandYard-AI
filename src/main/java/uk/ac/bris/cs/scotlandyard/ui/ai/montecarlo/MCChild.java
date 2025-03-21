package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 * The abstract class representing a monte carlo node with a parent.
 */
public abstract class MCChild extends MCNode {
    public MCNode parent;
    public Move parentAction;
    public abstract void accept(MCNodeVisitor visitor);

    public double UCB1(double explorationConstant) {
        return (this.numWins / this.numVisits) +  explorationConstant * Math.sqrt(Math.log(this.parent.numVisits)/this.numVisits);
    }
}