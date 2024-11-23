package GameSearch.mancala;

import java.io.Serializable;
import java.util.Arrays;

// Classe représentant la position actuelle du jeu de Mancala, incluant l'état du plateau et la logique associée.
public class MancalaPosition extends Position implements Serializable {
    private static final long serialVersionUID = 1L; // Assure la compatibilité de version pour la sérialisation

    public boolean extraTurn = false; // Indique si le joueur obtient un tour supplémentaire
    public static final int HUMAN_MANCALA = 6; // Index du Mancala (récipient) du joueur humain
    public static final int PROGRAM_MANCALA = 13; // Index du Mancala du programme (ordinateur)
    public int scorePenalty; // Pénalité de score (potentielle, selon les règles du jeu)

    public int[] board = new int[14]; // Tableau représentant l'état actuel du plateau de jeu de Mancala

    // Méthode pour cloner la position actuelle, créant une copie indépendante de l'objet
    @Override
    protected MancalaPosition clone() {
        MancalaPosition copy = new MancalaPosition();
        copy.board = this.board.clone();  // Copie profonde du tableau de jeu
        copy.extraTurn = this.extraTurn;  // Copie de l'indicateur de tour supplémentaire
        return copy;
    }

    // Constructeur pour initialiser le plateau avec l'état de départ standard du jeu Mancala
    public MancalaPosition() {
        // Configuration typique de Mancala : 4 graines par case, les Mancalas commencent à 0
        Arrays.fill(board, 4);
        board[HUMAN_MANCALA] = 0;  // Le Mancala humain commence avec 0 graines
        board[PROGRAM_MANCALA] = 0;  // Le Mancala du programme commence avec 0 graines
    }

    // Méthode pour afficher l'état actuel du plateau
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PROGRAM: ");  // Affiche les cases du programme (ordinateur)
        for (int i = 12; i >= 7; i--) sb.append(board[i]).append(" ");  // Parcourt les cases de l'ordinateur
        sb.append("\nMancalas: ").append(board[PROGRAM_MANCALA]).append(" | ").append(board[HUMAN_MANCALA]);  // Affiche les Mancalas
        sb.append("\nHUMAN:    ");  // Affiche les cases du joueur humain
        for (int i = 0; i <= 5; i++) sb.append(board[i]).append(" ");  // Parcourt les cases de l'humain
        return sb.toString();  // Retourne la représentation textuelle du plateau
    }
}
