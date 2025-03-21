package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

/**
 * Interface defining a Visitor compatible with the Monte Carlo tree.
 */
public interface MCNodeVisitor {
    void visit(MCFork fork);
    void visit(MCLeaf leaf);
    void visit(MCRoot mcRoot);
}