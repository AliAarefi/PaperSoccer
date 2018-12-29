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

	private int calculateHeuristic() {
		int width = api.board.length, height = api.board[0].length, ballPosition = api.ballPosition;
		if (api.side == 2) return (height - (ballPosition / width)) + Math.abs(width - (ballPosition % width));
		else return (ballPosition / width) + Math.abs(width - (ballPosition % width));
	}

	private void act() {
//		int heuristic = calculateHeuristic();

		// TODO

//		api.setDecision();
	}
}
