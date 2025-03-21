package uk.ac.bris.cs.scotlandyard.ui.ai.minimax;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.factories.MonteCarloFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.*;
import java.util.concurrent.*;

/**
 * Class implementing a {@link MoveSelectingStrategy} using minimax.
 */
public class MinimaxFinal implements MoveSelectingStrategy{
    int depth;
    ScoringStrategy scoringStrategy;
    int mrXTopNMoves;
    int detectiveTopNMoves;

    /**
     *
     * @param depth The depth to use for minimax
     * @param scoringStrategy The scoring strategy to use for minimax
     */
    public MinimaxFinal(int depth, ScoringStrategy scoringStrategy, int mrXTopNMoves, int detectiveTopNMoves) {
        this.depth = depth;
        this.scoringStrategy = scoringStrategy;
        this.mrXTopNMoves = mrXTopNMoves;
        this.detectiveTopNMoves = detectiveTopNMoves;
    }

    /**
     * Gets the total number of tickets from a {@link Iterator<ScotlandYard.Ticket>}.
     * @param iterator The iterator for tickets
     * @return The number of tickets in the iterator
     */
    public static int getNumTix(Iterator<ScotlandYard.Ticket> iterator) {
        int i = 0;

        for (Iterator<ScotlandYard.Ticket> it = iterator; it.hasNext(); ) {
            ScotlandYard.Ticket t = it.next();
            i++;
        }
        return i;
    }

    /**
     * Scores a {@link Iterable<ScotlandYard.Ticket>}
     * @param tickets
     * @return The score
     */
    public static int scoreTix(Iterable<ScotlandYard.Ticket> tickets) {
        int score = 0;
        for (ScotlandYard.Ticket t : tickets) {
            switch (t) {
                case TAXI -> score += 10;
                case BUS -> score += 5;
                case UNDERGROUND -> score += 3;
                case SECRET -> score += 2;
                case DOUBLE -> score += 2;
            }
        }
        return score;
    }

    /**
     * From a {@link List<Pair<Move, Double>>}, we select the best move based on the number of {@link uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket Tickets} that are used
     * and the relative value of the tickets.
     * @param moves
     * @return The score
     */
    public static Pair<Move, Double> selectBestMove(List<Pair<Move, Double>> moves) {
        // Sort moves by number of tickets used
        // Then by types of ticket
        // Then pick the highest
        List<Pair<Move, Double>> toBeProcessed = moves;

        // We create a new comparator the sorts based on the number of tickets that each move uses and sort using that
        toBeProcessed.sort(new Comparator<Pair<Move, Double>>() {
            @Override
            public int compare(Pair<Move, Double> o1, Pair<Move, Double> o2) {
                int numO1 = getNumTix(o1.left().tickets().iterator());
                int numO2 = getNumTix(o2.left().tickets().iterator());
                if (numO1 < numO2) {
                    return -1;
                } else if (numO1 == numO2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        // We select the lowest scoring move
        return toBeProcessed.get(0);
    }

    /**
     * This method selects the most efficient moves from a list of futures that are returned from the threads.
     * @param l
     * @return The input list with duplicates removed, with those kept chosen based on how efficiently they
     *         use tickets, and the relative value of tickets.
     */
    public static List<Pair<Move, Double>> convertDupMovesToMostEff(List<Future<Pair<Move, Double>>> l) {
        // Try to unwrap the futures.
        List<Pair<Move, Double>> unwrapped = new ArrayList<>(l.stream().map(d -> {
            try {
                return d.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).toList());
        List<Pair<Move, Double>> results = new LinkedList<>();
        for (Pair<Move, Double> p : unwrapped) {
            // We find the duplicates now:
            // The score must be the same and they must go to the same destination
            List<Pair<Move, Double>> dups = new ArrayList<>(unwrapped.stream()
                    .filter(d -> d.left()
                            .accept(new MoveDestinationVisitor()) == p.left().accept(new MoveDestinationVisitor()) && Math.abs(p.right() - d.right()) <= 20).toList());
            if (dups.size() >= 2) {
                // There are duplicates
                // This means that we choose the lowest scoring (most efficient) move to keep
                results.add(selectBestMove(dups)); // and we add only the most efficient move
            }
        }
        return results;
    }

    /**
     * Selects the best move for Mr X
     * @param board
     * @return
     */
    @Override
    public Move selectForMrX(Board board) {
        SimulationGameState s = new SimulationGameState(board);

        double max = Double.NEGATIVE_INFINITY;
        Move bestMove = s.getAvailableMoves().stream().findFirst().orElseThrow();
        double eval = 0;

        // Load the thread pool
        ExecutorService threads = Executors.newCachedThreadPool();

        ArrayList<MinimaxServiceFinal> tasks = new ArrayList<>();

        System.out.println("--- BEFORE ADVANCE ---");
        System.out.println();

        for (Move m : s.getAvailableMoves()) {
            SimulationGameState advanced = s.copy(s).advance(m);
            // We filter out moves that result in a position where Mr X can be caught; unless there are no other
            // possible moves.
            tasks.add(new MinimaxServiceFinal(advanced, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, scoringStrategy, m, mrXTopNMoves, detectiveTopNMoves));
        }

        try {
            List<Future<Pair<Move, Double>>> results = threads.invokeAll(tasks);

            List<Pair<Move, Double>> processed = convertDupMovesToMostEff(results);

            System.out.println("Every move found for Mr X: " + processed);

            for (Pair<Move, Double> f : processed) {
                if (f.right() >= max) {
                    // compare and update the score
                    bestMove = f.left();
                    max = f.right();
                }
            }
            threads.shutdownNow();
            threads.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return board.getAvailableMoves().stream().findFirst().orElseThrow();
        }

        System.out.println("Difference in Score: Initial Score: " + scoringStrategy.score(s) + " New Score: " + max);

        if (max - scoringStrategy.score(s) < -1000) {
            // There is no good move at this position. To proceed we will use MCTS.
            System.out.println("EMERGENCY!! There is no good move at this position. We use MCTS instead...");
            System.out.println("this is probably only barely going to complete, cross your fingers ;)");
            return new MonteCarloFactory().getMoveSelectingStrategy().selectForMrX(board);
        }
        return bestMove;

    }

    /**
     * Selects the best move for the detective
     * @param board
     * @return
     */
    @Override
    public Move selectForDetective(Board board) {
        SimulationGameState s = new SimulationGameState(board);

        double min = Double.POSITIVE_INFINITY;
        Move bestMove = s.getAvailableMoves().stream().findFirst().orElseThrow();
        double eval = 0;

        ExecutorService threads = Executors.newCachedThreadPool();

        ArrayList<MinimaxServiceFinal> tasks = new ArrayList<>();

        // Calculate minimax on each child move
        for (Move m : s.getAvailableMoves()) {
            SimulationGameState advanced = s.copy(s).advance(m);
            tasks.add(new MinimaxServiceFinal(advanced, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, scoringStrategy, m, mrXTopNMoves, detectiveTopNMoves));
        }

        try {
            List<Future<Pair<Move, Double>>> results = threads.invokeAll(tasks);
            for (Future<Pair<Move, Double>> f : results) {
                if (f.get().right() <= min) {
                    // compare and update the score
                    bestMove = f.get().left();
                    min = f.get().right();
                }
            }
            threads.shutdownNow();
            threads.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return board.getAvailableMoves().stream().findFirst().orElseThrow();
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        System.out.println("Difference in Score: Initial Score: " + scoringStrategy.score(s) + " New Score: " + min);
        return bestMove;
    }
}
