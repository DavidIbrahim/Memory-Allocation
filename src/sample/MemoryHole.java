package sample;

public class MemoryHole implements Comparable<MemoryHole>{


    private int holeNumber;
    private int startingIndex ;
    private int endingIndex ;
    private int size ;


    public MemoryHole(int holeNumber, int startingIndex, int size) {
        this.holeNumber = holeNumber;
        this.startingIndex = startingIndex;
        this.size = size;
        endingIndex=startingIndex+size;
    }

    public MemoryHole(int holeNumber) {
        this.holeNumber = holeNumber;
        size=0;
        startingIndex=0;
        endingIndex=0;

    }
    public MemoryHole(int startingIndex, int size) {
        this.startingIndex = startingIndex;
        this.size = size;

        endingIndex = startingIndex +size;
    }

    public void setEndingIndex(int endingIndex) {
        this.endingIndex = endingIndex;
        size = endingIndex-startingIndex;
    }

    public void setSize(int size) {
        this.size = size;
        endingIndex=startingIndex+size;

    }

    public int getHoleNumber() {
        return holeNumber;
    }

    public void setStartingIndex(int startingIndex) {

        this.startingIndex = startingIndex;
        size = endingIndex-startingIndex;
    }




    public int getStartingIndex() {
        return startingIndex;
    }

    public int getEndingIndex() {
        return endingIndex;
    }

    public int getSize() {
        return size;
    }


    @Override
    public int compareTo(MemoryHole o) {
        return startingIndex-o.startingIndex;
    }
}
