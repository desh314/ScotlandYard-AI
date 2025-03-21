package uk.ac.bris.cs.scotlandyard.ui.ai.factories;

import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.particleSwarmOpt.ParticleSwarmStrategy;

/**
 * Specialised {@link AbstractFactory} for producing {@link ParticleSwarmStrategy}.
 */
public class ParticleSwarmFactory implements AbstractFactory {

    private final int swarmEpochs;
    private final MoveSelectingStrategy mrXStrat;
    private final double c1;
    private final double c2;
    private final double w;

    /**
     *
     * @param swarmEpochs The depth to use in the minimax search
     * @param mrXStrat The strategy to use for Mr X.
     */
    public ParticleSwarmFactory(int swarmEpochs, MoveSelectingStrategy mrXStrat, double c1, double c2, double w) {
        this.swarmEpochs = swarmEpochs;
        this.mrXStrat = mrXStrat;
        this.c1 = c1;
        this.c2 = c2;
        this.w = w;
    }

    public ParticleSwarmFactory() {
        this.swarmEpochs = 4000;
        this.mrXStrat = new MinimaxFactory().getMoveSelectingStrategy();
        this.c1 = 0.25;
        this.c2 = 0.05;
        this.w = 0.6;
    }

    @Override
    public MoveSelectingStrategy getMoveSelectingStrategy() {
        return new ParticleSwarmStrategy(swarmEpochs, mrXStrat, c1, c2, w);
    }
}
