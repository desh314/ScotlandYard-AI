package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCFork;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCLeaf;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCNodeVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCRoot;

/**
 * Visitor for implementing back propagation in the monte carlo tree
 */
public class UpdatingMCNodeVisitor implements MCNodeVisitor { //backprop

    double outcome;

    /**
     *
     * @param simOutcome The outcome that is to be back propagated
     */
    public UpdatingMCNodeVisitor(double simOutcome) {
        outcome = simOutcome;
    }

    /**
     * This function implements the visitor's behaviour for a fork. We just update it and then go up a level
     * to its parent.
     * @param fork
     */
    @Override
    public void visit(MCFork fork) {
        fork.update(outcome);
        fork.parent.accept(this);
    }

    /**
     * This function implements the visitor's behaviour for a leaf. We update it and then go up a level to its parent.
     * @param leaf
     */
    @Override
    public void visit(MCLeaf leaf) {
        leaf.update(outcome);
        leaf.parent.accept(this);
    }

    /**
     * This function implements the visitor's behaviour for a root. We update it, but it has no parents so
     * we stop there.
     * @param mcRoot
     */
    @Override
    public void visit(MCRoot mcRoot) {
        mcRoot.update(outcome);
    }
}