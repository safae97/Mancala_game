package GameSearch.mancala;

import java.io.Serializable;
import java.util.Arrays;

public class MancalaPosition extends Position implements Serializable {
    private static final long serialVersionUID = 1L; // Ensure versioning for serialization compatibility

    public boolean extraTurn = false; // Indicates if the player gets another turn
    public static final int HUMAN_MANCALA = 6;
    public static final int PROGRAM_MANCALA = 13;
    public int scorePenalty;

    public int[] board = new int[14]; // Array representing the Mancala board

    @Override
    protected MancalaPosition clone() {
        MancalaPosition copy = new MancalaPosition();
        copy.board = this.board.clone();  // Deep copy the board array
        copy.extraTurn = this.extraTurn;  // Copy the extraTurn flag
        return copy;
    }
    // Constructor to initialize the board
    public MancalaPosition() {
        // Typical Mancala setup: 4 seeds per pit, Mancalas start at 0
        Arrays.fill(board, 4);
        board[HUMAN_MANCALA] = 0;
        board[PROGRAM_MANCALA] = 0;
    }

    @Override
    public String toString() {
        // Displays the board in a readable format
        StringBuilder sb = new StringBuilder();
        sb.append("PROGRAM: ");
        for (int i = 12; i >= 7; i--) sb.append(board[i]).append(" ");
        sb.append("\nMancalas: ").append(board[PROGRAM_MANCALA]).append(" | ").append(board[HUMAN_MANCALA]);
        sb.append("\nHUMAN:    ");
        for (int i = 0; i <= 5; i++) sb.append(board[i]).append(" ");
        return sb.toString();
    }
}
