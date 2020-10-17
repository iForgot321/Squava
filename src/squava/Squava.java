package squava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author iForgot321 2020-06-04
 *
 */
public class Squava extends JPanel implements ActionListener{

    private static final long serialVersionUID = 1L;
    private static Board board;
    private static int status, playerNo, humanPlayerNo, currOpening;
    private static Map<Long, Integer> cache;
    private static JLabel losingLabel;

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
        
        ng.addActionListener(this);
        undo.addActionListener(this);

        losingLabel = new JLabel("");
        losingLabel.setFont(new Font("Helvetica", Font.BOLD, 24));
        JPanel losingPanel = new JPanel(new BorderLayout());
        losingPanel.setBackground(Color.WHITE);
        losingPanel.add(losingLabel);
        losingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 50));
        add(losingPanel, BorderLayout.LINE_END);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (playerNo == humanPlayerNo && e.getX() > 50 && e.getX() < 300 && e.getY() > 50 && e.getY() < 300) {
                    int xCoord = (e.getX() - 50) / 50, yCoord = (e.getY() - 50) / 50;
                    if (!board.containsMove(xCoord, yCoord)) {
                        if(currOpening == -1){
                            board.performMove(playerNo, xCoord + 5 * yCoord);
                            playerNo = -playerNo;
                            humanPlayerNo = -humanPlayerNo;
                            int res = board.checkStatus();
                            if (res != -2) {
                                playerNo = -2;
                                status = 1;
                                showLabel(res);
                            }
                        }else{
                            board.performMove(playerNo, xCoord + 5 * yCoord);
                            playerNo = -playerNo;
                            int res = board.checkStatus();
                            if (res != -2) {
                                playerNo = -2;
                                status = 1;
                                showLabel(res);
                            }else{
                                board = getMove(board, new AlphaBetaAgent(12), playerNo);
                                playerNo = -playerNo;
                                res = board.checkStatus();
                                if (res != -2 && status != 1) {
                                    playerNo = -2;
                                    status = 2;
                                    showLabel(res);
                                }
                            }
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (status != 0 && board.showEnding().length >= 3) {
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

        long oMask = 1 << 24;
        long xMask = oMask << 25;
        for (int i = 0; i < 25; i++) {
            if ((board.getBoardValue() & oMask) != 0) {
                g2.setColor(Color.white);
                g2.fillOval(57 + 50 * (i % 5), 57 + 50 * (i / 5), 36, 36);
                g2.setColor(Color.black);
                g2.drawOval(57 + 50 * (i % 5), 57 + 50 * (i / 5), 36, 36);
            } else if ((board.getBoardValue() & xMask) != 0) {
                g2.fillOval(57 + 50 * (i % 5), 57 + 50 * (i / 5), 36, 36);
            }
            oMask >>= 1;
            xMask >>= 1;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        switch (e.getActionCommand()) {
            case "New Game":
                board.reset();
                switch (currOpening) {
                    case 0:
                        board.performMove(-1, 0);
                        break;
                    case 1:
                        board.performMove(-1, 1);
                        break;
                    case 2:
                        board.performMove(-1, 2);
                        board.performMove(1, 5);
                        break;
                    case 6:
                        board.performMove(-1, 6);
                        break;
                    case 7:
                        board.performMove(-1, 7);
                        board.performMove(1, 21);
                        break;
                    case 12:
                        board.performMove(-1, 12);
                        board.performMove(1, 6);
                        break;
                    default:
                        break;
                }
                playerNo = humanPlayerNo;
                status = 0;
                resetLabel();
                repaint();
                break;
            case "Undo":
                if(currOpening == -1){
                    board = board.prevBoard(1);
                    status = 0;
                    playerNo = -playerNo;
                    humanPlayerNo = -humanPlayerNo;
                    resetLabel();
                    repaint();
                }else if(board.getTotalMoves() > 2){
                    if (status == 1) {
                        board = board.prevBoard(1);
                    } else {
                        board = board.prevBoard(2);
                    }
                    status = 0;
                    playerNo = humanPlayerNo;
                    resetLabel();
                    repaint();
                }   
                break;
            case "Opening 0":
                board.reset();
                board.performMove(-1, 0);
                playerNo = 1;
                status = 0;
                currOpening = 0;
                humanPlayerNo = 1;
                resetLabel();
                repaint();
                break;
            case "Opening 1":
                board.reset();
                board.performMove(-1, 1);
                playerNo = 1;
                status = 0;
                currOpening = 1;
                humanPlayerNo = 1;
                resetLabel();
                repaint();
                break;
            case "Opening 2":
                board.reset();
                board.performMove(-1, 2);
                board.performMove(1, 5);
                playerNo = -1;
                status = 0;
                currOpening = 2;
                humanPlayerNo = -1;
                resetLabel();
                repaint();
                break;
            case "Opening 6":
                board.reset();
                board.performMove(-1, 6);
                playerNo = 1;
                status = 0;
                currOpening = 6;
                humanPlayerNo = 1;
                resetLabel();
                repaint();
                break;
            case "Opening 7":
                board.reset();
                board.performMove(-1, 7);
                board.performMove(1, 21);
                playerNo = -1;
                status = 0;
                currOpening = 7;
                humanPlayerNo = -1;
                resetLabel();
                repaint();
                break;
            case "Opening 12":
                board.reset();
                board.performMove(-1, 12);
                board.performMove(1, 6);
                playerNo = -1;
                status = 0;
                currOpening = 12;
                humanPlayerNo = -1;
                resetLabel();
                repaint();
                break;
            case "Self play":
                board.reset();
                playerNo = -1;
                status = 0;
                currOpening = -1;
                humanPlayerNo = -1;
                resetLabel();
                repaint();
                break;
            default:
                break;
        }
    }

    public static void main(String[] args) {
        board = new Board();
        board.performMove(-1, 6);
        cache = new HashMap<>();
        playerNo = 1;
        status = 0;
        currOpening = 6;
        humanPlayerNo = 1;
        try {
            createStates();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("Squava");
            
            Squava s = new Squava();
            frame.add(s);
            
            JMenuBar menuBar = new JMenuBar();
            JMenu menuFile = new JMenu("File");
            JMenu menuHelp = new JMenu("About");
            JMenuItem exit = new JMenuItem("Exit");
            JMenuItem op0 = new JMenuItem("Opening 0");
            JMenuItem op1 = new JMenuItem("Opening 1");
            JMenuItem op2 = new JMenuItem("Opening 2");
            JMenuItem op6 = new JMenuItem("Opening 6");
            JMenuItem op7 = new JMenuItem("Opening 7");
            JMenuItem op12 = new JMenuItem("Opening 12");
            JMenuItem selfPlay = new JMenuItem("Self play");
            
            op0.addActionListener(s);
            op1.addActionListener(s);
            op2.addActionListener(s);
            op6.addActionListener(s);
            op7.addActionListener(s);
            op12.addActionListener(s);
            selfPlay.addActionListener(s);
            exit.addActionListener(e -> frame.dispose());
            
            menuFile.add(op0);
            menuFile.add(op1);
            menuFile.add(op2);
            menuFile.add(op6);
            menuFile.add(op7);
            menuFile.add(op12);
            menuFile.add(selfPlay);
            menuFile.add(exit);

            menuBar.add(menuFile);
            menuBar.add(menuHelp);
            frame.setJMenuBar(menuBar);
            
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 440);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });

    }

    static void createStates() throws IOException {
        String file = "src/squava/resources/states.txt";
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String st;
            while ((st = in.readLine()) != null) {
                String[] temp = st.split(" ");
                cache.put(Long.parseLong(temp[0]), Integer.parseInt(temp[1]));
            }
        }
    }

    static Board getMove(Board board, Agent ab, int playerNo) {
        if (cache.containsKey(board.getBoardValue())) {
            board.performMove(playerNo, cache.get(board.getBoardValue()));
        } else if (cache.containsKey(Board.diagonalFlip(board.getBoardValue()))) {
            int pos = cache.get(Board.diagonalFlip(board.getBoardValue()));
            int x = 4 - (pos % 5), y = 4 - (pos / 5);
            board.performMove(playerNo, 5 * x + y);
        } else if (cache.containsKey(Board.antiDiagonalFlip(board.getBoardValue()))) {
            int pos = cache.get(Board.antiDiagonalFlip(board.getBoardValue()));
            int x = pos % 5, y = pos / 5;
            board.performMove(playerNo, 5 * x + y);
        } else if (cache.containsKey(Board.verticalFlip(board.getBoardValue()))) {
            int pos = cache.get(Board.verticalFlip(board.getBoardValue()));
            int x = pos % 5, y = 4 - (pos / 5);
            board.performMove(playerNo, 5 * y + x);
        } else if (cache.containsKey(Board.horizontalFlip(board.getBoardValue()))) {
            int pos = cache.get(Board.horizontalFlip(board.getBoardValue()));
            int x = 4 - (pos % 5), y = pos / 5;
            board.performMove(playerNo, 5 * y + x);
        } else {
            board = ab.findNextMove(board, playerNo);
        }
        return board;
    }
    
    static void showLabel(int res){
        switch(res){
            case -1:
                losingLabel.setText("Black has won");
                break;
            case 1:
                losingLabel.setText("White has won");
                break;
            case 0:
                losingLabel.setText("Draw");
                break;
        }
    }
    
    static void resetLabel(){
        losingLabel.setText("");
    }

}
