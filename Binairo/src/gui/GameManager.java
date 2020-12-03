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

}