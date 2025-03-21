package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.naivemontecarlo;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.GreedyAStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.scoringstrategies.FastScorer;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.Optional;

import static uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCSimService.chooseRandomMove;

/**
 * Class that simulates Scotland Yard from the perspective of MrX
 */
public class NaiveSimulatorMrX {
    int numSim;
    SimulationGameState initGameState;
    ScoringStrategy scorer;

    /**
     * Constructor to playout Scotland Yard from the perspective of MrX
     * @param numSims number of simulations to average from
     * @param initGameState initial game state to begin the simulation
     */
    public NaiveSimulatorMrX(int numSims, SimulationGameState initGameState) {
        this.numSim = numSims;
        this.initGameState = initGameState;
        scorer = new FastScorer(new GreedyAStar());
    }

    /**
     * Simulation of Scotland Yard
     * @param advancedGameState game state to begin the simulation
     * @return 1 if its a victory for the initial piece and 0 if its not
     */
    public int runSimMrX(SimulationGameState advancedGameState) {

        while (advancedGameState.getWinner().isEmpty()) {

            Optional<Move> m = chooseRandomMove(advancedGameState.getAvailableMoves());
            if (m.isEmpty()) {
                break;
            }

            advancedGameState = advancedGameState.copy(advancedGameState).advance(m.get());
        }

        boolean isMrXWinner = advancedGameState.getWinner().stream().anyMatch(x -> x.isMrX());
        if (isMrXWinner) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Used for scoring moves to implement a naive monte carlo simulation strategy
     * @param move move that should be scored
     * @return the average probability that the move is winning
     */
    public double scoreMove(Move move) {
        double outNum = 0;
        for (int i = 0; i < numSim; i++) {
            SimulationGameState advancedGameState = initGameState.copy(initGameState).advance(move);
            outNum += runSimMrX(advancedGameState);
        }

        return outNum / numSim;

    }

    /**
     * Scoring function used for monte carlo rollouts. Does numSim simulations and results the aggregate result.
     * @param simGameState initial game state to run the simulations on
     * @return 1 if the simulations in a victory, 0 otherwise
     */
    public int scoreMC(SimulationGameState simGameState) {
        double outNum = 0;
        for (int i = 0; i < numSim; i++) {
            outNum += runSimMrX(simGameState);
        }

        if (outNum >= numSim  - outNum) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Chooses the move with the highest possible probability of winning to implement naive monte carlo simulation
     * @return move with highest probability
     */
    public Move argMax() {
        double maxScore = Double.NEGATIVE_INFINITY;
        Move outMove = initGameState.getAvailableMoves().stream().toList().get(0);

        for (Move m : initGameState.getAvailableMoves()) {
            double score = scoreMove(m);
            if (score >= maxScore) {
                maxScore = score;
                outMove = m;
            }
        }

        System.out.println(maxScore);

        return outMove;
    }
}
