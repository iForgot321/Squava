package squava;

import java.util.Map;
import java.util.HashMap;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.Font;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JLabel;

/**
 * @author iForgot321 
 * @since 2020-06-04
 *
 */
public class Squava extends JPanel {

    private static final long serialVersionUID = 1L;
    private static Board board;
    private static int status;
    private static int playerNo;
    private static Map<Long, Integer> cache;

    public Squava() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JButton ng = new JButton("New Game");
        JButton undo = new JButton("Undo");
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomPanel.add(ng);
        bottomPanel.add(undo);
        add(bottomPanel, BorderLayout.PAGE_END);

        JLabel losingLabel = new JLabel("Black has won!");
        losingLabel.setFont(new Font("Helvetica", 1, 24));
        JPanel losingPanel = new JPanel(new BorderLayout());
        losingPanel.setVisible(false);
        losingPanel.setBackground(Color.WHITE);
        losingPanel.add(losingLabel);
        losingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 50));
        add(losingPanel, BorderLayout.LINE_END);

        ng.addActionListener((ActionEvent e) -> {
            board.reset();
            board.performMove(-1, 6);
            playerNo = 1;
            status = 0;
            losingPanel.setVisible(false);
            repaint();
        });
        undo.addActionListener((ActionEvent e) -> {
            if (status == 1) {
                board = board.prevBoard(1);
            } else {
                board = board.prevBoard(2);
            }
            status = 0;
            playerNo = 1;
            losingPanel.setVisible(false);
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (playerNo == 1 && e.getX() > 50 && e.getX() < 300 && e.getY() > 50 && e.getY() < 300) {
                    int xCoord = (e.getX() - 50) / 50, yCoord = (e.getY() - 50) / 50;
                    if (!board.containsMove(xCoord, yCoord)) {
                        board.performMove(playerNo, xCoord + 5 * yCoord);
                        playerNo = -playerNo;
                        int res = board.checkStatus();
                        if (res != -2) {
                            playerNo = -2;
                            status = 1;
                            losingPanel.setVisible(true);
                        }
                        repaint();

                        board = getMove(board, new AlphaBetaAgent(12));
                        playerNo = -playerNo;
                        res = board.checkStatus();
                        if (res != -2 && status != 1) {
                            playerNo = -2;
                            status = 2;
                            losingPanel.setVisible(true);
                        }
                        repaint();
                    }
                }
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (status != 0) {
            if (status == 1) {
                g2.setColor(Color.red);
            } else if (status == 2) {
                g2.setColor(Color.green);
            }
            for (int p : board.showEnding()) {
                g2.fillRect(50 + 50 * (p % 5), 50 + 50 * (p / 5), 50, 50);
            }
        }

        g2.setColor(Color.black);
        for (int i = 50; i <= 300; i += 50) {
            g2.drawLine(i, 50, i, 300);
            g2.drawLine(50, i, 300, i);
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.black);

        long omask = 1 << 24;
        long xmask = omask << 25;
        for (int i = 0; i < 25; i++) {
            if ((board.getBoardValue() & omask) != 0) {
                g2.setColor(Color.white);
                g2.fillOval(57 + 50 * (i % 5), 57 + 50 * (i / 5), 36, 36);
                g2.setColor(Color.black);
                g2.drawOval(57 + 50 * (i % 5), 57 + 50 * (i / 5), 36, 36);
            } else if ((board.getBoardValue() & xmask) != 0) {
                g2.fillOval(57 + 50 * (i % 5), 57 + 50 * (i / 5), 36, 36);
            }
            omask >>= 1;
            xmask >>= 1;
        }
    }

    public static void main(String[] args) {
        board = new Board();
        board.performMove(-1, 6);
        cache = new HashMap<>();
        playerNo = 1;
        status = 0;
        try {
            createStates();
        } catch (IOException e) {
            System.out.println(e);
        }
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("Squava");

            JMenuBar menuBar = new JMenuBar();
            JMenu menuFile = new JMenu("File");
            JMenu menuStat = new JMenu("Statistics");
            JMenu menuHelp = new JMenu("Help");
            JMenuItem exit = new JMenuItem("Exit");
            JMenuItem newGame = new JMenuItem("New Game");
            menuFile.add(newGame);
            menuFile.add(exit);

            menuBar.add(menuFile);
            menuBar.add(menuStat);
            menuBar.add(menuHelp);
            frame.setJMenuBar(menuBar);

            Squava s = new Squava();
            frame.add(s);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 440);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

    }

    public static void createStates() throws IOException {
        String file = "src/squava/resources/states.txt";
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String st;
            while ((st = in.readLine()) != null) {
                String[] temp = st.split(" ");
                cache.put(Long.parseLong(temp[0]), Integer.parseInt(temp[1]));
            }
        }
    }

    public static Board getMove(Board board, Agent ab) {
        if (cache.containsKey(board.getBoardValue())) {
            board.performMove(-1, cache.get(board.getBoardValue()));
        } else if (cache.containsKey(Board.diagFlip(board.getBoardValue()))) {
            int pos = cache.get(Board.diagFlip(board.getBoardValue()));
            int x = 4 - (pos % 5), y = 4 - (pos / 5);
            board.performMove(-1, 5 * x + y);
        } else if (cache.containsKey(Board.antiDiagFlip(board.getBoardValue()))) {
            int pos = cache.get(Board.antiDiagFlip(board.getBoardValue()));
            int x = pos % 5, y = pos / 5;
            board.performMove(-1, 5 * x + y);
        } else if (cache.containsKey(Board.vertFlip(board.getBoardValue()))) {
            int pos = cache.get(Board.vertFlip(board.getBoardValue()));
            int x = pos % 5, y = 4 - (pos / 5);
            board.performMove(-1, 5 * y + x);
        } else if (cache.containsKey(Board.horiFlip(board.getBoardValue()))) {
            int pos = cache.get(Board.horiFlip(board.getBoardValue()));
            int x = 4 - (pos % 5), y = pos / 5;
            board.performMove(-1, 5 * y + x);
        } else {
            board = ab.findNextMove(board, -1);
        }
        return board;
    }

}
