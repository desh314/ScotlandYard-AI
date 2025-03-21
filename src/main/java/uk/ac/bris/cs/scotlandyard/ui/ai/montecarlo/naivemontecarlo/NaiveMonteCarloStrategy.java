package uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.naivemontecarlo;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;

import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

public class NaiveMonteCarloStrategy implements MoveSelectingStrategy {
    SimulationGameState simGameState;
    int numSims;

    public NaiveMonteCarloStrategy(SimulationGameState initGameState, int numSims) {
        this.simGameState = initGameState;
        this.numSims = numSims;
    }

    @Override
    public Move selectForMrX(Board board) {
        return new NaiveSimulatorMrX(60, simGameState).argMax();
    }

    @Override
    public Move selectForDetective(Board board) {
        return new NaiveSimulatorDetective(60, simGameState).argMax();
    }
}
