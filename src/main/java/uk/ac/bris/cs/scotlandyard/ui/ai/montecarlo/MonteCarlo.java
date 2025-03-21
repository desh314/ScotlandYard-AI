package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.GreedyAStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.naivemontecarlo.NaiveSimulatorDetective;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.naivemontecarlo.NaiveSimulatorMrX;
import uk.ac.bris.cs.scotlandyard.ui.ai.scoringstrategies.FastScorer;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationUtilityFunctions;

import java.util.LinkedList;
import java.util.concurrent.Callable;

import static uk.ac.bris.cs.scotlandyard.ui.ai.minimax.archive.MinimaxStrategy.isMrXTurn;

// Inspired by: https://ai-boson.github.io/mcts

/**
 * Main class implementing the Monte Carlo Search Tree. Expands, updates, and back propagates the result.
 */
public class MonteCarlo implements Callable<MCChild> {

    public static double explorationConst;
    public int numSims;
    public SimulationGameState initGameState;
    public int treeIters;
    public boolean isMrXTurnInitially;
    public int lookAhead;
    public static ScoringStrategy scorer;

    /**
     *
     * @param explorationConst The 'exploration constant', usually ≈ 0.2
     * @param numSims The number of sims to perform and then average, usually ≈7-10
     * @param initGameState The initial game state that the monte carlo starts at
     * @param treeIters The number of times to do a rollout and expand the tree, usually ≈ 1000
     */
    public  MonteCarlo(double explorationConst, int numSims, SimulationGameState initGameState, int treeIters, int lookAhead) {
        this.explorationConst = explorationConst;
        this.numSims = numSims;
        this.initGameState = initGameState;
        this.treeIters = treeIters;
        isMrXTurnInitially = SimulationUtilityFunctions.isMrXTurn(initGameState.getAvailableMoves());
        this.lookAhead = lookAhead;
        this.scorer = new FastScorer(new GreedyAStar());
    }

    /**
     * Calculates the best child node. Finds the winrate for each child and returns the highest.
     * @param root The root node
     * @return The child node with the highest winrate.
     */
    public static MCChild argMaxProb(MCRoot root, SimulationGameState initGameState) {
        MCChild out = root.children.get(0);
        double max = Double.NEGATIVE_INFINITY;

        double initialScore = scorer.score(initGameState);

        for (MCChild child : root.children) {
            SimulationGameState advanced = initGameState.copy(initGameState).advance(child.parentAction);
            if (scorer.score(advanced) - scorer.score(initGameState) > 200  && child.parentAction.commencedBy().isMrX()) {
                continue;
            }

            if (scorer.score(advanced) > scorer.score(initGameState) && child.parentAction.commencedBy().isDetective()) {
                continue;
            }

            System.out.println("not filtering");


            double score = child.numWins;
            if (score >= max) {
                out = child;
                max = score;
            }
        }
        System.out.println(max);
        return out;
    }

    /**
     * Calculates UCB1 for each child node and returns the one with the highest result.
     * @param fork The node whose children are to be checked.
     * @return The child with the highest UCB1 value.
     */
    public static MCChild argmaxUCB1(MCFork fork) {
        if (fork.children.isEmpty()) {
            return fork;
        }
        MCChild out = fork.children.get(0);
        double max = Double.NEGATIVE_INFINITY;

        for (MCChild child : fork.children) {
            double score = child.UCB1(explorationConst);
            if (score >= max) {
                out = child;
                max = score;
            }
        }

        return out;
    }

    /**
     * Calculates UCB1 for each child node of a root, returning the one with the highest value
     * @param root The root whose children are to be checked
     * @return The child of the root with the highest UCB1 value.
     */
    public static MCChild argmaxUCB1(MCRoot root) {
        MCChild out = root.children.get(0);
        double max = Double.NEGATIVE_INFINITY;

        for (MCChild child : root.children) {
            double score = child.UCB1(explorationConst);
            if (score >= max) {
                out = child;
                max = score;
            }
        }

        return out;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public MCChild call() throws Exception {
        return chooseChild();
    }


    /**
     * This function is the main function necessary for Monte Carlo to function; performing all the steps necessary to
     * do an expansion of the tree. It expands the tree, then finds the result and back propagates it up the tree using
     * various vistors. By calling this function multiple times, we are able to produce a full monte carlo tree.
     * @param root The root of the tree to start at.
     */
    public void rollout(MCRoot root) {

        // Run the expanding visitors on the node
        ExpandingMCNodeVisitor expander = new ExpandingMCNodeVisitor();
        root.accept(expander);
        MCNode tail = expander.nodesInPath.get(expander.nodesInPath.size() - 1);
        double outcome = -1;


        // Get the outcome for the tail node
        if (tail.currGameState.getWinner().isEmpty()) {
            //MonteCarloSimulator simulator = new MonteCarloSimulator(numSims, tail.currGameState, isMrXTurnInitially, lookAhead);
            //outcome = simulator.score(tail.currGameState);
            Piece initPiece = ((MCFork) expander.nodesInPath.get(1)).parentAction.commencedBy();
            if (initPiece.isMrX()) {
                //System.out.println("Choosing For MRX");
                outcome = new NaiveSimulatorMrX(1, tail.currGameState).scoreMC(tail.currGameState);
            } else {
                outcome = new NaiveSimulatorDetective(1, tail.currGameState).scoreMC(tail.currGameState, initPiece);
            }


        } else {
            boolean isMrXWinnerTerminalState = tail.currGameState.getWinner().stream().anyMatch(Piece::isMrX);
            if (isMrXWinnerTerminalState == isMrXTurnInitially) {
                outcome = 1;
            }
            else {
                outcome = 0;
            }
        }

        // back propagate the outcome
        UpdatingMCNodeVisitor backpropogator = new UpdatingMCNodeVisitor(outcome);

        tail.accept(backpropogator);
    }

    /**
     * This function is a wrapper function that builds a monte carlo tree, by running rollouts multiple times.
     * @return A root of a monte carlo tree
     */
    public MCRoot buildTree() {
        MCRoot root = new MCRoot(initGameState, new LinkedList<>());
        for (int i = 0; i <= treeIters; i++) {
            rollout(root);
        }
        return root;
    }

    /**
     * A wrapper function that is used to abstract away the full monte carlo simulation. It builds a tree and then
     * selects the highest scoring child.
     * @return
     */
    public MCChild chooseChild() {
        MCRoot tree = buildTree();
        return argMaxProb(tree, initGameState);
    }
}

/*
//building a list: anamorphism
        //folding a list: catamorphism
        //hylomorphism!!!!!!!!!!!! :-}
        //foldr update (rollout leaf) (root.accept(expanding visitor))

 //tree expand [Root, N1, N2, N3] -> calculate rollout on N3 -> backprop by folding over [Root, N1, N2, N3]

 */