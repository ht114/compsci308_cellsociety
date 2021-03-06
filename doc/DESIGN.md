
# Design Goals
The purpose of this project is to implement a program that can simulate several different cell automation models based on configuration details provided in XML source files.  
The simulation models eventually implemented include: Spread of Fire, Game of Life, Percolation, Segregation, WATOR, and Rock Paper Scissors.  

### Feature Implementation:   
* *Supporting varied neighbor rearrangements:*   
  The "neighbor" configuration can be flexibly adjusted by modifying the neighbor definition in model's configuration file. The information is parsed and stored in the configuration scope, and then passed to the cells (simulation scope) for generating neighbors.
* *Supporting square/triangle cell shapes and finite/toroidal grid edge types:*   
  The cell shape and edge type can be defined in model's configuration file, and read in by the configuration part, who further passes the information to visualization and simulation parts at the initialization of cells and UI.
* *Handling invalid/missing file data and Exceptions:*
  XMLParser, at its initialization, reads in alert message contents from a source file and creates several XMLAlert objects, each for handling a specific exception/error scenario. The XMLAlert class implements JavaFX's Alert feature and pops up the alert dialogue box when its showAlert() method is invoked. This feature is implemented internally within the configuration scope.
* *Displaying simulation population history over time:*
  UI implements functions for displaying a lineChart that contains multiple series that each correspond to one of the cell states in the specific simulation type. This feature is implemented solely within the scope of visualization.
* *Allowing user modification on simulation parameters and speed:*
  User inputs received on the front end by UI (visualization) will be transferred to Simulation on the back end (configuration), where the changes are applied and further transferred to Cell (simulation) if necessary.
* *Allowing user modification on cell state:*
  The user action is received by UI (visualization), who communicates with Cell (simulation) directly to update the cell state. The configuration end has no knowledge about any of these changes.

### **Project Structure:**
* Simulation (Cell classes, Neighbor classes):  
  This part of the project consists of an abstract cell class that can be extended for each new simualtion added. When a cell is created, it has its state information, its indexes for its location within the Cell grid, and any numerical parameters necessary to the specific simulation passed through its constructor. The Simulation class (configuration part of the project) then calls for the cell to locate its neighbors and provides the Cell grid as well as the indexes of the desired neighbors.  
  An abstract Neighbor superclass was constructed such that it can be extended for new neighbors as new shapes are added. The Cell creates an instance of the necessary Neighbor class and then uses that class to set the Cell's neighbors.  
  After the Neighbors are set, the cell waits to be called upon by the Simulation class to first set its next state, using the method that was specifically written in the concrete implementation of Cell, and then is called again by the Simulation class to update its current state to its next state.
* Configuration (Simulation, XMLParser, XMLAlert):  
  The configuration part consists of Simulation (despite the name of this class, it's still more on the configuration side), XMLParser and XMLAlert. Simulation class extends Application and is therefore the entry point of the whole program, and in the constructor it calls readConfig() to read in general configuration data, such as valid model types, associated number of states and required initialization parameters.  
  In the start() method (inherited from Application), Simulation invokes method for initializing the program's opening scene where user can select a specific simulation model type. This step would initialize an IntroScene object, which would notify Simulation once the user clicks on any simulation model's button. 
  After receiving user inputs, Simulation goes through a pipeline of initializing an XMLParser for parsing specific model's XML file, retrieving parsing results, initializing the grid of cells with certain concrete Cell class, setting up the animation timeline, and initializes the UI for displaying simulation visualization in real time.  
  The animation is accomplished by attaching a Frame to the timeline, and updating the cell states between delay intervals of the frame. The default delay value is calculated based on the minDelay and maxDelay data read from the general configuration file in the beginning, and can be adjusted based on user input. To update cell states over the whole grid, Simulation loops through each cell in the grid and calls findNextState() for the cell to determine the next state to switch to, based on the current states of its neighbors and itself; after a full iteration, the grid is looped through again, where Simulation calls switchState() on each cell to update the state. This implementation decision was made in order to preserve a cell's current-generation state for its neighbors for finding their next states.  
  After the grid has been updated, Simulation would then notify UI to update its visualization. The specific rules for determining a cell's next state and the procedures for UI to update its visualization are all internally implemented in Cell and UI classes, and the configuration part (i.e. Simulation, XMLParser, XMLAlert classes) has completely no knowledge about those details. 
  Simulation also has several methods for controlling the flow of simulation (running, pausing, speeding up/down, etc.), which are public and can be invoked by UI if any user action occurs.
* Visualization (IntroScene, UI):  
  The visualization consists of the UI and IntroScene classes, which inherit Scene. The IntroScene contains
  a splash page from which the user can select a button that brings up a specific simulation. The types of simulations that
  the user can choose from include Fire, Game of Life, Percolation, Rock Paper Scissors, Segregation, and WaTor World. Each button
  calls a method from Simulation class to configure the Cells for each specific simulation. 
  After the user selects a simulation, the UI scene is displayed in Simulation. The UI scene contains a grid of cells that can be square or triangle shaped and
  keeps track of current Cell states through a 2d array of Cells that are passed into the UI class from Simulation. The UI also
  contains information about the corresponding color of each state, passed from the Simulation class. Using this information
  UI creates a Polygon for a Cell at that location, filled with the color corresponding to its state.
  The UI class has 2 public methods, drawGraph and drawGrid that are called by the Simulation class every time the simulation updates, so the
  graph of cell states and grid of cells displayed are updated. The UI class also calls userSwitchState from the 
  Cell class so that the user can click on a cell displayed in the grid and change the cell state. 
  

# Adding New Features
### Adding another simulation model:
* **Cell Class:**  
Our project has an abstract cell superclass that can be extended to create a new simulation. The superclass contains the majority of the necessary methods such that there are only three methods that have to be written. The core method is findNextState(). This is where the rules of the simulation come into play. The end result of this method is determining how the cell should respond, given its current neighbors. The myNextState variable must be assigned somewhere within this method. Additionally, the setParams() method needs to be written to assign all the parameters passed through the constructor to the appropriate variables. This allows the UI to adjust the parameters and cell regularly calls this method to ensure that they are updated. Lastly, the initializeStatesList() needs to be written. This is just creating a list of all possible states for the simulation.
* **Simulation:**   
At initialization of the application, Simulation reads in a source file SimulationConfig.txt, which provides information about the default grid size, valid simulation types and associated number of states and parameters. In order for the new simulation model to run, the name of this new model type as well as associated numbers of states and parameters shall be added to the general config file so that the parsing results of the new model's configuration will be accepted by Simulation class as a valid model.  
Additionally, the Simulation class will also need to add a case statement in the initGrid() method so that corresponding concrete cells can be initialized for the new simulation model.
### Adding a new Cell Shape
* **Neighbors:**  
There is an abstract Neighbors superclass that can be extended. Once extended, only one method needs to be written. The setIndexMap() creates a map with the agreed upon index for a current neighbor as a key. The value would then be a list with the change in row and then the change in column. For example, look at the image below. Here, for each cell neighbor, the larger number is the index, used as the key in the map, and the coordinates are the change in row and column from the original cell, stored in a list in the value of the map.
![](https://i.imgur.com/nIltupv.png)
 By creating this hashmap, the superclass then has defined methods that allow the cell simulation class to choose which neighbors are active and the cell should take into consideration.
 The new shape will also need to be added in the cell class in a switch case such that the correct neighborhood will be created.
* **Simulation & XMLParser:**  
The XMLParser reads in the cell shape by parsing text content within the tag 'CellShape' in a model's configuration file, and passes this piece of information to Simulation, who passes it down to UI and cells at their initialization call.  
Thus, in order for a new cell shape to take effect, the string specifying this shape must be provided within the 'CellShape' tag inside the source configuration file.
* **UI:**
The UI contains a switch for different cases of cell shapes. Based on the specific type of cell shape specified and passed
in from the Simulation class through the UI constructor, the UI calculates coordinate values of each Polygon that is displayed, which
represents a Cell. Each created Polygon is treated the same regardless of shape - they are added to the root, 
they change fill color based on underlying Cell state, and change state when clicked. 

### Adding a New Edge Type:
* **Neighbors:**  
This is currently handled in the Neighbors superclass in the handleEdgesAndAddCoords() method. When a cell is setting its neighbors and one of its neighbors would be out of bounds, it calls this method to determine if and how that neighbor should be added. Within this method, the edge type is checked and a corresponding method is called. For example, if the edge type is toroidal, a method is called find the correct coordinates to the correct neighbor on the other side of the grid. To add a new edge type, currently it would be necessary to write a method to determine the coordinates of the correct cell and then call that method within the handleEdgesAndAddCoords() method.
* **Simulation & XMLParser:**  
The XMLParser reads in the edge type information by parsing text content within the tag 'EdgeStyle' in a model's configuration file, and passes this piece of information to Simulation, who passes it down to cells at their initialization call.  
Thus, in order for a new cell shape to take effect, the string specifying this new edge type must be provided within the 'EdgeStyle' tag inside the source configuration file. Specific rules for handling the new edge type will be defined by the concrete Cell and Neighbors classes.

# Major Design Choices
* **Running one single simulation at a time:**
    * *Description*  
    The Simulation class (despite its naming, this class is more of the configuration part whereas Cell classes are more of the real Simulation part) was designed to extend Application at the very beginning of this project, which simplified the program control flow by enabling Simulation to directly control the animation timeline as well as communicate with all the other classes to process file parsing, error handling, and object initialization, etc. 
    * *Trade-off*  
    Such a design eventually limits the program's capability of running multiple simulation models at the same time. Moreover, as operations including JavaFX timeline control, file parsing and model setting up are all implemented in the Simulation class, we were unable to draw a clear line between front end and back end in the Simulation class, which would somehow limit the program's flexibility of adapting the same backend to various front ends.

* **Exception handling:**  
    * *Description*  
    The XMLAlert class is specifically created for popping alert dialogue boxes when any Java exception occurs, or when encountering missing/invalid data issues in parsing a model configuration XML file. There are several XMLAlerts initialized at the initialization of XMLParser, where the error messages are read in from a source file and sequentially passed into the constructor of XMLAlert.
    * *Trade-off*  
    While the XMLAlerts are created and stored in the XMLParser class, after the parsing results are transferred to Simulation, the latter will still have to validate the parsed data against what has been defined in the general config file, and will also need to access and pop up XMLAlert's dialogue boxes if any invalidity occurs. For this purpose, the XMLAlerts are declared as package-private variables in order to allow Simulation to access them.  
    With such a design, the program can function normally and is able to detect and handle the exception/error cases by showing corresponding XMLAlert diapogue boxes. Despite the effectiveness, the code structure appears a bit messy and still has space for improvements (eventually this part of code was refactored during the refactoring stage).

* **Abstract Superclass for Cell:**  
A lot of the methods for the cell class are the same, such as: findNeighbors(), updateState(), userSwitchState(), and several others. By creating an abstract superclass, these methods are shared with all its children and it avoids duplication, it's easy to extend and add new simulations, and it separates the code in a manner that is readable.

* **Abstract Superclass for Neighbor:**  
This was a harder decison. The benefits were, that regardless of the shape, there are several methods that would be the same. However, in each concrete implementation, there is only one method that is actually written. This made it hard to justify the necessity for a new class for each shape. However, no other option that wouldn't involve large amounts of duplicate code or adding the methods directly to the cell class were thought of, and so an abstract superclass was made. This worked well and makes it easy to add new shapes in terms of locating a cell's neighbors.

# Assumptions or Decision
* **Burning Simulation:** a cell will check if it should catch on fire once for every burning neighbor it has. This means that more burning neighbors increases the chance of a tree catching. This was decided because in a real forest fire situation, more fire nearby would also increase the likelihood of catching.
* **WATOR Simulation:** It was assumed that each cell could only house a maximum of one animal per step. This mimics reality, as two things cannot exist in the same space. Additionally, had more than one animal been allowed, it would become exceedingly difficult to keep track of all who was housed in a cell and how to handle the movements of each inhabitant. Thus, for simplicity and attempting to remain realistic, cells can only host one animal.
* **Segregation:** The rules for the movement of a cell, when unsatisfied, were left rather vague. As a result, it was decided that when a cell became dissatisfied, it would scroll from its starting location down each row until it either found an empty cell or looped all the way to its starting coordinates. If it made it back to where it began, the cell would not move, as there were no empty spaces that hadn't been claimed by other cells. This was a fairly easy way to ensure that the cell would only check each other cell once, as opposed to being completely random. While this does result in the first few steps displaying extremely large groups moving to the bottom and then the top of the grid, it quickly disperses and still results in a successful simulation.
* **Rocks Paper Scissors:** If, when a cell is checked to set its next state, the next state has already been set, it is assumed that it was "eaten" by a neighbor and it is not able to affect its neighbors in this step. Also, white is considered to be able to be "eaten" by any other cell, whether white tries to "eat" another color or is attempted to be "eaten" by another color, it always loses. Lastly, the "gradient" component of the simulation was not added due to time constraints, however it would be easy to add in the future. Within the CellRPS class a gradient variable would need to be created and incremented and decremented as appropriate, and then the XML file would need to add a parameter and the UI would need to add a parameter slider. All of these are extremely doable with our current structure.

