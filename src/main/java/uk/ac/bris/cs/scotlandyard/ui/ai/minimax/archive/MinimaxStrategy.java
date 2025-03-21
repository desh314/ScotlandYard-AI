package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.archive;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoringStrategy;
import uk.ac.bris.cs.scotlandyard.ui.ai.montecarlo.MCSimService;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

//depth 2 is realiable everything after that the program shits itself
//maybe implement early stopping or similar optimization
//randomly sample from the avaliable moves (some sort of heuristic?)

//implement parrelalization of the code?

// Sources for the psuedocode and explanations:
// https://youtu.be/l-hh51ncgDI?feature=shared
// https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
// https://en.wikipedia.org/wiki/Minimax
public class MinimaxStrategy implements MoveSelectingStrategy {

    //minimax works but need to find a way to get more depth
    //need to find the profiler problem and a way to speed it up!!

    private ScoringStrategy scoringStrategy;
    private int depth;

    public MinimaxStrategy(int depth, ScoringStrategy scoringStrategy) {
        this.depth = depth;
        this.scoringStrategy = scoringStrategy;
    }

    public static boolean isMrXTurn(SimulationGameState simGameState) {
        return (simGameState.getAvailableMoves().stream().findFirst().orElseThrow().commencedBy().isMrX());
    }

    @Override
    public Move selectForMrX(Board board) {
        SimulationGameState simGameState = new SimulationGameState(board);
        //mrX is the maximizer so we want the maximum scored move from the initial state.
        Move outMove = MCSimService.chooseRandomMove(board.getAvailableMoves()).get();
        Double max = Double.NEGATIVE_INFINITY;

        System.out.println("Mr X loc for Mr X: " + simGameState.getMrXLocation());
        ExecutorService threads = Executors.newCachedThreadPool();
        LinkedList<MinimaxService> tasks = new LinkedList<>();

        for (Move childMove : simGameState.getAvailableMoves()) {
            //execute the initial move
            SimulationGameState initGameState = simGameState.copy(simGameState).advance(childMove);

            tasks.add(new MinimaxService(initGameState, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, childMove, scoringStrategy));
        }

        try {
            System.out.println("Invoking threads for head of Mr X!!");
            List<Future<Pair<Move, Double>>> results = threads.invokeAll(tasks);
            for (Future<Pair<Move, Double>> f : results) {
                if (f.get().right() >= max) {
                    // compare and update the score
                    outMove = f.get().left();
                    max = f.get().right();
                }
            }
            threads.shutdownNow();
            threads.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return simGameState.getAvailableMoves().stream().findFirst().orElseThrow();
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        System.out.println("Difference in Score: Initial Score: " + scoringStrategy.score(simGameState) + " New Score: " + max);
        return outMove;
    }

    @Override
    public Move selectForDetective(Board board) {
        SimulationGameState simGameState = new SimulationGameState(board);
        //mrX is the maximizer so we want the maximum scored move from the initial state.
        Move outMove = MCSimService.chooseRandomMove(board.getAvailableMoves()).get();
        Double min = Double.POSITIVE_INFINITY;
        System.out.println("Mr X loc for detective: " + simGameState.getMrXLocation());


        ExecutorService threads = Executors.newCachedThreadPool();
        LinkedList<MinimaxService> tasks = new LinkedList<>();

        for (Move childMove : simGameState.getAvailableMoves()) {
            //execute the initial move
            SimulationGameState initGameState = simGameState.copy(simGameState).advance(childMove);

            tasks.add(new MinimaxService(initGameState, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, childMove, scoringStrategy));
        }

        try {
            System.out.println("Invoking threads for head of Detective!!");

            List<Future<Pair<Move, Double>>> results = threads.invokeAll(tasks);
            for (Future<Pair<Move, Double>> f : results) {
                if (f.get().right() <= min) {
                    // compare and update the score
                    outMove = f.get().left();
                    min = f.get().right();
                }
            }
            threads.shutdownNow();
            threads.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return simGameState.getAvailableMoves().stream().findFirst().orElseThrow();
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        System.out.println("Difference in Score: Initial Score: " + scoringStrategy.score(simGameState) + " New Score: " + min);
        return outMove;
    }


}
