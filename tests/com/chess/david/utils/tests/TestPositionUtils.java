package com.chess.david.utils.tests;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.junit.Test;

import com.chess.david.utils.PositionUtils;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.BoardUtils;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.board.Move.MoveFactory;
import com.chess.engine.classic.board.Move.MoveStatus;
import com.chess.engine.classic.board.MoveTransition;
import com.chess.engine.classic.player.ai.MoveStrategy;
import com.chess.engine.classic.player.ai.StockAlphaBeta;
import com.chess.pgn.FenUtilities;

public class TestPositionUtils {
	

	//@Test
	public void testMultipleHangingPieces() {
		Board board = FenUtilities
				.createGameFromFEN("rnb1kbn1/pppppppp/8/7q/2r1P3/1QN3N1/PPPP1PPP/R1B1KB1R w KQq - 0 1");
		StockAlphaBeta alphaBeta = new StockAlphaBeta(5);
		Collection<Move> bestMoves = alphaBeta.executeBestN(board, 3);
		Move captureOne = Move.MoveFactory
                .createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("g3"), BoardUtils.INSTANCE.getCoordinateAtPosition("h5"));
		assertTrue(bestMoves.contains(captureOne));
		Move captureTwo = Move.MoveFactory
                .createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("f1"), BoardUtils.INSTANCE.getCoordinateAtPosition("c4"));
		assertTrue(bestMoves.contains(captureTwo));
		Move captureThree = Move.MoveFactory
                .createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("b3"), BoardUtils.INSTANCE.getCoordinateAtPosition("c4"));
		assertTrue(bestMoves.contains(captureThree));
	}
	
	@Test
	public void testMultipleHangingPiecesThreats() {
		Board board = FenUtilities
				.createGameFromFEN("rnb1kbn1/pppppppp/8/7q/2r1P3/1QN3N1/PPPP1PPP/R1B1KB1R b KQq - 0 1");
		Collection<Move> threats = PositionUtils.getThreatsForOpposingPlayer(board, 6, 3, 100);
		Move captureOne = Move.MoveFactory
                .createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("g3"), BoardUtils.INSTANCE.getCoordinateAtPosition("h5"));
		assertTrue(threats.contains(captureOne));
		Move captureTwo = Move.MoveFactory
                .createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("f1"), BoardUtils.INSTANCE.getCoordinateAtPosition("c4"));
		assertTrue(threats.contains(captureTwo));
		Move captureThree = Move.MoveFactory
                .createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("b3"), BoardUtils.INSTANCE.getCoordinateAtPosition("c4"));
		assertTrue(threats.contains(captureThree));
	}

	//@Test
	public void testVectorMatchesNormalOutput1() {
		final Board board = FenUtilities.createGameFromFEN("4k2r/1R3R2/p3p1pp/4b3/1BnNr3/8/P1P5/5K2 w - - 1 0");
		final StockAlphaBeta alphaBeta = new StockAlphaBeta(7);
		final Collection<Move> bestMoves = alphaBeta.executeBestN(board, 1);
		assertEquals(bestMoves.iterator().next(), Move.MoveFactory.createMove(board,
				BoardUtils.INSTANCE.getCoordinateAtPosition("f7"), BoardUtils.INSTANCE.getCoordinateAtPosition("e7")));
	}

	//@Test
	public void testVectorMatchesNormalOutput2() {
		final Board board = FenUtilities
				.createGameFromFEN("6k1/3b3r/1p1p4/p1n2p2/1PPNpP1q/P3Q1p1/1R1RB1P1/5K2 b - - 0-1");
		final StockAlphaBeta alphaBeta = new StockAlphaBeta(6);
		final Collection<Move> bestMoves = alphaBeta.executeBestN(board, 1);
		assertEquals(bestMoves.iterator().next(), Move.MoveFactory.createMove(board,
				BoardUtils.INSTANCE.getCoordinateAtPosition("h4"), BoardUtils.INSTANCE.getCoordinateAtPosition("f4")));
	}
	
	//@Test
	public void testSwapActivePlayer() {
		final Board board = Board.createStandardBoard();
		final Board standardSwapped = PositionUtils.swapActivePlayer(board);
		assertTrue(standardSwapped.currentPlayer().getAlliance().isBlack());
	}

}
