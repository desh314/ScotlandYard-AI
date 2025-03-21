package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.GreedyAStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.scoringstrategies.FastScorer;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Class for a thread that performs a monte carlo simulation.
 */
public class MCSimService implements Callable<Integer> {
    SimulationGameState simGameState;
    boolean initMrXTurn;
    int lookAhead;
    ScoringStrategy scorer;

    /**
     * @param simGameState The initial game state when the monte carlo will begin
     * @param initMrXTurn  Whether it is mr x's turn when the simulation starts
     */
    public MCSimService(SimulationGameState simGameState, boolean initMrXTurn, int lookAhead) {
        this.simGameState = simGameState;
        this.initMrXTurn = initMrXTurn;
//        System.out.println("IS MRX TURN: " + initMrXTurn);
        this.lookAhead = lookAhead;
        this.scorer = new FastScorer(new GreedyAStar());
    }

    /**
     * Selects a random move
     *
     * @param avlMoves The available moves at a particular position
     * @return A random move
     */
    public static Optional<Move> chooseRandomMove(Set<Move> avlMoves) {
        var moves = List.copyOf(avlMoves);
        if (moves.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(moves.get(new Random().nextInt(moves.size())));
    }

//    /**
//     * When an object implementing interface {@code Runnable} is used
//     * to create a thread, starting the thread causes the object's
//     * {@code run} method to be called in that separately executing
//     * thread.
//     * <p>
//     * The general contract of the method {@code run} is that it may
//     * take any action whatsoever.
//     *
//     * @see Thread#run()
//     */
//    @Override
//    public Integer call() {
//
//        SimulationGameState initGameState = simGameState.copy(simGameState);
//
//        int moveCounter = 0;
//        double initialScore = scorer.score(initGameState);
//        double finalScore = initialScore;
//        // Do one full simulation and return its result
//        while (initGameState.getWinner().isEmpty()) {
//
//            if (moveCounter == lookAhead) {
//                finalScore = scorer.score(initGameState);
//                break;
//            }
//
//            Optional<Move> randomMove = chooseRandomMove(initGameState.getAvailableMoves());
//            if (randomMove.isPresent()) {
//                initGameState = initGameState.copy(initGameState).advance(randomMove.get());
//            } else {
//                break;
//            }
//
//            moveCounter++;
//        }
//
//        boolean isMrXWinner = initGameState.getWinner().stream().anyMatch(Piece::isMrX);
//
//        //if mrX turn initially == true and winner of sim is mrX ie isMrXWinner == true then 1
//        //if mrX turn initally == false then detectives are initial player and if isMrXWinner == false then detective have won so return 1
//
//
//        if (initMrXTurn == isMrXWinner) {
//            System.out.println("CORRECT WINNER");
//            return 1;
//        } else {
//            return 0;
//        }
//    }

    @Override
    public Integer call() {
        SimulationGameState runGameState = simGameState.copy(simGameState);
        int moveCount = 0; // The number of moves ahead that we have checked on this rollout

        double initialScore = scorer.score(runGameState);
        double finalScore = initialScore;

        while (runGameState.getWinner().isEmpty()) { // while we have not yet reached a terminal state / the game is not over

            if (moveCount == lookAhead) {
//                System.out.println("Current score: " + scorer.score(runGameState));
                finalScore = scorer.score(runGameState);
                //System.out.println("FinalScore - Init: " + (finalScore - initialScore));
                break;
            }
            // We select a random move and advance it, and increment the move counter.
            Optional<Move> randomMove = chooseRandomMove(runGameState.getAvailableMoves());
            if (randomMove.isEmpty()) {
                break;
            }

            runGameState = runGameState.copy(runGameState).advance(randomMove.get());
            moveCount++;
        }

        //System.out.println(finalScore - initialScore);

        if (initMrXTurn) {
            if (finalScore - initialScore >= 100) {
                return 1;
            } else {
                return 0;
            }
        } else {
            if (finalScore - initialScore <= -100) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
