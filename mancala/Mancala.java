package GameSearch.mancala;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Mancala extends GameSearch {


    public void saveGame(MancalaPosition position, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(position);
            System.out.println("Game saved to " + filename);
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    // Load the game state from a file
    public MancalaPosition loadGame(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            MancalaPosition position = (MancalaPosition) in.readObject();
            System.out.println("Game loaded from " + filename);
            return position;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return null;
        }
    }




    @Override
    public boolean drawnPosition(Position p) {
        MancalaPosition pos = (MancalaPosition) p;

        // Vérifie si les cases du côté HUMAN sont toutes vides
        boolean humanEmpty = true;
        for (int i = 0; i < 6; i++) {
            if (pos.board[i] > 0) {
                humanEmpty = false;
                break;
            }
        }

        // Vérifie si les cases du côté PROGRAM sont toutes vides
        boolean programEmpty = true;
        for (int i = 7; i < 13; i++) {
            if (pos.board[i] > 0) {
                programEmpty = false;
                break;
            }
        }

        // Si l'un des côtés est vide, transférer les graines restantes dans le Mancala correspondant
        if (humanEmpty || programEmpty) {
            if (!humanEmpty) { // PROGRAM est vide, transfert les graines restantes de HUMAN
                for (int i = 0; i < 6; i++) {
                    pos.board[MancalaPosition.HUMAN_MANCALA] += pos.board[i];
                    pos.board[i] = 0;
                }
            } else { // HUMAN est vide, transfert les graines restantes de PROGRAM
                for (int i = 7; i < 13; i++) {
                    pos.board[MancalaPosition.PROGRAM_MANCALA] += pos.board[i];
                    pos.board[i] = 0;
                }
            }
            return true; // Le jeu est terminé
        }

        return false; // Le jeu continue
    }
    @Override
    public boolean wonPosition(Position p, boolean player) {
        MancalaPosition pos = (MancalaPosition) p;

        // Vérifie si le jeu est terminé en utilisant drawnPosition
        if (!drawnPosition(p)) {
            return false; // Pas de gagnant tant que le jeu n'est pas terminé
        }

        // Si le jeu est terminé, compare les scores des Mancalas
        int humanScore = pos.board[MancalaPosition.HUMAN_MANCALA];
        int programScore = pos.board[MancalaPosition.PROGRAM_MANCALA];
        if (player) {
            return humanScore > programScore; // HUMAN gagne si son score est plus élevé
        } else {
            return programScore > humanScore; // PROGRAM gagne si son score est plus élevé
        }
    }
    @Override
    public float positionEvaluation(Position p, boolean player) {
        MancalaPosition pos = (MancalaPosition) p;

        // Différence des Mancalas
        int mancalaDifference = pos.board[MancalaPosition.PROGRAM_MANCALA] - pos.board[MancalaPosition.HUMAN_MANCALA];

        // Opportunités de capture
        int capturePotential = 0;
        for (int i = 0; i < 6; i++) {
            if (pos.board[i] == 0 && pos.board[12 - i] > 0) {
                capturePotential += pos.board[12 - i];
            }
        }

        // Réduire les graines adverses proches de leur Mancala
        int minimizeOpponentStones = 0;
        for (int i = 7; i < 12; i++) {
            minimizeOpponentStones += pos.board[i];
        }

        // Opportunités de tours supplémentaires
        int extraTurnPotential = 0;
        for (int i = 7; i < 13; i++) {
            if (pos.board[i] > 0 && (i + pos.board[i]) % 14 == MancalaPosition.PROGRAM_MANCALA) {
                extraTurnPotential++;
            }
        }

        int preventOpponentCapture = 0;
        for (int i = 7; i < 13; i++) {
            int nextIndex = (i + pos.board[i]) % 14;
            if (pos.board[i] > 0 && nextIndex >= 0 && nextIndex < 6 && pos.board[nextIndex] == 0) {
                // Vérifie si un coup rend une case vulnérable pour une capture
                int oppositeIndex = 12 - nextIndex;
                preventOpponentCapture -= pos.board[oppositeIndex]; // Réduction proportionnelle aux graines à capturer
            }
        }

        // Phases du jeu
        int totalSeeds = Arrays.stream(pos.board).sum();
        float phaseFactor = totalSeeds > 30 ? 1.0f : totalSeeds > 15 ? 1.5f : 2.0f;

        // Pondération dynamique
        return phaseFactor * (2.0f * mancalaDifference
                + 1.5f * capturePotential
                + 1.0f * extraTurnPotential
                - 1.0f * minimizeOpponentStones
                - 1.5f * preventOpponentCapture);
    }
    @Override
    public void printPosition(Position p) {
        System.out.println(p.toString());
    }
    @Override
    public Position[] possibleMoves(Position p, boolean player) {
        MancalaPosition pos = (MancalaPosition) p;
        List<ScoredPosition> scoredMoves = new ArrayList<>();
        int start = player ? 0 : 7;
        int end = player ? 5 : 12;

        for (int i = start; i <= end; i++) {
            if (pos.board[i] > 0) {
                // Clone the board to simulate the move
                MancalaPosition newPos = new MancalaPosition();
                System.arraycopy(pos.board, 0, newPos.board, 0, 14);

                // Simulate the move
                makeMove(newPos, player, new MancalaMove(i));

                // Evaluate the move
                float eval = positionEvaluation(newPos, player);

                // Bonus pour les coups critiques
                if (newPos.extraTurn) eval += 10.0f; // Bonus pour tour supplémentaire
                if (newPos.board[i] == 0 && newPos.board[12 - i] > 0) eval += 5.0f; // Bonus pour capture

                // Penalty for leaving opportunities for the opponent
                if (!player && newPos.board[12 - i] > 0) {
                    eval -= 5.0f; // Pénalisation de base pour opportunités adverses
                    eval -= newPos.board[12 - i]; // Réduction proportionnelle aux graines capturables
                }

                // Add the scored move to the list
                scoredMoves.add(new ScoredPosition(newPos, eval));
            }
        }

        // Trier les mouvements par score (du meilleur au pire)
        scoredMoves.sort((a, b) -> Float.compare(b.score, a.score));
        return scoredMoves.stream()
                .map(m -> m.position)
                .toArray(size -> new Position[size]);}

    // Classe interne pour stocker les scores des positions
    private static class ScoredPosition {
        Position position; // Position résultante après un mouvement
        float score;       // Score associé à cette position

        ScoredPosition(Position position, float score) {
            this.position = position;
            this.score = score;
        }
    }

    @Override
    public Position makeMove(Position p, boolean player, Move move) {
        MancalaPosition pos = (MancalaPosition) p;
        MancalaMove m = (MancalaMove) move;
        int[] board = pos.board;
        int index = m.pitIndex;
        int seeds = board[index];

        // Vérification si le mouvement est valide
        if (seeds <= 0) {
            throw new IllegalArgumentException("Invalid move: The selected pit is empty.");
        }

        board[index] = 0; // Vide le pit sélectionné
        int currentIndex = index;

        // Sème les graines une par une dans les cases suivantes
        while (seeds > 0) {
            currentIndex = (currentIndex + 1) % 14;

            // Saute la grande case (Mancala) de l'adversaire
            if ((player && currentIndex == MancalaPosition.PROGRAM_MANCALA) ||
                    (!player && currentIndex == MancalaPosition.HUMAN_MANCALA)) {
                continue;
            }

            board[currentIndex]++;
            seeds--;
        }

        // Capture : Si la dernière graine tombe dans un pit vide du côté du joueur
        if (board[currentIndex] == 1 &&
                ((player && currentIndex >= 0 && currentIndex <= 5) ||
                        (!player && currentIndex >= 7 && currentIndex <= 12))) {
            int oppositeIndex = 12 - currentIndex;
            int mancala = player ? MancalaPosition.HUMAN_MANCALA : MancalaPosition.PROGRAM_MANCALA;

            // Capture les graines opposées et la graine finale
            board[mancala] += board[oppositeIndex] + board[currentIndex];
            board[oppositeIndex] = 0;
            board[currentIndex] = 0;
        }

        // Vérifie si le joueur gagne un tour supplémentaire
        int ownMancala = player ? MancalaPosition.HUMAN_MANCALA : MancalaPosition.PROGRAM_MANCALA;
        pos.extraTurn = (currentIndex == ownMancala);

        // Évaluation des captures potentielles pour l'adversaire après le mouvement
        if (!player) { // Si c'est le tour de l'IA
            int potentialCapture = 0;
            for (int i = 0; i < 6; i++) {
                if (pos.board[i] == 0 && pos.board[12 - i] > 0) {
                    potentialCapture += pos.board[12 - i];
                }
            }
            pos.scorePenalty = potentialCapture * 2; // Ajout d'une pénalité si l'adversaire peut capturer
        }

        return pos;
    }
    @Override
    public boolean reachedMaxDepth(Position p, int depth) {
        MancalaPosition pos = (MancalaPosition) p;
        int totalSeeds = Arrays.stream(pos.board).sum();

        // Définir la profondeur maximale en fonction de la difficulté
        int maxDepth;
        switch (difficulty) {
            case SIMPLE:
                maxDepth = 2; // Profondeur limitée pour des décisions rapides
                break;
            case MEDIUM:
                maxDepth = totalSeeds > 20 ? 6 : 8; // Exploration modérée
                break;
            case HARD:
                maxDepth = totalSeeds > 20 ? 8 : 12; // Exploration approfondie
                break;
            default:
                maxDepth = 6; // Défaut au cas où
        }

        // Vérifier si la profondeur maximale ou la fin de jeu est atteinte
        return depth >= maxDepth || drawnPosition(p);
    }
    @Override
    public Move createMove() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("Enter pit index (0-5 for HUMAN): ");
            int pit = sc.nextInt();

            // Ensure the pit index is within the valid range
            if (pit < 0 || pit > 5) {
                System.out.println("Invalid input. Please enter a number between 0 and 5.");
                continue;
            }

            // Return the move without validating the pit's content
            return new MancalaMove(pit);
        }
    }
    private Difficulty difficulty; // Niveau de difficulté choisi par le joueur

    // Enumération des niveaux de difficulté
    public enum Difficulty {
        SIMPLE, MEDIUM, HARD
    }

    // Définir le niveau de difficulté
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MancalaPosition initialPosition = new MancalaPosition();
        Mancala game = new Mancala();

        System.out.println("Welcome to Mancala!");
        System.out.println("Choose game mode: ");
        System.out.println("1. Play against the Computer");
        System.out.println("2. Play against another Player");

        int modeChoice = sc.nextInt();
        boolean playAgainstComputer = modeChoice == 1;

        if (playAgainstComputer) {
            System.out.println("Choose difficulty: ");
            System.out.println("1. Simple");
            System.out.println("2. Medium");
            System.out.println("3. Hard");
            int difficultyChoice = sc.nextInt();
            switch (difficultyChoice) {
                case 1:
                    game.setDifficulty(Difficulty.SIMPLE);
                    break;
                case 2:
                    game.setDifficulty(Difficulty.MEDIUM);
                    break;
                case 3:
                    game.setDifficulty(Difficulty.HARD);
                    break;
                default:
                    System.out.println("Invalid choice. Defaulting to Medium.");
                    game.setDifficulty(Difficulty.MEDIUM);
            }
            System.out.println("Starting game against the computer at " + game.difficulty + " difficulty.");
        } else {
            System.out.println("Starting a two-player game.");
        }

        game.playGame(initialPosition, true, playAgainstComputer);
}
}
