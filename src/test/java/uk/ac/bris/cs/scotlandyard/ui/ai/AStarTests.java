package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.AStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;

public class AStarTests {

    @Test
    public void TestAStar() {
        Player mrX = new Player(MRX, defaultMrXTickets(), 1);
        Player detective = new Player(Piece.Detective.RED, defaultDetectiveTickets(), 10);
        Board.GameState g = null;
        try {
            g = MyGameStateFactory.a(new GameSetup(ScotlandYard.standardGraph(), ScotlandYard.STANDARD24MOVES), mrX, ImmutableList.of(detective));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // We create a new game state
        SimulationGameState sgs = new SimulationGameState(g);
        assertThat(new AStar().findDistance(57, 58, sgs)).isEqualTo(1);
        assertThat(new AStar().findDistance(100, 125, sgs)).isEqualTo(2);
        assertThat(new AStar().findDistance(128, 175, sgs)).isEqualTo(3);
    }

}
