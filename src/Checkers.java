import pt.iscte.guitoo.Color;
import pt.iscte.guitoo.StandardColor;
import pt.iscte.guitoo.board.Board;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;

//Para guardar e ler ficheiros, simplesmente guardar a matriz de Strings (em linhas ou em matriz) do ficheiro e de seguida 
//ler a matriz quando abrir o ficheiro. Se a matriz for nula numa certa posicao a string sera um espaco seguido de uma quebra
//de linha

public class Checkers {
	Board board;
	boolean turnW;
	String[][] game;
	int[] selected;

	Checkers() {
		turnW = true;
		game = new String[8][8];
		selected = null;
		initGame();
		board = new Board("Damas", 8, 8, 50);
		board.setBackgroundProvider(this::background);
		board.setIconProvider(this::icon);
		board.addAction("random", this::random);
		board.addAction("new", this::newGame);
		board.addAction("save", this::save);
		board.addAction("load", this::load);
		board.addMouseListener(this::click);
	}

	void load() {
		Checkers gui = new Checkers();
		gui.start();
		
		try {
			String fileName = board.promptText("File Name:");
			Scanner myScanner = new Scanner(new File(fileName));
			
			game = new String[8][8];
			selected = null;
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					String line = myScanner.nextLine();
					if (line.equals("null"))
						game[i][j] = null;
					else if (line.equals("black.png"))
						game[i][j] = "black.png";
					else if (line.equals("white.png"))
						game[i][j] = "white.png";
				}
			}
			String line = myScanner.nextLine();
			turnW = line.equals("true");
			myScanner.close();
			board.setBackgroundProvider(this::background);
			board.showMessage("Game loaded successfully");
		}
		catch (IOException e) {
			board.showMessage("Error loading the file: " + e.getMessage());
		}
	}	

	void save() {
		try {
			String fileName = board.promptText("File Name:");
			FileWriter fileWriter = new FileWriter(new File(fileName));

			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
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
			board.showMessage("Error saving the file: " + e.getMessage());
		}
	}

	//Used "trys" so it doesnt break the program when there is no more plays to be done
	void random() {
		int trys = 1000;
	
		while (trys-- > 0) {
			int line = (int) (Math.random() * 8);
			int col = (int) (Math.random() * 8);

			if (!sameTeam(line, col)) 
				continue;
			selected = new int[]{line, col};
			if (checkCaptures() && canCapture(line, col)) {
				for (int i = 0; i < 10000; i++) {
					int targetLine = (int)(Math.random() * 8);
					int targetCol = (int)(Math.random() * 8);
	
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
				int targetLine = (int)(Math.random() * 8);
				int targetCol = (int)(Math.random() * 8);

				if (!checkCaptures() && inBounds(targetLine, targetCol) && isValid(targetLine, targetCol)) {
					movePiece(targetLine, targetCol);
					return;
				}
			}
		}
	}
	
	//Checks if the move is valid checking if it is a dark square and if it is within the range without capture
	boolean isValid(int line, int col) {
		if (inBounds(line, col) && game[line][col] == null && isDark(line, col) && moveForw(line, col))
			return true;
		return false;
	}

	boolean inBounds(int line, int col) {
		if (line >= 0 && line < 8 && col >= 0 && col < 8)
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
	
	//Click
	void click(int line, int col) {
		if (selected == null || sameTeam(line, col)) {
			pieceSelect(line, col);
		} 
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
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
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
		for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++)
                game[i][j] = null;
        }
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 8; j++) {
				if (i % 2 == 0 && j % 2 != 0)
					game[i][j] = "black.png";
				if (i % 2 != 0 && j % 2 == 0)
					game[i][j] = "black.png";
			}
		}
		for(int i = 7; i > 4; i--) {
			for(int j = 0; j < 8; j++) {
				if (i % 2 == 0 && j % 2 != 0)
					game[i][j] = "white.png";
				if (i % 2 != 0 && j % 2 == 0)
					game[i][j] = "white.png";
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
        Checkers gui = new Checkers();
        gui.start();
    }

	public static void main(String[] args) {
		Checkers gui = new Checkers();
		gui.start();
	}
}
