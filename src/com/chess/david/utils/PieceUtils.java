package com.chess.david.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.chess.engine.classic.Alliance;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.BoardUtils;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.board.Move.MajorAttackMove;
import com.chess.engine.classic.board.Move.MajorMove;
import com.chess.engine.classic.board.Move.MoveStatus;
import com.chess.engine.classic.board.MoveTransition;
import com.chess.engine.classic.pieces.Piece;
import com.chess.engine.classic.pieces.Piece.PieceType;
import com.chess.engine.classic.player.Player;

public class PieceUtils {

	/*
	 * "How many legal moves does a piece have" was already implemented, as
	 * piece.calculateLegalMoves(board), but that method doesn't care about check.
	 * I've implemented a method that does care about check and switched back and
	 * forth depending on what definition is useful.
	 */

	public static Collection<Move> getLegalMovesAccountingForCheck(Board board, Piece piece) {
		Collection<Move> candidateMoves = piece.calculateLegalMoves(board);
		Player pieceOwner = piece.getPieceAllegiance().isWhite() ? board.whitePlayer() : board.blackPlayer();
		Collection<Move> legalMoves = new ArrayList<Move>();
		for (Move move : candidateMoves) {
			if (pieceOwner.makeMove(move).getMoveStatus() == MoveStatus.DONE) {
				legalMoves.add(move);
			}
		}
		return legalMoves;
	}

	public static Collection<Piece> getCaptureTargetsForPiece(Board board, Piece piece) {
		Collection<Piece> captureTargets = new HashSet<Piece>();
		for (Move move : piece.calculateLegalMoves(board)) {
			if (move.isAttack()) {
				captureTargets.add(move.getAttackedPiece());
			}
		}
		return captureTargets;
	}

	public static Collection<Move> getLegalMovesForCaptureTargets(Board board, Piece piece) {
		Collection<Piece> captureTargets = getCaptureTargetsForPiece(board, piece);
		Collection<Move> movesForTargets = new ArrayList<Move>();
		for (Piece captureTarget : captureTargets) {
			movesForTargets.addAll(getLegalMovesAccountingForCheck(board, captureTarget));
		}
		return movesForTargets;

	}

	/*
	 * I'm using the following definition: Piece A is pinned if, were it not in the
	 * way, an opposing Piece B could capture an allied Piece C that is of higher
	 * value than Piece B. This capture not be the best move, or it may not be legal
	 * due to check, but I would still colloquially say Piece A is pinned under
	 * those circumstances and I wanted to capture that in code.
	 */
	public static boolean isPinned(Board board, Piece piece) {
		Collection<Piece> opposingPieces = piece.getPieceAllegiance().isWhite() ? board.getBlackPieces()
				: board.getWhitePieces();
		for (Piece opposingPiece : opposingPieces) {
			PieceType opposingPieceType = opposingPiece.getPieceType();
			switch (opposingPieceType) {
			case KNIGHT, PAWN, KING -> {
				// these pieces can't create pins
				break;
			}

			case ROOK, BISHOP, QUEEN -> {
				if (isPinnedByOpposingPiece(board, piece, opposingPiece)) {
					return true;
				}
				break;
			}
			}
		}
		return false;

	}

	/*
	 * As with pinning, I'm using a colloquial definition - a piece is hanging if it
	 * can be captured and it isn't defended (so, if the capturing piece can't be
	 * recaptured).
	 */
	public static boolean isHanging(Board board, Piece piece) {
		Player opposingPlayer = piece.getPieceAllegiance().isBlack() ? board.whitePlayer() : board.blackPlayer();
		for (Move move : opposingPlayer.getLegalMoves()) {
			if (move.getDestinationCoordinate() == piece.getPiecePosition()) {
				Board postCapture = opposingPlayer.makeMove(move).getToBoard();
				Player pieceOwner = piece.getPieceAllegiance().isWhite() ? postCapture.whitePlayer()
						: postCapture.blackPlayer();
				for (Move responseMove : pieceOwner.getLegalMoves()) {
					if (responseMove.getDestinationCoordinate() == piece.getPiecePosition()) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * The movement code here (after the offset calculation) and the two
	 * ColumnExclusion methods are copied from
	 * com.chess.engine.classic.pieces.queen. The square:int mapping used is a8=0,
	 * h1=63.
	 */
	private static boolean isPinnedByOpposingPiece(Board board, Piece piece, Piece opposingPiece) {
		Collection<Move> opposingPieceMoves = opposingPiece.calculateLegalMoves(board);
		for (Move move : opposingPieceMoves) {
			if (move.getDestinationCoordinate() == piece.getPiecePosition()) {
				// Determine which int offset represents the rank/file/diagonal of the potential
				// pin
				int movementOffset = calculateCaptureOffset(opposingPiece.getPiecePosition(), piece.getPiecePosition());
				int candidateDestinationCoordinate = piece.getPiecePosition();
				while (true) {
					if (isFirstColumnExclusion(movementOffset, candidateDestinationCoordinate)
							|| isEightColumnExclusion(movementOffset, candidateDestinationCoordinate)) {
						return false;
					}
					candidateDestinationCoordinate += movementOffset;
					if (!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
						break;
					} else {
						final Piece pieceAtDestination = board.getPiece(candidateDestinationCoordinate);
						if (pieceAtDestination != null) {
							final Alliance pieceAtDestinationAllegiance = pieceAtDestination.getPieceAllegiance();
							if (opposingPiece.getPieceAllegiance() != pieceAtDestinationAllegiance
									&& opposingPiece.getPieceValue() < pieceAtDestination.getPieceValue()) {
								return true;
							}
							break;
						}
					}
				}
			}
		}
		return false;
	}

	/*
	 * Note that this method doesn't need to know what type of piece is capturing -
	 * we've already determined that it can legally capture capturedPiece, and the
	 * pair of squares that the two pieces are on specifies a unique line as a
	 * capture path. (Not considering knights, because they can't pin.)
	 */ private static int calculateCaptureOffset(int capturingPiece, int capturedPiece) {
		int diff = capturedPiece - capturingPiece;
		int diffSign = diff > 0 ? 1 : -1;
		if (diff < 7 && diff > -7) {
			return diffSign * 1;
		}
		if (diff == 7) {
			if (capturedPiece % 8 == 7) {
				return 1;
			} else {
				return 7;
			}
		}
		if (diff == -7) {
			if (capturedPiece % 8 == 0) {
				return -1;
			} else {
				return -7;
			}
		}
		if (diff % 9 == 0) {
			return diffSign * 9;
		}
		if (diff % 8 == 0) {
			return diffSign * 8;
		}
		if (diff % 7 == 0) {
			return diffSign * 7;
		}
		throw new RuntimeException();
	}

	private static boolean isFirstColumnExclusion(final int currentPosition, final int candidatePosition) {
		return BoardUtils.INSTANCE.FIRST_COLUMN.get(candidatePosition)
				&& ((currentPosition == -9) || (currentPosition == -1) || (currentPosition == 7));
	}

	private static boolean isEightColumnExclusion(final int currentPosition, final int candidatePosition) {
		return BoardUtils.INSTANCE.EIGHTH_COLUMN.get(candidatePosition)
				&& ((currentPosition == -7) || (currentPosition == 1) || (currentPosition == 9));
	}
}
