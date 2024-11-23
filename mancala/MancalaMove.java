package GameSearch.mancala;

public class MancalaMove extends Move {
    public int pitIndex; // Index du pit choisi

    public MancalaMove(int pitIndex) {
        this.pitIndex = pitIndex;
    }


    @Override
    public String toString() {
        return "Move pit: " + pitIndex;
    }
}
