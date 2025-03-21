package uk.ac.bris.cs.scotlandyard.ui.ai.minimax;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationUtilityFunctions;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

/**
 * Minimax service class that is used to implement multithreading for Minimax.
 */

// Sources for the psuedocode and explanations:
// https://youtu.be/l-hh51ncgDI?feature=shared
// https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
// https://en.wikipedia.org/wiki/Minimax
public class MinimaxServiceFinal implements Callable<Pair<Move, Double>> {

    ScoringStrategy scorer;
    SimulationGameState board;
    int depth;
    double alpha;
    double beta;
    Move move;
    int mrXTopNMoves;
    int detectiveTopNMoves;

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Pair<Move, Double> call() throws Exception {
        return new Pair<>(this.move, minimax());
    }

    /**
     *
     * @param board {@link SimulationGameState} to start minimax from
     * @param depth The depth of minimax from this point
     * @param alpha
     * @param beta
     * @param scorer The {@link ScoringStrategy} to use to score minimax
     * @param move The parent {@link Move} that led to this position
     * @param mrXTopNMoves How many of the top moves to consider for mr X
     * @param detectiveTopNMoves How many of the top moves to consider for the detectives
     */
    public MinimaxServiceFinal(SimulationGameState board, int depth, double alpha, double beta, ScoringStrategy scorer, Move move, int mrXTopNMoves, int detectiveTopNMoves) {
        this.scorer = scorer;
        this.board = board;
        this.depth = depth;
        this.alpha = alpha;
        this.beta = beta;
        this.move = move;
        this.mrXTopNMoves = mrXTopNMoves;
        this.detectiveTopNMoves = detectiveTopNMoves;
    }

    /**
     * Performs minimax
     * @return
     */
    public double minimax() {
        if (depth == 0 || !board.getWinner().isEmpty()) {
            return scorer.score(board);
        }
        if (SimulationUtilityFunctions.isMrXTurn(board.getAvailableMoves())) {
            double val = Double.NEGATIVE_INFINITY;

            // We only consider the top x moves because otherwise the branching factor is simply too high
            // This means that we score all the boards that would result from the current position
            // and only pursue the best ones.
            PriorityQueue<Pair<Move, Double>> selectedMoves = new PriorityQueue<>(Comparator.<Pair<Move, Double>>comparingDouble(d -> d.right()).reversed());

            selectedMoves.addAll(board.getAvailableMoves()
                    .stream()
                    .<Pair<Move, Double>>map(m -> new Pair<>(m, scorer.score(board.copy(board).advance(m))))
                    .toList());

            for (int i = 0; i < Integer.min(selectedMoves.size() - 1, mrXTopNMoves); i++) {
                Move current = selectedMoves.poll().left();
                SimulationGameState deep = board.copy(board).advance(current);
//                deep.flag = true;
                val = Double.max(val, new MinimaxServiceFinal(deep, depth - 1, alpha, beta, scorer, current, this.mrXTopNMoves, this.detectiveTopNMoves).minimax());

                if (val > beta) {
                    break;
                }
                alpha = Double.max(alpha, val);
            }
            return val;
        }
        else {
            double val = Double.POSITIVE_INFINITY;

            // For the detective to be able to work, we need to toss the bottom like 9 moves because without that the
            // branching factor is way too high.

            // We score all the boards that would result from the available moves, we sort the list, and then we
            // choose the top 2 best moves for the detective to pursue further.

            PriorityQueue<Pair<Move, Double>> selectedMoves = new PriorityQueue<>(Comparator.<Pair<Move, Double>>comparingDouble(d -> d.right()));
            selectedMoves.addAll(board.getAvailableMoves()
                    .stream()
                    .<Pair<Move, Double>>map(m -> new Pair<>(m, scorer.score(board.copy(board).advance(m))))
                    .toList());
//            System.out.println(selectedMoves);

            for (int i = 0; i < Integer.min((selectedMoves.size() - 1), detectiveTopNMoves); i++) {
                Move current = selectedMoves.poll().left();
                SimulationGameState deep = board.copy(board).advance(current);

                val = Double.min(val, new MinimaxServiceFinal(deep, depth - 1, alpha, beta, scorer, current, this.mrXTopNMoves, this.detectiveTopNMoves).minimax());

                if (val < alpha) {
                    break;
                }
                beta = Double.min(beta, val);
            }
            return val;
        }
    }

}
