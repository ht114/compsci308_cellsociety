package CellSociety;
//look into enum

import CellSociety.Neighbors.Neighbors;
import CellSociety.Neighbors.NeighborsSquare;
import CellSociety.Neighbors.NeighborsTriangle;

import java.util.ArrayList;
import java.util.List;


public abstract class Cell {
    protected String myCurrentState;
    protected String myNextState;
    protected int myCol;
    protected int myRow;
    protected List<Double> myParams;
    protected List<Cell> myNeighbors;
    protected Cell[][] myGrid;
    protected List<String> myStates;

    private final String SQUARE = "Square";
    private final String TRIANGLE = "Triangle";

    private int myNumUserCalls;

    /**
     * @author Carrie Hunner (clh87)
     *
     * @param row int index of the row of the cell in a grid of cells that will be passed through
     *            when setting neighbors
     * @param col int index of the column of the cell in a grid of cells that will be passed through when]
     *            setting the neighbors
     * @param initialState String indicating the initial state of the cell
     * @param parameters ArrayList of doubles containing any extra parameters needed e.g. probability
     *                   of catching fire or the health of a shark
     */
    Cell(int row, int col, String initialState, ArrayList<Double> parameters){
        myRow = row;
        myCol = col;
        myCurrentState = initialState;
        myNeighbors = new ArrayList<>();
        myParams = parameters;
        myNextState = "";
        myNumUserCalls = 0;

        myStates = new ArrayList<>();
        initializeStatesList();
    }

    //each implementation has to create a List of possible states
    abstract protected void initializeStatesList();

    /**
     * Calls the cell to use its neighbors and its rules to determine what its next state should be
     */
    abstract public void findNextState();


    /**
     * @return String corresponding to the cell's current state
     */
    public String getState(){
     return myCurrentState;
    }

    /**
     * Updates the cell's current state to its next state
     */
    public void updateState(){
        if(!myNextState.equals("")){
            myCurrentState = myNextState;
            myNextState = "";       //can be used in concrete classes to check if a nextState has been set
        }
        else {
            //likely occurs when user switches state
            throw new IllegalStateException("Next State never initialized");
        }

    }


    /**
     * Used to set the neighbors of the Cell.
     * @param cell ArrayList of ArrayLists of Cells, makes up the grid of cells
     * @param neighborIndexes ArrayList of integers corresponding with neighbor indexes
     */
    //Note: it will be set in each implementation so the concrete classes can choose if they want to call
    //a method for 4 or 8 neighbors, or they can call another method entirely.
    public void findNeighbors(Cell[][] cell, String shapeType, String edgeType, List<Integer> neighborIndexes){
        myGrid = cell;
        switch(shapeType){
            case SQUARE:
                NeighborsSquare squareNeighbors = new NeighborsSquare(myRow, myCol, myGrid);
                squareNeighbors.initializeEdgeAndIndexes(edgeType, neighborIndexes);
                myNeighbors =  squareNeighbors.getNeighborsList();
                return;
            case TRIANGLE:
                NeighborsTriangle triangleNeighbors = new NeighborsTriangle(myRow, myCol, myGrid);
                triangleNeighbors.initializeEdgeAndIndexes(edgeType, neighborIndexes);
                myNeighbors = triangleNeighbors.getNeighborsList();
                return;
        }
        throw new IllegalArgumentException("Unknown Shape Type");
    }


    private boolean isSelf(int tempRow, int tempCol) {
        return tempRow == myRow && tempCol == myCol;
    }

    /**
     * Used in WATOR and Segregation to determine if a cell has been claimed for the
     * following step.
     * @return String of the cell's next state.
     */
    public String getNextState(){
        return myNextState;
    }

    /**
     * Used in WATOR and Segregation to handle the movement of people/animals.
     * @param state String of the desired next state;
     */
    public void setNextState(String state){
        myNextState = state;
    }

    public void userSwitchState(){
        int rem = myNumUserCalls % (myStates.size() - 1);
        myCurrentState = myStates.get(rem);
    }

    /**
     * Allows for the UI to adjust the parameters of the simulation
     *  eg. probability of burning or shark health etc.
     *  It defaults to not changing anything, as several of the simulations
     *  do no have parameters that could be modified. It will be overriden when
     *  necessary in concrete cell implementations.
     * @param params ArrayList of doubles corresponding with the necessary
     *               parameters for each simulation
     */
    public void setParams(ArrayList<Double> params){
        return;
    }
}
