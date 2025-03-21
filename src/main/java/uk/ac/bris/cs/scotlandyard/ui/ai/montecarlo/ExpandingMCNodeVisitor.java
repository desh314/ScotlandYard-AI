package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.ArrayList;
import java.util.LinkedList;

import static uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MonteCarlo.argmaxUCB1;

/**
 * Visitor for expanding the monte carlo tree.
 */
public class ExpandingMCNodeVisitor implements MCNodeVisitor {
    //stores the nodes visited, to fold (backpropogate) afterwards
    //store the path taken through the tree
    public ArrayList<MCNode> nodesInPath = new ArrayList<>();

    /**
     * This function implements the visitor's behaviour if we have reached a leaf instead. We just add the node to the
     * path because there is no way to expand it.
     * @param leaf
     */
    @Override
    public void visit(MCLeaf leaf) {
        // This is a terminal node
        nodesInPath.add(leaf);
    }

    //[root, argmax(root), argmax(argmax(root)), fork that is fully expanded]

    /**
     * This function implements the visitor's behaviour if we have reached a root. If unexplored moves is empty,
     * then we add the current node to the path, and then select the child with the highest UCB1 value to explore
     * further. If we have not yet fully expanded, then we pop off the next move, then expand that
     * with all its children.
     * @param mcRoot
     */
    @Override
    public void visit(MCRoot mcRoot) {
        if (mcRoot.unexploredMoves.isEmpty()) {
            //argmax UCB1
            nodesInPath.add(mcRoot);
            argmaxUCB1(mcRoot).accept(this);
        } else {
            // It is not fully expanded.
            // We pop a move off and expand it
            Move chosenMove = mcRoot.unexploredMoves.pop();
            SimulationGameState advancedGameState = mcRoot.currGameState.copy(mcRoot.currGameState).advance(chosenMove);

            MCChild expandedChild = null;
            if (!advancedGameState.getWinner().isEmpty()) {
                // the game is over. The new node should be added as a leaf node
                expandedChild = new MCLeaf(advancedGameState, mcRoot, chosenMove);
            } else {
                expandedChild = new MCFork(mcRoot, chosenMove, new LinkedList<>(), advancedGameState);
            }
            mcRoot.children.add(expandedChild);
            nodesInPath.add(mcRoot);
            nodesInPath.add(expandedChild);
        }
    }

    /**
     * This function implements the visitor's behaviour if we have reached a fork. If unexplored moves is empty,
     * then we add the current node to the path, and then select the child with the highest UCB1 value to explore
     * further. If we have not yet fully expanded, then we pop off the next move, then expand that
     * with all its children.
     * @param fork
     */
    @Override
    public void visit(MCFork fork) {
        //if not fully expanded:
        //expand by popping of a move from avlMoves.
        //expand the nodes:
        //if it is fully expanded:
        //this is UCB1
        //this.getBestChild.accept(this)

        if (fork.unexploredMoves.isEmpty()) {
            //argmax UCB1
            nodesInPath.add(fork);
            argmaxUCB1(fork).accept(this);
        } else {
            // It is not fully expanded.
            // We pop a move off and expand it
//                nodesInPath.add(fork);
            Move chosenMove = fork.unexploredMoves.pop();
            SimulationGameState advancedGameState = fork.currGameState.copy(fork.currGameState).advance(chosenMove);

            MCChild expandedChild = null;
            if (!advancedGameState.getWinner().isEmpty()) {
                // the game is over
                expandedChild = new MCLeaf(advancedGameState, fork, chosenMove);
            } else {

                expandedChild = new MCFork(fork, chosenMove, new LinkedList<>(), advancedGameState);
            }

            fork.children.add(expandedChild);
//                nodesInPath.add(fork);
            nodesInPath.add(expandedChild);
        }
    }
}