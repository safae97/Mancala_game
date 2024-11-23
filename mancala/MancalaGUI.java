package GameSearch.mancala;

import javax.swing.*;
import java.awt.*;

public class MancalaGUI extends JFrame {
    private final JButton[] humanPits = new JButton[6];
    private final JButton[] programPits = new JButton[6];
    private final JLabel humanMancala = new JLabel("0", SwingConstants.CENTER);
    private final JLabel programMancala = new JLabel("0", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Human's turn!", SwingConstants.CENTER);

    private Mancala game;
    private MancalaPosition position;
    private boolean isHumanTurn = true;

    public MancalaGUI(Mancala game, MancalaPosition position) {
        this.game = game;
        this.position = position;

        setTitle("Mancala Game: Human vs Human");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout());
        setResizable(false); // Prevent resizing for a cleaner look

        // Board Panel setup
        JPanel boardPanel = new JPanel(new GridLayout(2, 8, 10, 10));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boardPanel.setBackground(new Color(255, 228, 196)); // Light peach background

        // Style Mancalas
        styleMancala(programMancala);
        boardPanel.add(programMancala);

        // Program pits (top row, reverse order)
        for (int i = 5; i >= 0; i--) {
            programPits[i] = createPitButton(i + 7);
            boardPanel.add(programPits[i]);
        }

        // Empty spaces for symmetry
        boardPanel.add(new JLabel(""));
        boardPanel.add(new JLabel(""));

        // Human pits (bottom row)
        for (int i = 0; i < 6; i++) {
            humanPits[i] = createPitButton(i);
            boardPanel.add(humanPits[i]);
        }

        // Human Mancala
        styleMancala(humanMancala);
        boardPanel.add(humanMancala);
        add(boardPanel, BorderLayout.CENTER);

        // Control Panel at the bottom
        JPanel mainControlPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10)); // 3 buttons in a row
        buttonPanel.setBackground(new Color(240, 240, 240)); // Match the board panel background

        // Create buttons
        JButton saveButton = createControlButton("Save Game");
        saveButton.addActionListener(e -> saveGame());

        JButton loadButton = createControlButton("Load Game");
        loadButton.addActionListener(e -> loadGame());

        JButton quitButton = createControlButton("Quit");
        quitButton.addActionListener(e -> System.exit(0));

        // Add buttons to the button panel
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(quitButton);

        // Add the status label above the button panel
        mainControlPanel.add(statusLabel, BorderLayout.NORTH);
        mainControlPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add the combined panel to the SOUTH
        add(mainControlPanel, BorderLayout.SOUTH);

        updateBoard();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void styleMancala(JLabel mancala) {
        mancala.setOpaque(true);
        mancala.setBackground(new Color(70, 130, 180)); // Steel blue background
        mancala.setForeground(Color.WHITE);
        mancala.setFont(new Font("Arial", Font.BOLD, 24));
        mancala.setPreferredSize(new Dimension(100, 200));  // Large rectangle
    }

    private JButton createPitButton(int pitIndex) {
        JButton button = new JButton("4");
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(100, 149, 237)); // Cornflower blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> handleMove(pitIndex));
        return button;
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton (text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(100, 149, 237)); // Cornflower blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void handleMove(int pitIndex) {
        // Check if the player can play in this pit
        if ((isHumanTurn && pitIndex >= 0 && pitIndex < 6) || (!isHumanTurn && pitIndex >= 7 && pitIndex < 13)) {
            if (position.board[pitIndex] == 0) {
                JOptionPane.showMessageDialog(this, "Invalid move: This pit is empty.");
                return;
            }

            // Perform the move
            position = (MancalaPosition) game.makeMove(position, isHumanTurn, new MancalaMove(pitIndex));

            // Check if the player has an extra turn
            if (!position.extraTurn) {
                isHumanTurn = !isHumanTurn;
                statusLabel.setText(isHumanTurn ? "Player1's turn!" : "Player2's turn!");
            } else {
                statusLabel.setText(isHumanTurn ? "Player1 gets another turn!" : "Player2 gets another turn!");
            }

            // Update the board
            updateBoard();
            // Check if the game is over
            if (game.drawnPosition(position)) {
                String winnerMessage;
                int humanScore = position.board[MancalaPosition.HUMAN_MANCALA];
                int programScore = position.board[MancalaPosition.PROGRAM_MANCALA];

                if (humanScore > programScore) {
                    winnerMessage = "Human wins!";
                } else if (programScore > humanScore) {
                    winnerMessage = "Program wins!";
                } else {
                    winnerMessage = "It's a draw!";
                }

                JOptionPane.showMessageDialog(this, "Game Over!\n" + winnerMessage);
                System.exit(0);
            }
        } else {
            JOptionPane.showMessageDialog(this, "It's not your turn or invalid pit selection!");
        }
    }

    private void saveGame() {
        // Prompt for a file to save the game
        String filename = JOptionPane.showInputDialog(this, "Enter filename to save the game:");
        if (filename != null && !filename.trim().isEmpty()) {
            game.saveGame(position, filename);
            JOptionPane.showMessageDialog(this, "Game saved successfully!");
        }
    }

    private void loadGame() {
        // Prompt for a file to load the game
        String filename = JOptionPane.showInputDialog(this, "Enter filename to load the game:");
        if (filename != null && !filename.trim().isEmpty()) {
            MancalaPosition loadedPosition = game.loadGame(filename);
            if (loadedPosition != null) {
                this.position = loadedPosition;
                updateBoard();
                JOptionPane.showMessageDialog(this, "Game loaded successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load game. Please check the file.");
            }
        }
    }

    private void updateBoard() {
        // Update pits for Human
        for (int i = 0; i < 6; i++) {
            humanPits[i].setText(String.valueOf(position.board[i]));
        }

        // Update pits for Program
        for (int i = 0; i < 6; i++) {
            programPits[i].setText(String.valueOf(position.board[i + 7]));
        }

        // Update Mancalas
        humanMancala.setText(String.valueOf(position.board[MancalaPosition.HUMAN_MANCALA]));
        programMancala.setText(String.valueOf(position.board[MancalaPosition.PROGRAM_MANCALA]));
    }

    public static void main(String[] args) {
        MancalaPosition initialPosition = new MancalaPosition();
        Mancala game = new Mancala();
        new MancalaGUI(game, initialPosition);
    }
}