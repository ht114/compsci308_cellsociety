package CellSociety;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static com.sun.javafx.application.PlatformImpl.exit;
import static java.lang.Math.ceil;


/**
 * @author Hsingchih Tang
 * Trunk of the cell society project
 * Control the simulation flow by invoking and connecting XMLParser, IntroScene, UI and Cell classes
 * Retrieve simulation configuration parameters from XMLParser and pass arguments into UI and Cells
 * Respond to user action of playing/resuming/stepping/switching simulation
 */
public class Simulation extends Application {

    private final String configFilePath = "resources/SimulationConfig.txt";
    private int myWidth;
    private int myHeight;
    private double delay;
    private double distributionAccuracy;
    private String myTitle;
    final String GOL_XML = "Game of Life";
    final String WATOR_XML = "WaTor";
    final String FIRE_XML = "RPS";
    final String SEG_XML = "Segregation";
    final String PERC_XML = "Percolation";
    final String RPS_XML = "RPS";
    private List<String> SIM_TYPE_LIST = new ArrayList<>();
    private Map<String,Integer> SIM_PARAM_NUM = new HashMap<>();
    private Map<String,Integer> SIM_STATE_NUM = new HashMap<>();

    private Timeline myTimeline;
    private Stage myStage;
    private Cell[][] myGrid;
    private Scene myIntroScene;
    private UI myUIScene;
    private String SIM_TYPE;
    private String cellShape;
    private String edgeType;
    private boolean specConfig = false;
    private List<String> stateList;
    private List<Double> parametersList;
    private List<Integer> neighborList;
    private Map<String, String> stateImageMap;
    private Map<String, Double> statePercentMap;
    private Map<List<Integer>,String> cellStateMap;
    private XMLParser myParser;


    /**
     * Constructor of a Simulation object
     * Call readConfig() to set up the Simulation class with specific parameters
     */
    public Simulation(){
        super();
        try{
            readConfig();
        }catch (FileNotFoundException e){
            System.out.println("Simulation configuration file not found.");
            exit();
        }
    }


    /**
     * Read the configuration text file for basic parameters (default size, simulation delay, etc.) in Simulation
     * @throws FileNotFoundException if the configuration file is not found
     */
    private void readConfig() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(configFilePath));
        myTitle = sc.nextLine();
        myWidth = Integer.valueOf(sc.nextLine());
        myHeight = Integer.valueOf(sc.nextLine());
        delay = Double.valueOf(sc.nextLine());
        distributionAccuracy = Double.valueOf(sc.nextLine());
        while(sc.hasNextLine()){
            String modelName = sc.nextLine();
            Integer paramNum = Integer.valueOf(sc.nextLine());
            Integer stateNum = Integer.valueOf(sc.nextLine());
            SIM_TYPE_LIST.add(modelName);
            SIM_PARAM_NUM.put(modelName,paramNum);
            SIM_STATE_NUM.put(modelName,stateNum);
        }
    }


    /**
     * Entry point of the program
     * @param stage where scene shall be displayed
     */
    public void start(Stage stage) {
        this.myStage = stage;
        initIntroScene();
    }


    /**
     * Initialize the introduction scene where user can choose type of simulation to run
     * Set stage to the initialized IntroScene
     */
    private void initIntroScene() {
        Group myIntroRoot = new Group();
        myIntroScene = new IntroScene(myIntroRoot, myWidth, myHeight, this);
        myStage.setScene(myIntroScene);
        myStage.setTitle(myTitle);
        myStage.show();
    }


    /**
     * External classes can call this method to change the simulation type
     * The corresponding XML file should be available in resources folder
     * @param s name of the new simulation's XML file
     */
    public void setSimType(String s) {
        this.SIM_TYPE = s;
    }


    /**
     * A private method that's expected to be called from switchSimulation() or resetSimulation()
     * Initialize the grid of cells of a specific concrete cell class and set each cell's initial state
     * Then pipeline to the next step of creating UI scene for displaying visualization
     */
    private void initGrid() {
        if(!readXML()){
            return;
        }
        myGrid = new Cell[myHeight][myWidth];
        initStateList();
        for (int i = 0; i < myGrid.length; i++) {
            for (int j = 0; j < myGrid[0].length; j++) {
                Cell currCell = null;
                String currCellState = defineState(i,j);
                ArrayList<Double> params = new ArrayList<>(parametersList);
                switch (SIM_TYPE) {
                    case GOL_XML:
                        currCell = new CellGameOfLife(i, j, currCellState, params);
                        break;
                    case WATOR_XML:
                        currCell = new CellWATOR(i, j, currCellState, params);
                        break;
                    case FIRE_XML:
                        currCell = new CellFire(i, j, currCellState, params);
                        break;
                    case SEG_XML:
                        currCell = new CellSegregation(i, j, currCellState, params);
                        break;
                    case PERC_XML:
                        currCell = new CellPercolation(i, j, currCellState, params);
                        break;
                }
                myGrid[i][j] = currCell;
            }
        }
        initNeighbors();
        initUI();
    }


    /**
     * Initialize a list of states with/without the percentage distribution read from XML file
     */
    private void initStateList() {
        this.stateList = new ArrayList<>();
        if(statePercentMap.size()==0){
            stateList.addAll(stateImageMap.keySet());
        }else{
            for (String state : statePercentMap.keySet()) {
                for (int i = 0; i < ceil(statePercentMap.get(state) * distributionAccuracy); i++) {
                    stateList.add(state);
                }
            }
        }
    }


    /**
     * Generate state for the current cell based on simulation configuration read from XML file
     * @param row Row index of the cell
     * @param col Column index of the cell
     * @return a String indicating the cell's initial state
     */
    private String defineState(int row, int col){
        Random myRandom = new Random();
        int randIdx = myRandom.nextInt(stateList.size());
        if(this.specConfig){
            List<Integer> cellIdx = Arrays.asList(row,col);
            return cellStateMap.get(cellIdx);
        }else{
            return stateList.get(randIdx);
        }
    }


    /**
     * Loop through all cells in the grid and initialize neighbors for each cell
     */
    private void initNeighbors(){
        for (Cell[] row :myGrid) {
            for (Cell currCell:row) {
                //currCell.findNeighbors(myGrid);
                currCell.findNeighbors(myGrid,cellShape,edgeType,neighborList);
            }
        }
    }


    /**
     * Initialize the UI class for creating visualization of the simulation
     */
    private void initUI() {
        Group myUIRoot = new Group();
        //myUIScene = new UI(myUIRoot, myWidth, myHeight, this);
        myUIScene = new UI(myUIRoot, myWidth, myHeight, cellShape, this);
        myUIScene.drawGrid();
        myStage.setScene(myUIScene);
        myStage.show();
        initTimeline();
    }


    /**
     * Initialize a new timeline for the simulation
     * updateGrid() is called by eventHandler of the frame each 'delay' interval
     */
    private void initTimeline() {
        var frame = new KeyFrame(Duration.millis(delay), e -> updateGrid());
        this.myTimeline = new Timeline();
        this.myTimeline.setCycleCount(Timeline.INDEFINITE);
        this.myTimeline.getKeyFrames().add(frame);
    }


    /**
     * Check for error in XML parsing results. Terminate further grid initialization if invalid.
     * @param parser XMLParser object which handled the input file
     * @return boolean value indicating whether the parsed information is valid
     */
    private boolean validateSimulation(XMLParser parser){
        if(!SIM_TYPE_LIST.contains(parser.getSimType())){
            myParser.modelErrAlert.showAlert();
            return false;
        }else if(SIM_PARAM_NUM.get(parser.getSimType())!=parser.getParameters().size()){
            myParser.paramErrAlert.showAlert();
            return false;
        }else if(SIM_STATE_NUM.get(parser.getSimType())!=parser.getStateImg().keySet().size()){
            myParser.stateErrAlert.showAlert();
            return false;
        }
        return parser.isParseSuccess();
    }


    /**
     * Read XML file containing simulation parameters
     */
    private boolean readXML() {
        String myFilePath;
        if(SIM_TYPE_LIST.contains(SIM_TYPE)){
            myFilePath = "resources/"+SIM_TYPE+".xml";
        }else{
            myFilePath = SIM_TYPE;
        }
        File f = new File(myFilePath);
        try{
            myParser= new XMLParser(f);
        }catch (Exception e){
            myParser.parserConfigAlert.showAlert();
            return false;
        }
        if(!validateSimulation(myParser)) {
            return false;
        }
        this.myWidth = myParser.getWidth();
        this.myHeight = myParser.getHeight();
        this.specConfig = myParser.isSpecConfig();
        this.cellShape = myParser.getCellShape();
        this.edgeType = myParser.getEdgeType();
        this.neighborList = myParser.getNeighbors();
        this.parametersList = myParser.getParameters();
        this.stateImageMap = myParser.getStateImg();
        this.statePercentMap = myParser.getStatePercent();
        this.cellStateMap = myParser.getCellState();
        return true;
    }


    /**
     * @return the 2D array of Cells for this simulation
     */
    public Cell[][] getGrid() {
        return this.myGrid;
    }


    /**
     * @return an immutable map associating state and corresponding image visualization
     */
    public Map<String, String> getStateImageMap(){
        return Collections.unmodifiableMap(this.stateImageMap);
    }


    /**
     * Update all Cells' states in the grid
     */
    private void updateGrid() {
        for (Cell[] row : myGrid) {
            for (Cell currCell:row) {
                currCell.findNextState();
            }
        }
        for (Cell[] row : myGrid) {
            for (Cell currCell:row) {
                currCell.updateState();
            }
        }
        this.myUIScene.drawGrid();
    }


    /**
     * Pause the simulation
     * Expected to be called by UI when a pause button is pressed
     */
    public void pauseSimulation() {
        this.myTimeline.stop();
    }


    /**
     * Run the simulation
     * Expected to be called by UI when a start/resume button is pressed
     */
    public void playSimulation() {
        this.myTimeline.play();
    }


    /**
     * Update Cell states to the next generation and then pause
     * Expected to be called by UI when a step button is pressed
     */
    public void stepSimulation() {
        this.myTimeline.pause();
        updateGrid();
    }


    /**
     * Reset all cell states according to XML file's configuration
     * and still remain in the same simulation model
     */
    public void resetSimulation() {
        this.myTimeline.pause();
        initGrid();
    }


    /**
     * Switch to the other simulation model by reading the corresponding XML file
     * and reinitializing all Cells in the grid
     * @param newSimType path to the XML file for the new Simulation
     */
    public void switchSimulation(String newSimType) {
        this.myTimeline.stop();
        this.setSimType(newSimType);
        initGrid();
    }


    /**
     * Slow down the simulation by increasing the delay time interval by twice
     */
    public void slowdown() {
        this.myTimeline.stop();
        this.delay *= 2;
        initTimeline();
        playSimulation();
    }


    /**
     * Speed up the simulation by reducing the delay time interval to its half
     */
    public void speedup() {
        this.myTimeline.stop();
        this.delay /= 2;
        initTimeline();
        playSimulation();
    }


    /**
     * @return the current simulation model's XML file path
     */
    public String getSimulationType() {
        return this.SIM_TYPE;
    }


    /**
     * Main method to launch the Breakout game program.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
