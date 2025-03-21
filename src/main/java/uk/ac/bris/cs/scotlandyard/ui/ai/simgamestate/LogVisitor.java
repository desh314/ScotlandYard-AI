package uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.LogEntry;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a visitor that performs the required functionality for the mr X logbook.
 */
public class LogVisitor implements Move.Visitor<List<LogEntry>> {

    private List<LogEntry> log;

    private ImmutableList<Boolean> revealMoves;

    //need to update the state of the piece that has been moved
    //change the location on the board, remove the ticket used to make the move

    /**
     *
     * @param log The current logbook
     * @param revealMoves The 'reveal moves' for mr X.
     */
    public LogVisitor(List<LogEntry> log, ImmutableList<Boolean> revealMoves) {
        this.log = log;
        this.revealMoves = revealMoves;
    }

    /**
     * This function adds the {@link uk.ac.bris.cs.scotlandyard.model.Move.SingleMove} to the Log.
     * @param currentLog The current log
     * @param move The {@link uk.ac.bris.cs.scotlandyard.model.Move.SingleMove} to be added
     * @return The new log
     */
    private List<LogEntry> addToLog(List<LogEntry> currentLog, Move.SingleMove move) {

        if (revealMoves.get(currentLog.size())) {
            this.log.add(LogEntry.reveal(move.ticket, move.destination));
        } else {
            this.log.add(LogEntry.hidden(move.ticket));
        }

        return log;
    }

    /**
     * This function adds the {@link uk.ac.bris.cs.scotlandyard.model.Move.DoubleMove} to the Log.
     * @param currentLog The current log
     * @param move The {@link uk.ac.bris.cs.scotlandyard.model.Move.DoubleMove} to be added
     * @return The new log
     */
    private List<LogEntry> addDoubleToLog(List<LogEntry> currentLog, Move.DoubleMove move) {
        // This function reuses the code that we have in addToLog by splitting out the DoubleMove into two SingleMoves.
        Move.SingleMove move1 = new Move.SingleMove(move.commencedBy(), move.source(), move.ticket1, move.destination1);
        Move.SingleMove move2 = new Move.SingleMove(move.commencedBy(), move.destination1, move.ticket2, move.destination2);
        List<LogEntry> logInProgress = addToLog(currentLog, move1);
        return new ArrayList<>(addToLog(logInProgress, move2));
    }


    /**
     * This function implements the visitor's behaviour. For a single move, we use the single move adder.
     * @param move The move to be added
     * @return The new log
     */
    @Override
    public List<LogEntry> visit(Move.SingleMove move) {
        //add the ticket to be hidden or revealed depending on the size of move log before its added,
        //at specific sizes, the ticket added shold be revealed.
        return addToLog(this.log, move);
    }

    /**
     * This function implements the visitor's behaviour. For a double move, we use the double move adder.
     * @param move The move to be added
     * @return The new log
     */
    @Override
    public List<LogEntry> visit(Move.DoubleMove move) {
        return addDoubleToLog(this.log, move);
    }
}