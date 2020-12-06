package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

public class GameManager {
	
	private Main main;
    
	@FXML 
	private ComboBox<Integer> size;
	@FXML
	private Label result;
	@FXML
	private Label error1;
	@FXML
	private Label error2;
	@FXML
    private Button start;
	@FXML
    private Button offline;
	@FXML
    private Button restart;
	@FXML
    private Button exit;
	@FXML
    private Button hint;
	@FXML
    private Button done;
	@FXML
	private Pane loading;
    @FXML
    private GridPane gridPane = new GridPane();
	private int matrix_size=6;
	    
    private Button[][] circles;
	private Boolean[][] initial_given;
	private Boolean[][] given;
	private Boolean[][] matrix;
	private Boolean[][] solution;
	private Boolean[][] ourMatrix;
	private Boolean[][] ourSolution;

    Service<?> handleOfflineService = new HandleOfflineService();
    Service<?> handleStartService = new HandleStartService();
    
	ObservableList<Integer> dim = FXCollections.observableArrayList(6,8,10,14,20);
	
	final static String colorWhite = "-fx-background-color: #ffffff";
	final static String colorBlack = "-fx-background-color: #000000";
	final static String colorNull  = "-fx-background-color: #b0b0b0";

	final static String globalPath = GameManager.class.getResource("/resources").toString().substring(6);
	
    public GameManager() {}
    
	@FXML
    void initialize() {
		size.setItems(dim);
		size.setValue(6);
		
		loading.setVisible(false);		
		restart.setDisable(true);	
		hint.setDisable(true);	
		done.setDisable(true);
		
		start.setOnMouseClicked(new EventHandler<Event>() {					
			@Override
    		public void handle(Event event) {				
				if(!handleStartService.isRunning()) {
					loading.setVisible(true);
					handleStartService.start();
				}
    		}			
    	});
		
		handleStartService.setOnSucceeded(e ->{
			loading.setVisible(false);
			handleStartService.reset();
		});
		
		
		offline.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {				
				if(!handleOfflineService.isRunning()) {
					loading.setVisible(true);				
					handleOfflineService.start();
				}
			}
		});
		
		handleOfflineService.setOnSucceeded(e ->{			
			loading.setVisible(false);			
			handleOfflineService.reset();
		});
		
		
    	exit.setOnMouseClicked(new EventHandler<Event>() {
    		@Override
    		public void handle(Event event) {
    			main.exitGame();
    		}
    	});
    	
    	hint.setOnMouseClicked(new EventHandler<Event>() {
    		@Override
    		public void handle(Event event) {
    			result.setText("");    
    			getHint();
    		};
		});
    	
    	done.setOnMouseClicked(new EventHandler<Event>() {
    		@Override
    		public void handle(Event event) {
    			result.setText("");    
    			checkCorrectness();
    		};
		});
    	
    	restart.setOnMouseClicked(new EventHandler<Event>() {
    		@Override
    		public void handle(Event event) {
    			for(int i=0;i<matrix_size; i++) {
    				for (int j = 0; j < matrix_size; j++) {
    					given[i][j]=initial_given[i][j];
    					matrix[i][j]=initial_given[i][j];
		    			if (initial_given[i][j]==null) {
							circles[j][i].setText("");
							circles[j][i].setDisable(false);
						}
		    			if(matrix[i][j]==null)
		    				circles[j][i].setStyle(colorNull);		    			
		    			else if(matrix[i][j])
		    				circles[j][i].setStyle(colorBlack);    			
		    			else if(!matrix[i][j])
		    				circles[j][i].setStyle(colorWhite);	    						
    				}    				
    			}  
    			clearText();
    		}
		});
    }
	
	private void clearGrid() {
		Platform.runLater( () -> {
			restart.setDisable(false);
			hint.setDisable(false);
			done.setDisable(false);
			
			clearText();
			gridPane.getChildren().clear();
			gridPane.setGridLinesVisible(false);
			
		});
	}
	
	private void clearText() {
		Platform.runLater( () -> {
			result.setText("");    			
			error1.setText("");	  			
			error2.setText("");	
		});
	}
	
	private void initNewMatrixes() {
		circles		 	= new Button [matrix_size][matrix_size]; 
		given 			= new Boolean[matrix_size][matrix_size];
		initial_given 	= new Boolean[matrix_size][matrix_size];
		matrix 			= new Boolean[matrix_size][matrix_size];
		solution 		= new Boolean[matrix_size][matrix_size];
	}
	
	
	private void handleStartButton() {
		System.out.println("Pressed Start Button");
		
		matrix_size = (int) size.getValue();
		
		clearGrid();	
				
		initNewMatrixes();
		
		initMatrixFromWebSite();
		
		initCircleMatrix();
	}
	
	private void handleOfflineButton() {
		System.out.println("Pressed Offline Button");
		
		matrix_size = (int) size.getValue();
				
		clearGrid();
		
		initNewMatrixes();

		generateNewPuzzleOffline();	
		
		for (int i = 0; i < matrix_size; i++) {
			for (int j = 0; j < matrix_size; j++) {
				matrix[i][j] = ourSolution[i][j];
				given[i][j] = matrix[i][j];
				initial_given[i][j] = given[i][j];					
			}
		}
		// getSolutionFromMiniZinc();
		
		initCircleMatrix();
	}
	
	private void initCircleMatrix() {
		for (int i = 0; i < matrix_size; i++) {
			for (int j = 0; j < matrix_size; j++) {
    			final Integer innerI = new Integer(i);
    			final Integer innerJ = new Integer(j);
				circles[i][j] = new Button();
				circles[i][j].setShape(new Circle(1.5));
				circles[i][j].setStyle(colorNull);
				circles[i][j].setMinWidth((gridPane.getWidth()/matrix_size)-1);					
				circles[i][j].setMinHeight((gridPane.getHeight()/matrix_size)-1);
				setFixedCircles(i, j);
    			circles[i][j].setOnMouseClicked(new EventHandler<Event>() {
		    		@Override
		    		public void handle(Event event) {
		    			if(matrix[innerJ][innerI]==null){
		    				circles[innerI][innerJ].setStyle(colorBlack);	
		    				matrix[innerJ][innerI]=true;
		    			}
		    			else if(matrix[innerJ][innerI]){
		    				circles[innerI][innerJ].setStyle(colorWhite);	
		    				matrix[innerJ][innerI]=false;
		    			}
		    			else if(!matrix[innerJ][innerI]){
		    				circles[innerI][innerJ].setStyle(colorNull);	
		    				matrix[innerJ][innerI]=null;				    			
		    			}
		    			clearText();
		    			checkMove();
		    		}
		    	});
			}
		}
		
		for (int i = 0; i < matrix_size; i++) {
			for (int j = 0; j < matrix_size; j++) {
				if(matrix[i][j] != null) {
					if(!matrix[i][j])
						circles[j][i].setStyle(colorWhite);
					else
						circles[j][i].setStyle(colorBlack);
				}
			}
		}
		
		Platform.runLater(() -> {
			for (int i = 0; i < matrix_size; i++) {
				for (int j = 0; j < matrix_size; j++) {
					gridPane.add(circles[i][j], i, j, 1, 1);
				}
			}
			gridPane.setGridLinesVisible(true);
		});
	}
	
	
	
	public void setFixedCircles(int i, int j) {
		if(!isEditable(j,i)) {
			circles[i][j].setText(".");
			circles[i][j].setDisable(true);
			circles[i][j].setOpacity(1);
			circles[i][j].setFont(new Font(20));
			if(given[j][i]) {
				circles[i][j].setTextFill(Color.WHITE);
			}
		}	
	}
		
	
	public void initMatrixFromWebSite() {
		int problem_size = 0;
		switch (matrix_size) {
		case 6: problem_size = 1; break;
		case 8: problem_size = 3; break;
		case 10:problem_size = 5; break;
		case 14:problem_size = 7; break;
		case 20:problem_size = 9; break;
		}
        String html = downloadWebPage("https://www.puzzle-binairo.com/?size=" + problem_size);        
        Pattern pattern = Pattern.compile("var task = \'(.*?)\'");
        Matcher matcher = pattern.matcher(html);
        matcher.find();
        String s = matcher.group(1).toString();  
        
		int i=0,j=0;
		for(char c : s.toCharArray()) {			
			Integer value = null;
			try {
				value = Integer.parseInt(""+c);
			}catch(Exception e) {}
			
			int skip = 1;
			if(value != null) {
				given[i][j] = matrix[i][j] = value == 1;
				initial_given[i][j] = given[i][j];
			}
			else 
				skip = (c - 'a'+1);
			
			for(int k=0;k<skip;++k) {
				j++;
				if(j==matrix_size) {
					j=0;
					i++;
				}
			}
		}
		System.out.println("Puzzle:");
		printMatrix(initial_given);
		
		getSolutionFromMiniZinc();
	}
	
	public boolean isEditable(int i, int j) {
		return given[i][j] == null;
	}
	
	public void printMatrix(Boolean m[][]) {
		for(int i=0;i<matrix_size;++i) {
			for (int j = 0; j < matrix_size; j++)
				System.out.print( (m[i][j] != null ? m[i][j] ? '1' : '0' : 'N')+ " ");
			System.out.println();
		}
		System.out.println("-------------------------------");
	}
	
	public static String downloadWebPage(String webpage) 
    { 
		String html = "";
		System.out.println("downloading...");
        try { 
            URL url = new URL(webpage); 
            BufferedReader readr = new BufferedReader(new InputStreamReader(url.openStream())); 
            String line; 
            while ((line = readr.readLine()) != null) { 
                html = html + line;
            } 
            readr.close(); 
        } 
        catch (MalformedURLException mue) { 
            System.out.println("Malformed URL Exception raised"); 
        } 
        catch (IOException ie) { 
            System.out.println("IOException raised"); 
        }
        System.out.println("downloaded");
        return html;
    } 	

	public void getHint() {
		if(isMatrixFilled())
			return;
		
		Random random= new Random();
		boolean hinted = false;
		
		while(!hinted) {
			int i = random.nextInt(matrix_size);
			int j = random.nextInt(matrix_size);
			
			if (given[i][j] == null && solution[i][j]!=matrix[i][j]) {
				matrix[i][j] = solution[i][j];
				given[i][j] = solution[i][j];
				setFixedCircles(j, i);				
				circles[j][i].setStyle(matrix[i][j] ? colorBlack : colorWhite);
				
				hinted = true;
			}
		}
		
		checkMove();
	}
	
	private boolean isMatrixFilled() {
		for(int i=0;i<matrix_size;i++)
			for (int j = 0; j < matrix_size; j++)
				if(matrix[i][j]==null)
					return false;
		return true;
	}
	
	public void checkCorrectness() {
		boolean correct = false;
		int total = matrix_size*matrix_size;
		int nEqual = 0;
		
		if (isMatrixFilled()) {
			//getSolutions();
			nEqual = 0;
			for (int i = 0; i < matrix_size; i++)
				for (int j = 0; j < matrix_size; j++)
					if (matrix[i][j] == solution[i][j])
						nEqual++;
			if (nEqual == total) {
				correct = true;
			}
			if (correct) {
				result.setText("CONGRATULATIONS!\nTHE PUZZLE IS SOLVED");
				result.setTextFill(Color.web("#008000"));
				result.setAlignment(Pos.CENTER);
			} else {
				result.setText("THERE ARE MISTAKES IN THE PUZZLE");
				result.setTextFill(Color.web("#ff0000"));
				result.setAlignment(Pos.CENTER);
			} 
		}
		else {
			result.setText("THE PUZZLE IS NOT COMPLETED");
			result.setTextFill(Color.web("#ff0000"));
			result.setAlignment(Pos.CENTER);
		}		
	}
	
	public void getSolutionFromMiniZinc() {
		
		String param = generateMatrixInputForMiniZinc(matrix);

//		System.out.println(param);
		Process process;
		System.out.println("calculating solution...");
		
		try {

			File myObj = new File(globalPath+"/in");
			myObj.createNewFile();

			FileWriter myWriter = new FileWriter(myObj);
			myWriter.write(param);
			myWriter.close();
			
			process = Runtime.getRuntime().exec("python " + globalPath+"/binairo.py");
			process.waitFor();

			myObj = new File(globalPath+"/out");
			Scanner myReader = new Scanner(myObj);
			
			int row = 0;
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				String[] splitted = data.split("\\s+");

				for (int i = 0; i < matrix_size; i++) {
					if (splitted[i].equals("1")) {
						solution[row][i] = true;
					} else {
						solution[row][i] = false;
					}
				}
				row++;
			}

			myReader.close();
			
			System.out.println("Solution:");
			printMatrix(solution);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void checkMove() {
		Platform.runLater(()->{
			error1.setTextFill(Color.web("#ff0000"));
			error1.setAlignment(Pos.CENTER);
			error2.setTextFill(Color.web("#ff0000"));
			error2.setAlignment(Pos.CENTER);
			if(!checkRowsThreeInARow() || !checkColumnsThreeInARow())
				error1.setText("3 DI FILA");
			else
				error1.setText("");
			if(!checkTheNumberInRows() || !checkTheNumberInColumns())
				error2.setText("NUMERO ERRATO");
			else
				error2.setText("");
		});
	}

	private boolean checkTheNumberInRows() {
		for(int i=0; i<matrix_size; i++) {
			int contBlack=0;
			int contWhite=0;
			for(int j=0; j<matrix_size; j++) {
				if(matrix[i][j]!=null) {
					if(matrix[i][j]==true) 
						contBlack++;
					if(matrix[i][j]==false) 
						contWhite++;
				}
			}
			if(contBlack>matrix_size/2 || contWhite>matrix_size/2) 
				return false;
		}
		return true;
	}

	private boolean checkTheNumberInColumns() {
		for(int i=0; i<matrix_size; i++) {
			int contBlack=0;
			int contWhite=0;
			for(int j=0; j<matrix_size; j++) {
				if(matrix[j][i]!=null) {
					if(matrix[j][i]==true) 
						contBlack++;					
					if(matrix[j][i]==false) 
						contWhite++;
				}
			}
			if(contBlack>matrix_size/2 || contWhite>matrix_size/2) 
				return false;
		}
		return true;
	}

	public boolean checkRowsThreeInARow() {
		for(int i=0; i<matrix_size; i++) 
			for(int j=1; j<matrix_size-1; j++) 
				if(matrix[i][j]!=null) 
					if(matrix[i][j]==matrix[i][j-1] && matrix[i][j]==matrix[i][j+1]) 
						return false;
		return true;
	}
	
	public boolean checkColumnsThreeInARow() {
		for(int j=0; j<matrix_size; j++) 
			for(int i=1; i<matrix_size-1; i++) 
				if(matrix[i][j]!=null) 
					if(matrix[i][j]==matrix[i-1][j] && matrix[i][j]==matrix[i+1][j]) 
						return false;
		return true;
	}
	
	public void generateNewPuzzleOffline() {			
		
		Random random= new Random();
		ourMatrix=new Boolean[matrix_size][matrix_size];
		Boolean startValue=true;
		for(int i=0;i<matrix_size;i++) {
			ourMatrix[i][random.nextInt(matrix_size)]=startValue;
			startValue= (!startValue);
		}
		
		System.out.println("First Matrix:");
		printMatrix(ourMatrix);
		
		generateSolutionFromMatrixOffline();
		System.out.println("Solution:");
		printMatrix(ourMatrix);
		
		generatePuzzleFromSolution();	
		System.out.println("Generated Puzzle:");
		printMatrix(ourSolution);		
	}	

	private void generateSolutionFromMatrixOffline() {
		ourSolution=new Boolean[matrix_size][matrix_size];		
		String param = generateMatrixInputForMiniZinc(ourMatrix);
		
		try {

			File myObj = new File(globalPath+"/in");
			myObj.createNewFile();

			FileWriter myWriter = new FileWriter(myObj);
			myWriter.write(param);
			myWriter.close();
			Process process = Runtime.getRuntime().exec("python " + globalPath+"/binairo.py");
			process.waitFor();

			myObj = new File(globalPath+"/out");
			Scanner myReader = new Scanner(myObj);
			
			int row = 0;
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				String[] splitted = data.split("\\s+");

				for (int i = 0; i < matrix_size; i++) {
					if (splitted[i].equals("1")) {
						ourMatrix[row][i] = true;
						solution[row][i] = true;
					} else {
						ourMatrix[row][i] = false;
						solution[row][i] = false;
					}
				}
				row++;
			}
			
//			for (int i = 0; i < matrix_size; ++i) {
//				for (int j = 0; j < matrix_size; j++)
//					ourSolution[i][j]=ourMatrix[i][j];
//			}

			myReader.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}

	private void generatePuzzleFromSolution() {
		Random random= new Random();
		System.out.print("generating");
		
		int solutions = 1;
		while (solutions == 1) {
			
			for (int i = 0; i < matrix_size; ++i) 
				for (int j = 0; j < matrix_size; j++)
					ourSolution[i][j]=ourMatrix[i][j];
			
			// CANCELLO 5 CERCHI
			// for(int c=0;c<5;) {
				int i = random.nextInt(matrix_size);
				int j = random.nextInt(matrix_size);
				if(ourMatrix[i][j] != null) {
					ourMatrix[i][j] = null;
					// c++;
				}
			// }
			
			String param = generateMatrixInputForMiniZinc(ourMatrix);
			Process process;
			try {

				File myObj = new File(globalPath + "/in");
				myObj.createNewFile();

				FileWriter myWriter = new FileWriter(myObj);
				myWriter.write(param);
				myWriter.close();
				
				process = Runtime.getRuntime().exec("python " + globalPath + "/binairoCountSolutions.py");
				process.waitFor();

				myObj = new File(globalPath + "/outSolutions");
				Scanner myReader = new Scanner(myObj);
				solutions = Integer.valueOf(myReader.nextLine());
				myReader.close();

				System.out.print(".");

			} catch (Exception e) {
				System.out.println(e);
			} 
		}
		System.out.println();
	}

	
	private String generateMatrixInputForMiniZinc(Boolean[][] m) {
		String param = "" + matrix_size + "\n";
		for (int i = 0; i < matrix_size; i++) {
			for (int j = 0; j < matrix_size; j++) 
				param += (m[i][j] == null ? 'N' : m[i][j] ? '1' : '0') + " ";
			param += "\n";
		}
		return param;
	}

    class HandleOfflineService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {                	
                	handleOfflineButton();
                    return null;
                }
            };
        }
    }
    

    class HandleStartService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {                	
                	handleStartButton();
                    return null;
                }
            };
        }
    }

    public Main getMain() {
		return main;
	}

	public void setMain(Main main) {
		this.main = main;
	}
}