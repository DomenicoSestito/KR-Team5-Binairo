package gui;

import application.Main;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class GameManager {
	
	private Main main;
    
    @FXML
    private Button exit;
    
    public GameManager() {}

	@FXML
    void initialize() {

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