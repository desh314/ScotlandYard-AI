package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.AStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.GreedyAStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.factories.MinimaxFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.factories.MonteCarloFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.factories.ParticleSwarmFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCSimService;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.naivemontecarlo.NaiveSimulatorDetective;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.naivemontecarlo.NaiveSimulatorMrX;
import uk.ac.bris.cs.scotlandyard.ui.ai.particleSwarmOpt.ParticleSwarmStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.scoringstrategies.FastScorer;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import javax.annotation.Nonnull;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static uk.ac.bris.cs.scotlandyard.ui.ai.minimax.archive.MinimaxStrategy.isMrXTurn;

/**
 * Client class for the Scotland Yard AI
 */
public class Client implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "Herlock Sholmes";
    }

    @Override
    public void onStart() {
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        SimulationGameState simGameState = new SimulationGameState(board);

        if (simGameState.getMrX().location() == -1) {
            System.out.println("Not enough information");
            return MCSimService.chooseRandomMove(board.getAvailableMoves()).get();
        }

        if (board.getAvailableMoves().size() == 1) {
            return board.getAvailableMoves().asList().get(0); // If there is only one move then take it.
        }

        //MONTE CARLO TREE SEARCH
        return new MoveSelector().chooseMove(new MonteCarloFactory().getMoveSelectingStrategy(), board);

        //MINIMAX
//        return new MoveSelector().chooseMove(new MinimaxFactory().getMoveSelectingStrategy(), board);

        //PSO
//        return new MoveSelector().chooseMove(new ParticleSwarmFactory().getMoveSelectingStrategy(), board);



    }
}
