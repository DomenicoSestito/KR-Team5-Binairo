package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

public class GameManager {
	
	private Main main;
    
	@FXML 
	private ComboBox size;
	@FXML
	private Label result;
	@FXML
    private Button start;
	@FXML
    private Button exit;
	@FXML
    private Button hint;
	@FXML
    private Button done;
    @FXML
    private GridPane gridPane = new GridPane();
	private int matrix_size=6;
	    
    private Button[][] circles;
	private Boolean[][] given;
	private Boolean[][] matrix;
	private ArrayList<Boolean[][]> solutions = new ArrayList<Boolean[][]>();
	
	ObservableList<Integer> dim = FXCollections.observableArrayList(6,8,10,14,20);
	
    public GameManager() {}
    
	@FXML
    void initialize() {
		
		size.setItems(dim);
		size.setValue(6);
		
		start.setOnMouseClicked(new EventHandler<Event>() {
			
			int problem_size = 0;		
					
			@Override
    		public void handle(Event event) {
    			matrix_size = (int) size.getValue();
    			
    			gridPane.getChildren().clear();

    			gridPane.setGridLinesVisible(false);
    			
    			switch (matrix_size) {
    			case 6:
    				problem_size = 1;
    				break;
    			case 8:
    				problem_size = 3;
    				break;
    			case 10:
    				problem_size = 5;
    				break;
    			case 14:
    				problem_size = 7;
    				break;
    			case 20:
    				problem_size = 9;
    				break;

    			default:
    				break;
    			}
    			
    			circles = new Button[matrix_size][matrix_size]; 
    			given = new Boolean[matrix_size][matrix_size];
    			matrix = new Boolean[matrix_size][matrix_size];
    			
    			initMatrix(getString(problem_size));
    			
    			for (int i = 0; i < matrix_size; i++) {
					for (int j = 0; j < matrix_size; j++) {
		    			final Integer innerI = new Integer(i);
		    			final Integer innerJ = new Integer(j);
						circles[i][j] = new Button();
						circles[i][j].setShape(new Circle(1.5));
						circles[i][j].setStyle("-fx-background-color: #b0b0b0");
						circles[i][j].setMinWidth(gridPane.getWidth()/matrix_size);					
						circles[i][j].setMinHeight(gridPane.getHeight()/matrix_size);
						setFixedCircles(i, j);
		    			circles[i][j].setOnMouseClicked(new EventHandler<Event>() {
				    		@Override
				    		public void handle(Event event) {
				    			if(matrix[innerJ][innerI]==null){
				    				circles[innerI][innerJ].setStyle("-fx-background-color: #000000");	
				    				matrix[innerJ][innerI]=true;
				    			}
				    			else if(matrix[innerJ][innerI]){
				    				circles[innerI][innerJ].setStyle("-fx-background-color: #ffffff");	
				    				matrix[innerJ][innerI]=false;
				    			}
				    			else if(!matrix[innerJ][innerI]){
				    				circles[innerI][innerJ].setStyle("-fx-background-color: #b0b0b0");	
				    				matrix[innerJ][innerI]=null;				    			
				    			}
				    			checkTheMove();
				    		}
				    	});
					}
				}
    			
    			for (int i = 0; i < matrix_size; i++) {
					for (int j = 0; j < matrix_size; j++) {
						gridPane.add(circles[i][j], i, j, 1, 1);
					}
				}

    			gridPane.setGridLinesVisible(true);
    			
    			for (int i = 0; i < matrix_size; i++) {
					for (int j = 0; j < matrix_size; j++) {
						if(matrix[i][j] != null) {
							if(!matrix[i][j])
								circles[j][i].setStyle("-fx-background-color: #ffffff");
							else
								circles[j][i].setStyle("-fx-background-color: #000000");
						}
					}
				}
    		}
			
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
    			if(matrix!=null)
    				getHint();
    		};
		});
    	
    	done.setOnMouseClicked(new EventHandler<Event>() {
    		@Override
    		public void handle(Event event) {
    			if(matrix!=null)
    				checkCorrectness();
    		};
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
	
    public Main getMain() {
		return main;
	}

	public void setMain(Main main) {
		this.main = main;
	}
	
	public String getString(int problem_size) {
		
		String url = "https://www.puzzle-binairo.com/?size=" + problem_size; 
        String html = downloadWebPage(url);
        
        Pattern pattern = Pattern.compile("var task = \'(.*?)\'");
        Matcher matcher = pattern.matcher(html);
        matcher.find();
        String value = matcher.group(1).toString();
        
		return value;
	}
	
	
	public void initMatrix(String s) {
		
		int i=0,j=0;
		for(char c : s.toCharArray()) {			
			Integer value = null;
			try {
				value = Integer.parseInt(""+c);
			}catch(Exception e) {}
			
			int skip = 1;
			if(value != null)
				given[i][j] = matrix[i][j] = value == 1;
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
		getSolutions();
	}
	
	public boolean isEditable(int i, int j) {
		return given[i][j] == null;
	}
	
	public void printMatrix() {
		for(int i=0;i<matrix_size;++i) {
			for (int j = 0; j < matrix_size; j++) {
				System.out.print(matrix[i][j]);
			}
			System.out.println();
		}
	}
	
	public static String downloadWebPage(String webpage) 
    { 
		String html = "";
        try { 
  
            // Create URL object 
            URL url = new URL(webpage); 
            BufferedReader readr =  
              new BufferedReader(new InputStreamReader(url.openStream())); 
            // read each line from stream till end 
            String line; 
            while ((line = readr.readLine()) != null) { 
                // writer.write(line); 
                html = html + line;
            } 
  
            readr.close(); 
        } 
  
        // Exceptions 
        catch (MalformedURLException mue) { 
            System.out.println("Malformed URL Exception raised"); 
        } 
        catch (IOException ie) { 
            System.out.println("IOException raised"); 
        }
        
        return html;
    } 
	
	public void getHint() {
		
		//getSolutions();
		 
		Boolean[][] solution = solutions.get(0);

		int hint=0;
		for(int i=0;i<matrix_size;i++) {
			for (int j = 0; j < matrix_size; j++) {
				if (given[i][j] == null && solution[i][j]!=matrix[i][j] && hint==0) {
					if(!solution[i][j]) {
						System.out.println(i+" "+j+" "+solution[i][j]);
						matrix[i][j] = solution[i][j];
						given[i][j] = solution[i][j];
						setFixedCircles(j, i);
						circles[j][i].setStyle("-fx-background-color: #ffffff");
					}
					else {
						System.out.println(i+" "+j+" "+solution[i][j]);
						matrix[i][j] = solution[i][j];
						given[i][j] = solution[i][j];
						setFixedCircles(j, i);
						circles[j][i].setStyle("-fx-background-color: #000000");
					}
					hint++;
				}
			}
		}
	}
	
	public void checkCorrectness() {
		boolean correct = false;
		int total = matrix_size*matrix_size;
		int nEqual = 0;
		
		boolean complete = true;
		for(int i=0;i<matrix_size;i++)
			for (int j = 0; j < matrix_size; j++)
				if(matrix[i][j]==null)
					complete = false;
		
		
		if (complete) {
			//getSolutions();
			for (Boolean[][] solution : solutions) {
				nEqual = 0;
				for (int i = 0; i < matrix_size; i++)
					for (int j = 0; j < matrix_size; j++)
						if (matrix[i][j] == solution[i][j])
							nEqual++;
				if (nEqual == total) {
					correct = true;
					break;
				}
			}
			if (correct) {
				result.setText("CONGRATULATIONS! THE PUZZLE IS SOLVED");
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
	
	public void getSolutions() {
		
		String param = "" + matrix_size + "\n";
		for (int i = 0; i < matrix_size; i++) {
			for (int j = 0; j < matrix_size; j++) {
				param += (matrix[i][j] == null ? 'N' : matrix[i][j] ? '1' : '0') + " ";
			}
			param += "\n";
		}

		System.out.println(param);
		Process process;
		
		try {

			String globalPath = GameManager.class.getResource("/resources").toString().substring(6);
			File myObj = new File(globalPath+"/in");
			myObj.createNewFile();

			FileWriter myWriter = new FileWriter(myObj);
			myWriter.write(param);
			myWriter.close();

//			process = new ProcessBuilder("python", globalPath+"/binairo.py").start();
			process = Runtime.getRuntime().exec("python " + globalPath+"/binairo.py");
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			

			System.out.println("output cmd:");
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

			myObj = new File(globalPath+"/out");
			Scanner myReader = new Scanner(myObj);

			Boolean[][] solution = new Boolean[matrix_size][matrix_size];
			
			int row = 0;
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				System.out.println(data);
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

			for (int i = 0; i < matrix_size; ++i) {
				for (int j = 0; j < matrix_size; j++)
					System.out.print(solution[i][j]);
				System.out.println();
			}
			solutions.add(solution);

			myReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void checkTheMove() {
		if(!checkRowsThreeInARow() || !checkColumnsThreeInARow())
			System.out.println("3 DI FILA");
		if(!checkTheNumberInRows() || !checkTheNumberInColumns()) {
			System.out.println("Numero errato");
		}
	}

	private boolean checkTheNumberInRows() {
		for(int i=0; i<matrix_size; i++) {
			int contBlack=0;
			int contWhite=0;
			for(int j=0; j<matrix_size; j++) {
				if(matrix[i][j]!=null) {
					if(matrix[i][j]==true) {
						contBlack++;
					}
					if(matrix[i][j]==false) {
						contWhite++;
					}
				}
			}
			if(contBlack>matrix_size/2 || contWhite>matrix_size/2) {
				return false;
			}
		}
		return true;
	}

	private boolean checkTheNumberInColumns() {
		for(int i=0; i<matrix_size; i++) {
			int contBlack=0;
			int contWhite=0;
			for(int j=0; j<matrix_size; j++) {
				if(matrix[j][i]!=null) {
					if(matrix[j][i]==true) {
						contBlack++;
					}
					if(matrix[j][i]==false) {
						contWhite++;
					}
				}
			}
			if(contBlack>matrix_size/2 || contWhite>matrix_size/2) {
				return false;
			}
		}
		return true;
	}

	public boolean checkRowsThreeInARow() {
		for(int i=0; i<matrix_size; i++) {
			for(int j=1; j<matrix_size-1; j++) {
				if(matrix[i][j]!=null) {
					if(matrix[i][j]==matrix[i][j-1] && matrix[i][j]==matrix[i][j+1]) {
						return false;
					}
				}
			}
		}
		return true;
	}
	public boolean checkColumnsThreeInARow() {
		for(int j=0; j<matrix_size; j++) {
			for(int i=1; i<matrix_size-1; i++) {
				if(matrix[i][j]!=null) {
					if(matrix[i][j]==matrix[i-1][j] && matrix[i][j]==matrix[i+1][j]) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	
	private boolean checkOnColumns(Integer innerI, Integer innerJ) {
		if(innerI==0) {
			if(matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI+1][innerJ] && matrix[innerI][innerJ]==matrix[innerI+2][innerJ]) {
				return false;
			}
		}
		else if(innerI==(int) size.getValue()-1) {
			if(matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI-1][innerJ] && matrix[innerI][innerJ]==matrix[innerI-2][innerJ]) {	
				return false;
			}
		}
		else { 
			if(innerI==1) {
				if((matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI+1][innerJ] && matrix[innerI][innerJ]==matrix[innerI-1][innerJ]) || (matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI+1][innerJ] && matrix[innerI][innerJ]==matrix[innerI+2][innerJ])) {
					return false;
				}
			}
			else if(innerI==(int) size.getValue()-2) {
				if((matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI+1][innerJ] && matrix[innerI][innerJ]==matrix[innerI-1][innerJ]) || (matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI-1][innerJ] && matrix[innerI][innerJ]==matrix[innerI-2][innerJ])) {
					return false;
				}
			}
			else {
				if((matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI+1][innerJ] && matrix[innerI][innerJ]==matrix[innerI-1][innerJ]) || (matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI+1][innerJ] && matrix[innerI][innerJ]==matrix[innerI+2][innerJ]) || (matrix[innerI][innerJ]!=null && matrix[innerI][innerJ]==matrix[innerI-1][innerJ] && matrix[innerI][innerJ]==matrix[innerI-2][innerJ])){
					return false;
				}
			}	
		}
		return true;
	} 
	private boolean checkOnRows(Integer innerI, Integer innerJ) {
		if(innerJ==0) {
			if(matrix[innerI][innerJ]==matrix[innerI][innerJ+1] && matrix[innerI][innerJ]==matrix[innerI][innerJ+2]) {
				return false;
			}
		}
		else if(innerJ==(int) size.getValue()-1) {
			if(matrix[innerI][innerJ]==matrix[innerI][innerJ-1] && matrix[innerI][innerJ]==matrix[innerI][innerJ-2]) {	
				return false;
			}
		}
		else { 
			if(innerJ==1) {
				if((matrix[innerI][innerJ]==matrix[innerI][innerJ+1] && matrix[innerI][innerJ]==matrix[innerI][innerJ-1]) || (matrix[innerI][innerJ]==matrix[innerI][innerJ+1] && matrix[innerI][innerJ]==matrix[innerI][innerJ+2])) {
					return false;
				}
			}
			else if(innerJ==(int) size.getValue()-2) {
				if((matrix[innerI][innerJ]==matrix[innerI][innerJ+1] && matrix[innerI][innerJ]==matrix[innerI][innerJ-1]) || ( matrix[innerI][innerJ]==matrix[innerI][innerJ-1] && matrix[innerI][innerJ]==matrix[innerI][innerJ-2])) {
					return false;
				}
			}
			else {
				if((matrix[innerI][innerJ]==matrix[innerI][innerJ+1] && matrix[innerI][innerJ]==matrix[innerI][innerJ-1]) || (matrix[innerI][innerJ]==matrix[innerI][innerJ+1] && matrix[innerI][innerJ]==matrix[innerI][innerJ+2]) || ( matrix[innerI][innerJ]==matrix[innerI][innerJ-1] && matrix[innerI][innerJ]==matrix[innerI][innerJ-2])){
					return false;
				}
			}	
		}
		return true;
	} 

}