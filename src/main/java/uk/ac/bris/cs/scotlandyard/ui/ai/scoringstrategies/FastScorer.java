package uk.ac.bris.cs.scotlandyard.ui.ai.scoringstrategies;

import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.AStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.DistanceStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.GreedyAStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.List;

import static uk.ac.bris.cs.scotlandyard.ui.ai.minimax.archive.MinimaxStrategy.isMrXTurn;

/**
 * This class implements a decent, fast scoring strategy. The score is based on the distances of the detectives
 * to Mr X.
 */
public class FastScorer implements ScoringStrategy {

    private final DistanceStrategy distanceStrategy;

    public FastScorer(DistanceStrategy distanceStrategy) {
        this.distanceStrategy = distanceStrategy;
    }



    /**
     * This function performs the scoring
     * @param simGameState The current game state.
     * @return The score
     */
    @Override
    public Double score(SimulationGameState simGameState) {
        //to make the score reflect proximity, need to add more weighting to closer distances.

        double sum = 0;
        int mrXLoc = simGameState.getMrXLocation();

        if (mrXLoc == -1 || simGameState.previousTurnWasMrX) {
            //System.out.println("Flag is set! using SGS.getMrX().location()");
            //System.out.println(simGameState.getMrX());
            mrXLoc = simGameState.getMrX().location();
        }

        //System.out.println("Correct MrX Location: " + mrXLoc);

        List<Integer> detectiveLoc = simGameState.getDetectives().stream().map(Player::location).toList();
        for (int l : detectiveLoc) {
            sum += distanceStrategy.findDistance(mrXLoc, l, simGameState);
        }

        return (sum);

    }


}


//            sum += precalculator.aStarLookup.get(new Pair<>(mrXLoc, l));
//        for (int node : simGameState.getSetup().graph.adjacentNodes(mrXLoc)) {
//            if (simGameState.getSetup().graph.edgeValue(mrXLoc, node).orElseThrow().contains(ScotlandYard.Transport.BUS)) {
//                sum += 1000;
//            }
//            else if (simGameState.getSetup().graph.edgeValue(mrXLoc, node).orElseThrow().contains(ScotlandYard.Transport.UNDERGROUND)) {
//                sum += 1500;
//            }
//        }

//        for (int i : detectiveLoc) {
//            double diff = 0;
//            for (int j : detectiveLoc) {
//                diff += Math.abs(i - j) * 0.357;
//            }
//            sum += diff;
//        }

//        sum += simGameState.getAvailableMoves().size() * 1.1;