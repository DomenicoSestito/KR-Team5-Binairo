package gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
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
		    			circles[i][j].setOnMouseClicked(new EventHandler<Event>() {
				    		@Override
				    		public void handle(Event event) {
				    			if(isEditable(innerJ, innerI)){				    			
					    			if(matrix[innerI][innerJ]==null){
					    				circles[innerI][innerJ].setStyle("-fx-background-color: #000000");	
					    				matrix[innerI][innerJ]=true;
					    			}
					    			else if(matrix[innerI][innerJ]){
					    				circles[innerI][innerJ].setStyle("-fx-background-color: #ffffff");	
					    				matrix[innerI][innerJ]=false;				    			
					    			}
					    			else if(!matrix[innerI][innerJ]){
					    				circles[innerI][innerJ].setStyle("-fx-background-color: #b0b0b0");	
					    				matrix[innerI][innerJ]=null;				    			
					    			}	
				    			}
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
		
		getSolutions();
		 
		Random rand = new Random();
		Boolean[][] solution = solutions.get(rand.nextInt(solutions.size()));

		int hint=0;
		for(int i=0;i<matrix_size;i++) {
			for (int j = 0; j < matrix_size; j++) {
				if (matrix[i][j] == null && hint==0) {
					matrix[i][j] = solution[i][j];
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
			getSolutions();
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
		
	}

}