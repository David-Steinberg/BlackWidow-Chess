package com.chess.engine.classic.player.ai;

import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.BoardUtils;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.board.MoveTransition;
import com.chess.engine.classic.player.Player;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import java.util.Collection;
import java.util.Comparator;
import java.util.Observable;
import java.util.PriorityQueue;

import static com.chess.engine.classic.board.BoardUtils.mvvlva;
import static com.chess.engine.classic.board.Move.MoveFactory;

public class StockAlphaBeta extends Observable implements MoveStrategy {

	private final BoardEvaluator evaluator;
	private final int searchDepth;
	private long boardsEvaluated;
	private int quiescenceCount;
	private static final int MAX_QUIESCENCE = 5000 * 5;

	private enum MoveSorter {

		STANDARD {
			@Override
			Collection<Move> sort(final Collection<Move> moves) {
				return Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
						.compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
						.compare(mvvlva(move2), mvvlva(move1)).result()).immutableSortedCopy(moves);
			}
		},
		EXPENSIVE {
			@Override
			Collection<Move> sort(final Collection<Move> moves) {
				return Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
						.compareTrueFirst(BoardUtils.kingThreat(move1), BoardUtils.kingThreat(move2))
						.compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
						.compare(mvvlva(move2), mvvlva(move1)).result()).immutableSortedCopy(moves);
			}
		};

		abstract Collection<Move> sort(Collection<Move> moves);
	}

	public StockAlphaBeta(final int searchDepth) {
		this.evaluator = StandardBoardEvaluator.get();
		this.searchDepth = searchDepth;
		this.boardsEvaluated = 0;
		this.quiescenceCount = 0;
	}

	@Override
	public String toString() {
		return "StockAB";
	}

	@Override
	public long getNumBoardsEvaluated() {
		return this.boardsEvaluated;
	}

	@Override
	public Move execute(final Board board) {
		final long startTime = System.currentTimeMillis();
		final Player currentPlayer = board.currentPlayer();
		Move bestMove = MoveFactory.getNullMove();
		int highestSeenValue = Integer.MIN_VALUE;
		int lowestSeenValue = Integer.MAX_VALUE;
		int currentValue;
		System.out.println(board.currentPlayer() + " THINKING with depth = " + this.searchDepth);
		int moveCounter = 1;
		int numMoves = board.currentPlayer().getLegalMoves().size();
		for (final Move move : MoveSorter.EXPENSIVE.sort((board.currentPlayer().getLegalMoves()))) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			this.quiescenceCount = 0;
			final String s;
			if (moveTransition.getMoveStatus().isDone()) {
				final long candidateMoveStartTime = System.nanoTime();
				currentValue = currentPlayer.getAlliance().isWhite()
						? min(moveTransition.getToBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue)
						: max(moveTransition.getToBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue);
				if (currentPlayer.getAlliance().isWhite() && currentValue > highestSeenValue) {
					move.setScore(currentValue);
					highestSeenValue = currentValue;
					bestMove = move;
					if (moveTransition.getToBoard().blackPlayer().isInCheckMate()) {
						break;
					}
				} else if (currentPlayer.getAlliance().isBlack() && currentValue < lowestSeenValue) {
					move.setScore(currentValue);
					lowestSeenValue = currentValue;
					bestMove = move;
					if (moveTransition.getToBoard().whitePlayer().isInCheckMate()) {
						break;
					}
				}

				final String quiescenceInfo = " " + score(currentPlayer, highestSeenValue, lowestSeenValue) + " q: "
						+ this.quiescenceCount;
				s = "\t" + toString() + "(" + this.searchDepth + "), m: (" + moveCounter + "/" + numMoves + ") " + move
						+ ", best:  " + bestMove

						+ quiescenceInfo + ", t: " + calculateTimeTaken(candidateMoveStartTime, System.nanoTime());
			} else {
				s = "\t" + toString() + "(" + this.searchDepth + ")" + ", m: (" + moveCounter + "/" + numMoves + ") "
						+ move + " is illegal! best: " + bestMove;
			}
			System.out.println(s);
			setChanged();
			notifyObservers(s);
			moveCounter++;
		}

		final long executionTime = System.currentTimeMillis() - startTime;
		final String result = board.currentPlayer() + " SELECTS " + bestMove + " [#boards evaluated = "
				+ this.boardsEvaluated + " time taken = " + executionTime / 1000 + " rate = "
				+ (1000 * ((double) this.boardsEvaluated / executionTime));
		System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, rate = %.1f\n",
				board.currentPlayer(), bestMove, this.boardsEvaluated, executionTime,
				(1000 * ((double) this.boardsEvaluated / executionTime)));
		setChanged();
		notifyObservers(result);
		return bestMove;
	}

	public Collection<Move> executeBestN(final Board board, int n) {
		final long startTime = System.currentTimeMillis();
		final Player currentPlayer = board.currentPlayer();
		Comparator<Move> comparator = Comparator.comparing(Move::getScore);
		if (currentPlayer.getAlliance().isBlack()) {
			comparator = comparator.reversed();
		}
		PriorityQueue<Move> moveQueue = new PriorityQueue<Move>(comparator);
		Move bestMove = MoveFactory.getNullMove();
		int highestSeenValue = Integer.MIN_VALUE;
		int lowestSeenValue = Integer.MAX_VALUE;
		int lowestScoreInMaxHeap = Integer.MIN_VALUE;
		int highestScoreInMinHeap = Integer.MAX_VALUE;
		int currentValue;
		System.out.println(board.currentPlayer() + " THINKING with depth = " + this.searchDepth);
		int moveCounter = 1;
		int numMoves = board.currentPlayer().getLegalMoves().size();
		for (final Move move : MoveSorter.EXPENSIVE.sort((board.currentPlayer().getLegalMoves()))) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			this.quiescenceCount = 0;
			final String s;
			if (moveTransition.getMoveStatus().isDone()) {
				final long candidateMoveStartTime = System.nanoTime();
				currentValue = currentPlayer.getAlliance().isWhite()
						? min(moveTransition.getToBoard(), this.searchDepth - 1, lowestScoreInMaxHeap,
								highestScoreInMinHeap)
						: max(moveTransition.getToBoard(), this.searchDepth - 1, lowestScoreInMaxHeap,
								highestScoreInMinHeap);
				if (moveQueue.size() < n) {
					move.setScore(currentValue);
					moveQueue.add(move);
					if (currentPlayer.getAlliance().isWhite() && currentValue>highestSeenValue) {
						highestSeenValue=currentValue;
						bestMove=move;
					}
					if (currentPlayer.getAlliance().isBlack() && currentValue<lowestSeenValue) {
						lowestSeenValue=currentValue;
						bestMove=move;
					}
					if (moveQueue.size() == n) {
						if (currentPlayer.getAlliance().isWhite()) {
							lowestScoreInMaxHeap = moveQueue.peek().getScore();
						}
						else if (currentPlayer.getAlliance().isBlack()) {
							highestScoreInMinHeap = moveQueue.peek().getScore();
						}
					}
				}
				else {

				if (currentPlayer.getAlliance().isWhite() && currentValue > lowestScoreInMaxHeap) {
					move.setScore(currentValue);
					moveQueue.add(move);
					Move droppedMove = moveQueue.poll();
					System.out.println(
							"dropping move " + droppedMove + " with score " + String.valueOf(droppedMove.getScore())
									+ " for move " + move + " with score " + String.valueOf(currentValue));
					lowestScoreInMaxHeap = moveQueue.peek().getScore();
					if (currentValue > highestSeenValue) {
						bestMove = move;
						highestSeenValue = currentValue;
					}
				} else if (currentPlayer.getAlliance().isBlack() && currentValue < highestScoreInMinHeap) {
					move.setScore(currentValue);
					moveQueue.add(move);
					Move droppedMove = moveQueue.poll();
					System.out.println(
							"dropping move " + droppedMove + " with score " + String.valueOf(droppedMove.getScore())
									+ " for move " + move + " with score " + String.valueOf(currentValue));
					highestScoreInMinHeap = moveQueue.peek().getScore();
					if (currentValue < lowestSeenValue) {
						bestMove = move;
						lowestSeenValue = currentValue;

					}
				}
				}

				final String quiescenceInfo = " " + score(currentPlayer, highestSeenValue, lowestSeenValue)
						+ " q: " + this.quiescenceCount;
				s = "\t" + toString() + "(" + this.searchDepth + "), m: (" + moveCounter + "/" + numMoves + ") " + move
						+ ", best:  " + bestMove

						+ quiescenceInfo + ", t: " + calculateTimeTaken(candidateMoveStartTime, System.nanoTime());
			} else {
				s = "\t" + toString() + "(" + this.searchDepth + ")" + ", m: (" + moveCounter + "/" + numMoves + ") "
						+ move + " is illegal! best: " + bestMove;
			}
			System.out.println(s);
			setChanged();
			notifyObservers(s);
			moveCounter++;
		}

		final long executionTime = System.currentTimeMillis() - startTime;
		final String result = board.currentPlayer() + " SELECTS " + bestMove + " [#boards evaluated = "
				+ this.boardsEvaluated + " time taken = " + executionTime / 1000 + " rate = "
				+ (1000 * ((double) this.boardsEvaluated / executionTime));
		System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, rate = %.1f\n",
				board.currentPlayer(), bestMove, this.boardsEvaluated, executionTime,
				(1000 * ((double) this.boardsEvaluated / executionTime)));
		setChanged();
		notifyObservers(result);
		return moveQueue;
	}

	private static String score(final Player currentPlayer, final int highestSeenValue, final int lowestSeenValue) {

		if (currentPlayer.getAlliance().isWhite()) {
			return "[score: " + highestSeenValue + "]";
		} else if (currentPlayer.getAlliance().isBlack()) {
			return "[score: " + lowestSeenValue + "]";
		}
		throw new RuntimeException("bad bad boy!");
	}

	private int max(final Board board, final int depth, final int highest, final int lowest) {
		if (depth == 0 || BoardUtils.isEndGame(board)) {
			this.boardsEvaluated++;
			return this.evaluator.evaluate(board, depth);
		}
		int currentHighest = highest;
		for (final Move move : MoveSorter.STANDARD.sort((board.currentPlayer().getLegalMoves()))) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			if (moveTransition.getMoveStatus().isDone()) {
				final Board toBoard = moveTransition.getToBoard();
				currentHighest = Math.max(currentHighest,
						min(toBoard, calculateQuiescenceDepth(toBoard, depth), currentHighest, lowest));
				if (currentHighest >= lowest) {
					return lowest;
				}
			}
		}
		return currentHighest;
	}

	private int min(final Board board, final int depth, final int highest, final int lowest) {
		if (depth == 0 || BoardUtils.isEndGame(board)) {
			this.boardsEvaluated++;
			return this.evaluator.evaluate(board, depth);
		}
		int currentLowest = lowest;
		for (final Move move : MoveSorter.STANDARD.sort((board.currentPlayer().getLegalMoves()))) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			if (moveTransition.getMoveStatus().isDone()) {
				final Board toBoard = moveTransition.getToBoard();
				currentLowest = Math.min(currentLowest,
						max(toBoard, calculateQuiescenceDepth(toBoard, depth), highest, currentLowest));
				if (currentLowest <= highest) {
					return highest;
				}
			}
		}
		return currentLowest;
	}

	private int calculateQuiescenceDepth(final Board toBoard, final int depth) {
		if (depth == 1 && this.quiescenceCount < MAX_QUIESCENCE) {
			int activityMeasure = 0;
			if (toBoard.currentPlayer().isInCheck()) {
				activityMeasure += 1;
			}
			for (final Move move : BoardUtils.lastNMoves(toBoard, 2)) {
				if (move.isAttack()) {
					activityMeasure += 1;
				}
			}
			if (activityMeasure >= 2) {
				this.quiescenceCount++;
				return 2;
			}
		}
		return depth - 1;
	}

	private static String calculateTimeTaken(final long start, final long end) {
		final long timeTaken = (end - start) / 1000000;
		return timeTaken + " ms";
	}

}