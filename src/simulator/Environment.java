package simulator;

import java.util.ArrayList;
import java.util.Arrays;

public class Environment {
	private int width, height;
	private int board[][];
	private int ballPosition;
	private boolean finished;

	public Environment(int x, int y) {
		if ((x | y) % 2 == 1) return;  // Both x and y have to be even (number of squares in rows & columns)
		height = x + 1;
		width = y + 1;
	}

	void clear() {
		finished = false;
		board = new int[width * height][width * height];

		// filling board edges in adjacency matrix, exploration based on upper triangular matrix but filling all cells
		for (int i = 0; i < width * height; i++)
			for (int j = i; j < width * height; j++) {
				if (j - i == width && i % width == 0) {  // left edge
					board[i][j] = 1;
					board[j][i] = 1;
				} else if (j - i == width && i % width == width - 1) {  // right edge
					board[i][j] = 1;
					board[j][i] = 1;
				} else if (j - i == 1 && j < width) {  // top edge
					if (j == width / 2 || j == width / 2 + 1)
						continue;
					board[i][j] = 1;
					board[j][i] = 1;
				} else if (j - i == 1 && j > width * (height - 1)) {  // bottom edge
					if (j == width * (height - 1) + width / 2 || j == width * (height - 1) + width / 2 + 1)
						continue;
					board[i][j] = 1;
					board[j][i] = 1;
				}
			}

		ballPosition = (height / 2) * width + (width / 2);
	}

	boolean doAction(int source, int destination) {
		if (finished)
			return false;
		if (source != ballPosition)
			return false;
		if (source == destination)
			return false;

		// to handle goal nodes:
		int centerOfLineOfGoals[] = {width / 2, width * (height - 1) + width / 2};
		if (source == centerOfLineOfGoals[0]) {
			if (destination < 0 && destination > -4) {  // goal nodes for top goal : -1, -2, -3
				finished = true;
				return true;
			}
		} else if (source == centerOfLineOfGoals[1]) {
			if (destination < -3 && destination > -7) {  // goal nodes for bottom goal : -4, -5, -6
				finished = true;
				return true;
			}
		} else if (source == centerOfLineOfGoals[0] - 1 || source == centerOfLineOfGoals[0] + 1) {
			if (destination == -2) {
				finished = true;
				return true;
			}
		} else if (source == centerOfLineOfGoals[1] - 1 || source == centerOfLineOfGoals[1] + 1) {
			if (destination == -5) {
				finished = true;
				return true;
			}
		}

		if (destination < 0 || destination > height * width - 1)  // out of range (goal nodes excepted before)
			return false;

		// to handle corners:
		if (source == 0) {  // top left corner
			if (destination == source + width + 1 && board[source][destination] == 0) {
				board[source][destination] = 1;
				ballPosition = destination;
				return true;
			} else
				return false;
		} else if (source == width - 1) {  // top right corner
			if (destination == source + width + 1 && board[source][destination] == 0) {
				board[source][destination] = 1;
				ballPosition = destination;
				return true;
			} else
				return false;
		} else if (source == width * (height - 1)) {  // bottom left corner
			if (destination == source - width + 1 && board[source][destination] == 0) {
				board[source][destination] = 1;
				ballPosition = destination;
				return true;
			} else
				return false;

		} else if (source == width * height - 1) {  // bottom right corner
			if (destination == source - width - 1 && board[source][destination] == 0) {
				board[source][destination] = 1;
				ballPosition = destination;
				return true;
			} else
				return false;

		}

		// to handle edges and center nodes
		if (source > 0 && source < width - 1) {  // top edge nodes
			return checkMovementValidity(source, destination, "top");
		} else if (source % width == 0 && source != 0 && source != (height - 1) * width) {  // left edge nodes
			return checkMovementValidity(source, destination, "left");
		} else if (source % width == width - 1 && source != width - 1 && source != height * width - 1) {  // right edge nodes
			return checkMovementValidity(source, destination, "right");
		} else if (source > (height - 1) * width && source < height * width - 1) {  // bottom edge nodes
			return checkMovementValidity(source, destination, "bottom");
		} else {  // center nodes
			return checkMovementValidity(source, destination, "center");
		}
	}

	private boolean checkMovementValidity(int source, int destination, String sourceArea) {
		ArrayList<Integer> legals = new ArrayList<>();
		switch (sourceArea) {
			case "center":
				legals.addAll(Arrays.asList(source - width - 1, source - width, source - width + 1, source - 1,
						source + 1, source + width - 1, source + width, source + width + 1));
				break;
			case "left":
				legals.addAll(Arrays.asList(source - width + 1, source + 1, source + width + 1));
				break;
			case "right":
				legals.addAll(Arrays.asList(source - width - 1, source - 1, source + width - 1));
				break;
			case "top":
				legals.addAll(Arrays.asList(source + width - 1, source + width, source + width + 1));
				break;
			case "bottom":
				legals.addAll(Arrays.asList(source - width - 1, source - width, source - width + 1));
				break;
		}
		if (board[source][destination] == 0 && legals.contains(destination)) {
			board[source][destination] = 1;
			ballPosition = destination;
			return true;
		}
		return false;
	}

	int calculateDegreeOfNode(int nodeID) {
		int summation = 0;
		for (int i = 0; i < height * width; i++) {
			summation += board[nodeID][i];
		}
		return summation;
	}
}
