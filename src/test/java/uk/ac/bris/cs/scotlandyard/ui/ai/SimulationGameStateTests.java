package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.io.IOException;

import static org.testfx.assertions.api.Assertions.assertThat;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public class SimulationGameStateTests {

    @Test
    public void TestSimulationGameStateMrXLocationCorrectInitially() throws IOException {
        Player mrX = new Player(MRX, defaultMrXTickets(), 1);
        Player detective = new Player(Piece.Detective.RED, defaultDetectiveTickets(), 10);
        Board.GameState g = MyGameStateFactory.a(new GameSetup(ScotlandYard.standardGraph(), ScotlandYard.STANDARD24MOVES), mrX, ImmutableList.of(detective));

        // We create a new game state
        SimulationGameState sgs = new SimulationGameState(g);
        // Then we check that the location is correct initially
        assertThat(sgs.getMrXLocation()).isEqualTo(1);
    }

    @Test
    public void TestSimulationGameStateMrXLocationCorrectForDetectives() throws IOException {
        Player mrX = new Player(MRX, defaultMrXTickets(), 1);
        Player detective = new Player(Piece.Detective.RED, defaultDetectiveTickets(), 10);
        Board.GameState g = MyGameStateFactory.a(new GameSetup(ScotlandYard.standardGraph(), ScotlandYard.STANDARD24MOVES), mrX, ImmutableList.of(detective));
//        g = g.advance(g.getAvailableMoves().stream().findFirst().orElseThrow());

        // We create a new game state
        SimulationGameState sgs = new SimulationGameState(g);
        sgs = sgs.copy(sgs).advance(sgs.getAvailableMoves().stream().findFirst().orElseThrow());
        sgs = sgs.copy(sgs).advance(sgs.getAvailableMoves().stream().findFirst().orElseThrow());
        sgs = sgs.copy(sgs).advance(sgs.getAvailableMoves().stream().findFirst().orElseThrow());
        // TODO: FInish these tests
        // Then we check that the location is correct initially
        assertThat(sgs.getMrXLocation()).isNotEqualTo(-1);
        
        
    }

}
