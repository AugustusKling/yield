package yield.config.function.where;

import org.codehaus.jparsec.OperatorTable;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.Binary;
import org.codehaus.jparsec.functors.Unary;

/**
 * Parses a query string and returns an syntax tree.
 */
public class FilterParser {
	private static final Terminals TERMS = Terminals.caseSensitive(
			new String[] { "contains", "and", "or", "<", "<=", "=", ">=", ">",
					"(", ")", "lower", "not", "-", "coalesce" },
			new String[] {});

	private static Parser<?> term(String... names) {
		return TERMS.token(names);
	}

	private static <T> Parser<T> op(String name, T value) {
		return term(name).retn(value);
	}

	/**
	 * Binary functions.
	 */
	private enum Bin implements Binary<Expr> {
		and {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryAnd(name(), a, b);
			}
		},
		or {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryOr(name(), a, b);
			}
		},
		contains {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryContains(name(), a, b);
			}
		},
		/**
		 * Equality.
		 */
		EQ {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryEqual(name(), a, b);
			}
		},
		/**
		 * {@link Expr} {@code a} lower than {@link Expr} {@code b}.
		 */
		LT {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryLowerThan(name(), a, b, false);
			}
		},
		/**
		 * {@link Expr} {@code a} lower than or equal to {@link Expr} {@code b}.
		 */
		LTE {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryLowerThan(name(), a, b, true);
			}
		},
		/**
		 * {@link Expr} {@code a} greater than {@link Expr} {@code b}.
		 */
		GT {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryLowerThan(name(), b, a, false);
			}
		},
		/**
		 * {@link Expr} {@code a} greater than or equal to {@link Expr}
		 * {@code b}.
		 */
		GTE {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryLowerThan(name(), b, a, true);
			}
		},
		coalesce {
			@Override
			public Expr map(Expr a, Expr b) {
				return new ExprBinaryCoalesce(name(), a, b);
			}
		};
	}

	/**
	 * Unary functions.
	 */
	private enum Un implements Unary<Expr> {
		lower {
			@Override
			public Expr map(Expr from) {
				return new ExprUnaryLower(name(), from);
			}
		},
		not {
			@Override
			public Expr map(Expr from) {
				return new ExprUnaryNot(name(), from);
			}
		},
		minus {
			@Override
			public Expr map(Expr from) {
				return new ExprUnaryMinus(name(), from);
			}
		};
	}

	private Parser<Expr> astParser;

	public FilterParser() {
		Parser<Expr> atom = Parsers.or(Terminals.Identifier.PARSER
				.map(new org.codehaus.jparsec.functors.Map<String, Expr>() {

					@Override
					public Expr map(String from) {
						return new ExprRef(from);
					}
				}), Terminals.StringLiteral.PARSER
				.map(new org.codehaus.jparsec.functors.Map<String, Expr>() {

					@Override
					public Expr map(String from) {
						return new ExprLiteral(from);
					}
				}), Terminals.DecimalLiteral.PARSER
				.map(new org.codehaus.jparsec.functors.Map<String, Expr>() {

					@Override
					public Expr map(String from) {
						return new ExprLiteral(Double.valueOf(from));
					}
				}));
		final Parser<Void> IGNORED = Scanners.WHITESPACES.skipMany();

		final Parser<?> TOKENIZER = Parsers.or(
				Terminals.DecimalLiteral.TOKENIZER,
				Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
				TERMS.tokenizer());

		Parser.Reference<Expr> ref = Parser.newReference();
		Parser<Expr> unit = ref.lazy().between(term("("), term(")")).or(atom);
		Parser<Expr> parser = new OperatorTable<Expr>()
				.infixl(op("and", Bin.and), 10).infixl(op("or", Bin.or), 10)
				.infixl(op("contains", Bin.contains), 20)
				.infixl(op("<", Bin.LT), 20).infixl(op("<=", Bin.LTE), 20)
				.infixl(op("=", Bin.EQ), 20).infixl(op(">=", Bin.GTE), 20)
				.infixl(op(">", Bin.GT), 20)
				.infixl(op("coalesce", Bin.coalesce), 30)
				.prefix(op("lower", Un.lower), 30)
				.prefix(op("not", Un.not), 30).prefix(op("-", Un.minus), 30)
				.build(unit);
		ref.set(parser);
		astParser = parser.from(TOKENIZER, IGNORED);
	}

	/**
	 * Parses a query string and returns an syntax tree.
	 * 
	 * @param query
	 *            Query to parse.
	 * @return Syntax tree.
	 */
	public Expr buildExpression(CharSequence query) {
		try {
			return astParser.parse(query);
		} catch (ParserException e) {
			throw new IllegalArgumentException("Could only parse quey up to: "
					+ query.subSequence(0, e.getLocation().column), e);
		}
	}
}
