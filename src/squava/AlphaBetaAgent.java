package squava;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlphaBetaAgent extends Agent {

    int depth;
    Board bestMove;
    long count;
    Map<Long, HashEntry> cache;

    public AlphaBetaAgent(int depth) {
        super();
        this.depth = depth;
    }

    @Override
    public Board findNextMove(Board board, int playerNo) {
        bestMove = board;
        count = 0;
        cache = new HashMap<>();
        search(board, this.depth, Integer.MIN_VALUE+1, Integer.MAX_VALUE, playerNo);
        return this.bestMove;
    }

    private int search(Board board, int depth, int alpha, int beta, int playerNo) {
        count++;
        if (depth == 0) {
            return board.evaluate() * -playerNo;
        }

        int status = board.checkStatus();
        switch (status) {
            case Board.P1:
                return (Integer.MAX_VALUE - (this.depth - depth)) * -playerNo;
            case Board.P2:
                return ((Integer.MIN_VALUE + 1) + (this.depth - depth)) * -playerNo;
            case Board.DRAW:
                return 0;
            default:
                int alphaO = alpha;
                HashEntry ttEntry = ttLookUp(board.getBoardValue());
                if (ttEntry != null) {
                    switch (ttEntry.flag) {
                        case 0:
                            return ttEntry.value;
                        case 1:
                            alpha = Math.max(alpha, ttEntry.value);
                            break;
                        case 2:
                            beta = Math.min(beta, ttEntry.value);
                            break;
                    }
                    if (alpha >= beta) {
                        return ttEntry.value;
                    }
                }
                
                int value = Integer.MIN_VALUE + 1;
                
                List<Board> children = new ArrayList<>();
                if (board.getTotalMoves() == 0) {
                    for (int p : Board.OPENINGS) {
                        Board temp = new Board(board);
                        temp.performMove(-1, p);
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
                        -> playerNo * (node1.evaluate() - node2.evaluate()));
                
                for (Board temp : children) {
                    int res = -search(temp, depth - 1, -beta, -alpha, -playerNo);

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
                if (value <= alphaO) {
                    cache.put(board.getBoardValue(), new HashEntry(value, (byte) 2));
                } else if (value >= beta) {
                    cache.put(board.getBoardValue(), new HashEntry(value, (byte) 1));
                } else {
                    cache.put(board.getBoardValue(), new HashEntry(value, (byte) 0));
                }
                return value;
        }
    }
    
    private HashEntry ttLookUp(long board) {
        if (cache.containsKey(board)) {
            return cache.get(board);
        } else if (cache.containsKey(Board.diagonalFlip(board))) {
            return cache.get(Board.diagonalFlip(board));
        } else if (cache.containsKey(Board.antiDiagonalFlip(board))) {
            return cache.get(Board.antiDiagonalFlip(board));
        } else if (cache.containsKey(Board.verticalFlip(board))) {
            return cache.get(Board.verticalFlip(board));
        } else if (cache.containsKey(Board.horizontalFlip(board))) {
            return cache.get(Board.horizontalFlip(board));
        } else {
            return null;
        }
    }

}

class HashEntry {

    public byte flag;
    public int value;

    public HashEntry(int value, byte flag) {
        this.flag = flag;
        this.value = value;
    }
}
