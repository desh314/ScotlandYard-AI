package uk.ac.bris.cs.scotlandyard.ui.ai.factories;

import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCStrategy;

/**
 * Specialised {@link AbstractFactory} for producing {@link MCStrategy}.
 */
public class MonteCarloFactory implements AbstractFactory {

    private final double initExplorationCost;
    private final int numParallelStrategies;
    private final int numSims;
    private final int treeIters;
    private final int lookAhead;

    /**
     *
     * @param initExplorationCost The exploration cost to use in Monte Carlo
     * @param numParallelStrategies The number of parallel MonteCarlo instances to run
     * @param numSims The number of simulations to average each time (in Simulator)
     * @param treeIters The number of times to rollout when building tree
     * @param lookAhead the number of lookaheads
     */
    public MonteCarloFactory(double initExplorationCost, int numParallelStrategies, int numSims, int treeIters, int lookAhead) {
        this.initExplorationCost = initExplorationCost;
        this.numParallelStrategies = numParallelStrategies;
        this.numSims = numSims;
        this.treeIters = treeIters;
        this.lookAhead = lookAhead;
    }

    /**
     * Returns a Monte Carlo Factory that produces Monte Carlo strategies with reasonable defaults.
     */
    public MonteCarloFactory() {
        this.initExplorationCost = 0.3;
        this.numParallelStrategies = 10;
        this.numSims = 1;
        this.treeIters = 8000;
        this.lookAhead = 8;
    }

    @Override
    public MoveSelectingStrategy getMoveSelectingStrategy() {
        return new MCStrategy(initExplorationCost, numParallelStrategies, numSims, treeIters, lookAhead);
    }
}
