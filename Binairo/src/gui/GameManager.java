package gui;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class GameManager {
	
	private Main main;
    
	@FXML 
	private ComboBox size;

	@FXML
    private Button start;
	@FXML
    private Button exit;
    
	ObservableList<Integer> dim = FXCollections.observableArrayList(6,8,10,14,20);
	private int matrix_size;
	
	private Boolean[][] matrix = new Boolean[6][6];
	
    public GameManager() {}

	@FXML
    void initialize() {
		
		size.setItems(dim);
		size.setValue(6);
		
		start.setOnMouseClicked(new EventHandler<Event>() {
			@Override
    		public void handle(Event event) {
    			matrix_size = (int) size.getValue();
    			System.out.println(matrix_size);
    			
    			initMatrix("c0c1g0f1c0a110e");
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
	
	
	public void initMatrix(String s) {
		
		int i=0,j=0;
		for(char c : s.toCharArray()) {
//			System.out.println(c + " " + (c- 'a'+1));
			
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

}