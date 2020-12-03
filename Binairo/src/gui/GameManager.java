package gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

public class GameManager {
	
	private Main main;
    
	@FXML 
	private ComboBox size;

	@FXML
    private Button start;
	@FXML
    private Button exit;
    @FXML
    private GridPane gridPane = new GridPane();
	private int matrix_size=6;
	    
    private Button[][] circles;
	private Boolean[][] matrix;
	
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
    			matrix = new Boolean[matrix_size][matrix_size];
    			
    			System.out.println(matrix_size);
    			
    			initMatrix(getString(problem_size));
    			
    			for (int i = 0; i < matrix_size; i++) {
					for (int j = 0; j < matrix_size; j++) {
		    			final Integer innerI = new Integer(i);
		    			final Integer innerJ = new Integer(j);
						circles[i][j] = new Button();
						circles[i][j].setStyle("-fx-background-color: #b0b0b0");
						circles[i][j].setMinWidth(gridPane.getWidth()/matrix_size);
						circles[i][j].setMinHeight(gridPane.getHeight()/matrix_size);
		    			circles[i][j].setOnMouseClicked(new EventHandler<Event>() {
				    		@Override
				    		public void handle(Event event) {
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
				    	});
					}
				}
    			
    			for (int i = 0; i < matrix_size; i++) {
					for (int j = 0; j < matrix_size; j++) {
						gridPane.add(circles[i][j], i, j, 1, 1);
					}
				}
    			
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
    			
    			printMatrix();
    		}
			
    	});
		
    	exit.setOnMouseClicked(new EventHandler<Event>() {
    		@Override
    		public void handle(Event event) {
    			main.exitGame();
    		}
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
				matrix[i][j] = value == 1;
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
            // writer.close(); 
            System.out.println("Successfully Downloaded."); 
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

}