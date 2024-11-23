package GameSearch.mancala;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MancalaGameUI extends JFrame {
    private final JButton[] humanPits = new JButton[6];
    private final JButton[] programPits = new JButton[6];
    private final JLabel humanMancala = new JLabel("0", SwingConstants.CENTER);
    private final JLabel programMancala = new JLabel("0", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Human's Turn!", SwingConstants.CENTER);
    private int helpCounter = 0;
    private Mancala game;
    private MancalaPosition position;
    private boolean humanTurn;

    public MancalaGameUI() {
        game = new Mancala();
        selectDifficulty();  // User selects difficulty level
        position = new MancalaPosition();
        humanTurn = true;

        setupUI();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupUI() {
        setTitle("Mancala Game: Human vs Computer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout());

        JPanel boardPanel = setupBoardPanel();
        JPanel controlPanel = setupControlPanel();

        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel setupBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(2, 8, 10, 10));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boardPanel.setBackground(new Color(255, 228, 196)); // Light peach background

        styleMancala(programMancala);
        boardPanel.add(programMancala);

        for (int i = 5; i >= 0; i--) {
            programPits[i] = createPitButton(i + 7, false);
            boardPanel.add(programPits[i]);
        }

        // Symmetry placeholders
        boardPanel.add(new JLabel(""));
        boardPanel.add(new JLabel(""));

        for (int i = 0; i < 6; i++) {
            humanPits[i] = createPitButton(i, true);
            boardPanel.add(humanPits[i]);
        }

        styleMancala(humanMancala);
        boardPanel.add(humanMancala);

        return boardPanel;
    }

    private JPanel setupControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));

        JButton saveButton = createControlButton("Save Game");
        JButton loadButton = createControlButton("Load Game");
        JButton helpButton = createControlButton("Help");
        JButton quitButton = createControlButton("Quit");

        saveButton.addActionListener(e -> saveGame());
        loadButton.addActionListener(e -> loadGame());
        helpButton.addActionListener(e -> showHelp(helpButton));
        quitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(helpButton);
        buttonPanel.add(quitButton);

        controlPanel.add(statusLabel, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);

        return controlPanel;
    }

    private JButton createPitButton(int index, boolean isHuman) {
        JButton button = new JButton(String.valueOf(position.board[index]));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setEnabled(isHuman);
        button.setBackground(new Color(100, 149, 237)); // Cornflower blue
        button.setForeground(Color.WHITE); // White text for better visibility
        button.addActionListener(e -> handlePitClick(index));
        return button;
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(100, 149, 237)); // Cornflower blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleMancala(JLabel mancala) {
        mancala.setOpaque(true);
        mancala.setBackground(new Color(70, 130, 180)); // Steel blue background
        mancala.setForeground(Color.WHITE);
        mancala.setFont(new Font("Arial", Font.BOLD, 24));
        mancala.setPreferredSize(new Dimension(100, 200));
    }

    private void selectDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select Difficulty Level:",
                "Mancala Game",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (choice == 0) game.setDifficulty(Mancala.Difficulty.SIMPLE);
        else if (choice == 1) game.setDifficulty(Mancala.Difficulty.MEDIUM);
        else if (choice == 2) game.setDifficulty(Mancala.Difficulty.HARD);
        else System.exit(0);
    }

    private void handlePitClick(int pitIndex) {
        if (!humanTurn || position.board[pitIndex] == 0) {
            JOptionPane.showMessageDialog(this, "Invalid move! Choose a valid pit.");
            return;
        }

        Move move = new MancalaMove(pitIndex);
        position = (MancalaPosition) game.makeMove(position, true, move);
        updateBoard();

        if (position.extraTurn) {
            statusLabel.setText("Human gets another turn!");
        } else {
            humanTurn = false;
            statusLabel.setText("Computer's Turn...");
            SwingUtilities.invokeLater(this::handleComputerMove);
        }
    }

    private void handleComputerMove() {
        List<Object> result = game.alphaBeta(0, position, false);
        MancalaPosition bestMove = (MancalaPosition) result.get(1);
        position = bestMove;
        updateBoard();

        if (position.extraTurn) {
            statusLabel.setText("Computer gets another turn!");
            SwingUtilities.invokeLater(this::handleComputerMove);
        } else {
            humanTurn = true;
            statusLabel.setText("Human's Turn");
        }
    }

    private void updateBoard() {
        for (int i = 0; i < 6; i++) {
            humanPits[i].setText(String.valueOf(position.board[i]));
            programPits[i].setText(String.valueOf(position.board[i + 7]));
            programPits[i].setForeground(Color.WHITE); // Set text color to white for computer pits
        }

        humanMancala.setText(String.valueOf(position.board[MancalaPosition.HUMAN_MANCALA]));
        programMancala.setText(String.valueOf(position.board[MancalaPosition.PROGRAM_MANCALA]));

        if (game.drawnPosition(position)) {
            int humanScore = position.board[MancalaPosition.HUMAN_MANCALA];
            int programScore = position.board[MancalaPosition.PROGRAM_MANCALA];
            String winner = humanScore > programScore ? "Human Wins!" :
                    programScore > humanScore ? "Computer Wins!" : "It's a Draw!";
            JOptionPane.showMessageDialog(this, "Game Over! " + winner);
            System.exit(0);
        }
    }

    private void saveGame() {
        String filename = JOptionPane.showInputDialog(this, "Enter filename to save:");
        if (filename != null && !filename.trim().isEmpty()) {
            game.saveGame(position, filename);
        }
    }

    private void loadGame() {
        String filename = JOptionPane.showInputDialog(this, "Enter filename to load:");
        if (filename != null && !filename.trim().isEmpty()) {
            MancalaPosition loadedPos = game.loadGame(filename);
            if (loadedPos != null) {
                position = loadedPos;
                updateBoard();
                humanTurn = true;
                statusLabel.setText("Human's Turn");
            }
        }
    }

    private void showHelp(JButton helpButton) {
        if (helpCounter < 3) {
            if (humanTurn) {
                try {
                    List<Object> result = game.alphaBeta(0, position, true);
                    MancalaPosition bestMove = (MancalaPosition) result.get(1);

                    if (bestMove != null) {
                        int recommendedPit = getBestMovePitIndex(position, bestMove);
                        if (recommendedPit != -1) {
                            JOptionPane.showMessageDialog(this, "AI recommends choosing pit: " + recommendedPit);
                        } else {
                            JOptionPane.showMessageDialog(this, "No valid moves available.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "No valid moves available.");
                    }
                    helpCounter++;
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this, "AI encountered an error: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "It's not your turn!");
            }
        } else {
            helpButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "You have used the Help feature 3 times.");
        }
    }

    private int getBestMovePitIndex(MancalaPosition currentPosition, MancalaPosition bestMove) {
        for (int i = 0; i < 6; i++) {
            if (currentPosition.board[i] != bestMove.board[i]) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MancalaGameUI::new);
    }
}