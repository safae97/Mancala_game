package GameSearch.mancala;

import java.util.*;

public abstract class GameSearch {
    public static final boolean DEBUG = false;

    public static boolean PROGRAM = false;
    public static boolean HUMAN = true;

    // Vérifie si la position est une position nulle (égalité).
    public abstract boolean drawnPosition(Position p);

    // Vérifie si un joueur a gagné dans la position donnée.
    public abstract boolean wonPosition(Position p, boolean player);

    // Évalue une position et retourne un score pour un joueur donné.
    public abstract float positionEvaluation(Position p, boolean player);

    // Affiche l'état actuel de la position.
    public abstract void printPosition(Position p);

    // Retourne les mouvements possibles pour un joueur à partir d'une position donnée.
    public abstract Position [] possibleMoves(Position p, boolean player);

    // Effectue un mouvement et retourne la nouvelle position.
    public abstract Position makeMove(Position p, boolean player, Move move);

    // Vérifie si la profondeur maximale a été atteinte pour l'alpha-bêta.
    public abstract boolean reachedMaxDepth(Position p, int depth);

    // Exécute l'algorithme alpha-bêta pour évaluer la meilleure position.
    protected List<Object> alphaBeta(int depth, Position p, boolean player) {
        return alphaBetaHelper(depth, p, player, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    // Fonction récursive qui applique l'algorithme alpha-bêta pour trouver le meilleur mouvement.
    protected List<Object> alphaBetaHelper(int depth, Position p, boolean player, float alpha, float beta) {
        // Si la profondeur maximale est atteinte ou que la position est nulle, on retourne l'évaluation.
        if (reachedMaxDepth(p, depth) || drawnPosition(p)) {
            float eval = positionEvaluation(p, player);
            return Arrays.asList(eval, null); // Score et pas de mouvement
        }
        List<Object> bestMove = new ArrayList<>();
        Position[] moves = possibleMoves(p, player);
        Arrays.sort(moves, Comparator.comparingDouble(m -> -positionEvaluation((Position) m, player)));

        float bestValue = Float.NEGATIVE_INFINITY;

        for (Position move : moves) {
            // Exploration récursive
            List<Object> evalResult = alphaBetaHelper(depth + 1, move, !player, -beta, -alpha);
            float eval = -((Float) evalResult.get(0)); // Inverser l'évaluation pour l'adversaire

            if (eval > bestValue) {
                bestValue = eval;
                bestMove.clear();
                bestMove.add(move); // Meilleur mouvement
                bestMove.addAll(evalResult.subList(1, evalResult.size())); // Autres détails
            }
            // Mettre à jour alpha et réaliser un cutoff si nécessaire
            alpha = Math.max(alpha, eval);
            if (alpha >= beta) {
                break;
            }
        }
        bestMove.add(0, bestValue); // Ajouter le score en premier
        return bestMove;
    }

    // Fonction utilitaire pour obtenir l'indice du puits dans le meilleur mouvement.
    private int getPitIndex(MancalaPosition current, MancalaPosition best) {
        for (int i = 0; i < 6; i++) {
            if (!Arrays.equals(current.board, best.board)) {
                return i; // Retourner l'indice du puits où le mouvement a été effectué
            }
        }

        return -1;
    }

    // Trouve l'indice du meilleur puits pour un mouvement donné.
    private int getBestMovePitIndex(MancalaPosition current, MancalaPosition best, boolean player) {
        int start = player ? 0 : 7;
        int end = player ? 5 : 12;

        for (int i = start; i <= end; i++) {
            if (current.board[i] > 0) { // Vérifie que le puits n'est pas vide
                MancalaPosition simulatedMove = new MancalaPosition();
                System.arraycopy(current.board, 0, simulatedMove.board, 0, 14);
                makeMove(simulatedMove, player, new MancalaMove(i)); // Simule le mouvement
                if (Arrays.equals(simulatedMove.board, best.board)) {
                    return i; // Retourne l'indice du puits correspondant
                }
            }
        }
        return -1; // Aucun mouvement valide trouvé
    }

    // Fonction principale pour jouer une partie de Mancala.
    public void playGame(Position startingPosition, boolean humanPlayFirst, boolean playAgainstComputer) {
        Scanner scanner = new Scanner(System.in);
        MancalaPosition pos = (MancalaPosition) startingPosition;
        boolean currentPlayer = humanPlayFirst;
        int remainingHelps = 3;

        while (true) {
            printPosition(pos);

            // Vérifiez si la partie est terminée
            if (drawnPosition(pos)) {
                System.out.println("Game Over!");
                System.out.println("Final Scores:");
                System.out.println("Human: " + pos.board[MancalaPosition.HUMAN_MANCALA]);
                System.out.println("Program: " + pos.board[MancalaPosition.PROGRAM_MANCALA]);

                if (pos.board[MancalaPosition.HUMAN_MANCALA] > pos.board[MancalaPosition.PROGRAM_MANCALA]) {
                    System.out.println("Human Wins!");
                } else if (pos.board[MancalaPosition.PROGRAM_MANCALA] > pos.board[MancalaPosition.HUMAN_MANCALA]) {
                    System.out.println("Program Wins!");
                } else {
                    System.out.println("It's a Draw!");
                }
                break;
            }

            if (currentPlayer == HUMAN) { // Tour du joueur humain (Joueur 1)
                System.out.println("Human's turn! (Type 'options' for save/load/quit, or enter help, or enter pit index 0-5)");
                String input = scanner.next();

                if ("help".equalsIgnoreCase(input) && playAgainstComputer) { // Option help pour Human vs Computer
                    if (remainingHelps > 0) {
                        remainingHelps--;
                        System.out.println("AI is calculating the best move for you...");
                        try {
                            List<Object> result = alphaBeta(0, pos, HUMAN);
                            MancalaPosition bestMove = (MancalaPosition) result.get(1);

                            if (bestMove != null) {
                                int recommendedPit = getBestMovePitIndex(pos, bestMove, HUMAN);
                                if (recommendedPit != -1) {
                                    System.out.println("AI recommends choosing pit: " + recommendedPit);
                                } else {
                                    System.out.println("No valid moves available.");
                                }
                            } else {
                                System.out.println("No valid moves available.");
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println("AI encountered an error: " + e.getMessage());
                        }
                        System.out.println("You have " + remainingHelps + " help(s) remaining.");
                    } else {
                        System.out.println("You have used all your helps.");
                    }
                    continue; // Retourner au joueur après l'aide
                }

                if ("options".equalsIgnoreCase(input)) { // Options pour Player 1
                    System.out.println("Options: (1) Save Game (2) Load Game (3) Quit");
                    int option = scanner.nextInt();
                    switch (option) {
                        case 1:
                            System.out.print("Enter filename to save the game: ");
                            String saveFile = scanner.next();
                            ((Mancala) this).saveGame(pos, saveFile);
                            System.out.println("Game saved.");
                            continue;
                        case 2:
                            System.out.print("Enter filename to load the game: ");
                            String loadFile = scanner.next();
                            MancalaPosition loadedPos = ((Mancala) this).loadGame(loadFile);
                            if (loadedPos != null) {
                                pos = loadedPos;
                                currentPlayer = humanPlayFirst; // Revenir à l'ordre initial
                            }
                            continue;
                        case 3:
                            System.out.println("Exiting the game. Goodbye!");
                            return;
                        default:
                            System.out.println("Invalid option. Continuing the game...");
                    }
                    continue;
                }

                try {
                    int pit = Integer.parseInt(input);
                    if (pit < 0 || pit > 5 || pos.board[pit] == 0) {
                        System.out.println("Invalid move: The selected pit is empty or out of range. Try again.");
                        continue;
                    }
                    Move move = new MancalaMove(pit);
                    pos = (MancalaPosition) makeMove(pos, HUMAN, move);

                    if (!pos.extraTurn) {
                        currentPlayer = playAgainstComputer ? PROGRAM : !currentPlayer; // Changer de joueur
                    } else {
                        System.out.println("Human gets another turn!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Enter a number between 0 and 5.");
                }
            } else {
                if (playAgainstComputer) { // Ordinateur (PROGRAM)
                    System.out.println("Computer's turn!");
                    List<Object> result = alphaBeta(0, pos, PROGRAM);
                    MancalaPosition bestMove = (MancalaPosition) result.get(1);
                    pos = bestMove;

                    if (!pos.extraTurn) {
                        currentPlayer = HUMAN; // Retourner à Human
                    } else {
                        System.out.println("Computer gets another turn!");
                    }
                } else { // Joueur 2
                    while (true) { // Boucle pour Player 2
                        System.out.println("Player 2's turn! (enter pit index 7-12)");
                        String input = scanner.next();

                        try {
                            int pit = Integer.parseInt(input);
                            if (pit < 7 || pit > 12 || pos.board[pit] == 0) {
                                System.out.println("Invalid move: The selected pit is empty or out of range. Try again.");
                                continue;
                            }
                            Move move = new MancalaMove(pit);
                            pos = (MancalaPosition) makeMove(pos, !HUMAN, move);
                            currentPlayer = HUMAN; // Retourner à Human
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Enter a number between 7 and 12.");
                        }
                    }
                }
            }
        }
    }
}
