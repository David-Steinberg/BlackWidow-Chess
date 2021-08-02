package com.chess.david.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.classic.Alliance;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.pieces.Piece;
import com.chess.engine.classic.player.ai.StockAlphaBeta;

public class PositionUtils {

	/*
	 * I implemented "get a vector of threats in the position" in
	 * com.chess.engine.classic.player.ai.StockAlphaBeta.executeBestN. I also
	 * updated the Move class to include a Score field, and updated
	 * StockAlphaBeta.execute to set a value for that field
	 * (StockAlphaBeta.executeBestN does this too).
	 */

	/*
	 * This method returns a list of up to maxThreats threats, defined as moves for
	 * the player not-to-move that would gain at least threatThreshold centipawns
	 * for that player.
	 */
	public static Collection<Move> getThreatsForOpposingPlayer(Board board, int depth, int maxThreats,
			int threatThreshold) {
		StockAlphaBeta alphaBetaCurrent = new StockAlphaBeta(depth);
		int currentEval = alphaBetaCurrent.execute(board).getScore();
		Board opposingPlayerToMove = swapActivePlayer(board);
		// I think active player making a null move means the engine should look ahead
		// one fewer ply to keep the evaluations similar, but I'm not confident.
		StockAlphaBeta alphaBetaOpposing = new StockAlphaBeta(depth - 1);
		Collection<Move> candidateThreats = alphaBetaOpposing.executeBestN(opposingPlayerToMove, maxThreats);
		List<Move> threats = new ArrayList<Move>();
		for (Move move : candidateThreats) {
			int evalDiff = board.currentPlayer().getAlliance().isWhite() ? currentEval - move.getScore()
					: move.getScore() - currentEval;
			if (evalDiff >= threatThreshold) {
				threats.add(move);
			}
		}
		return threats;
	}

	public static Board swapActivePlayer(Board board) {
		Board.Builder builder = new Board.Builder();
		for (Piece piece : board.getAllPieces()) {
			builder.setPiece(piece);
		}
		builder.setEnPassantPawn(board.getEnPassantPawn());
		builder.setMoveMaker(board.currentPlayer().getAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
		builder.setMoveTransition(board.getTransitionMove());
		return builder.build();
	}

}
