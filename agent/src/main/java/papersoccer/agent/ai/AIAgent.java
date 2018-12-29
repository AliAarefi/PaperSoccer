package papersoccer.agent.ai;

import papersoccer.agent.AgentAPI;

import java.io.IOException;

public class AIAgent {
	private AgentAPI api;

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

	private int[] getValidMoves() {
		int[][] board = api.board;
		// TODO
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
