package CellSociety;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Simulation extends Application{

    public static final double DEFAULT_WIDTH = 400;
    public static final double DEFAULT_HEIGHT = 400;
    public static final String GOL_XML = "Game of Life.xml";
    public static final String WATOR_XML = "WaTor.xml";
    public static final String FIRE_XML = "Fire.xml";
    public static final String SEG_XML = "Segregation.xml";
    public static final String PERC_XML = "Percolation.xml";

    private Stage myStage;
    private double delay;
    private Cell[][] grid;
    private Scene myIntroScene;
    private UI myUIScene;
    private Group myRoot;
    private double myWidth = DEFAULT_WIDTH;
    private double myHeight = DEFAULT_HEIGHT;
    private String SIM_TYPE;
    private boolean pause = false;




    public void start (Stage stage) {
        myStage = stage;
        myRoot = new Group();
        myIntroScene = new IntroScene(myRoot,DEFAULT_WIDTH,DEFAULT_HEIGHT,this);
        myStage.setScene(myIntroScene);
        myStage.setTitle("Cell Society");
        myStage.show();
        // somewhere in the scene's method of handling button,
        // when a button is pressed, SIM_TYPE will be updated to corresponding file name
        readXML();
        myUIScene = new UI(myRoot,DEFAULT_WIDTH,DEFAULT_HEIGHT,this);
    }


    private void initGrid(){

    }

    private void updateGrid(){

    }


    private void readXML(){
        File f = new File(SIM_TYPE);


    }


    private void pauseSimulation(){
        this.pause = true;
    }


    private void resumeSimulation(){
        this.pause = false;
    }


    private void stepSimulation(){

    }


    private void resetSimulation(){

    }

    private void switchSimulation(String keyWord){

    }

    private void slowdown(){

    }

    private void speedup(){

    }


    public String getSimulationType(){
        return this.SIM_TYPE;
    }





    /**
     * Main method to launch the Breakout game program.
     */
    public static void main (String[] args) {
        launch(args);
    }
}