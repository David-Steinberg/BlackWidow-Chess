package com.chess.david.utils.tests;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.chess.david.utils.PieceUtils;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.BoardUtils;
import com.chess.engine.classic.pieces.Piece;
import com.chess.pgn.FenUtilities;

public class TestPieceUtils {

	@Test
	public void testLegalMovesStartingPosition() {
		Board board = Board.createStandardBoard();
		Piece a2Pawn = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("a2"));
		assertEquals(2,
				PieceUtils.getLegalMovesAccountingForCheck(board, a2Pawn).size());
	}
	
	@Test
	public void testLegalMovesWhileInCheck() {
		Board board = FenUtilities.createGameFromFEN("k3r3/8/8/8/5B2/8/8/4K3 w - - 0 1");
		Piece f4Bishop = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("f4"));
		assertEquals(2, PieceUtils.getLegalMovesAccountingForCheck(board, f4Bishop).size());
	}

	@Test
	public void testCaptureTargetsStartingPosition() {
		Board board = Board.createStandardBoard();
		Piece a2Pawn = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("a2"));
		Collection<Piece> captureTargets = PieceUtils.getCaptureTargetsForPiece(board, a2Pawn);
		assertEquals(0, captureTargets.size());
	}

	@Test
	public void testCaptureTargetsKnight() {
		Board board = FenUtilities.createGameFromFEN("4k3/3ppp2/8/3q1r2/6b1/4N3/2P3P1/4K3 w - - 0 1");
		Piece e3Knight = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("e3"));
		Collection<Piece> captureTargets = PieceUtils.getCaptureTargetsForPiece(board, e3Knight);
		assertEquals(3, captureTargets.size());
		assertTrue(captureTargets.contains(board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("d5"))));
		assertTrue(captureTargets.contains(board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("f5"))));
		assertTrue(captureTargets.contains(board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("g4"))));
	}

	@Test
	public void testPinnedStartingPosition() {
		Board board = Board.createStandardBoard();
		Piece a2Pawn = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("a2"));
		assertFalse(PieceUtils.isPinned(board, a2Pawn));
	}

	@Test
	public void testPinnedBishopOnFile() {
		Board board = FenUtilities.createGameFromFEN("4k3/8/4r3/8/8/4B3/8/4K3 w - - 0 1");
		Piece e3Bishop = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("e3"));
		assertTrue(PieceUtils.isPinned(board, e3Bishop));
	}

	@Test
	public void testPinDoesntWrapToNextRank() {
		Board board = FenUtilities.createGameFromFEN("4k3/8/5r1B/K7/8/8/8/8 w - - 0 1");
		Piece h6Bishop = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("h6"));
		assertFalse(PieceUtils.isPinned(board, h6Bishop));
	}
	
	@Test
	public void testIsHangingStartingPosition() {
		Board board = Board.createStandardBoard();
		Piece a2Pawn = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("a2"));
		assertFalse(PieceUtils.isHanging(board, a2Pawn));
	}
	
	@Test
	public void testHangingBishop() {
		Board board = FenUtilities.createGameFromFEN("rnbqkbnr/pppppppp/6B1/8/8/8/PPPPPPPP/RNBQK1NR w KQkq - 0 1");
		Piece g6Bishop = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("g6"));
		assertTrue(PieceUtils.isHanging(board, g6Bishop));
	}
	
	@Test
	public void testDefendedBishop() {
		Board board = FenUtilities.createGameFromFEN("rnbqkbnr/pppppppp/6B1/7P/8/8/PPPPPPP1/RNBQK1NR w KQkq - 0 1");
		Piece g6Bishop = board.getPiece(BoardUtils.INSTANCE.getCoordinateAtPosition("g6"));
		assertFalse(PieceUtils.isHanging(board, g6Bishop));
	}

}
