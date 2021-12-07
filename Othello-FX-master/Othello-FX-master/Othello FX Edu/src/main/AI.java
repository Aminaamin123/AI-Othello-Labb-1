package main;

import com.eudycontreras.othello.capsules.AgentMove;
import com.eudycontreras.othello.capsules.MoveWrapper;
import com.eudycontreras.othello.capsules.ObjectiveWrapper;
import com.eudycontreras.othello.controllers.Agent;
import com.eudycontreras.othello.controllers.AgentController;
import com.eudycontreras.othello.enumerations.PlayerTurn;
import com.eudycontreras.othello.models.GameBoardState;

import java.util.List;
import java.util.Date;

public class AI extends Agent {

    public AI(PlayerTurn playerTurn) {
        super(playerTurn);
    }

    public AI(String agentName) {
        super(agentName);
    }

    public AI(String agentName, PlayerTurn playerTurn) {
        super(agentName, playerTurn);
    }

    @Override
    public AgentMove getMove(GameBoardState gameState)
    {

        if (AgentController.isTerminal(gameState, PlayerTurn.PLAYER_ONE)) {
            //If there is no possible move for the AI Agent.
            return new MoveWrapper(null);
        }
        //If the Agent is NOT Terminal meaning, we need to make a move and reset the counter of the Methods in AgentMove
        setNodesExamined(0);
        setPrunedCounter(0);
        setReachedLeafNodes(0);
        setSearchDepth(0);

        //Run miniMax algorithm and calculate the time of its execution
        long timeBefore = new Date().getTime();
        int minimaxTreeBuild = minimaxThreeBuild(gameState, 0, true, UserSettings.MIN_VALUE, UserSettings.MAX_VALUE);
        long timeAfter = new Date().getTime();

        System.out.println("Time of miniMax search was: " + (timeAfter-timeBefore) + " ms");

        //Match an available move with the utility Value of the minimaxThreeBuild Alpha Beta pruning search.
        ObjectiveWrapper agentMove = null;
        List<GameBoardState> states = gameState.getChildStates();

        //for all states in the list of game board states and check so that is matches the value returned from the algorithm search
        for (GameBoardState state : states) {
            if (state.utilityValue == minimaxTreeBuild) {
                agentMove = state.getLeadingMove(); //Leading move, gets the move that created this current state
                break;
            }
        }
        return new MoveWrapper(agentMove); // return the optimal move
    }

    /**
     * minimaxThreeBuild is the algorithm for building the tree and the alpha-beta pruning-miniMax algorithm.
     * @param node parent node whose children will be traversed
     * @param depth depth of current "parent" node
     * @param maximizingPlayer true if maximizing player - false for minimizing player
     * @param alpha alpha-value
     * @param beta beta-value
     * @return the utility value of AI move
     */
    private int minimaxThreeBuild(GameBoardState node, int depth, boolean maximizingPlayer, int alpha, int beta) {

        //set the boxes on the GUI game board
        setSearchDepth(depth);
        setNodesExamined(getNodesExamined() + 1);
        int value = UserSettings.MIN_VALUE;

        //If we reached the leaf node in maximal depth, then we return the utility Value
        if (depth == UserSettings.MAX_TREE_DEPTH) return getUtility(node);

        //If maximizingPlayer is true
        if (maximizingPlayer) {
            //Get all available moves for max Player
            List<ObjectiveWrapper> possibleMoves = AgentController.getAvailableMoves(node, PlayerTurn.PLAYER_ONE);

            //Traverse / iterate through all available moves for the max player saved in the list
            for (ObjectiveWrapper move : possibleMoves) {
                GameBoardState childState = AgentController.getNewState(node, move);
                value = Math.max(value, minimaxThreeBuild(childState, depth + 1, false, alpha, beta));

                //if utility value is bigger or equal to beta then prune remaining child nodes that does not affect the final utility value.
                if (value >= beta) {
                    setPrunedCounter(getPrunedCounter() + 1); // increment the pruned counter for each iteration, so we don't go back and look at already pruned nodes
                    node.utilityValue = value; // set the utility value of the node that was reached so that we can check what move to choose after the algorithm has executed
                    return value;

                }
                node.addChildState(childState);
                alpha = Math.max(alpha, value);
            }
            node.utilityValue = value; //utility = payoff value

        } else {  //Get all available moves for min Player maximizingPlayer = false
            List<ObjectiveWrapper> possibleMoves = AgentController.getAvailableMoves(node, PlayerTurn.PLAYER_TWO);
            for (ObjectiveWrapper move : possibleMoves) {
                GameBoardState childState = AgentController.getNewState(node, move);
                value = Math.min(value, minimaxThreeBuild(childState, depth + 1, true, alpha, beta));

                //if utility value is less than or equal to alpha then prune remaining child nodes that does not affect the final utility value.
                if (value <= alpha) {
                    setPrunedCounter(getPrunedCounter() + 1);
                    node.utilityValue = value;
                    return value;
                }
                node.addChildState(childState);
                beta = Math.min(beta, value);
            }
            node.utilityValue = value;
        }
        return value;
    }


    private int getUtility(GameBoardState state)
    {
        setReachedLeafNodes(getReachedLeafNodes() + 1);
        return (state.getWhiteCount() - state.getBlackCount());
    }

}




