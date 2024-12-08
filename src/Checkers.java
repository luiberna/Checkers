import pt.iscte.guitoo.Color;
import pt.iscte.guitoo.StandardColor;
import pt.iscte.guitoo.board.Board;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;

//Criar uma funcao que verifica o final do jogo conta as pecas e indica o vencedor
//Alterar o load para dar load a tabuleiros de todos os tamanhos

public class Checkers {
	Board board;
	boolean turnW;
	String[][] game;
	int[] selected;
	int gl, gc, np;

	Checkers() {
		gl = 8;
		gc = 8;
		np = 12;
		turnW = true;
		game = new String[gl][gc];
		selected = null;
		initGame();
		board = new Board("Damas", gl, gc, 80);
		board.setBackgroundProvider(this::background);
		board.setIconProvider(this::icon);
		board.addAction("random", this::random);
		board.addAction("new", this::newGame);
		board.addAction("save", this::save);
		board.addAction("load", this::load);
		board.addMouseListener(this::click);
	}

	//Constructor for variable board sizes
	Checkers(int line, int col, int numPieces) {
		gl = line;
		gc = line;
		np = numPieces;
		turnW = true;
		game = new String[gl][gc];
		selected = null;
		initGame();
		board = new Board("Damas", gl, gc, 80);
		board.setBackgroundProvider(this::background);
		board.setIconProvider(this::icon);
		board.addAction("random", this::random);
		board.addAction("new", this::newGame);
		board.addAction("save", this::save);
		board.addAction("load", this::load);
		board.addMouseListener(this::click);
	}

	int numberOfWhite() {
		int totalPieces = 0;
		for (int i = 0; i < gl; i++) {
			for (int j = 0; j < gc; j++) {
				if (game[i][j] == "white.png")
					totalPieces++;
			}
		}
		return totalPieces;
	}

	int numberOfBlack() {
		int totalPieces = 0;
		for (int i = 0; i < gl; i++) {
			for (int j = 0; j < gc; j++) {
				if (game[i][j] == "black.png")
					totalPieces++;
			}
		}
		return totalPieces;
	}

	//Loads the information of the file to the board matrix
	void load() {
		int tmp_line;
		int fileLines = 0;
		try {
			String fileName = board.promptText("File Name:");
			if (fileName == null)
				return;

			Scanner countScanner = new Scanner(new File(fileName));
			while (countScanner.hasNextLine()) {
				fileLines++;
				countScanner.nextLine();
			}
			fileLines--;
			countScanner.close();
			tmp_line = (int)(Math.sqrt(fileLines));

			Scanner myScanner = new Scanner(new File(fileName));
			Checkers gui = new Checkers(tmp_line, tmp_line, 0);

			gui.game = new String[tmp_line][tmp_line];
			gui.selected = null;
			for (int i = 0; i < tmp_line; i++) {
				for (int j = 0; j < tmp_line; j++) {
					String line = myScanner.nextLine();
					if (line.equals("null"))
						gui.game[i][j] = null;
					else if (line.equals("black.png"))
						gui.game[i][j] = "black.png";
					else if (line.equals("white.png"))
						gui.game[i][j] = "white.png";
				}
			}
			String line = myScanner.nextLine();
			gui.turnW = line.equals("true");
			myScanner.close();
			gui.board.showMessage("Game loaded successfully");
			gui.start();
		}
		catch (IOException e) {
			board.showMessage("Error loading the file");
		}
	}

	//Saves the information of the board in a file
	void save() {
		try {
			String fileName = board.promptText("File Name:");
			if (fileName == null)
				return;
			FileWriter fileWriter = new FileWriter(new File(fileName));
			for (int i = 0; i < gl; i++) {
				for (int j = 0; j < gc; j++) {
					if (game[i][j] == null)
						fileWriter.write("null" + "\n");
					else
						fileWriter.write(game[i][j] + "\n");
				}
			}
			if (turnW)
				fileWriter.write("true");
			else
				fileWriter.write("false");
			board.showMessage("File created: " + fileName);
			fileWriter.close();
		}
		catch (IOException e) { 
			board.showMessage("Error saving the file");
		}
	}

	//Counts the pieces of each team see who is the winner
	void getWinner() {
		int blackPieces = 0;
		int whitePieces = 0;

		for (int i = 0; i < gl; i++) {
			for (int j = 0; j < gc; j++) {
				if (game[i][j] == "black.png")
					blackPieces++;
				if (game[i][j] == "white.png")
					whitePieces++;
			}
		}
		if (blackPieces > whitePieces)
			board.showMessage("Black Wins with: " + blackPieces + " pieces!");
		else if(blackPieces < whitePieces)
			board.showMessage("White Wins with: " + whitePieces + " pieces!");
		else
			board.showMessage("Tie!");
	}

	//Used "trys" so it doesnt break the program when there is no more plays to be done
	void random() {
		int trys = 1000;
	
		while (trys-- > 0) {
			int line = (int) (Math.random() * gl);
			int col = (int) (Math.random() * gc);

			if (!turnW)
				board.setTitle("White plays" + "White Pieces: " + numberOfWhite() + " vs " + "Black Pieces: " + numberOfBlack());
			else
				board.setTitle("Black plays" + "White Pieces: " + numberOfWhite() + " vs " + "Black Pieces: " + numberOfBlack());
			if (!sameTeam(line, col)) 
				continue;
			selected = new int[]{line, col};
			if (checkCaptures() && canCapture(line, col)) {
				for (int i = 0; i < 10000; i++) {
					int targetLine = (int)(Math.random() * gl);
					int targetCol = (int)(Math.random() * gc);
	
					if (checkCaptures() && inBounds(targetLine, targetCol) && isDark(targetLine, targetCol)) {
						capture(targetLine, targetCol);
						if (canCapture(selected[0], selected[1]))
							continue;
						turnW = !turnW;
						selected = null;
						return;
					}
				}
			}
			for (int i = 0; i < 1000; i++) {
				int targetLine = (int)(Math.random() * gl);
				int targetCol = (int)(Math.random() * gc);

				if (!checkCaptures() && inBounds(targetLine, targetCol) && isValid(targetLine, targetCol)) {
					movePiece(targetLine, targetCol);
					return;
				}
			}
		}
		if (checkWinner()) {
			getWinner();
		}
	}
	
	//Checks if the move is valid checking if it is a dark square and if it is within the range without capture
	boolean isValid(int line, int col) {
		if (inBounds(line, col) && game[line][col] == null && isDark(line, col) && moveForw(line, col))
			return true;
		return false;
	}

	boolean inBounds(int line, int col) {
		if (line >= 0 && line < gl && col >= 0 && col < gc)
			return true;
		return false;
	}

	//Checks if the move is valid in case there is no capture
	boolean moveForw(int line, int col) {
		if (turnW && line == (selected[0] - 1) && (col == (selected[1] + 1) || col == (selected[1] - 1)))
			return true;
		if (!turnW && line == (selected[0] + 1) && (col == (selected[1] + 1) || col == (selected[1] - 1)))
			return true;
		return false;
	}

	//Checks if it is a dark square
	boolean isDark(int line, int col) {
		if ((line % 2 == 0 && col % 2 != 0) || (line % 2 != 0 && col % 2 == 0))
			return true;
		return false;
	}

	//Verifies if the piece has a possibility of capture
	boolean canCapture(int line, int col) {
		if (turnW && inBounds(line - 1, col + 1) && !sameTeam(line - 1, col + 1) && game[line - 1][col + 1] != null) {
			if (inBounds(line - 2, col + 2) && game[line - 2][col + 2] == null && isDark(line - 2, col + 2))
				return true;
		}
		if(turnW && inBounds(line - 1, col - 1) && !sameTeam(line - 1, col - 1) && game[line - 1][col - 1] != null) {
			if (inBounds(line - 2, col - 2) && game[line - 2][col - 2] == null && isDark(line - 2, col - 2))
				return true;
		}
		if (!turnW && inBounds(line + 1, col + 1) && !sameTeam(line + 1, col + 1) && game[line + 1][col + 1] != null) {
			if (inBounds(line + 2, col + 2) && game[line + 2][col + 2] == null && isDark(line + 2, col + 2))
				return true;
		}
		if(!turnW && inBounds(line + 1, col - 1) && !sameTeam(line + 1, col - 1) && game[line + 1][col - 1] != null) {
			if (inBounds(line + 2, col - 2) && game[line + 2][col - 2] == null && isDark(line + 2, col - 2))
				return true;
		}
		return false;
	}

	//Does all the verifications as canCapture(checking if there is a piece to capture) and captures it
	void capture(int line, int col) {
		if (turnW && inBounds(selected[0] - 1, selected[1] + 1) && !sameTeam(selected[0] - 1, selected[1] + 1) && game[selected[0] - 1][selected[1] + 1] != null) {
			if (inBounds(selected[0] - 2, selected[1] + 2) && game[selected[0] - 2][selected[1] + 2] == null && isDark(selected[0] - 2, selected[1] + 2)) {
				if (line == selected[0] - 2 && col == selected[1] + 2) {
					game[selected[0] - 1][selected[1] + 1] = null;
					game[line][col] = game[selected[0]][selected[1]];
					game[selected[0]][selected[1]] = null;
					selected = new int[]{line, col};
					return;
				}
			}
		}
		if (turnW && inBounds(selected[0] - 1, selected[1] - 1) && !sameTeam(selected[0] - 1, selected[1] - 1) && game[selected[0] - 1][selected[1] - 1] != null) {
			if (inBounds(selected[0] - 2, selected[1] - 2) && game[selected[0] - 2][selected[1] - 2] == null && isDark(selected[0] - 2, selected[1] - 2)) {
				if (line == selected[0] - 2 && col == selected[1] - 2) {
					game[selected[0] - 1][selected[1] - 1] = null;
					game[line][col] = game[selected[0]][selected[1]];
					game[selected[0]][selected[1]] = null;
					selected = new int[]{line, col};
					return;
				}
			}
		}
		if (!turnW && inBounds(selected[0] + 1, selected[1] + 1) && !sameTeam(selected[0] + 1, selected[1] + 1) && game[selected[0] + 1][selected[1] + 1] != null) {
			if (inBounds(selected[0] + 2, selected[1] + 2) && game[selected[0] + 2][selected[1] + 2] == null && isDark(selected[0] + 2, selected[1] + 2)) {
				if (line == selected[0] + 2 && col == selected[1] + 2) {
					game[selected[0] + 1][selected[1] + 1] = null;
					game[line][col] = game[selected[0]][selected[1]];
					game[selected[0]][selected[1]] = null;
					selected = new int[]{line, col};
					return;
				}
			}
		}
		if (!turnW && inBounds(selected[0] + 1, selected[1] - 1) && !sameTeam(selected[0] + 1, selected[1] - 1) && game[selected[0] + 1][selected[1] - 1] != null) {
			if (inBounds(selected[0] + 2, selected[1] - 2) && game[selected[0] + 2][selected[1] - 2] == null && isDark(selected[0] + 2, selected[1] - 2)) {
				if (line == selected[0] + 2 && col == selected[1] - 2) {
					game[selected[0] + 1][selected[1] - 1] = null;
					game[line][col] = game[selected[0]][selected[1]];
					game[selected[0]][selected[1]] = null;
					selected = new int[]{line, col};
					return;
				}
			}
		}
	}

	boolean checkNoPieces() {
		int blackPieces = 0;
		int whitePieces = 0;

		for (int i = 0; i < gl; i++) {
			for (int j = 0; j < gc; j++) {
				if (game[i][j] == "white.png")
					whitePieces++;
				if (game[i][j] == "black.png")
					blackPieces++;
			}
		}
		if (blackPieces == 0 || whitePieces == 0)
			return true;
		return false;
	}

	boolean checkWinner() {
		if (checkNoPieces())
			return true;
		if (checkCaptures())
			return false;
		for (int i = 0; i < gl; i++) {
			for (int j = 0; j < gc; j++) {
				if (turnW && game[i][j] == "white.png" && (inBounds(i - 1, j + 1) && game[i - 1][j + 1] == null || inBounds(i - 1, j - 1) && game[i - 1][j - 1] == null))
					return false;
				if (!turnW && game[i][j] == "black.png" && (inBounds(i + 1, j + 1) && game[i + 1][j + 1] == null || inBounds(i + 1, j - 1) && game[i + 1][j - 1] == null))
					return false;
			}
		}
		return true;
	}
	
	//Click
	void click(int line, int col) {
		if (checkWinner()) {
			getWinner();
			return;
		}
		if (selected == null || sameTeam(line, col))
			pieceSelect(line, col);
		else {
			if (checkCaptures() && canCapture(selected[0], selected[1])) {
				capture(line, col);
				if (canCapture(selected[0], selected[1]))
					return; 
				turnW = !turnW; 
				selected = null;
			} 
			else if (!checkCaptures() && isValid(line, col))
				movePiece(line, col);
		}
		if (turnW)
			board.setTitle("White plays! " + "      White Pieces: " + numberOfWhite() + " vs " + "Black Pieces: " + numberOfBlack());
		else
			board.setTitle("Black plays! " + "      White Pieces: " + numberOfWhite() + " vs " + "Black Pieces: " + numberOfBlack());
	}

	//Checks if the piece that is selected is from the same team so we can change the piece we want to play
	boolean sameTeam(int line, int col) {
		if (turnW && game[line][col] == "white.png")
			return true;
		if (!turnW && game[line][col] == "black.png")
			return true;
		return false;
	}

	//Checks if there is any chance of a capture in the current play
	boolean checkCaptures() {
		for (int i = 0; i < gl; i++) {
			for (int j = 0; j < gc; j++) {
				if(turnW && game[i][j] == "white.png" || !turnW && game[i][j] == "black.png") {
					if (canCapture(i, j))
						return true;
				}
			}
		}
		return false;
	}

	//Moves the piece if the movment is valid
	void movePiece(int line, int col) {
		if(isValid(line, col))
		{
			game[line][col] = game[selected[0]][selected[1]];
			game[selected[0]][selected[1]] = null;
			selected = null;
			turnW = !turnW;
		}
	}

	//Selects the piece to be played
	void pieceSelect(int line, int col) {
		if (turnW && game[line][col] == "white.png")
			selected = new int[]{line, col};
		else if (!turnW && game[line][col] == "black.png")
			selected = new int[]{line, col};
	}

	String icon(int line, int col) {
        return game[line][col];
    }

	//Inicializa a matriz com as pecas corretas
	void initGame() {
		for (int i = 0; i < gl; i++) {
			for (int j = 0; j < gc; j++) {
				game[i][j] = null;
			}
		}
		int placedPieces = 0;
		for (int i = 0; i < gl && placedPieces < np; i++) {
			for (int j = 0; j < gc; j++) {
				if (isDark(i, j)) {
					game[i][j] = "black.png";
					placedPieces++;
					if (placedPieces == np)
						break;
				}
			}
		}
		placedPieces = 0;
		for (int i = gl - 1; i >= 0 && placedPieces < np; i--) {
			for (int j = 0; j < gc; j++) {
				if (isDark(i, j)) {
					game[i][j] = "white.png";
					placedPieces++;
					if (placedPieces == np) 
						break;
				}
			}
		}
	}
	
	//Inicializa o tabuleiro com as cores corretas
	Color background(int line, int col) {
		if (selected != null && selected[0] == line && selected[1] == col)
			return StandardColor.YELLOW;
		else if (line % 2 == 0 && col % 2 != 0)
			return StandardColor.BLACK;
		else if (line % 2 != 0 && col % 2 == 0)
			return StandardColor.BLACK;
        return StandardColor.WHITE;
    }

	void start() {
		board.open();
	}

	void newGame() {
		int lines = board.promptInt("Number of lines & columns: ");
		if (lines == -1 || lines == 0 || lines == 1 || lines == 2) {
			board.showMessage("Number of lines not allowed");
			return;
		}
		int numPieces = board.promptInt("Number of pieces per player: ");
		if (numPieces == -1)
			return;
		if (numPieces > ((lines - 1) * 2) || lines == 3 && numPieces > 1) {
			board.showMessage("Number of pieces not allowed");
			return;
		}
        Checkers gui = new Checkers(lines, lines, numPieces);
        gui.start();
    }

	public static void main(String[] args) {
		Checkers gui = new Checkers();
		gui.start();
	}
}
