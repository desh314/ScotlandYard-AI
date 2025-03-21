package uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationUtilityFunctions.*;

/**
 * This function implements an internal GameState with various utility functions used throughout the AI.
 */
public class SimulationGameState {
    GameSetup setup;

    public boolean previousTurnWasMrX = false; // if this is true, then this is a state where Mr X is making moves and scoring
                    // Here, we must use getMrX().location().
    public Set<Player> getRemaining() {
        return remaining;
    }

    Set<Player> remaining;
    List<LogEntry> mrXLog;

    public Player getMrX() {
        return mrX;
    }

    public Player mrX;

    public List<Player> getDetectives() {
        return detectives;
    }

    public List<LogEntry> getMrXLog() {return mrXLog; }

    List<Player> detectives;
    Set<Move> avlMoves;
    Set<Piece> winner;
    Board initBoard;

    /**
     * This creates a Simulation game state using a Board, such as what we would have when we are asked
     * to select a move
     * @param board
     */
    public SimulationGameState(Board board) {

        /*
        If mrX turn:
            - mrX position: from the available moves
            - detective position: get detective location from board

        If detective turn:
            - mrX position: from the board log
            - detective position: get detective location from board

        Common:
            - remaining: from get available moves
            - avlMoves: from get avaliable moves
            - mrX log: from get mrX log
            - setup: from get Setup
         */

        //extract information from the board
        avlMoves = new HashSet<>(board.getAvailableMoves());
        mrXLog = new ArrayList<>(board.getMrXTravelLog());
        setup = board.getSetup();
        winner = new HashSet<>();
        initBoard = board;

        //initialize the information of the detectives and mrX depending on whose turn it is
        boolean mrXTurn = isMrXTurn(avlMoves);

        int mrXPos = -1;

        if (mrXTurn) {
            for (Move avlMove : List.copyOf(avlMoves)) {
                if (avlMove.commencedBy().isMrX()) {
                    mrXPos =  avlMove.source();
                    break;
                }
            }
        } else {
            for (int entry = mrXLog.size() - 1; entry >= 0; entry--) {
                if (mrXLog.get(entry).location().isPresent()) {
                    mrXPos = mrXLog.get(entry).location().get();
                    break;
                }
            }
        }

        detectives = new ArrayList<>();
        remaining = new HashSet<>();

        Set<Piece> remainingPieces = avlMoves.stream().map(Move::commencedBy).collect(Collectors.toSet());


        for (Piece p : board.getPlayers()) {
            HashMap<ScotlandYard.Ticket, Integer> tickets = new HashMap<>();

            // Add the tickets for each player
            for (ScotlandYard.Ticket ticket : ScotlandYard.Ticket.values()) {
                tickets.put(ticket, board.getPlayerTickets(p).orElseThrow().getCount(ticket));
            }

            if (p.isDetective()) {
                Player newDet = new Player(p, ImmutableMap.copyOf(tickets), board.getDetectiveLocation((Piece.Detective) p).orElseThrow());
                detectives.add(newDet);
                if (remainingPieces.contains(p)) {
                    remaining.add(newDet);
                }
            } else {
                Player newMrX = new Player(p, ImmutableMap.copyOf(tickets), mrXPos);
                mrX = newMrX;
                if (remainingPieces.contains(p)) {
                    remaining.add(newMrX);
                }
            }
        }

        if (mrXTurn) {
            this.previousTurnWasMrX = false;
        } else {
            this.previousTurnWasMrX = true;
        }

    }

    /**
     * This creates a Simulation game state using a lot of other information, if we do not have a board.
     * @param mrXLog Mr X's logbook
     * @param remaining The remaining players
     * @param detectives The detectives in the game
     * @param mrX Mr X
     * @param initBoard The board
     */
    public SimulationGameState(List<LogEntry> mrXLog, Set<Player> remaining, List<Player> detectives, Player mrX, Board initBoard, boolean flag) {
        this.mrXLog = new ArrayList<>(mrXLog);
        //remaining logic is handled by advance.
        this.remaining = new HashSet<>(remaining);
        this.detectives = new ArrayList<>(detectives);
        this.mrX = mrX;
        this.initBoard = initBoard;
        this.winner = new HashSet<>();
        this.previousTurnWasMrX = flag;


        HashSet<Move> tempMoves = new HashSet<Move>();

        // Add the available moves
        if (this.remaining.stream().map(Player::isMrX).reduce(false, (val, acc) -> val || acc)) {
            tempMoves.addAll(makeSingleMoves(initBoard.getSetup(), this.detectives, this.mrX, this.mrX.location()));

            if (this.mrX.has(ScotlandYard.Ticket.DOUBLE)
                    && this.mrX.tickets().values().stream().mapToInt(integer -> integer).sum() >= 2
                    && initBoard.getSetup().moves.size() >= 2) {
                tempMoves.addAll(makeDoubleMoves(initBoard.getSetup(), this.detectives, this.mrX, this.mrX.location()));

            }
        } else {
            tempMoves.addAll(this.remaining.stream()
                    .flatMap(d -> makeSingleMoves(initBoard.getSetup(), this.detectives, d, d.location())
                            .stream())
                    .collect(Collectors.toSet()));

        }

        this.avlMoves = Set.copyOf(tempMoves);
    }

    /**
     * This function advances the game state after a particular move is played
     * @param move
     * @return the new game state
     */
    public SimulationGameState advance(Move move) {

        if (!avlMoves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);

        Player movedBy = (Player) remaining.stream().filter(x -> x.piece() == move.commencedBy()).toArray()[0];

        Move.Visitor<Player> makeMove = new MoveVisitor(movedBy);
        Move.Visitor<List<LogEntry>> addToLog = new LogVisitor(mrXLog, initBoard.getSetup().moves);

        // Adds the move to the log if mr X moved.
        if (movedBy.isMrX()) {
            mrXLog = move.accept(addToLog);
        }

        // Removes the necessary tickets and updates the locations.
        Player updatedPlayer = move.accept(makeMove);

        this.remaining.remove(movedBy);

        // If it was a detective that moved, we need to add the ticket that they used to Mr X.
        if (movedBy != mrX) {
            this.detectives.remove(movedBy);
            this.detectives.add(updatedPlayer);

            // We use the ticketDelta function to get a list of all the tickets that changed. We give the number of used
            // tickets to Mr X.
            ImmutableMap<ScotlandYard.Ticket, Integer> ticketDelta = ticketDelta(movedBy.tickets(), updatedPlayer.tickets());
            for (Map.Entry<ScotlandYard.Ticket, Integer> m : ticketDelta.entrySet()) {
                for (int i = 1; i <= m.getValue(); i++) {
                    this.mrX = this.mrX.give(m.getKey());
                }
            }
        } else {
            this.mrX = updatedPlayer;
        }

        Set<Player> tempRemove = new HashSet<Player>();

        // If any of the players have no remaining tickets, then they can no longer play.
        if (!remaining.isEmpty()) {
            for (Player r : this.remaining) {
                if (r.tickets().values().stream().mapToInt(Integer::intValue).sum() == 0) {
                    tempRemove.add(r);
                }
            }

            for (Player r : tempRemove) {
                remaining.remove(r);
            }
        }

        boolean newFlag;

        // If there are no remaining players, then we reset the remaining.
        if (remaining.isEmpty()) {
            Set<Player> resetRemaining = new HashSet<Player>();
            if (movedBy.isMrX()) {
                newFlag = true;
                resetRemaining.addAll(this.detectives);
            } else {
                newFlag = false;
                resetRemaining.add(this.mrX);
            }
            return new SimulationGameState(mrXLog, resetRemaining, detectives, mrX, initBoard, newFlag);
        } else {
            if (movedBy.isMrX())
                newFlag = true;
            else {
                newFlag = false;
            }
            return new SimulationGameState(mrXLog, remaining, detectives, mrX, initBoard, newFlag);
        }
    }

    /**
     * This function returns the players in the game
     * @return
     */
    public ImmutableSet<Piece> getPlayers() {

        Set<Piece> pieces = new HashSet<Piece>(remaining.stream().map(Player::piece).collect(Collectors.toSet()));

        for (Player d : detectives) {
            pieces.add(d.piece());
        }

        return ImmutableSet.copyOf(pieces);
    }

    /**
     * This function gets the winner of the game
     * @return
     */
    public Set<Piece> getWinner() {

        // If the detectives have run out of tickets, then the winner is Mr X
        if (detectives.stream().map(x -> x.tickets().values().stream().mapToInt(Integer::intValue).sum()).mapToInt(Integer::intValue).sum() == 0) {
            this.winner = ImmutableSet.of(mrX.piece());
            return ImmutableSet.of(mrX.piece());
        }

        // If any of the detectives have landed on Mr X's location, then the detectives win.
        if (this.detectives.stream().anyMatch(d -> d.location() == this.mrX.location())) {
            // If any of the detectives have a location that is on Mr X
            HashSet<Piece> pieces = new HashSet<>(this.getPlayers());
            pieces.remove(this.mrX.piece());

            this.winner = ImmutableSet.copyOf(pieces);
            return ImmutableSet.copyOf(pieces);
        }

        // If mr X has no available moves then the detectives win
        if (avlMoves.stream().noneMatch(m -> m.commencedBy().isMrX()) && !this.avlMoves.isEmpty() && this.remaining.contains(this.mrX)) {
            //System.out.println(avlMoves);
            //System.out.println(this.remaining);
            // If there are no available moves remaining for Mr X
            HashSet<Piece> pieces = new HashSet<>(this.getPlayers());
            pieces.remove(this.mrX.piece());

            this.winner = ImmutableSet.copyOf(pieces);
            return ImmutableSet.copyOf(pieces);
        }

        // If mr X has made the requisite number of moves without getting caught then they win.
        if (initBoard.getSetup().moves.size() == mrXLog.size()) {
            this.winner = ImmutableSet.of(mrX.piece());
            return ImmutableSet.of(mrX.piece());
        }

        if (avlMoves.isEmpty()) {
            if (this.remaining.contains(this.mrX)) {
                //if there are no avaliable moves and its mrX turn then mrX looses as he cannot go anywhere
                HashSet<Piece> pieces = new HashSet<>(this.getPlayers());
                pieces.remove(this.mrX.piece());
                this.winner = ImmutableSet.copyOf(pieces);
                return ImmutableSet.copyOf(pieces);

            } else {
                //if there are no avaliable moves and its detective turn then mrX looses
                this.winner = ImmutableSet.of(mrX.piece());
                return ImmutableSet.of(mrX.piece());

            }
        }

        if (this.winner == null) {
            return ImmutableSet.of();
        }

        return this.winner;
    }

    public Optional<Player> getCaughtBy() {
        for (Player d : this.detectives) {
            if (d.location() == this.mrX.location()) {
                return Optional.of(d);
            }
        }

        return Optional.empty();
    }


    public GameSetup getSetup() {
        return this.initBoard.getSetup();
    }

    /**
     * This function gets the available moves in the game state
     * @return
     */
    public Set<Move> getAvailableMoves() {
        if (this.winner.isEmpty()) {
            return this.avlMoves;
        } else {
            return ImmutableSet.of();
        }

    }

    /**
     * This function creates a copy of the game state
     * @param simGameState
     * @return
     */
    public SimulationGameState copy(SimulationGameState simGameState) {
        return new SimulationGameState(mrXLog, remaining, detectives, mrX, initBoard, previousTurnWasMrX);
    }

    public int getMrXLocAfterMrXMoveAdvance() {
        return mrX.location();
    }

    /**
     * This is a utility function to get Mr X's position. If we are Mr X, then we get it by looking at the source of a
     * move. If we are a detective, then we get it by looking at Mr X's last revealed logbook entry.
     * @return
     */
    public int getMrXLocation() {
        boolean mrXTurn = isMrXTurn(avlMoves);

        int mrXPos = -1;

        if (mrXTurn) {
            for (Move avlMove : List.copyOf(avlMoves)) {
                if (avlMove.commencedBy().isMrX()) {
                    return avlMove.source();
                }
            }
        } else {
            for (int entry = mrXLog.size() - 1; entry >= 0; entry--) {
                if (mrXLog.get(entry).location().isPresent()) {
                    return mrXLog.get(entry).location().get();
                }
            }
        }

        return mrXPos;
    }
//    advanceSimulation :: SimGameState -> Move -> (SimGameState, Integer)
    public static Pair<SimulationGameState, Integer> advanceSimulation(SimulationGameState simGameState, Move move) {

        boolean isMrXTurnInit = isMrXTurn(simGameState.getAvailableMoves()); // Whether it is Mr X who is doing the move
        SimulationGameState advanced = simGameState.copy(simGameState).advance(move);
        boolean isMrXTurnEnd = isMrXTurn(advanced.getAvailableMoves());

        // MrX -> Detective: actual MrX location
        if (isMrXTurnInit && !isMrXTurnEnd) {
            return new Pair(advanced, advanced.getMrX().location());
        } else { // Detective -> MrX: logbook mr x location
            return new Pair(advanced, advanced.getMrXLocation());
        }
    }
}