package squava;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public class Board {

    private long boardValue;
    private int totalMoves;
    private int prevMove;
    private List<Integer> allMoves;

    public static final int DEFAULT_BOARD_SIZE = 5;
    public static final int IN_PROGRESS = -2;
    public static final int DRAW = 0;
    public static final int P1 = -1;
    public static final int P2 = 1;

    private static final int[] WIN_MASKS = {
        0b1111000000000000000000000, 0b111100000000000000000000,
        0b10000100001000010000000, 0b11110000000000000000,
        0b1111000000000000000, 0b100001000010000100,
        0b1000010000100001000000000, 0b10000100001000010000,
        0b100001000010000100000000, 0b1000010000100001000,
        0b1000100010001000000000, 0b10000010000010000010, 0b111100000000000,
        0b11110000000000, 0b100010001000100000000, 0b10001000100010000,
        0b1000001000001000001000000, 0b1000001000001000001,
        0b1000010000100001000000, 0b10000100001000010,
        0b100000100000100000100000, 0b1000100010001000,
        0b100001000010000100000, 0b1000010000100001, 0b1111000000,
        0b111100000, 0b11110, 0b1111
    };

    private static final int[] LOSE_MASKS = {
        0b1110000000000000000000000, 0b111000000000000000000000,
        0b11100000000000000000000, 0b10000100001000000000000,
        0b10001000100000000000000, 0b10000010000010000000000,
        0b11100000000000000000, 0b1110000000000000000, 0b111000000000000000,
        0b10000100001000000000000, 0b100001000010000000,
        0b1000100010000000000000, 0b100010001000000000,
        0b100000100000100000000000, 0b100000100000100000, 0b111000000000000,
        0b1000010000100000000000000, 0b10000100001000000000, 0b100001000010000,
        0b10001000100000000000000, 0b100000100000100, 0b111000000000000,
        0b11100000000000, 0b100001000010000000000000, 0b1000010000100000000,
        0b10000100001000, 0b1000100010000000000000, 0b100010001000000000,
        0b10000010000010000000, 0b10000010000010, 0b111000000000000,
        0b11100000000000, 0b1110000000000, 0b10000100001000000000000,
        0b100001000010000000, 0b1000010000100, 0b1000001000001000000000000,
        0b1000001000001000000, 0b1000001000001, 0b100010001000000000000,
        0b10001000100000000, 0b1000100010000, 0b11100000000000,
        0b1110000000000, 0b1000010000100000000000, 0b10000100001000000,
        0b100001000010, 0b100000100000100000000000, 0b100000100000100000,
        0b100010001000, 0b1000100010000000, 0b1110000000000,
        0b100001000010000000000, 0b1000010000100000, 0b10000100001,
        0b10001000100, 0b10000010000010000000000, 0b1110000000, 0b111000000,
        0b11100000, 0b100001000010000000, 0b1000010000100,
        0b10000010000010000000, 0b10000010000010, 0b100010001000,
        0b1000100010000000, 0b11100, 0b1110, 0b111, 0b1000010000100,
        0b100000100000100, 0b10001000100
    };

    private final static int[] SQUARE_MASKS = {
        0b1001000000000001001000000, 0b0100100000000000100100000,
        0b0000010010000000000010010, 0b0000001001000000000001001
    };

    private final static int[][] DIRS = {
        {0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
    };

    public final static int[] OPENINGS = {0, 1, 2, 6, 7, 12};

    public HashSet<Integer> P1winningMoves;
    public HashSet<Integer> P2winningMoves;
    public HashSet<Integer> P1losingMoves;
    public HashSet<Integer> P2losingMoves;

    public Board() {
        boardValue = 0;
        prevMove = 0;
        totalMoves = 0;
        allMoves = new ArrayList<>();
        P1winningMoves = new HashSet<>();
        P1losingMoves = new HashSet<>();
        P2winningMoves = new HashSet<>();
        P2losingMoves = new HashSet<>();
    }

    public Board(Board board) {
        this.boardValue = board.getBoardValue();
        this.totalMoves = board.totalMoves;
        this.prevMove = board.prevMove;
        this.allMoves = new ArrayList<>();
        board.getAllMoves().forEach((p) -> {
            this.allMoves.add(p);
        });
        this.P1winningMoves = new HashSet<>();
        this.P1losingMoves = new HashSet<>();
        this.P2winningMoves = new HashSet<>();
        this.P2losingMoves = new HashSet<>();
        board.P1losingMoves.forEach((i) -> {
            this.P1losingMoves.add(i);
        });
        board.P2losingMoves.forEach((i) -> {
            this.P2losingMoves.add(i);
        });
        board.P1winningMoves.forEach((i) -> {
            this.P1winningMoves.add(i);
        });
        board.P2winningMoves.forEach((i) -> {
            this.P2winningMoves.add(i);
        });
    }

    public void performMove(int playerNo, int position) {
        this.totalMoves++;
        int x = position / DEFAULT_BOARD_SIZE;
        int y = position % DEFAULT_BOARD_SIZE;
        for (int[] dir : DIRS) {
            if (x + 3 * dir[0] <= 4 && x + 3 * dir[0] >= 0 && y + 3 * dir[1] <= 4 && y + 3 * dir[1] >= 0
                    && (boardValue & mask(x + 3 * dir[0], y + 3 * dir[1], playerNo)) != 0) {
                if ((boardValue & mask(x + dir[0], y + dir[1], playerNo)) != 0
                        && (boardValue & mask(x + 2 * dir[0], y + 2 * dir[1], 0 - playerNo)) == 0) {
                    getWinningMoves(playerNo).add(5 * (x + 2 * dir[0]) + y + 2 * dir[1]);
                    getLosingMoves(playerNo).remove(5 * (x + 2 * dir[0]) + y + 2 * dir[1]);
                } else if ((boardValue & mask(x + 2 * dir[0], y + 2 * dir[1], playerNo)) != 0
                        && (boardValue & mask(x + dir[0], y + dir[1], 0 - playerNo)) == 0) {
                    getWinningMoves(playerNo).add(5 * (x + dir[0]) + y + dir[1]);
                    getLosingMoves(playerNo).remove(5 * (x + dir[0]) + y + dir[1]);
                }
            }
            if (x + 2 * dir[0] <= 4 && x + 2 * dir[0] >= 0 && y + 2 * dir[1] <= 4 && y + 2 * dir[1] >= 0
                    && x - dir[0] <= 4 && x - dir[0] >= 0 && y - dir[1] <= 4 && y - dir[1] >= 0
                    && (boardValue & mask(x + 2 * dir[0], y + 2 * dir[1], playerNo)) != 0
                    && (boardValue & mask(x - dir[0], y - dir[1], playerNo)) != 0
                    && (boardValue & mask(x + dir[0], y + dir[1], 0 - playerNo)) == 0) {
                getWinningMoves(playerNo).add(5 * (x + dir[0]) + y + dir[1]);
                getLosingMoves(playerNo).remove(5 * (x + dir[0]) + y + dir[1]);
            }
            if (x + 2 * dir[0] <= 4 && x + 2 * dir[0] >= 0 && y + 2 * dir[1] <= 4 && y + 2 * dir[1] >= 0
                    && (boardValue & mask(x + 2 * dir[0], y + 2 * dir[1], playerNo)) != 0
                    && (boardValue & mask(x + dir[0], y + dir[1], 0 - playerNo)) == 0
                    && !getWinningMoves(playerNo).contains(5 * (x + dir[0]) + y + dir[1])) {
                getLosingMoves(playerNo).add(5 * (x + dir[0]) + y + dir[1]);
            }
            if (x + dir[0] <= 4 && x + dir[0] >= 0 && y + dir[1] <= 4 && y + dir[1] >= 0
                    && (boardValue & mask(x + dir[0], y + dir[1], playerNo)) != 0) {
                if (x + 2 * dir[0] <= 4 && x + 2 * dir[0] >= 0 && y + 2 * dir[1] <= 4 && y + 2 * dir[1] >= 0
                        && (boardValue & mask(x + 2 * dir[0], y + 2 * dir[1], 0 - playerNo)) == 0
                        && !getWinningMoves(playerNo).contains(5 * (x + 2 * dir[0]) + y + 2 * dir[1])) {
                    getLosingMoves(playerNo).add(5 * (x + 2 * dir[0]) + y + 2 * dir[1]);
                }
                if (y - dir[1] <= 4 && y - dir[1] >= 0 && x - dir[0] <= 4 && x - dir[0] >= 0
                        && (boardValue & mask(x - dir[0], y - dir[1], 0 - playerNo)) == 0
                        && !getWinningMoves(playerNo).contains(5 * (x - dir[0]) + y - dir[1])) {
                    getLosingMoves(playerNo).add(5 * (x - dir[0]) + y - dir[1]);
                }
            }
        }

        getWinningMoves(0 - playerNo).remove(5 * x + y);
        getLosingMoves(0 - playerNo).remove(5 * x + y);
        boardValue |= mask(position / DEFAULT_BOARD_SIZE, position % DEFAULT_BOARD_SIZE, playerNo);
        prevMove = position;
        allMoves.add(position);
    }

    public static long mask(int row, int col, int playerNo) {
        int offset = 5 * (4 - row) + (4 - col);
        if (playerNo == -1) {
            offset += 25;
        }
        return 1L << offset;
    }

    public int evaluate() {
        int eval = 0;
        if (P1winningMoves.size() >= 2) {
            eval += 500;
        } else if (P1winningMoves.size() == 1) {
            eval += 5;
        }
        if (P2winningMoves.size() >= 2) {
            eval -= 500;
        } else if (P2winningMoves.size() == 1) {
            eval -= 5;
        }
        eval -= 2 * P1losingMoves.size();
        eval += 2 * P2losingMoves.size();

        eval += 2 * Long.bitCount((boardValue >> 25) & 0b0101010001000001000101010);
        eval -= 2 * Long.bitCount(boardValue & 0b0101010001000001000101010);
        eval += Long.bitCount((boardValue >> 25) & 0b1010100000101010000010101);
        eval -= Long.bitCount(boardValue & 0b1010100000101010000010101);
        eval -= Long.bitCount((boardValue >> 25) & 0b0000000100010100010000000);
        eval += Long.bitCount(boardValue & 0b0000000100010100010000000);

        for (int mask : SQUARE_MASKS) {
            int p1c = Long.bitCount((boardValue >> 25) & mask);
            int p2c = Long.bitCount(boardValue & mask);
            switch (p1c) {
                case 2:
                    eval += 3;
                    break;
                case 3:
                    eval += 10;
                    break;
                case 4:
                    eval += 20;
                    break;
            }
            switch (p2c) {
                case 2:
                    eval -= 3;
                    break;
                case 3:
                    eval -= 10;
                    break;
                case 4:
                    eval -= 20;
                    break;
            }
        }

        return eval;
    }

    public int checkStatus() {
        if (getTotalMoves() <= 4) {
            return IN_PROGRESS;
        }

        long shift = boardValue >> 25;
        for (int mask : WIN_MASKS) {
            if ((boardValue & mask) == mask) {
                return P2;
            }
            if ((shift & mask) == mask) {
                return P1;
            }
        }
        for (int mask : LOSE_MASKS) {
            if ((boardValue & mask) == mask) {
                return P1;
            }
            if ((shift & mask) == mask) {
                return P2;
            }
        }

        if (getTotalMoves() >= 25) {
            return DRAW;
        }
        return IN_PROGRESS;
    }

    public int[] showEnding() {
        long shift = boardValue >> 25;
        for (int mask : WIN_MASKS) {
            if ((boardValue & mask) == mask || (shift & mask) == mask) {
                long tmask = 1 << 24;
                int[] res = new int[4];
                int count = 0;
                for (int i = 0; i < 25; i++) {
                    if ((tmask & mask) != 0) {
                        res[count] = i;
                        if (++count == 4) {
                            break;
                        }
                    }
                    tmask >>= 1;
                }
                return res;
            }
        }
        for (int mask : LOSE_MASKS) {
            if ((boardValue & mask) == mask || (shift & mask) == mask) {
                long tmask = 1 << 24;
                int[] res = new int[3];
                int count = 0;
                for (int i = 0; i < 25; i++) {
                    if ((tmask & mask) != 0) {
                        res[count] = i;
                        if (++count == 3) {
                            break;
                        }
                    }
                    tmask >>= 1;
                }
                return res;
            }
        }
        return new int[1];
    }

    public List<Integer> getEmptyPositions(int playerNo) {
        List<Integer> emptyPositions = new ArrayList<>();
        if (!getWinningMoves(playerNo).isEmpty()) {
            getWinningMoves(playerNo).forEach((i) -> {
                emptyPositions.add(i);
            });
            return emptyPositions;
        } else if (!getWinningMoves(0 - playerNo).isEmpty()) {
            getWinningMoves(0 - playerNo).forEach((i) -> {
                emptyPositions.add(i);
            });
            return emptyPositions;
        }

        List<Integer> losingPositions = new ArrayList<>();
        long omask = 1 << 24;
        long xmask = omask << 25;
        for (int i = 0; i < 25; i++) {
            if (getLosingMoves(playerNo).contains(i)) {
                losingPositions.add(i);
            } else if ((boardValue & omask) == 0 && (boardValue & xmask) == 0) {
                emptyPositions.add(i);
            }
            omask >>= 1;
            xmask >>= 1;
        }

        return emptyPositions.isEmpty() ? losingPositions : emptyPositions;
    }

    public void reset() {
        boardValue = 0;
        prevMove = 0;
        totalMoves = 0;
        allMoves.clear();
        P1winningMoves.clear();
        P1losingMoves.clear();
        P2winningMoves.clear();
        P2losingMoves.clear();
    }

    public static long antiDiagFlip(long board) {
        long mid = board & 0x2082083041041L;
        board = ((board & 0x200000100000L) >> 16) | ((board & 0x20000010L) << 16)
                | ((board & 0x410000208000L) >> 12) | ((board & 0x410000208L) << 12)
                | ((board & 0x820800410400L) >> 8) | ((board & 0x8208004104L) << 8)
                | ((board & 0x1041040820820L) >> 4) | ((board & 0x104104082082L) << 4);

        return board | mid;
    }

    public static long diagFlip(long board) {
        long mid = board & 0x222220111110L;
        board = ((board & 0x2000001000000L) >> 24) | ((board & 0x2000001L) << 24)
                | ((board & 0x1100000880000L) >> 18) | ((board & 0x44000022L) << 18)
                | ((board & 0x888000444000L) >> 12) | ((board & 0x888000444L) << 12)
                | ((board & 0x444400222200L) >> 6) | ((board & 0x11110008888L) << 6);

        return board | mid;
    }

    public static long horiFlip(long board) {
        long mid = board & 0x842108421084L;
        board = ((board & 0x25294A5294A52L) >> 1) | ((board & 0x1294A5294A529L) << 1);
        board = ((board & 0x318C6318C6318L) >> 3) | ((board & 0x6318C6318C63L) << 3);
        return board | mid;
    }

    public static long vertFlip(long board) {
        return ((board << 20) & 0x3E00001F00000L)
                | ((board << 10) & 0x1F00000F8000L)
                | (board & 0xF800007C00L)
                | ((board >> 10) & 0x7C00003E0L)
                | ((board >> 20) & 0x3E00001FL);
    }

    public void printBoard() {
        long omask = 1 << 24;
        long xmask = omask << 25;
        System.out.println("   0 1 2 3 4");
        for (int i = 0; i < 25; i++) {
            if (i % 5 == 0) {
                System.out.print("\n" + (i / 5) + "  ");
            }
            if ((boardValue & omask) != 0) {
                System.out.print("O ");
            } else if ((boardValue & xmask) != 0) {
                System.out.print("X ");
            } else {
                System.out.print("- ");
            }
            omask >>= 1;
            xmask >>= 1;
        }
        System.out.println();
    }

    public static long arToLong(int[][] boardValues) {
        long board = 0;
        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                if (boardValues[i][j] != 0) {
                    board |= mask(i, j, boardValues[i][j]);
                }
            }
        }
        return board;
    }

    public Board prevBoard(int moves) {
        if (totalMoves == 0) {
            return new Board();
        }
        if (moves <= 0 || moves > totalMoves) {
            return this;
        }
        Board temp = new Board();
        for (int i = 0; i < allMoves.size() - moves; i++) {
            temp.performMove(i % 2 == 0 ? -1 : 1, allMoves.get(i));
        }
        return temp;
    }

    public boolean containsMove(int row, int col) {
        return (boardValue & (mask(col, row, -1) | mask(col, row, 1))) != 0;
    }

    public long getBoardValue() {
        return boardValue;
    }

    public void setBoardValue(long boardValue) {
        this.boardValue = boardValue;
    }

    public int getTotalMoves() {
        return totalMoves;
    }

    public void setTotalMoves(int totalMoves) {
        this.totalMoves = totalMoves;
    }

    private HashSet<Integer> getWinningMoves(int playerNo) {
        if (playerNo == -1) {
            return P1winningMoves;
        } else {
            return P2winningMoves;
        }
    }

    private HashSet<Integer> getLosingMoves(int playerNo) {
        if (playerNo == -1) {
            return P1losingMoves;
        } else {
            return P2losingMoves;
        }
    }

    public int getPrevMove() {
        return prevMove;
    }

    public void setPrevMove(int prevMove) {
        this.prevMove = prevMove;
    }

    public List<Integer> getAllMoves() {
        return allMoves;
    }
}
