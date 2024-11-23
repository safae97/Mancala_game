package GameSearch.mancala;

import javax.swing.*;
import java.awt.*;

public class MancalaMenuUI extends JFrame {
    public MancalaMenuUI() {
        setTitle("Mancala - Choose Game Mode");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false); // Prevent resizing for a cleaner look

        // Set a background color
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBackground(new Color(255, 228, 196)); // Light peach background
        backgroundPanel.setLayout(new BorderLayout());

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to Mancala!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Verdana", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(70, 130, 180)); // Steel blue color
        backgroundPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBackground(new Color(255, 228, 196)); // Match background color

        // Human vs Human Button
        JButton humanVsHumanButton = new JButton("Human vs Human");
        styleButton(humanVsHumanButton);
        humanVsHumanButton.addActionListener(e -> startHumanVsHuman());

        // Human vs Computer Button
        JButton humanVsComputerButton = new JButton("Human vs Computer");
        styleButton(humanVsComputerButton);
        humanVsComputerButton.addActionListener(e -> startHumanVsComputer());

        buttonPanel.add(humanVsHumanButton);
        buttonPanel.add(humanVsComputerButton);

        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);
        add(backgroundPanel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setBackground(new Color(100, 149, 237)); // Cornflower blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(200, 50));
    }

    private void startHumanVsHuman() {
        // Close the menu and open the Human vs Human interface
        dispose();
        Mancala game = new Mancala();
        MancalaPosition position = new MancalaPosition();
        new MancalaGUI(game, position);
    }

    private void startHumanVsComputer() {
        // Close the menu and open the Human vs Computer interface
        dispose();
        new MancalaGameUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MancalaMenuUI::new);
    }
}