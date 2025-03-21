package uk.ac.bris.cs.scotlandyard.ui.ai.particleSwarmOpt;

import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.AStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.distancestrategies.GreedyAStar;
import uk.ac.bris.cs.scotlandyard.ui.ai.simgamestate.SimulationGameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
Evaluation of PSO Strategy:
(+) the detectives work together so they share information about MrX location so reduce information asymmetry
(+) pretty fast algorithm as its iterative
(+) the detectives very quickly move to MrX last known location but dont simulate the game in any way so long term strategy isnt taken into account

(-) approximates the node positions using a decimal location so there is error on the actual location on the graph
- addressed by parallel updating of the location and graph node but still there is the |node1 - node2| approx used

(-) as tickets are not taken into account the detectives use up a lot of tickets quickly. They often bunch up and get stuck due to this.
- can play with the constants to change this. Decrease the weighting of following the swarm.

(-) sometimes the detectives bunch up too tight and eliminate each others possible moves hence giving MrX space
- can be addresed by adding a term that disincentivizes the detectives from getting too close when scoring moves
- 1 / (sum of A* distances between detectives)

 */

/**
 * Class that runs particle swarm optimization
 * The detectives are modelled as particles in a swarm that try to explore the graph using a loss function
 * in the form of the distance to MrX.
 */

public class PSO {
    //game state information
    GameSetup gameSetup;
    int mrXPos;
    List<Player> detectives;
    SimulationGameState initGameState;

    //hyper parameters for PSO
    double c1;
    double c2;
    double w;

    //global optimum swarm location
    double swarmLocation;
    int swarmNode = 0;

    /**
     *Constructor for PSO
     * @param simGameState the game state to run the simulation on
     * @param c1 weight for best known position of detective in velocity update
     * @param c2 weight for the best known position of the swarm in the velocity update
     * @param w weight for the previous velocity of the detective in velocity update
     */
    public PSO(SimulationGameState simGameState, double c1, double c2, double w) {
        this.gameSetup = simGameState.getSetup();
        this.mrXPos = simGameState.getMrXLocation();
        this.detectives = simGameState.getDetectives();
        this.initGameState = simGameState;

        this.c1 = c1;
        this.c2 = c2;
        this.w = w;

        //initialize the initial swarm location as the average of the locations of the detectives

        //swarm location is the double that we use to run the optimization
        swarmLocation = detectives.stream().mapToDouble(x -> (double) x.location()).sum() / detectives.size();

        //swarm node is the node we approximate to be closest to the optimal location
        swarmNode =  (int) swarmLocation;
    }

    /**
     * Loss function for optimization using A* distance from detective to MrX (Used for nodes)
     * @param detNode node location for the detective
     * @return Squared A* distance from MrX to the detective
     */
    public double aStarLoss(int detNode) {
        return Math.pow(new GreedyAStar().findDistance(mrXPos, detNode, initGameState), 2);
    }

    /**
     * Loss function for optimization using squared distance from detective location to MrX (Used for PSO locations)
     * @param detLoc the detective location from the PSO iterative procedure
     * @return the squared distance from MrX to the detective
     */
    public double squaredLoss(double detLoc) {
        return Math.pow(detLoc - mrXPos, 2);
    }

    /**
     * Class that models each detective as a particle
     */
    class Particle {
        double velocity;

        //location and best approximated node for the current iteration
        double currentLocation;
        public int currentNode;

        //location and best approximated node for the best known location over the whole iterative procedure
        double bestKnownLocation;
        double bestKnownNode;

        /**
         * Constructor to create a detective particle.
         * @param initGraphPos the initial position of the detective on the graph.
         */
        public Particle(int initGraphPos) {
            velocity = new Random().nextDouble() * 0.1;

            currentLocation = initGraphPos;
            currentNode = initGraphPos;

            bestKnownLocation = initGraphPos;
            bestKnownNode = initGraphPos;
        }

        /**
         * Updates the velocity of the particle (detective) using the best known location of the particle and the best known position of the swarm
         */
        public void updateVelocity() {
            //parameters to add noise/temperature into the iterative procedure to help escape local minimums
            double r1 = new Random().nextDouble()*0.1;
            double r2 = new Random().nextDouble()*0.1;

            velocity = w * velocity + c1*r1*(bestKnownLocation - currentLocation) + c2*r2*(swarmLocation - currentLocation);
        }

        public void updateLocation() {
            double initNodeLoss = aStarLoss(currentNode);
            double initLocationLoss = squaredLoss(currentLocation);
            //update the location of the detective using the velocity
            currentLocation += velocity;

            //update the graph position by finding the node that is closest to the updated location from the current node
            //this will approximate the graph node that is closest to the updated location
            int closestNode = currentNode;
            double minLoss = Math.abs(currentNode - currentLocation);
            for (int node : gameSetup.graph.adjacentNodes(currentNode)) {
                double currLoss = Math.abs(node - currentLocation); //need to use absolute distance as comparing node position and double.
                if (currLoss <= minLoss) {
                    closestNode = node;
                    minLoss = currLoss;
                }
            }
            currentNode = closestNode;

            double newNodeLoss = aStarLoss(currentNode);
            double newSquaredLoss = squaredLoss(currentLocation);

            //update the best known node position by comparing the A* loss
            if (initNodeLoss > newNodeLoss) {
                bestKnownNode = currentNode;
            }

            //update the best known location by comparing the squared loss
            if (initLocationLoss > newSquaredLoss) {
                bestKnownLocation = currentLocation;
            }

            if (squaredLoss(swarmLocation) > newSquaredLoss) {
                swarmLocation = currentLocation;
            }

            if (aStarLoss(swarmNode) > newNodeLoss) {
                swarmNode = currentNode;
            }
        }
    }

    /**
     * Run PSO
     * @param epochs number of iterations to run PSO
     */

    public void runSwarmOpt(int epochs) {
        //initialize the particles from the detective locations
        ArrayList<Particle> particles = new ArrayList<>(detectives.stream().map(x -> new Particle(x.location())).toList());

        for (int epoch = 0; epoch <= epochs; epoch++) {
            particles.forEach(Particle::updateVelocity);
            particles.forEach(Particle::updateLocation);
        }

    }

    /**
     * Scoring function for a move in a game state. Adds up the A* distance from the swarm or optimum location found to all the detectives
     * Assumption: the move that has detectives closest to the swarm node will be the one where the collective detectives are moving towards MrX
     * @param move move to be scored
     * @return sum of the A* distances
     */

    public double scoreMove (Move move) {
        SimulationGameState simGameState = initGameState.copy(initGameState).advance(move);
        double out = 0;
        for (Player d : simGameState.getDetectives()) {
            out += new GreedyAStar().findDistance(swarmNode, d.location(), simGameState);
        }

//        double bunchingFactor = 0;
//        for (Player d1 : simGameState.getDetectives()) {
//            for (Player d2 : simGameState.getDetectives()) {
//                bunchingFactor = aStar(d2.location(), d1.location(), simGameState) * 0.5;
//            }
//        }

        return out;
    }

    /**
     * Argmin over the set of available moves from the game state which PSO is initialized with.
     * @param epochs number of iterations to run PSO
     * @return move that results in the game state with the least distance between the swarm optimum found and the detectives
     */
    public Move chooseMove(int epochs) {

        runSwarmOpt(epochs);

        double min = Double.POSITIVE_INFINITY;
        Move outMove = initGameState.getAvailableMoves().stream().toList().get(0);

        for (Move move : initGameState.getAvailableMoves()) {
            double moveScore = scoreMove(move);
            if (moveScore <= min) {
                min = moveScore;
                outMove = move;
            }
        }

        return outMove;
    }
}