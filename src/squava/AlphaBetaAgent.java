package squava;

import java.util.List;
import java.util.ArrayList;

public class AlphaBetaAgent extends Agent {

    int depth;
    Board bestMove;
    long count;

    public AlphaBetaAgent(int depth) {
        super();
        this.depth = depth;
    }

    @Override
    public Board findNextMove(Board board, int playerNo) {
        bestMove = board;
        count = 0;
        search(board, this.depth, Integer.MIN_VALUE, Integer.MAX_VALUE, playerNo);
        return this.bestMove;
    }

    private int search(Board board, int depth, int alpha, int beta, int playerNo) {
        count++;
        if (depth == 0) return board.evaluate();
        int status = board.checkStatus();
        switch (status) {
            case Board.P1:
                return Integer.MAX_VALUE - (this.depth - depth);
            case Board.P2:
                return Integer.MIN_VALUE + (this.depth - depth);
            case Board.DRAW:
                return 0;
            default:
                if (playerNo == Board.P1) {
                    int value = Integer.MIN_VALUE;
                    List<Board> children = new ArrayList<>();
                    if (board.getTotalMoves() == 0) {
                        for (int p : Board.OPENINGS) {
                            Board temp = new Board(board);
                            temp.performMove(playerNo, p);
                            children.add(temp);
                        }
                    } else {
                        board.getEmptyPositions(playerNo).forEach((c) -> {
                            Board temp = new Board(board);
                            temp.performMove(playerNo, c);
                            children.add(temp);
                        });
                    }
                    children.sort((Board node1, Board node2)
                            -> node2.evaluate() - node1.evaluate());
                    
                    for (Board temp : children) {
                        int res = search(temp, depth - 1, alpha, beta, 0 - playerNo);
                        if (res > value) {
                            if (this.depth == depth) {
                                bestMove = temp;
                            }
                            value = res;
                        }
                        alpha = Math.max(alpha, value);
                        if (alpha >= beta) {
                            break;
                        }
                    }
                    return value;
                } else {
                    int value = Integer.MAX_VALUE;
                    List<Board> children = new ArrayList<>();
                    board.getEmptyPositions(playerNo).forEach((c) -> {
                        Board temp = new Board(board);
                        temp.performMove(playerNo, c);
                        children.add(temp);
                    });
                    children.sort((Board node1, Board node2)
                            -> node1.evaluate() - node2.evaluate());
                    
                    for (Board temp : children) {
                        int res = search(temp, depth - 1, alpha, beta, 0 - playerNo);
                        if (res < value) {
                            value = res;
                            if (this.depth == depth) {
                                bestMove = temp;
                            }
                        }
                        beta = Math.min(beta, value);
                        if (alpha >= beta) {
                            break;
                        }
                    }
                    return value;
                }
        }
    }

}
