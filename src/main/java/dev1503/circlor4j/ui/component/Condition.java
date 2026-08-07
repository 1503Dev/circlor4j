package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;

/**
 * Evaluates small boolean expressions over StatusManager paths, e.g.
 * "xxxx/enabled == 1 || (xxx/mode == 0 && xxx/mode != 2)".
 * Supports ==, !=, <, <=, >, >=, &&, ||, parentheses, numeric literals and paths.
 */
public final class Condition {

	private enum Op { NONE, EQ, NE, LT, LE, GT, GE, AND, OR }

	private enum TokenType { LPAREN, RPAREN, OP, OPERAND, END }

	private static final class Token {
		final TokenType type;
		final Op op;
		final String text;

		Token(TokenType type, Op op, String text) {
			this.type = type;
			this.op = op;
			this.text = text;
		}
	}

	private static final class Lexer {
		private final String src;
		private int pos;
		private Token current;

		Lexer(String src) {
			this.src = src;
			this.current = this.nextToken();
		}

		Token peek() {
			return this.current;
		}

		void next() {
			this.current = this.nextToken();
		}

		private void skipWs() {
			while (this.pos < this.src.length() && Character.isWhitespace(this.src.charAt(this.pos))) {
				this.pos++;
			}
		}

		private char peek2() {
			return this.pos + 1 < this.src.length() ? this.src.charAt(this.pos + 1) : '\0';
		}

		private Token nextToken() {
			this.skipWs();
			if (this.pos >= this.src.length()) {
				return new Token(TokenType.END, Op.NONE, null);
			}
			char c = this.src.charAt(this.pos);
			if (c == '(') {
				this.pos++;
				return new Token(TokenType.LPAREN, Op.NONE, null);
			}
			if (c == ')') {
				this.pos++;
				return new Token(TokenType.RPAREN, Op.NONE, null);
			}
			if (c == '&' && this.peek2() == '&') {
				this.pos += 2;
				return new Token(TokenType.OP, Op.AND, null);
			}
			if (c == '|' && this.peek2() == '|') {
				this.pos += 2;
				return new Token(TokenType.OP, Op.OR, null);
			}
			if (c == '=' && this.peek2() == '=') {
				this.pos += 2;
				return new Token(TokenType.OP, Op.EQ, null);
			}
			if (c == '!' && this.peek2() == '=') {
				this.pos += 2;
				return new Token(TokenType.OP, Op.NE, null);
			}
			if (c == '<' && this.peek2() == '=') {
				this.pos += 2;
				return new Token(TokenType.OP, Op.LE, null);
			}
			if (c == '>' && this.peek2() == '=') {
				this.pos += 2;
				return new Token(TokenType.OP, Op.GE, null);
			}
			if (c == '<') {
				this.pos++;
				return new Token(TokenType.OP, Op.LT, null);
			}
			if (c == '>') {
				this.pos++;
				return new Token(TokenType.OP, Op.GT, null);
			}
			int start = this.pos;
			while (this.pos < this.src.length()) {
				char ch = this.src.charAt(this.pos);
				if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '/' || ch == '-' || ch == '.') {
					this.pos++;
				} else {
					break;
				}
			}
			return new Token(TokenType.OPERAND, Op.NONE, this.src.substring(start, this.pos));
		}
	}

	private static final class Parser {
		private final StatusManager status;
		private final Lexer lexer;

		Parser(StatusManager status, Lexer lexer) {
			this.status = status;
			this.lexer = lexer;
		}

		boolean parse() {
			return this.parseOr();
		}

		private boolean parseOr() {
			boolean value = this.parseAnd();
			while (this.lexer.peek().type == TokenType.OP && this.lexer.peek().op == Op.OR) {
				this.lexer.next();
				boolean rhs = this.parseAnd();
				value = value || rhs;
			}
			return value;
		}

		private boolean parseAnd() {
			boolean value = this.parseCmp();
			while (this.lexer.peek().type == TokenType.OP && this.lexer.peek().op == Op.AND) {
				this.lexer.next();
				boolean rhs = this.parseCmp();
				value = value && rhs;
			}
			return value;
		}

		private boolean parseCmp() {
			Token token = this.lexer.peek();
			if (token.type == TokenType.LPAREN) {
				this.lexer.next();
				boolean value = this.parseOr();
				if (this.lexer.peek().type == TokenType.RPAREN) {
					this.lexer.next();
				}
				return value;
			}
			double left = this.parseOperand();
			if (this.lexer.peek().type == TokenType.OP && isComparison(this.lexer.peek().op)) {
				Op op = this.lexer.peek().op;
				this.lexer.next();
				double right = this.parseOperand();
				return compare(left, op, right);
			}
			return left != 0.0;
		}

		private double parseOperand() {
			Token token = this.lexer.peek();
			this.lexer.next();
			if (token.type != TokenType.OPERAND || token.text == null) {
				return 0.0;
			}
			try {
				return Double.parseDouble(token.text);
			} catch (NumberFormatException e) {
				return this.status.getDouble(token.text, 0.0);
			}
		}

		private static boolean isComparison(Op op) {
			return op == Op.EQ || op == Op.NE || op == Op.LT || op == Op.LE || op == Op.GT || op == Op.GE;
		}

		private static boolean compare(double left, Op op, double right) {
			return switch (op) {
				case EQ -> left == right;
				case NE -> left != right;
				case LT -> left < right;
				case LE -> left <= right;
				case GT -> left > right;
				case GE -> left >= right;
				default -> false;
			};
		}
	}

	private Condition() {
	}

	public static boolean evaluate(StatusManager status, String expression) {
		if (expression == null || expression.isBlank()) {
			return true;
		}
		return new Parser(status, new Lexer(expression)).parse();
	}
}
