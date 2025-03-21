package uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.*;

/**
 * This function contains utility functions used in {@link SimulationGameState}
 */
public class SimulationUtilityFunctions {

    /**
     * This function checks whose turn it is
     * @param avlMoves The available moves
     * @return True if Mr X's turn, False otherwise.
     */
    public static boolean isMrXTurn(Set<Move> avlMoves) {
        return avlMoves.stream().anyMatch(x -> x.commencedBy().isMrX());
    }

    /**
     * This function returns all the possible single moves for a particular board
     * @param setup
     * @param detectives
     * @param player
     * @param source
     * @return
     */
    public static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
        // we create a hashmap to store the moves
        HashSet<Move.SingleMove> possibleMoves = new HashSet<>();

        for (int destination : setup.graph.adjacentNodes(source)) {
            //  find out if destination is occupied by a detective
            //  if the location is occupied, don't add to the collection of moves to return

            if (detectives.stream().anyMatch(d -> d.location() == destination)) {
                continue;
            }

            // checks all regular modes of transport using regular tickets
            for (ScotlandYard.Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
                if (!player.has(t.requiredTicket())) {
                    continue;
                }
                possibleMoves.add(new Move.SingleMove(player.piece(), player.location(), t.requiredTicket(), destination));
            }

            // if the player is mr x and they have remaining secret tickets, then we also add these as possible moves.

            if (player.isMrX() && player.has(ScotlandYard.Ticket.SECRET)) {
                for (ScotlandYard.Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
                    possibleMoves.add(new Move.SingleMove(player.piece(), player.location(), ScotlandYard.Ticket.SECRET, destination));
                }
            }
        }

        return possibleMoves;
    }

    /**
     * This function returns all the possible double moves for a particular situation
     * @param setup
     * @param detectives
     * @param player
     * @param source
     * @return
     */
    public static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
        // First, we get all possible single moves that we can then extend.
        Set<Move.SingleMove> singleMoves = makeSingleMoves(setup, detectives, player, source);

        Set<Move.DoubleMove> doubleMoves = new HashSet<>();

        for (Move.SingleMove sm1 : singleMoves) {
            for (Move.SingleMove sm2 : makeSingleMoves(setup, detectives, player, sm1.destination)) {

                // if tickets are different then need more than one of each
                if (player.hasAtLeast(sm1.ticket, 1) && player.hasAtLeast(sm2.ticket, 1) && sm1.ticket != sm2.ticket) {
                    doubleMoves.add(new Move.DoubleMove(sm1.commencedBy(), sm1.source(), sm1.ticket, sm1.destination, sm2.ticket, sm2.destination));
                }

                // if tickets are the same then need more than two of the same

                if (sm1.ticket == sm2.ticket && player.hasAtLeast(sm1.ticket, 2)) {
                    doubleMoves.add(new Move.DoubleMove(sm1.commencedBy(), sm1.source(), sm1.ticket, sm1.destination, sm2.ticket, sm2.destination));
                }
            }
        }
        return doubleMoves;
    }

    /**
     * This function is a utility function that calculates the change in ticket numbers based on the Map before and the
     * Map after.
     * @param before
     * @param after
     * @return
     */
    public static ImmutableMap<ScotlandYard.Ticket, Integer> ticketDelta(ImmutableMap<ScotlandYard.Ticket, Integer> before, ImmutableMap<ScotlandYard.Ticket, Integer> after){
        HashMap<ScotlandYard.Ticket, Integer> deltas = new HashMap<>();
        for (Map.Entry<ScotlandYard.Ticket, Integer> m : before.entrySet()) {
            deltas.put(m.getKey(), m.getValue() - after.get(m.getKey()));
        }
        return ImmutableMap.copyOf(deltas);
    }

}