package com.chess.david.utils;

import java.util.Collection;

import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.board.Move.MoveStatus;
import com.chess.engine.classic.board.MoveTransition;
import com.chess.engine.classic.player.ai.StockAlphaBeta;

public class MoveUtils {

	public static int centipawnLoss(Board board, Move move, int depth) {
		StockAlphaBeta alphaBeta = new StockAlphaBeta(depth);
		int currentEval = alphaBeta.execute(board).getScore();
		Board boardAfterMove = board.currentPlayer().makeMove(move).getToBoard();
		StockAlphaBeta alphaBetaLessDepth = new StockAlphaBeta(depth - 1);
		int evalAfterMove = alphaBetaLessDepth.execute(boardAfterMove).getScore();
		int evalDiff = board.currentPlayer().getAlliance().isWhite() ? currentEval - evalAfterMove
				: evalAfterMove - currentEval;
		return evalDiff;
	}

	public static boolean capturesHangingPiece(Board board, Move move) {
		return (move.isAttack() && PieceUtils.isHanging(board, move.getAttackedPiece()));
	}

	/*
	 * You might ask “does this move permit the opponent to play a good move”
	 * because you want to know how this move compares to the best move. So I’m
	 * going to say: in position P, move A permits move B if active player is in a
	 * worse position after A+B than they were in P, and also B is a bad move (or
	 * illegal) in response to the best move in P.
	 */
	public static boolean permitsGoodMove(Board board, Move move, int depth, int candidates,
			int centipawnLossThreshold, int responseAlreadyPlayableThreshold) {
		StockAlphaBeta alphaBeta = new StockAlphaBeta(depth);
		Move bestMove = alphaBeta.execute(board);
		if (move.equals(bestMove)) {
			return false;
		}
		int initialScore = bestMove.getScore();
		Board boardAfterGivenMove = board.currentPlayer().makeMove(move).getToBoard();
		Board boardAfterBestMove = board.currentPlayer().makeMove(bestMove).getToBoard();
		StockAlphaBeta alphaBetaAfterGiven = new StockAlphaBeta(depth - 1);
		Collection<Move> responseCandidates = alphaBetaAfterGiven.executeBestN(boardAfterGivenMove, candidates);
		for (Move response : responseCandidates) {
			int responseEvalDiff = board.currentPlayer().getAlliance().isWhite() ? initialScore - response.getScore()
					: response.getScore() - initialScore;
			if (responseEvalDiff > centipawnLossThreshold) {
				Move responseToBestMove = Move.MoveFactory.createMove(boardAfterBestMove,
						response.getCurrentCoordinate(), response.getDestinationCoordinate());
				MoveTransition transition = boardAfterBestMove.currentPlayer().makeMove(responseToBestMove);
				if (transition.getMoveStatus() != MoveStatus.DONE) {
					return true;
				}
				StockAlphaBeta alphaBetaResponse = new StockAlphaBeta(depth - 2);
				int responseToBestMoveEval = alphaBetaResponse.execute(transition.getToBoard()).getScore();
				int responseToBestMoveEvalDiff = board.currentPlayer().getAlliance().isWhite()
						? responseToBestMoveEval - initialScore
						: initialScore - responseToBestMoveEval;
				if (responseToBestMoveEvalDiff>responseAlreadyPlayableThreshold) {
					return true;
				}
			}
		}
		return false;

	}

}
