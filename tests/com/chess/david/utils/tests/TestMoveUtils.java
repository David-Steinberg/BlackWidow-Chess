package com.chess.david.utils.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.chess.david.utils.MoveUtils;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.BoardUtils;
import com.chess.engine.classic.board.Move;
import com.chess.pgn.FenUtilities;

public class TestMoveUtils {

	@Test
	public void testCentipawnLossStartingPosition() {
		Board board = Board.createStandardBoard();
		Move d4 = Move.MoveFactory.createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("d2"),
				BoardUtils.INSTANCE.getCoordinateAtPosition("d4"));
		assertFalse(MoveUtils.centipawnLoss(board, d4, 5) > 30);
	}
	
	@Test
	public void testCentipawnLossUnpin() {
		Board board = FenUtilities.createGameFromFEN("rnbqk2r/ppppbppp/5n2/8/2B5/Q4N2/PPPP1PPP/RNB1R1K1 w Qkq - 0 1");
		Move rf1 = Move.MoveFactory.createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("e1"),
				BoardUtils.INSTANCE.getCoordinateAtPosition("f1"));
		assertTrue(MoveUtils.centipawnLoss(board, rf1, 5) > 500);
	}

	@Test
	public void testPermitsGoodMoveStartingPosition() {
		Board board = Board.createStandardBoard();
		Move e4 = Move.MoveFactory.createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("e2"),
				BoardUtils.INSTANCE.getCoordinateAtPosition("e4"));
		assertFalse(MoveUtils.permitsGoodMove(board, e4, 5, 5, 100, 50));
		Move d4 = Move.MoveFactory.createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("d2"),
				BoardUtils.INSTANCE.getCoordinateAtPosition("d4"));
		assertFalse(MoveUtils.permitsGoodMove(board, d4, 5, 5, 100, 50));
	}

	@Test
	public void testPermitsGoodMoveUnpin() {
		Board board = FenUtilities.createGameFromFEN("rnbqk2r/ppppbppp/5n2/8/2B5/Q4N2/PPPP1PPP/RNB1R1K1 w Qkq - 0 1");
		Move rf1 = Move.MoveFactory.createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("e1"),
				BoardUtils.INSTANCE.getCoordinateAtPosition("f1"));
		assertTrue(MoveUtils.permitsGoodMove(board, rf1, 5, 5, 100, 50));
	}
	
	@Test
	public void testCapturesHangingPieceStartingPosition() {
		Board board = Board.createStandardBoard();
		Move d4 = Move.MoveFactory.createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("d2"),
				BoardUtils.INSTANCE.getCoordinateAtPosition("d4"));
		assertFalse(MoveUtils.capturesHangingPiece(board, d4));
	}
	
	@Test
	public void testCapturesHangingBishop() {
		Board board = FenUtilities.createGameFromFEN("rnbqkbnr/pppppppp/6B1/8/8/8/PPPPPPPP/RNBQK1NR b KQkq - 0 1");
		Move hxg6 = Move.MoveFactory.createMove(board, BoardUtils.INSTANCE.getCoordinateAtPosition("h7"),
				BoardUtils.INSTANCE.getCoordinateAtPosition("g6"));
		assertTrue(MoveUtils.capturesHangingPiece(board, hxg6));
	}

}
