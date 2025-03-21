package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.io.IOException;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public class MonteCarloTests {

    @Test
    public void TestUpdatingMCNodeVisitorRoot() throws IOException {
        Player mrX = new Player(MRX, defaultMrXTickets(), MRX_LOCATIONS.get(0));
        Player detective = new Player(Piece.Detective.RED, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(0));
        Board.GameState g = MyGameStateFactory.a(new GameSetup(ScotlandYard.standardGraph(), ScotlandYard.STANDARD24MOVES), mrX, ImmutableList.of(detective));
        MCRoot myRoot = new MCRoot(new SimulationGameState(g), new LinkedList<>());


        myRoot.accept(new UpdatingMCNodeVisitor(1));
        assertThat(myRoot.numVisits).isEqualTo(1);
        assertThat(myRoot.numWins).isEqualTo(1);
    }

    @Test
    public void TestUpdatingMCNodeVisitorLeaf() throws IOException {
        Player mrX = new Player(MRX, defaultMrXTickets(), MRX_LOCATIONS.get(0));
        Player detective = new Player(Piece.Detective.RED, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(0));
        Board.GameState g = MyGameStateFactory.a(new GameSetup(ScotlandYard.standardGraph(), ScotlandYard.STANDARD24MOVES), mrX, ImmutableList.of(detective));
        MCRoot myRoot = new MCRoot(new SimulationGameState(g), new LinkedList<>());
        Move myMove = new Move.SingleMove(Piece.MrX.MRX, 11, Ticket.TAXI, 12);
        MCLeaf myLeaf = new MCLeaf(new SimulationGameState(g), myRoot, myMove);
        myRoot.children.add(myLeaf);


        myLeaf.accept(new UpdatingMCNodeVisitor(1));
        assertThat(myLeaf.numVisits).isEqualTo(1);
        assertThat(myLeaf.numWins).isEqualTo(1);

        assertThat(myRoot.numWins).isEqualTo(1);
        assertThat(myRoot.numVisits).isEqualTo(1);
    }

    @Test
    public void TestUpdatingandExpandingMCNodeVisitorForkLoss() throws IOException {
        Player mrX = new Player(MRX, defaultMrXTickets(), MRX_LOCATIONS.get(0));
        Player detective = new Player(Piece.Detective.RED, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(0));
        Board.GameState g = MyGameStateFactory.a(new GameSetup(ScotlandYard.standardGraph(), ScotlandYard.STANDARD24MOVES), mrX, ImmutableList.of(detective));

        MCRoot myRoot = new MCRoot(new SimulationGameState(g), new LinkedList<>());
        int  numMovesLen = myRoot.unexploredMoves.size();

        for (int moveCount = 0; moveCount < numMovesLen; moveCount++) {
            myRoot.accept(new ExpandingMCNodeVisitor());
        }

        //expanded the root correctly
        assertThat(myRoot.unexploredMoves).isEmpty();
        assertThat(myRoot.children.size()).isEqualTo(numMovesLen);

        ExpandingMCNodeVisitor checkVisitor = new ExpandingMCNodeVisitor();
        myRoot.accept(checkVisitor);

        assertThat(checkVisitor.nodesInPath.size()).isEqualTo(2);

        MCNode fork = checkVisitor.nodesInPath.get(checkVisitor.nodesInPath.size() - 1);

        fork.accept(new UpdatingMCNodeVisitor(0));

        assertThat(fork.numWins).isEqualTo(0);
        assertThat(fork.numVisits).isEqualTo(1);

        assertThat(myRoot.numWins).isEqualTo(0);
        assertThat(myRoot.numVisits).isEqualTo(1);
    }

    @Test
    public void TestUpdatingandExpandingMCNodeVisitorForkWin() throws IOException {
        Player mrX = new Player(MRX, defaultMrXTickets(), MRX_LOCATIONS.get(0));
        Player detective = new Player(Piece.Detective.RED, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(0));
        Board.GameState g = MyGameStateFactory.a(new GameSetup(ScotlandYard.standardGraph(), ScotlandYard.STANDARD24MOVES), mrX, ImmutableList.of(detective));

        MCRoot myRoot = new MCRoot(new SimulationGameState(g), new LinkedList<>());
        int  numMovesLen = myRoot.unexploredMoves.size();

        for (int moveCount = 0; moveCount < numMovesLen; moveCount++) {
            myRoot.accept(new ExpandingMCNodeVisitor());
        }

        //expanded the root correctly
        assertThat(myRoot.unexploredMoves).isEmpty();
        assertThat(myRoot.children.size()).isEqualTo(numMovesLen);

        ExpandingMCNodeVisitor checkVisitor = new ExpandingMCNodeVisitor();
        myRoot.accept(checkVisitor);

        assertThat(checkVisitor.nodesInPath.size()).isEqualTo(2);

        MCNode fork = checkVisitor.nodesInPath.get(checkVisitor.nodesInPath.size() - 1);

        fork.accept(new UpdatingMCNodeVisitor(1));

        assertThat(fork.numWins).isEqualTo(1);
        assertThat(fork.numVisits).isEqualTo(1);

        assertThat(myRoot.numWins).isEqualTo(1);
        assertThat(myRoot.numVisits).isEqualTo(1);
    }
}
