package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MinimaxFinal;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MoveDestinationVisitor;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MinimaxTests {

    Piece testPiece = new Piece() {
        @Nonnull
        @Override
        public String webColour() {
            return "";
        }

        @Override
        public boolean isDetective() {
            return false;
        }
    };
    @Test
    public void TestGetNumTixNormal() {
        List<ScotlandYard.Ticket> l = List.of(ScotlandYard.Ticket.DOUBLE, ScotlandYard.Ticket.BUS, ScotlandYard.Ticket.TAXI);
        assertThat(MinimaxFinal.getNumTix(l.iterator())).isEqualTo(3);
        assertThat(MinimaxFinal.getNumTix(List.<ScotlandYard.Ticket>of().iterator())).isEqualTo(0);
        assertThat(MinimaxFinal.getNumTix(List.<ScotlandYard.Ticket>of(ScotlandYard.Ticket.SECRET).iterator())).isEqualTo(1);
    }

    @Test
    public void TestScoreTix() {
        assertThat(MinimaxFinal.scoreTix(List.of(ScotlandYard.Ticket.TAXI))).isEqualTo(10);
        assertThat(MinimaxFinal.scoreTix(List.of(ScotlandYard.Ticket.BUS))).isEqualTo(5);
        assertThat(MinimaxFinal.scoreTix(List.of(ScotlandYard.Ticket.UNDERGROUND))).isEqualTo(3);
        assertThat(MinimaxFinal.scoreTix(List.of(ScotlandYard.Ticket.SECRET))).isEqualTo(2);
        assertThat(MinimaxFinal.scoreTix(List.of(ScotlandYard.Ticket.DOUBLE))).isEqualTo(2);

        assertThat(MinimaxFinal.scoreTix(List.of(
                ScotlandYard.Ticket.TAXI,
                ScotlandYard.Ticket.BUS,
                ScotlandYard.Ticket.DOUBLE
        ))).isEqualTo(17);
    }

    @Test
    public void TestSelectBestMove() {
        LinkedList<Pair<Move, Double>> l = new LinkedList<Pair<Move, Double>>(List.of(
                new Pair<>((Move) new Move.SingleMove(testPiece, 19, ScotlandYard.Ticket.TAXI, 20), 10.0),
                new Pair<>((Move) new Move.DoubleMove(testPiece, 19, ScotlandYard.Ticket.TAXI, 18, ScotlandYard.Ticket.BUS, 20), 10.0)
                ));
        assertThat(MinimaxFinal.selectBestMove(l)).isEqualTo(new Pair<>((Move) new Move.SingleMove(testPiece, 19, ScotlandYard.Ticket.TAXI, 20), 10.0));

        l = new LinkedList<>(List.of(
                new Pair<>((Move) new Move.SingleMove(testPiece, 19, ScotlandYard.Ticket.TAXI, 20), 10.0),
                new Pair<>((Move) new Move.SingleMove(testPiece, 19, ScotlandYard.Ticket.BUS, 20), 10.0)
        ));
        assertThat(MinimaxFinal.selectBestMove(l)).isEqualTo(new Pair<>((Move) new Move.SingleMove(testPiece, 19, ScotlandYard.Ticket.TAXI, 20), 10.0));
    }

    @Test
    public void TestMoveDestinationVisitor() {
        Move testMove = new Move.SingleMove(testPiece, 10, ScotlandYard.Ticket.TAXI, 15);
        assertThat(testMove.accept(new MoveDestinationVisitor())).isEqualTo(15);

        testMove = new Move.DoubleMove(testPiece, 10, ScotlandYard.Ticket.TAXI, 15, ScotlandYard.Ticket.BUS, 30);
        assertThat(testMove.accept(new MoveDestinationVisitor())).isEqualTo(30);
    }


}
