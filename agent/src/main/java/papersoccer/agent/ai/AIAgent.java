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

	private void act() {
//		int heuristic = calculateHeuristic();

		// TODO

//		api.setDecision();
	}
}
