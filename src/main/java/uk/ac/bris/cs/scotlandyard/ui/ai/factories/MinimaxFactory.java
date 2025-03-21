package uk.ac.bris.cs.scotlandyard.ui.ai.factories;

import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.AStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.GreedyAStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MinimaxFinal;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.archive.MinimaxStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.scoringstrategies.FastScorer;

/**
 * Specialised {@link AbstractFactory} for producing {@link MinimaxStrategy}.
 */
public class MinimaxFactory implements AbstractFactory {

    private final int depth;
    private final ScoringStrategy scoringStrategy;
    private final int mrXTopNMoves;
    private final int detectiveTopNMoves;

    /**
     *
     * @param depth The depth to use in minimax
     * @param scoringStrategy The {@link ScoringStrategy} to use when doing minimax
     * @param mrXTopNMoves How many of the top moves to consider for the detectives
     * @param detectiveTopNMoves How many of the top moves to consider for the detectives
     */
    public MinimaxFactory(int depth, ScoringStrategy scoringStrategy, int mrXTopNMoves, int detectiveTopNMoves) {
        this.depth = depth;
        this.scoringStrategy = scoringStrategy;
        this.mrXTopNMoves = mrXTopNMoves;
        this.detectiveTopNMoves = detectiveTopNMoves;
    }

    public MinimaxFactory() {
        this.depth = 6;
        this.scoringStrategy = new FastScorer(new GreedyAStar());
        this.detectiveTopNMoves = 1;
        this.mrXTopNMoves = 4;
    }

    @Override
    public MoveSelectingStrategy getMoveSelectingStrategy() {
        return new MinimaxFinal(depth, scoringStrategy, mrXTopNMoves, detectiveTopNMoves);
    }
}
