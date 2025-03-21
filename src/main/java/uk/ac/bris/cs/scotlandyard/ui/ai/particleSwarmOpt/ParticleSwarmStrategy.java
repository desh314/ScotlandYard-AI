package uk.ac.bris.cs.scotlandyard.ui.ai.particleSwarmOpt;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.factories.MinimaxFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.archive.MinimaxStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.scoringstrategies.FastScorer;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

/**
 * This class implements a particle swarm method as a strategy
 */
public class ParticleSwarmStrategy implements MoveSelectingStrategy {

    private final MoveSelectingStrategy mrXStrat;
    private final int swarmEpochs;
    private final double c1;
    private final double c2;
    private final double w;

    /**
     * @param swarmEpochs The number of epochs
     * @param mrXStrat The move selecting strategy to use for Mr X.
     */
    public ParticleSwarmStrategy(int swarmEpochs, MoveSelectingStrategy mrXStrat, double c1, double c2, double w) {
        this.swarmEpochs = swarmEpochs;
        this.mrXStrat = mrXStrat;
        this.c1 = c1;
        this.c2 = c2;
        this.w = w;
    }

    @Override
    public Move selectForMrX(Board board) {
        return mrXStrat.selectForMrX(board);
    }

    @Override
    public Move selectForDetective(Board board) {
        PSO pso = new PSO(new SimulationGameState(board), c1, c2, w);
        return pso.chooseMove(swarmEpochs);
    }
}
