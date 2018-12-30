package papersoccer.agent.ai;

import papersoccer.agent.AgentAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class AIAgent {
	private AgentAPI api;
	static final int width = 9;  // paper soccer specific
	static final int height = 11;  // paper soccer specific

	private AIAgent() {
		try {
			api = new AgentAPI((obj, oldValue, newValue) -> {
				switch (newValue) {
					case YOUR_TURN:
						act();
						break;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Integer[] getValidMoves() {
		TreeSet<Integer> moves = new TreeSet<>();
		for (int i = -6; i < api.board.length; i++)
			if (isValid(api.ballPosition, i))
				moves.add(i);
		return moves.toArray(new Integer[0]);
	}

	boolean isValid(int source, int destination) {
		if (source == destination)
			return false;

		// to handle goal nodes:
		int[] centerOfLineOfGoals = {width / 2, width * (height - 1) + width / 2};
		if (source == centerOfLineOfGoals[0]) {
			if (destination < 0 && destination > -4) {  // goal nodes for top goal : -1, -2, -3
				return true;
			}
		} else if (source == centerOfLineOfGoals[1]) {
			if (destination < -3 && destination > -7) {  // goal nodes for bottom goal : -4, -5, -6
				return true;
			}
		} else if (source == centerOfLineOfGoals[0] - 1 || source == centerOfLineOfGoals[0] + 1) {
			if (destination == -2) {
				return true;
			}
		} else if (source == centerOfLineOfGoals[1] - 1 || source == centerOfLineOfGoals[1] + 1) {
			if (destination == -5) {
				return true;
			}
		}

		// to handle corners:
		int[] corners = {0, width - 1, width * (height - 1), width * height - 1};
		int[] legalSourceForEachCorner = {width + 1, 2 * width, width * (height - 2) + 1, width * height - width - 2};
		for (int i = 0; i < corners.length; i++) {
			// if source placed on one of corners
			if (source == corners[i])
				return false;
			// if destination placed on one of corners
			if (destination == corners[i]) {
				return source == legalSourceForEachCorner[i];
			}
		}

		// to handle edges and center nodes
		return checkMovementValidity(source, destination, findSourceArea(source));
	}

	private String findSourceArea(int dot) {
		if (dot > 0 && dot < width - 1)  // top edge nodes
			return "top";
		else if (dot % width == 0 && dot != (height - 1) * width)  // left edge nodes
			return "left";
		else if (dot % width == width - 1 && dot != width - 1 && dot != height * width - 1)  // right edge nodes
			return "right";
		else if (dot > (height - 1) * width && dot < height * width - 1)  // bottom edge nodes
			return "bottom";
		else  // center nodes
			return "center";

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
		return api.board[source][destination] == 0 && legals.contains(destination);
	}

	private int calculateHeuristic() {
		int width = api.board.length, height = api.board[0].length, ballPosition = api.ballPosition;
		if (api.side == 2) return (height - (ballPosition / width)) + Math.abs(width - (ballPosition % width));
		else return (ballPosition / width) + Math.abs(width - (ballPosition % width));
	}

	private int max(int depth) {
		int heuristic = calculateHeuristic();
		if (depth == 0 || heuristic <= Integer.MIN_VALUE || heuristic >= Integer.MAX_VALUE) {
			// TODO return
		}

		int result = Integer.MIN_VALUE;
		// TODO iterate over valid actions
		return result;
	}

	private int min(int depth) {
		int heuristic = calculateHeuristic();
		// TODO same as max
		return 0;
	}

	private int minimax(int depth) {
		return max(depth);
	}

	private void act() {
		int dest = 1;  // TODO call minimax and find ball position

		api.setDecision(api.ballPosition, dest);
	}
}
