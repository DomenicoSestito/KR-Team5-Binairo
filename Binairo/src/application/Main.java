package application;
	
import java.io.IOException;

import gui.GameManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;


public class Main extends Application {

	private Stage primaryStage;
	private Scene scene;
	
	private GameManager gameManager=null;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Binairo");
		
        AnchorPane pane;
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("../gui/GameManager.fxml"));
		try {
			pane = (AnchorPane) loader.load();
			scene = new Scene(pane);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gameManager = loader.getController();	
        gameManager.setMain(this);
		
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
        primaryStage.show();
		
	}
	
	public void exitGame(){
		primaryStage.close();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
