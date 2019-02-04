package CellSociety;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CellWATOR extends Cell {

    private final String FISH = "Fish";
    private final String SHARK = "Shark";
    private final String EMPTY = "Empty";

    private double myFishReproTime;
    private double mySharkEnergy;
    private double myNewSharkEnergy;
    private double mySharkEatingEnergy;
    private double mySharkReproTime;

    private double myTurnsSurvived;
    private Random myRand;
    private List<CellWATOR> myEmptyNeighbors;
    private List<CellWATOR> myFishNeighbors;
    private CellWATOR myNextLocCell;

    CellWATOR(int row, int col, String initialState, ArrayList<Double> parameters){
        super(row, col, initialState, parameters);

        myRand = new Random();
        myEmptyNeighbors = new ArrayList<>();
        myFishNeighbors = new ArrayList<>();

        myFishReproTime = parameters.get(0);
        mySharkEnergy = parameters.get(1);
        myNewSharkEnergy = parameters.get(1);
        mySharkEatingEnergy = parameters.get(2);
        mySharkReproTime = parameters.get(3);

        myTurnsSurvived = 0;
    }

    @Override
    protected void initializeStatesList() {
        myStates.add(FISH);
        myStates.add(EMPTY);
        myStates.add(SHARK);
    }

    /**
     * Finds and sets the next state of the cell by checking neighbors
     * and following the rules set.
     */
    @Override
    public void findNextState() {
        myFishNeighbors.clear();
        myEmptyNeighbors.clear();

        myNextLocCell = null;

        switch(myCurrentState){
            case FISH:
                //making sure fish wasn't eaten
                System.out.println(myCurrentState + "at Row: " + myRow + " Col: " + myCol);
                if(!myNextState.equals(EMPTY)){
                    findEmptyNeighbors();
                    System.out.println("Above has " + myEmptyNeighbors.size() + " Empty Neighbors");

                    //can't move
                    if(myEmptyNeighbors.size() == 0){
                        fishStays();
                        System.out.println("Above can't move");
                        return;
                    }
                    else{
                        moveFish();
                        checkForBaby(FISH);
                        return;
                    }
                }
            case SHARK:
                System.out.println(myCurrentState + "at Row: " + myRow + " Col: " + myCol);
                findFishNeighbors();
                findEmptyNeighbors();
                System.out.println("Above has " + myEmptyNeighbors.size() + " Empty Neighbors");
                System.out.println("Above has " + myFishNeighbors.size() + " Fish Neighbors");

                //shark died
                if(mySharkEnergy <= 0){
                    myNextState = EMPTY;
                    return;
                }

                //eating fish
                if(myFishNeighbors.size() != 0){
                    eatFish();
                    checkForBaby(SHARK);
                    return;
                }
                //no fish to eat
                else{
                    //moves to empty space
                    if(myEmptyNeighbors.size() != 0){
                        moveSharkToEmptyNeighbor();
                        checkForBaby(SHARK);
                        return;
                    }
                    //can't move
                    else{
                        sharkStays();
                        return;
                    }
                }

            case EMPTY:
                if(myNextState.equals("")){
                    myNextState = EMPTY;
                }
        }
    }

    private void sharkStays() {
        myNextState = SHARK;
        myTurnsSurvived++;
        mySharkEnergy--;
    }

    private void moveSharkToEmptyNeighbor() {
        int nextLocationIndex;
        nextLocationIndex = myRand.nextInt(myEmptyNeighbors.size());
        CellWATOR nextCell = myEmptyNeighbors.get(nextLocationIndex);
        System.out.println("Above wants to move to empty row: " + nextCell.myRow + " Col: " + nextCell.myCol);
        nextCell.setNextState(SHARK);
        nextCell.setSharkEnergy(mySharkEnergy - 1);
        nextCell.setTurnsSurvived(myTurnsSurvived + 1);
    }

    private void eatFish() {
        int nextLocationIndex;
        nextLocationIndex = myRand.nextInt(myFishNeighbors.size());
        CellWATOR nextCell = myFishNeighbors.get(nextLocationIndex);
        System.out.println("Above going to eat: " + nextCell.myRow + " Col: " + nextCell.myCol);
        System.out.println("Fish Health: " + mySharkEnergy);

        //make sure if fish had already planned on moving, that cell will be empty instead
        if(nextCell.getNextLocCell() != null){
            nextCell.getNextLocCell().setNextState(EMPTY);
        }
        nextCell.setNextState(SHARK);
        nextCell.setSharkEnergy(mySharkEnergy - 1 + mySharkEatingEnergy);
        nextCell.setTurnsSurvived(myTurnsSurvived + 1);
        myNextState = EMPTY;
        System.out.println("Set above to EMPTY cuz eating fish");
    }

    private void fishStays() {
        myNextState = FISH;
        System.out.println("Fish Stayed row: " + myRow + "");
        myTurnsSurvived++;
        return;
    }

    public CellWATOR getNextLocCell(){
        return myNextLocCell;
    }

    //moves fish to empty spot
    private void moveFish() {
        int numEmptyNeighbors = myEmptyNeighbors.size();
        int nextLocationIndex = myRand.nextInt(numEmptyNeighbors);
        CellWATOR nextCell = myEmptyNeighbors.get(nextLocationIndex);
        System.out.println("Above wants to move to row: " + nextCell.myRow + " Col: " + nextCell.myCol);
        myNextLocCell = nextCell;
        nextCell.setNextState(FISH);
        nextCell.setTurnsSurvived(myTurnsSurvived++);
    }

    private void checkForBaby(String s){
        myTurnsSurvived = 0;
        switch(s){
            case SHARK:
                if(myTurnsSurvived > mySharkReproTime){
                    myNextState = SHARK;
                    mySharkEnergy = myNewSharkEnergy;
                    return;
            }
            case FISH:
                if(myTurnsSurvived > myFishReproTime){
                    myNextState = FISH;
                    return;
                }
        }
        myNextState = EMPTY;
    }

    private void findFishNeighbors(){
        for(Cell c: myNeighbors){
            if(c.getState().equals(FISH)){
                //make sure not claimed by other shark
                if(!c.getNextState().equals(SHARK)){
                    myFishNeighbors.add((CellWATOR)c);
                }
                else{
                    System.out.println("Fish neighbor claimed by other shark");
                }
            }
        }
    }

    /**
     * Sets the number of turns survived.
     * This value is used in determining the reproduction of the animal.
     * @param turnsSurvived double of the number of turns survived.
     */
    public void setTurnsSurvived(double turnsSurvived){
        myTurnsSurvived = turnsSurvived;
    }

    /**
     * Sets the shark's energy.
     * Used in determining if shark dies.
     * @param energy double of the shark's current energy.
     */
    public void setSharkEnergy(double energy){
        mySharkEnergy = energy;
    }

    //locates and stores empty neighbors
    private void findEmptyNeighbors(){
        for(Cell c: myNeighbors){
            if(c.getState().equals(EMPTY)){
                if(c.getNextState().equals("") || c.getNextState().equals(EMPTY)){
                    myEmptyNeighbors.add((CellWATOR)c);
                }
                else{
                    System.out.println("Above has a claimed neighbor: " + c.getNextState());
                }

            }
        }
    }


    /**
     * Finds and sets neighbors.
     * Assumes the cell can have up to four neighbors.
     * @param cell 2D int array of Cell objects
     */
    @Override
    public void findNeighbors(Cell[][] cell) {
        myGrid = cell;
        generateFourNeighbors();
    }
}
