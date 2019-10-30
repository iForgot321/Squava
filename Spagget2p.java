/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spagget2p;
//import java.util.Scanner;

import java.util.HashMap;
import java.io.PrintWriter;
import java.io.IOException;

/**
 *
 * @author iforg
 */
public class Spagget2p {

    HashMap<Long, Byte> states = new HashMap<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int[][] board = new int[5][5];
        //Scanner scan = new Scanner(System.in);
        try {
            PrintWriter writer = new PrintWriter("output.txt", "UTF-8");

            int beginx = 2;
            int beginy = 2;
            int b = search(board, 1, 1, beginx, beginy, writer);
            if (b == 0) {
                writer.println("Black wins by going " + (beginx + 1) + ", " + (beginy + 1) + ". ");
            } else if (b == 1) {
                writer.println("Black loses by going " + (beginx + 1) + ", " + (beginy + 1) + ". ");
            }
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    public static int search(int[][] board, int colour, int moves, int xmove, int ymove, PrintWriter writer) {
        board[xmove][ymove] = colour;
        //System.out.println("Colour "+colour+" went "+"("+(xmove+1)+", "+(ymove+1)+") on move "+moves);
        //printBoard(board);
        if (cond(board, colour, moves, xmove, ymove) == 0) {
            return 0; //node wins
        } else if (cond(board, colour, moves, xmove, ymove) == 1) {
            return 1; //node loses
        } else if (cond(board, colour, moves, xmove, ymove) == 2) {
            return 2; //node ties
        } else {
            //if (colour == 1) { // black (1)
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (board[i][j] == 0) {
                        int res = search(board, colour * -1, moves + 1, i, j, writer);
                        if (res == 0) {
                            writer.println((colour*-1)+" W (" + (j + 1) + " " + (i + 1) + ") m:" + moves);
                            printBoard(board, writer);
                            board[i][j] = 0;
                            return 1;
                        }
                        board[i][j] = 0;
                    }
                }
            }
//            if (colour*-1 == -1) {
//                System.out.println("Black wins in this variation when it plays ("+(xmove+1)+", "+(ymove+1)+"). Move: " + moves + ".");
//            } else {
//                System.out.println("White wins in this variation when it plays ("+(xmove+1)+", "+(ymove+1)+"). Move: " + moves + ".");
//            }
//
//            printBoard(board);
            return 0;
//            } else { // white (-1)
//                for (int i = 0; i < 5; i++) {
//                    for (int j = 0; j < 5; j++) {
//                        if(board[i][j] == 0){
//                            int res = search(board, colour*-1, i, j); //black makes this move
//                            if(res == 0){
//                                return 1;
//                            }
//                        }
//                    }
//                }
//            }
        }
    }

    public static int cond(int[][] board, int colour, int moves, int xmove, int ymove) {
        int s1 = 1 + lineCount(board, colour, xmove, ymove, 0, 1) + lineCount(board, colour, xmove, ymove, 0, -1);
        if (s1 == 4) {
            return 0;
        } else if (s1 == 3) {
            return 1;
        }
        int s2 = 1 + lineCount(board, colour, xmove, ymove, 1, 0) + lineCount(board, colour, xmove, ymove, -1, 0);
        if (s2 == 4) {
            return 0;
        } else if (s2 == 3) {
            return 1;
        }
        int d1 = 1 + lineCount(board, colour, xmove, ymove, 1, 1) + lineCount(board, colour, xmove, ymove, -1, -1);
        if (d1 == 4) {
            return 0;
        } else if (d1 == 3) {
            return 1;
        }
        int d2 = 1 + lineCount(board, colour, xmove, ymove, -1, 1) + lineCount(board, colour, xmove, ymove, 1, -1);
        if (d2 == 4) {
            return 0;
        } else if (d2 == 3) {
            return 1;
        }

        if (moves == 25) {
            return 2;
        }
        return -1;
    }

    public static int lineCount(int[][] board, int colour, int xmove, int ymove, int xco, int yco) {
        int c = 0;
        for (int i = 1; i < 3; i++) {
            if (xmove + (i * xco) > 4 || xmove + (i * xco) < 0 || ymove + (i * yco) > 4 || ymove + (i * yco) < 0 || board[xmove + (i * xco)][ymove + (i * yco)] != colour) {
                break;
            } else {
                //System.out.println("xco: "+xco+", yco: "+yco+", i: "+i);
                c++;
            }
        }
        return c;
    }

    public static void printBoard(int[][] board, PrintWriter writer) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == 0) {
                    writer.print("  ");
                } else if (board[i][j] == 1) {
                    writer.print("X ");
                } else {
                    writer.print("O ");
                }
            }
            writer.println();
        }
        writer.println();
    }
    
    public static long compress(int[][] board){
        long c = 0;
        for(int i = 0; i < 25; i++){
            int a = board[Math.floorDiv(i, 5)][i%5];
            //System.out.println(i+" "+a);
            if(a == 1){
                c += (long) Math.pow(2, 2*i);
                //System.out.println(Long.toBinaryString((long) Math.pow(2, 2*i)));
            }else if(a == -1){
                c += (long) Math.pow(2, (2*i)+1);
                //System.out.println(Long.toBinaryString((long) Math.pow(2, (2*i)+1)));
            }
        }
        return c;
    }
}
