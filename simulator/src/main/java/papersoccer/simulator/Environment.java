package papersoccer.simulator;

import papersoccer.common.ServerMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Environment {
	private int width, height;
	private int[][] board;
	private int ballPosition;
	private boolean finished;
	private String turn;
	private String worldCache = null;

	public Environment(int x, int y) {
		if ((x | y) % 2 == 1) return;  // Both x and y have to be even (number of squares in rows & columns)
		height = x + 1;
		width = y + 1;
	}

	void clear() {
		finished = false;
		board = new int[width * height][width * height];
		turn = ServerMessage.turn_of_bottom_player;
		worldCache = null;

		// Filling board edges in adjacency matrix, exploration based on upper triangular matrix but filling all cells
		for (int i = 0; i < width * height; ++i)
			for (int j = i; j < width * height; ++j) {
				if (j - i == width && i % width == 0) {  // left edge
					board[i][j] = board[j][i] = 1;
				} else if (j - i == width && i % width == width - 1) {  // right edge
					board[i][j] = board[j][i] = 1;
				} else if (j - i == 1 && j < width) {  // top edge
					if (j == width / 2 || j == width / 2 + 1)
						continue;
					board[i][j] = board[j][i] = 1;
				} else if (j - i == 1 && j > width * (height - 1)) {  // bottom edge
					if (j == width * (height - 1) + width / 2 || j == width * (height - 1) + width / 2 + 1)
						continue;
					board[i][j] = board[j][i] = 1;
				}
			}

		ballPosition = (height / 2) * width + (width / 2);
	}

	boolean doAction(int source, int destination) {
		if (finished || source != ballPosition || source == destination)
			return false;

		// to handle goal nodes:
		int[] centerOfLineOfGoals = {width / 2, width * (height - 1) + width / 2};
		if (source == centerOfLineOfGoals[0]) {
			if (destination < 0 && destination > -4) {  // goal nodes for top goal : -1, -2, -3
				finishTheGame();
				return true;
			}
		} else if (source == centerOfLineOfGoals[1]) {
			if (destination < -3 && destination > -7) {  // goal nodes for bottom goal : -4, -5, -6
				finishTheGame();
				return true;
			}
		} else if (source == centerOfLineOfGoals[0] - 1 || source == centerOfLineOfGoals[0] + 1) {
			if (destination == -2) {
				finishTheGame();
				return true;
			}
		} else if (source == centerOfLineOfGoals[1] - 1 || source == centerOfLineOfGoals[1] + 1) {
			if (destination == -5) {
				finishTheGame();
				return true;
			}
		} else if (destination < 0 || destination > height * width - 1)  // out of range
			return false;

		// to handle corners:
		int[] corners = {0, width - 1, width * (height - 1), width * height - 1};
		int[] legalSourceForEachCorner = {width + 1, 2 * width, width * (height - 2) + 1, width * height - width - 2};
		for (int i = 0; i < corners.length; ++i) {
			// if source placed on one of corners
			if (source == corners[i])
				return false;
			// if destination placed on one of corners
			if (destination == corners[i]) {
				if (source == legalSourceForEachCorner[i]) {
					board[source][destination] = board[destination][source] = 1;
					worldCache = null;
					ballPosition = destination;
					finishTheGame();
					return true;
				} else return false;
			}
		}

		// to handle edges and center nodes
		String sourceArea;
		if (source > 0 && source < width - 1)  // top edge nodes
			sourceArea = "top";
		else if (source % width == 0 && source != (height - 1) * width)  // left edge nodes
			sourceArea = "left";
		else if (source % width == width - 1 && source != width - 1 && source != height * width - 1)  // right edge nodes
			sourceArea = "right";
		else if (source > (height - 1) * width && source < height * width - 1)  // bottom edge nodes
			sourceArea = "bottom";
		else  // center nodes
			sourceArea = "center";
		if (checkMovementValidity(source, destination, sourceArea)) {
			board[source][destination] = board[destination][source] = 1;
			worldCache = null;
			ballPosition = destination;
			setTurn();
			return true;
		}
		return false;
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
		return board[source][destination] == 0 && legals.contains(destination);
	}

	int calculateDegreeOfNode(int nodeID) {
		return Arrays.stream(board[nodeID]).parallel().sum();
	}

	String convertToString() {
		if (worldCache == null) {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < width * height; ++i)
				result.append(IntStream.of(board[i])
						.mapToObj(Integer::toString)
						.collect(Collectors.joining(" "))).append('\n');
			worldCache = result.toString().trim();
		}
		return worldCache;
	}

	int getBallPosition() {
		return ballPosition;
	}

	String getTurn() {
		return turn;
	}

	private String getAnotherPlayerTurn() {
		if (Objects.equals(this.turn, ServerMessage.turn_of_bottom_player))
			return ServerMessage.turn_of_upper_player;
		return ServerMessage.turn_of_bottom_player;
	}

	private void finishTheGame() {
		turn = ServerMessage.game_finished;
		finished = true;
	}

	private void setTurn() {
		int degree = calculateDegreeOfNode(ballPosition);
		if (degree == 8 || (degree == 5 && isDivari(ballPosition)))
			turn = ServerMessage.game_finished;
		else if (degree < 2)
			turn = getAnotherPlayerTurn();
	}

	private boolean isDivari(int position) {
		int[] centerOfLineOfGoals = {width / 2, width * (height - 1) + width / 2};
		if (position == centerOfLineOfGoals[0] || position == centerOfLineOfGoals[1])  // exceptions
			return false;
		if (position % width == 0)  // on left edge
			return true;
		if (position % width == width - 1)  // on right edge
			return true;
		if (position > (height - 1) * width && position < height * width - 1)  // on bottom edge
			return true;
		return position > 0 && position < width - 1;  // on top edge
	}

	public String getDimensions() {
		return height + " " + width;
	}
}
