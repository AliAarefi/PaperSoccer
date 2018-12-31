package papersoccer.agent.ai;

class GameState {
	GameState parent, bestChild;
	int benefit;
	int[][] board;
	int ballPosition;

	GameState(int[][] board, int ballPosition) {
		this.board = board;
		this.ballPosition = ballPosition;
	}

	GameState nextState(int dest) {
		int[][] newBoard = new int[board.length][board[0].length];
		for (int i = 0; i < board.length; ++i) System.arraycopy(board[i], 0, newBoard[i], 0, board[i].length);
		newBoard[ballPosition][dest] = newBoard[dest][ballPosition] = 1;
		GameState child = new GameState(newBoard, dest);
		child.parent = this;
		return child;
	}

	boolean hasBonus() {
		for (int i = 0; i < board.length; ++i) {
			if (ballPosition == i) continue;
			if (board[i][ballPosition] == 1) return true;
		}
		return false;
	}
}
