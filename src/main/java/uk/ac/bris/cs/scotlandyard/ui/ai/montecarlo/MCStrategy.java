package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Class implementing a {@link MoveSelectingStrategy} using monte carlo search trees.
 */
public class MCStrategy implements MoveSelectingStrategy {

    private final double initExplorationCost;
    private final int numParallelInstances;
    private final int numSims;
    private final int treeIters;
    private final int lookAhead;

    /**
     *
     * @param initExplorationCost The 'exploration cost' for Monte Carlo
     * @param numParallelInstances The number of parallel instances to run, whose results' maximum is then used
     * @param numSims The number of simulations to average
     * @param treeIters The number of times to rollout
     * @param lookAhead The number of lookaheads
     */
    public MCStrategy(double initExplorationCost, int numParallelInstances, int numSims, int treeIters, int lookAhead) {
        this.initExplorationCost = initExplorationCost;
        this.numParallelInstances = numParallelInstances;
        this.numSims = numSims;
        this.treeIters = treeIters;
        this.lookAhead = lookAhead;
    }

    /**
     * This function uses multithreading to select a move. We perform the monte carlo simulation several times in
     * parallel, and we pick the move that results in the highest winning probability.
     * @param board The initial board state
     * @param initExplorationCost The 'exploration cost' parameter, usually ≈ 0.2
     * @return The move that results in the highest win probability
     */
//    public Move chooseMultiThreadedMove(Board board, double initExplorationCost) {
//        ExecutorService threadPool = Executors.newCachedThreadPool();
//        LinkedList<MonteCarlo> tasks = new LinkedList<>();
//
//        // Add the threads to the task list
//        for (int i = 0; i < numParallelInstances; i++) {
//            tasks.add(new MonteCarlo(initExplorationCost, numSims, new SimulationGameState(board), treeIters, lookAhead, ));
//        }
//
//        try {
//            List<Future<MCChild>> results = threadPool.invokeAll(tasks); // Start all threads
//
//            Move outMove = results.get(0).get().parentAction;
//            double max = Double.NEGATIVE_INFINITY;
//
//            // Select the best move
//            for (Future<MCChild> r : results) {
//                double currentResult = r.get().numWins / r.get().numVisits;
//                if (currentResult > max) {
//                    outMove = r.get().parentAction;
//                    max = currentResult;
//                }
//            }
//
//            threadPool.shutdownNow();
//            threadPool.awaitTermination(0, TimeUnit.SECONDS);
//            return outMove;
//
//        } catch (InterruptedException e) {
//            return board.getAvailableMoves().stream().findFirst().orElseThrow();
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public Move chooseMove (Board board, double initExplorationCost) {
        SimulationGameState initGameState = new SimulationGameState(board);

//        Move outMove = initGameState.getAvailableMoves().stream().toList().get(0);
//        double maxScore = Double.NEGATIVE_INFINITY;
//
//        for (Move m : initGameState.getAvailableMoves()) {
//            System.out.println("Executed Move: " + m);
//            MCChild scoreNode = new MonteCarlo(initExplorationCost, numSims, initGameState, treeIters, lookAhead, outMove.commencedBy()).chooseChild();
//            double score = scoreNode.numWins / scoreNode.numVisits;
//            if (score >= maxScore) {
//                maxScore = score;
//                outMove = m;
//            }
//        }

        MCChild scoreNode = new MonteCarlo(initExplorationCost, numSims, initGameState, treeIters, lookAhead).chooseChild();

        if (!scoreNode.currGameState.getWinner().isEmpty())

            return ((MCLeaf) scoreNode).parentAction;
        else {
            return ((MCFork) scoreNode).parentAction;
        }
    }

    /**
     * Wrapper function that calls {@link #(Board, double)}
     * @param board The initial board state
     * @return The best move
     */
    @Override
    public Move selectForMrX(Board board) {
        return chooseMove(board, 2.5);
    }

    /**
     * Wrapper function that calls {@link #(Board, double)}
     * @param board The initial board state
     * @return The best move
     */
    @Override
    public Move selectForDetective(Board board) {
        return chooseMove(board, 0.1);
    }
}
