package uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate;


import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * This class implements a visitor that performs the required functionality to update the state after a move is played.
 */
public class MoveVisitor implements Move.Visitor<Player> {

    private final Player player;

    //need to update the state of the piece that has been moved
    //change the location on the board, remove the ticket used to make the move

    /**
     *
     * @param player The player that made the move
     */
    public MoveVisitor(@Nonnull Player player) {
        this.player = Objects.requireNonNull(player);
    }

    /**
     * This function implements the functionality for a {@link uk.ac.bris.cs.scotlandyard.model.Move.SingleMove}.
     * Updates the player's tickets and location.
     * @param move The move
     * @return The updated player
     */
    @Override
    public Player visit(Move.SingleMove move) {
        return player.use(move.ticket).at(move.destination);
    }

    /**
     * This function implements the functionality for a {@link uk.ac.bris.cs.scotlandyard.model.Move.DoubleMove}.
     * Updates the player's tickets and location.
     * @param move The move
     * @return The updated player
     */
    @Override
    public Player visit(Move.DoubleMove move) {
        return player.use(move.ticket1).use(move.ticket2).use(ScotlandYard.Ticket.DOUBLE).at(move.destination2);
    }
}