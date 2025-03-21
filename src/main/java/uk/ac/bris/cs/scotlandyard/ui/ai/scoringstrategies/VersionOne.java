package uk.ac.bris.cs.scotlandyard.ui.ai.scoringstrategies;

import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.AStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.GreedyAStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.List;

/**
 * This class contains the initial scoring function that we used
 */
public class VersionOne implements ScoringStrategy {
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

        List<Integer> detectiveLoc = simGameState.getDetectives().stream().map(Player::location).toList();
        for (int l : detectiveLoc) {
            sum += new GreedyAStar().findDistance(mrXLoc, l, simGameState) * 30;
        }


        //sort the distances such that the other detectives should follow the detective that is closest to mrX
        //ie, instead of doing a double for loop just choose the closes detective to mrX to calculate the distance deltas from.

        // Need to disregard this so that it doesnt get stuck with mr x far away
        // Average location of detectives - location of mr x compared to threshold
        // Global heuristic to detect centre of mass of detectives vs centre of mass of mr x
        // if mr x is suspected to be close, we circle
        if (sum < (100 * 30)) {

            for (int i : detectiveLoc) {
                double diff = 0;
                for (int j : detectiveLoc) {
                    diff += Math.abs(i - j) * 0.357;
                }
                sum += diff;
            }
        }
        //double detectiveProximity = 0;

        //detectiveProximity = Math.sqrt(detectiveLoc.stream().mapToDouble(x -> x^2).sum());


        // We should also really prefer locations where there are more available moves, should try to prevent it from getting stuck
        sum += simGameState.getAvailableMoves().size() * 1.1;
        return (sum);

        // If it is a reveal round, we want mr x to be in a position where it can move away quickly;
    }
}
