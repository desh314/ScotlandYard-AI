package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.archive;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import static uk.ac.bris.cs.scotlandyard.ui.ai.minimax.archive.MinimaxStrategy.isMrXTurn;

// Sources for the psuedocode and explanations:
// https://youtu.be/l-hh51ncgDI?feature=shared
// https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
// https://en.wikipedia.org/wiki/Minimax

public class MinimaxService implements Callable<Pair<Move, Double>> {

    private SimulationGameState simGameState;
    private int depth;
    private double alpha;
    private double beta;
    private Move move;
    private ScoringStrategy scoringStrategy;


    public MinimaxService(SimulationGameState simGameState, int depth, double alpha, double beta, Move move, ScoringStrategy scoringStrategy) {
        this.simGameState = simGameState;
        this.depth = depth;
        this.alpha = alpha;
        this.beta = beta;
        this.move = move;
        this.scoringStrategy = scoringStrategy;
    }

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
     * This is the actual implementation of minimax that is used when the thread is started.
     * @return The result of minimax
     */
    public Double minimax() {
        // Original psuedocode from https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
        //stopping condition
        if (depth == 0 || (!simGameState.getWinner().isEmpty())) {
            return scoringStrategy.score(simGameState);
        }

        if (isMrXTurn(this.simGameState)) {
            //MrX is the maximizing player
            double maxEval = Double.NEGATIVE_INFINITY;

            HashSet<Integer> previousMoveDests = new HashSet<>();

            LinkedList<Pair<Move, Double>> selected = new LinkedList<>();
            for (Move childMove : this.simGameState.getAvailableMoves()) {
                selected.push(new Pair<>(childMove, scoringStrategy.score(this.simGameState.copy(this.simGameState).advance(childMove))));
            }
            selected.sort(Comparator.<Pair<Move, Double>>comparingDouble(d -> d.right()).reversed());
            int limit = Integer.min(selected.size() - 1, 3);
            // To lower the branching factor, we select only a few of the possible moves at each position
            // Mr X is able to check a lot more moves
            // We check the best n moves, according to the scoring function
            // This helps because then we don't bother checking obviously stupid stuff like moving towards
            // the detectives.
            for (Pair<Move, Double> p : selected.subList(0, limit)) {
                int dest = p.left().accept(new MoveDestinationVisitor());
                if (p.left().source() == dest) {
                    // Filter out moves that are just wiggles
                    continue;
                }
                if (previousMoveDests.contains(dest)) {
                    // Filter out moves that duplicate their destinations
                    continue;
                } else {
                    previousMoveDests.add(dest);
                    SimulationGameState updated = this.simGameState.copy(this.simGameState).advance(p.left());
                    Double eval = new MinimaxService(updated, this.depth - 1, alpha, beta, p.left(), scoringStrategy).minimax();
                    maxEval = Math.max(maxEval, eval);

                    //alpha pruning
                    alpha = Math.max(alpha, eval);
                    if (beta >= alpha) {
                        break;
                    }
                }
            }
            return maxEval;
        } else {
            //Detectives are the minimizing player
            double minEval = Double.POSITIVE_INFINITY;
            LinkedList<Pair<Move, Double>> selected = new LinkedList<>();
            for (Move childMove : this.simGameState.getAvailableMoves()) {
                selected.push(new Pair<>(childMove, scoringStrategy.score(this.simGameState.copy(this.simGameState).advance(childMove))));
            }
            selected.sort(Comparator.<Pair<Move, Double>>comparingDouble(d -> d.right()));
            int limit = Integer.min(selected.size() - 1, 1);
            for (Pair<Move, Double> childMove : selected.subList(0, limit)) {
                SimulationGameState updated = this.simGameState.copy(this.simGameState).advance(childMove.left());
                Double eval = new MinimaxService(updated, this.depth - 1, alpha, beta, childMove.left(), scoringStrategy).minimax();
                minEval = Math.min(eval, minEval);

                //beta pruning
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }

            return minEval;
        }
    }
}
