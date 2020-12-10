package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	
	final class Pair<T> {

		private T left;
		private T right;

		public T getL() {
			return left;
		}

		public void setL(T left) {
			this.left = left;
		}
	
		public T getR() {
			return right;
		}
	
		public void setR(T right) {
			this.right = right;
		}
	
		public Pair(T left, T right)
		{
			if (left == null || right == null) { 
				throw new IllegalArgumentException("left and right must be non-null!");
			}
			this.left = left;
			this.right = right;
		}

		public boolean equals(Object o)
		{
			if (! (o instanceof Pair)) { return false; }
			Pair<?> p = (Pair<?>)o;
			return left.equals(p.left) && right.equals(p.right);
		} 

		public int hashCode()
		{
			return 7 * left.hashCode() + 13 * right.hashCode();
		} 
	}
	    
	
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
	private Label error3;
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
	    
    private Button[][] circles; //ciò che viene cliccato sulla matrice sull'interfaccia
	private Boolean[][] initial_given; //matrice con i valori in caso vogliamo fare restart sulla partita
	private Boolean[][] given;
	private Boolean[][] matrix; //matrice sulla quale modifichiamo i valori
	private Boolean[][] solution; //matrice con la soluzione di minizinc
	private Boolean[][] ourMatrix; //matrice che viene generata casuale per l'offline e sul quale si cerca l'istanza
	private Boolean[][] ourSolution; //matrice che rappresenta l'istanza che ha solo una soluzione
	
	private List<Pair<Integer>> editables = new ArrayList<Pair<Integer>>();

    Service<?> handleOfflineService = new HandleOfflineService();
    Service<?> handleStartService = new HandleStartService();
    
	ObservableList<Integer> dim = FXCollections.observableArrayList(6,8,10,14,20);
	
	final static String colorWhite = "-fx-background-color: #ffffff";
	final static String colorBlack = "-fx-background-color: #000000";
	final static String colorNull  = "-fx-background-color: #b0b0b0";

	final static String globalPath = GameManager.class.getResource("/resources").toString().substring(6);
	
    public GameManager() {}
    
    //Eventi sui tasti
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
					setDisableAllButtons(true);
					handleStartService.start();
				}
    		}			
    	});
		
		handleStartService.setOnSucceeded(e ->{
			loading.setVisible(false);
			setDisableAllButtons(false);
			handleStartService.reset();
		});
		
		
		offline.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {				
				if(!handleOfflineService.isRunning()) {
					loading.setVisible(true);
					setDisableAllButtons(true);			
					handleOfflineService.start();
				}
			}
		});
		
		handleOfflineService.setOnSucceeded(e ->{			
			loading.setVisible(false);
			setDisableAllButtons(false);		
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
    			checkWin();
    		};
		});
    	
    	restart.setOnMouseClicked(new EventHandler<Event>() {
    		@Override
    		public void handle(Event event) {
    			setDisableButtonsForGame(false);
    			
    			editables.clear();
    			for(int i=0;i<matrix_size; i++) {
    				for (int j = 0; j < matrix_size; j++) {
    					given[i][j]=initial_given[i][j];
    					matrix[i][j]=initial_given[i][j];
		    			if (initial_given[i][j]==null) {
							circles[j][i].setText("");
							circles[j][i].setDisable(false);
						}
		    			if(matrix[i][j]==null) {
		    				circles[j][i].setStyle(colorNull);
		    				editables.add(new Pair<Integer>(Integer.valueOf(i), Integer.valueOf(j)));
		    			}
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
	
	//Pulisce la matrice dove appaiono cerchi e griglia
	private void clearGrid() {
		Platform.runLater( () -> {			
			clearText();
			gridPane.getChildren().clear();
			gridPane.setGridLinesVisible(false);
			
		});
	}
	
	//Disabilita i tasti done e hint (es. dopo una vittoria)
	public void setDisableButtonsForGame(boolean disable) {
		Platform.runLater( () -> {
			hint.setDisable(disable);
			done.setDisable(disable);
		});
	}

	//Nel momento di attesa della creazione del livello (sia new che offline) non è possibile premere nessun tasto
	public void setDisableAllButtons(boolean disable) {
		setDisableButtonsForGame(disable);
		Platform.runLater( () -> {
			restart.setDisable(disable);
			start.setDisable(disable);
			offline.setDisable(disable);
		});
	}
	
	//Cancella i messaggi sull'interfaccia (es. il messaggio di vittoria dopo aver premuto restart/new)
	private void clearText() {
		Platform.runLater( () -> {
			result.setText("");    			
			error1.setText("");	  			
			error2.setText("");
			error3.setText("");
		});
	}
	
	//Inizializza le istanze delle matrici
	private void initNewMatrixes() {
		circles		 	= new Button [matrix_size][matrix_size]; 
		given 			= new Boolean[matrix_size][matrix_size];
		initial_given 	= new Boolean[matrix_size][matrix_size];
		matrix 			= new Boolean[matrix_size][matrix_size];
		solution 		= new Boolean[matrix_size][matrix_size];
		editables.clear();
	}
	
	//Eventi che vengono chiamati quando si preme "NEW" (online mode)
	private void handleStartButton() {
		System.out.println("Pressed Start Button");
		
		matrix_size = (int) size.getValue();
		
		clearGrid();	
				
		initNewMatrixes();
		
		initMatrixFromWebSite();
		
		initCircleMatrix();
	}
	
	//Eventi che vengono chiamati quando si preme "OFFLINE" (offline mode)	
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

		initEditables();
		
		initCircleMatrix();
	}
	
	/*
	 * Inizialliza la matrice vuota con bottoni per cliccare su di loro e poter cambiare il colore
	 * Click su cella vuota -> crea pallino nero(TRUE)
	 * Click su cella nera -> crea pallino bianco(FALSE)
	 * Click su cella bianca -> svuota la cella(NULL)
	 * Dopo l'interfaccia viene pulita da eventuali messaggi presenti
	 * Infine si controlla se la mossa fatta sulla cella può generare errore (3 cerchi di fila oppure troppi cerchi di un certo colore presenti)
	 * e se la cella corrisponde ad un'errore rispetto alla soluzione oppure no
	 */
	private void initCircleMatrix() {
		for (int i = 0; i < matrix_size; i++) {
			for (int j = 0; j < matrix_size; j++) {
    			final Integer innerI = Integer.valueOf(i);
    			final Integer innerJ = Integer.valueOf(j);
				circles[i][j] = new Button();
				circles[i][j].setShape(new Circle(1.5));
				circles[i][j].setStyle(colorNull);
				circles[i][j].setMinSize(gridPane.getWidth()/matrix_size, gridPane.getHeight()/matrix_size);
				circles[i][j].setMaxSize(gridPane.getWidth()/matrix_size, gridPane.getHeight()/matrix_size);
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
		    			checkMoveWithSolution(innerJ,innerI);
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
	
	//Fissa un cerchio(non sostituibile) se diverso da null (quindi se nero(TRUE) o bianco(FALSE))
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
		
	/*
	 * Scarica dal sito l'istanza a seconda della size scelta e viene parserizzata per assegnare alla matrice
	 * il valore true o false nella rispettiva cella
	 */
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
        
		int i=0, j=0;
		for(char c : s.toCharArray()) {			
			Integer value = null;
			try {
				value = Integer.parseInt(""+c);
			} catch(Exception e) {}
			
			int skip = 1;
			if(value != null) {
				given[i][j] = matrix[i][j] = value == 1;
				initial_given[i][j] = given[i][j];
			}
			else {
				skip = (c - 'a'+1);
			}
			for(int k=0; k < skip; ++k) {
				j++;
				if(j == matrix_size) {
					j = 0;
					i++;
				}
			}
		}
		System.out.println("Puzzle:");
		printMatrix(initial_given);
		
		initEditables();
		getSolutionFromMiniZinc();
	}
	
	//La funzione che chiama questa si assicura che la cella sia diversa da null per poterla bloccare
	public boolean isEditable(int i, int j) {
		return given[i][j] == null;
	}
	
	//Aggiunge in una lista le cell null al momento sul quale sarà possibile dopo effettuare un evento
	public void initEditables() {
		for(int m = 0; m<matrix_size; m++) {
			for(int n = 0; n<matrix_size; n++) {
				if(given[m][n]==null) {
					editables.add(new Pair<Integer>(Integer.valueOf(m),Integer.valueOf(n)));
				}
			}
		}
	}
	
	//Stampa su console la matrice che gli viene passata
	public void printMatrix(Boolean m[][]) {
		for(int i=0;i<matrix_size;++i) {
			for (int j = 0; j < matrix_size; j++)
				System.out.print( (m[i][j] != null ? m[i][j] ? '1' : '0' : 'N')+ " ");
			System.out.println();
		}
		System.out.println("-------------------------------");
	}
	
	//Scarica dal sito web passato come parametro l'html
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

	/*
	 * Prendiamo dalla lista delle celle che devono essere ancora assegnate una coppia casuale di indici
	 * che vengono assegnati dalla soluzione alla nostra matrice per poi essere fissati
	 * Questa coppia di indici viene poi eliminata dalla lista di celle che devono essere ancora assegnate
	 */
	public void getHint() {
		
		
		Random random= new Random();
	
		int i = random.nextInt(editables.size());
//		System.out.println(editables.size());
		Pair<Integer> pair = editables.get(i);
		matrix[pair.getL()][pair.getR()] = solution[pair.getL()][pair.getR()];
		given[pair.getL()][pair.getR()] = solution[pair.getL()][pair.getR()];
		setFixedCircles(pair.getR(), pair.getL());
		circles[pair.getR()][pair.getL()].setStyle(matrix[pair.getL()][pair.getR()] ? colorBlack : colorWhite);
		editables.remove(i);
		
//		while(!hinted) {
//			int i = random.nextInt(matrix_size);
//			int j = random.nextInt(matrix_size);
//			
//			if (given[i][j] == null && solution[i][j]!=matrix[i][j]) {
//				matrix[i][j] = solution[i][j];
//				given[i][j] = solution[i][j];
//				setFixedCircles(j, i);				
//				circles[j][i].setStyle(matrix[i][j] ? colorBlack : colorWhite);
//				
//				hinted = true;
//			}
//		}
		
		checkMove();
		if(checkSolution()) {
			win();
			return;
		} 
	}
	
	//Controlla che tutte le celle siano riempite
	private boolean isMatrixFilled() {
		for(int i=0;i<matrix_size;i++)
			for (int j = 0; j < matrix_size; j++)
				if(matrix[i][j]==null)
					return false;
		return true;
	}
	
	//Controlla la nostra matrice con la soluzione, se una cella della nostra matrice è diversa dalla soluzione ritorna falso
	public boolean checkSolution() {
		for (int i = 0; i < matrix_size; i++)
			for (int j = 0; j < matrix_size; j++)
				if (matrix[i][j] != solution[i][j])
					return false;
		return true;
	}
	
	//Se il puzzle è completato correttamente viene mostrato il messaggio e vengono disattivi i tasti relativi al gioco(done e hint)
	public void win() {
		result.setText("CONGRATULATIONS!\nTHE PUZZLE IS SOLVED");
		result.setTextFill(Color.web("#008000"));
		result.setAlignment(Pos.CENTER);
		setDisableButtonsForGame(true);
	}
	
	/*
	 * Controlla 3 diverse situazioni
	 * 1. Se la nostra matrice corrisponde alla soluzione (in caso viene mostrato un messaggio di vittoria)
	 * 2. Se la matrice è riempita ma non corrisponde alla soluzione
	 * 3. Se il tasto done viene premuto quando la matrice non è ancora completa 
	 */
	public void checkWin() {
		if(checkSolution()) {
			win();
		} 
		else if(isMatrixFilled()){
			result.setText("THERE ARE MISTAKES IN THE PUZZLE");
			result.setTextFill(Color.web("#ff0000"));
			result.setAlignment(Pos.CENTER);
		} 
		else {
			result.setText("THE PUZZLE IS NOT COMPLETED");
			result.setTextFill(Color.web("#ff0000"));
			result.setAlignment(Pos.CENTER);
		}
	}
	
	/*
	 * Questa funzione genera la soluzione attraverso il file python che richiama minizinc
	 * 1. Viene creato l'input da passare su minizinc e scritto sul file "in" che viene creato
	 * 2. Dopo viene lanciato il processo python che contiene l'API di minizinc
	 * 3. Una volta finito il processo viene parserizzato il file "out" creato
	 * 4. Nella matrice "solution" viene salvato il corrispettivo valore: true (nero) oppure false (bianco)
	 */
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
	
	/*
	 * Controlla se la mossa sulla matrice è corretta, altrimenti viene scritto il messaggio
	 * che sono presenti 3 cerchi uguali di fila oppure
	 * che il numero dei cerchi di un certo colore supera il massimo che si possono posizionare
	 */
	public void checkMove() {
		Platform.runLater(()->{
			error1.setTextFill(Color.web("#ff0000"));
			error1.setAlignment(Pos.CENTER);
			error2.setTextFill(Color.web("#ff0000"));
			error2.setAlignment(Pos.CENTER);
			error3.setTextFill(Color.web("#ff0000"));
			error3.setAlignment(Pos.CENTER);
			if(!checkRowsThreeInARow() || !checkColumnsThreeInARow())
				error1.setText("3 DI FILA");
			else
				error1.setText("");
			if(!checkTheNumberInRows() || !checkTheNumberInColumns())
				error2.setText("NUMERO ERRATO");
			else
				error2.setText("");
			if(checkEqualsRows())
				error3.setText("PIU' RIGHE UGUALI");
			else if (checkEqualsColumns())
				error3.setText("PIU' COLONNE UGUALI");
			else
				error3.setText("");
		});
	}
	

	// Verifichiamo che il valore della cella fillata sia corretta o meno
	private void checkMoveWithSolution(Integer innerI, Integer innerJ) {
		if(editables.contains(new Pair<Integer>(innerI,innerJ))) {
//			System.out.println("contiene");
			if(solution[innerI][innerJ]==matrix[innerI][innerJ]){
				editables.remove(new Pair<Integer>(innerI,innerJ));
				//System.out.println("Questo colore è una soluzione per questa cella");
			}
		}
		else {
//			System.out.println("non contiene");
			editables.add(new Pair<Integer>(innerI,innerJ));
		}
//		System.out.println(editables.size());
	}

	/*
	 * Ci assicuriamo che in ogni riga non siano presenti un numero di celle dello stesso colore
	 * maggiore della metà della dimensione della matrice
	 * 
	 * contBlack>matrix_size/2 || contWhite>matrix_size/2
	 */
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

	/*
	 * Ci assicuriamo che in ogni colonna non siano presenti un numero di celle dello stesso colore
	 * maggiore della metà della dimensione della matrice
	 * 
	 * contBlack>matrix_size/2 || contWhite>matrix_size/2
	 */
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

	/*
	 * Partendo dalla seconda cella fino alla penultima ci assicuriamo che in una riga la cella non abbia
	 * una cella dello stesso colore sia nella cella precedente che in quella successiva
	 */
	public boolean checkRowsThreeInARow() {
		for(int i=0; i<matrix_size; i++) 
			for(int j=1; j<matrix_size-1; j++) 
				if(matrix[i][j]!=null) 
					if(matrix[i][j]==matrix[i][j-1] && matrix[i][j]==matrix[i][j+1]) 
						return false;
		return true;
	}
	
	/*
	 * Partendo dalla seconda cella fino alla penultima ci assicuriamo che in una colonna la cella non abbia
	 * una cella dello stesso colore sia nella cella precedente che in quella successiva
	 */
	public boolean checkColumnsThreeInARow() {
		for(int j=0; j<matrix_size; j++) 
			for(int i=1; i<matrix_size-1; i++) 
				if(matrix[i][j]!=null) 
					if(matrix[i][j]==matrix[i-1][j] && matrix[i][j]==matrix[i+1][j]) 
						return false;
		return true;
	}
	
	private boolean checkEqualsColumns() {
		Boolean[] currentColumn;
		for(int i=0; i<matrix_size; i++) {
			currentColumn =getColumn(matrix,i);
			if(containsNull(currentColumn))
				continue;
			for(int j=i+1; j<matrix_size; j++) {
				if(Arrays.equals(currentColumn, getColumn(matrix,j))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkEqualsRows() {
		Boolean[] currentRow;
		for(int i=0; i<matrix_size; i++) {
			currentRow = matrix[i];
			if(containsNull(currentRow))
				continue;
			for(int j=i+1; j<matrix_size; j++) {
				if(Arrays.equals(currentRow, matrix[j])) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static Boolean[] getColumn(Boolean[][] array, int index){
		Boolean[] column = new Boolean[array[0].length];
	    for(int i=0; i<column.length; i++){
	       column[i] = array[i][index];
	    }
	    return column;
	}
	
	private static boolean containsNull(Boolean[] current) {
		for(int i=0; i<current.length; i++) {
			if(current[i]==null) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Per ogni riga viene assegnato un valore (true o false alternato) in una colonna casuale
	 * 
	 */
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

	/*
	 * Questa funzione genera la soluzione attraverso il file python che richiama minizinc
	 * 1. Viene creato l'input da passare su minizinc e scritto sul file "in" che viene creato
	 * 2. Dopo viene lanciato il processo python che contiene l'API di minizinc
	 * 3. Una volta finito il processo viene parserizzato il file "out" creato
	 * 4. Nelle matrice "solution" e "ourMatrix" vengono salvati il corrispettivo valore: true (nero) oppure false (bianco)
	 */
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

	/*
	 * Generiamo un'istanza che abbia solo una soluzione data una soluzione
	 * Ad ogni ciclo while viene
	 * 1. salvata la corrente matrice
	 * 2. eliminato almeno un cerchio
	 * 3. viene lanciato il processo di minizinc con un altro script python
	 * 
	 * se il numero di soluzione è ancora uguale a 1 continuiamo a ciclare
	 * se il numero di soluzioni è maggiore di 1 torniamo come istanza la precedente salvata
	 */
	private void generatePuzzleFromSolution() {
		Random random = new Random();
		System.out.print("generating");
		
		int solutions = 1;
		while (solutions == 1) {
			
			for (int i = 0; i < matrix_size; ++i) 
				for (int j = 0; j < matrix_size; j++)
					ourSolution[i][j]=ourMatrix[i][j];
			
			// CANCELLO ALMENO 1 CERCHIO
			for(int c = 0; c < 1;) {
				int i = random.nextInt(matrix_size);
				int j = random.nextInt(matrix_size);
				if(ourMatrix[i][j] != null) {
					ourMatrix[i][j] = null;
					c++;
				}
			}
			
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

	//Genera l'input da passare a minizinc per l'analisi
	private String generateMatrixInputForMiniZinc(Boolean[][] m) {
		String param = "" + matrix_size + "\n";
		for (int i = 0; i < matrix_size; i++) {
			for (int j = 0; j < matrix_size; j++) 
				param += (m[i][j] == null ? 'N' : m[i][j] ? '1' : '0') + " ";
			param += "\n";
		}
		return param;
	}

	/*
	 * Le due classi qua sotto permettono di gestire l'inizializzazione delle matrice
	 * sia per l'online che per l'offline 
	 */
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