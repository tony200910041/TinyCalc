/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util.math;

/**
 * Requires the following classes to work:
 * myjava.util.math.Symbol
 * myjava.util.math.ArgumentNumberException
 * myjava.util.math.MismatchedBracketException
 */

import java.lang.reflect.*;
import java.util.*;
import java.math.*;

public final class EvalUtilities
{
	private static final MathContext MC_100 = new MathContext(100,RoundingMode.HALF_UP);
	public static ArrayList<Symbol> toRPN(ArrayList<Symbol> input)
	{
		/*
		 * algorithm: (from Wikipedia)
		 * 
		    While there are tokens to be read:		
		        Read a token.
		        If the token is a number, then add it to the output queue.
		        If the token is a function token, then push it onto the stack.
		        If the token is a function argument separator (e.g., a comma):		
		            Until the token at the top of the stack is a left parenthesis, pop operators off the stack onto the output queue. If no left parentheses are encountered, either the separator was misplaced or parentheses were mismatched.
		        If the token is an operator, o1, then:		
		            while there is an operator token, o2, at the top of the operator stack, and either
		                o1 is left-associative and its precedence is less than or equal to that of o2, or
		                o1 is right associative, and has precedence less than that of o2,		
		            then pop o2 off the operator stack, onto the output queue;		
		            push o1 onto the operator stack.		
		        If the token is a left parenthesis, then push it onto the stack.
		        If the token is a right parenthesis:		
		            Until the token at the top of the stack is a left parenthesis, pop operators off the stack onto the output queue.
		            Pop the left parenthesis from the stack, but not onto the output queue.
		            If the token at the top of the stack is a function token, pop it onto the output queue.
		            If the stack runs out without finding a left parenthesis, then there are mismatched parentheses.		
		    When there are no more tokens to read:		
		        While there are still operator tokens in the stack:		
		            If the operator token on the top of the stack is a parenthesis, then there are mismatched parentheses.
		            Pop the operator onto the output queue.		
		    Exit.
		 */
		ArrayList<Symbol> output = new ArrayList<>(input.size()/2);
		Stack<Symbol> stack = new Stack<>();
		for (Symbol symbol: input)
		{
			Symbol.Type type = symbol.getType();
			switch (type)
			{
				case VALUE:
				output.add(symbol);
				break;
				
				case FUNCTION:
				case OPENING_BRACKET:
				stack.add(symbol);				
				break;
				
				case ARG_SEPARATOR:
				try
				{
					while (stack.peek().getType() != Symbol.Type.OPENING_BRACKET)
					{
						output.add(stack.pop());
					}
				}
				catch (NullPointerException ex)
				{
					//stack.peek() is null
					throw new MismatchedBracketException("wrong , position or missing (");
				}
				break;
				
				case OPERATOR:
				if (symbol.isLeftAssociative())
				{
					if (stack.size() != 0)
					{
						while (stack.peek().getType() == Symbol.Type.OPERATOR)
						{
							if ((symbol.getPrecedence() <= stack.peek().getPrecedence()))
							{
								output.add(stack.pop());
								if (stack.size() == 0) break;
							}
							else break;
						}
					}
					stack.add(symbol);
				}
				else
				{
					if (stack.size() != 0)
					{
						while (stack.peek().getType() == Symbol.Type.OPERATOR)
						{
							if ((symbol.getPrecedence() < stack.peek().getPrecedence()))
							{
								output.add(stack.pop());
								if (stack.size() == 0) break;
							}
							else break;
						}
					}
					stack.add(symbol);
				}
				break;
				
				case CLOSING_BRACKET:
				try
				{
					while (stack.peek().getType() != Symbol.Type.OPENING_BRACKET)
					{
						output.add(stack.pop());
					}
				}
				catch (NullPointerException ex)
				{
					//stack.peek() is null
					throw new MismatchedBracketException("no (");
				}
				stack.pop(); //remove "("
				try
				{
					Symbol s = stack.peek();
					if (s.getType() == Symbol.Type.FUNCTION)
					{
						output.add(stack.pop());
					}
				}
				catch (Exception ex)
				{
				}
				break;
			}
		}
		if (stack.size() != 0)
		{
			Symbol.Type type = stack.peek().getType();
			if ((type == Symbol.Type.OPENING_BRACKET)||(type == Symbol.Type.CLOSING_BRACKET))
			{
				throw new MismatchedBracketException("Mismatch " + stack.peek() + " in stack");
			}
			else while (stack.size() != 0)
			{
				output.add(stack.pop());
			}
		}
		return output;
	}
	
	public static BigDecimal[] evaluate(ArrayList<Symbol> symbols)
	{
		/*
		 * algorithm: (from Wikipedia)
		 * 
		    While there are input tokens left
		        Read the next token from input.
		        If the token is a value
		            Push it onto the stack.
		        Otherwise, the token is an operator (operator here includes both operators and functions).
		            It is known a priori that the operator takes n arguments.
		            If there are fewer than n values on the stack
		                (Error) The user has not input sufficient values in the expression.
		            Else, Pop the top n values from the stack.
		            Evaluate the operator, with the values as arguments.
		            Push the returned results, if any, back onto the stack.
		    If there is only one value in the stack
		        That value is the result of the calculation.
		    Otherwise, there are more values in the stack
		        (Error) The user input has too many values.
		 */
		Stack<Symbol> stack = new Stack<>();
		for (Symbol symbol: symbols)
		{
			Symbol.Type type = symbol.getType();
			switch (type)
			{
				case VALUE:
				stack.add(symbol);
				break;
				
				case OPERATOR:
				case FUNCTION:
				int argNo = getArgumentNumber(symbol.toString());
				if (stack.size() < argNo)
				{
					throw new ArgumentNumberException("Missing argument(s) or syntax error: stack size " + stack.size() + " < " + argNo);
				}
				else
				{
					ArrayList<Symbol> args = new ArrayList<>(argNo);
					for (int i=0; i<argNo; i++)
					{
						args.add(stack.pop());
					}
					/*
					 * The first element in stack will be the last element in args
					 */
					Collections.reverse(args);
					//symbol: function name
					stack.add(new Symbol(eval(symbol.get().toString(), args.toArray(new Symbol[0]))));
				}
				break;
			}
		}
		if (stack.size() == 1)
		{
			return (BigDecimal[])(stack.pop().get());
		}
		else
		{
			throw new ArgumentNumberException((stack.size()-1) + " extra argument(s)");
		}
	}
	
	static int getArgumentNumber(String functionName)
	{
		switch (functionName)
		{
			case "+":
			case "-":
			case "*":
			case "/":
			case "^":
			case "&":
			case "|":
			case "%":
			return 2;
			
			default:
			return EvalUtilities.getMethod(functionName.toLowerCase()).getParameterTypes().length;
		}
	}
	
	static BigDecimal[] eval(String functionName, Symbol... args)
	{
		/*
		 * now Symbol[] args contains only values
		 * return a BigDecimal[] containing the result(s)
		 */
		BigDecimal arg0 = new BigDecimal("0");
		BigDecimal arg1 = new BigDecimal("0");
		if (args.length >= 1)
		{
			arg0 = args[0].getFirst();
		}
		if (args.length >= 2)
		{
			arg1 = args[1].getFirst();;
		}
		switch (functionName)
		{
			case "+":
			return new BigDecimal[]{arg0.add(arg1)};
			
			case "-":
			return new BigDecimal[]{arg0.subtract(arg1)};
			
			case "*":
			return new BigDecimal[]{arg0.multiply(arg1)};
			
			case "/":
			return new BigDecimal[]{arg0.divide(arg1,MC_100)};
			
			case "^":
			return new BigDecimal[]{EvalUtilities.pow(arg0,arg1)};
			
			case "&":
			{
				try
				{
					return new BigDecimal[]{new BigDecimal(arg0.toBigIntegerExact().and(arg1.toBigIntegerExact()))};
				}
				catch (ArithmeticException ex)
				{
					throw new IllegalArgumentException(ex.getMessage());
				}
			}
			
			case "|":
			{
				try
				{
					return new BigDecimal[]{new BigDecimal(arg0.toBigIntegerExact().or(arg1.toBigIntegerExact()))};
				}
				catch (ArithmeticException ex)
				{
					throw new IllegalArgumentException(ex.getMessage());
				}
			}
			
			case "%":
			{
				try
				{
					return new BigDecimal[]{arg0.remainder(arg1)};
				}
				catch (ArithmeticException ex)
				{
					throw new IllegalArgumentException(ex.getMessage());
				}
			}
			
			default:
			Method method = EvalUtilities.getMethod(functionName.toLowerCase());
			try
			{
				Object o;
				switch (method.getParameterTypes().length)
				{
					case 0:
					o = method.invoke(null,new Object[0]);
					break;
					
					default:
					Double[] values = new Double[args.length];
					for (int i=0; i<args.length; i++)
					{
						values[i] = args[i].getFirst().doubleValue();
					}
					o = method.invoke(null,(Object[])values);
					break;
				}
				if (o instanceof Double)
				{
					return new BigDecimal[]{BigDecimal.valueOf((Double)o)};
				}
				else if (o instanceof Long)
				{
					return new BigDecimal[]{BigDecimal.valueOf((Long)o)};
				}
				else if (o instanceof BigInteger)
				{
					return new BigDecimal[]{new BigDecimal((BigInteger)o)};
				}
				else if (o instanceof BigDecimal)
				{
					return new BigDecimal[]{(BigDecimal)o};
				}
				else if (o instanceof double[])
				{
					double[] result = (double[])o;
					BigDecimal[] _return = new BigDecimal[result.length];
					for (int i=0; i<result.length; i++)
					{
						_return[i] = BigDecimal.valueOf(result[i]);
					}
					return _return;
				}
				else throw new Exception(); //unusual case
			}
			catch (InvocationTargetException ex1)
			{
				try
				{
					throw (IllegalArgumentException)(ex1.getCause());
				}
				catch (ClassCastException ex2)
				{
					try
					{
						throw (ArithmeticException)(ex1.getCause());
					}
					catch (ClassCastException ex3)
					{
						throw ex2;
					}
				}
			}
			catch (Exception ex4)
			{
				throw new SyntaxException(functionName);
			}
		}
	}
	
	static BigDecimal pow(BigDecimal arg0, BigDecimal arg1)
	{
		try
		{
			//check if arg1 (power) is an integer
			BigInteger bi = arg1.toBigIntegerExact();
			return arg0.pow(bi.intValue(),MC_100);
		}
		catch (ArithmeticException ex)
		{
			//arg1 is not an integer
			int n = arg1.intValue();
			switch (arg1.signum())
			{
				case 0: //=0
				if (arg0.compareTo(BigDecimal.ZERO) != 0)
				{
					return new BigDecimal(1);
				}
				else throw new ArithmeticException("Invalid calculation: 0 ^ 0");
				
				case 1: //>0
				{
					BigDecimal result = arg0.pow(n);
					double x = Math.pow(arg0.doubleValue(), arg1.doubleValue()-n);
					if ((new Double(x).equals(Double.NaN))||(new Double(Math.abs(x)).equals(Double.POSITIVE_INFINITY)))
					{
						throw new ArithmeticException("too large result");
					}
					else
					{
						return result.multiply(BigDecimal.valueOf(x));
					}
				}
				
				case -1: //<0
				{
					//n<0, so -n>0
					//e.g. 2.2^(-2.1)
					//now n=-2, result=(2.2^-2)*(2.2*-0.1)=(2.2*-0.1)/(2.2^2)
					BigDecimal result = arg0.pow(-n);
					double x = Math.pow(arg0.doubleValue(), arg1.doubleValue()-n);
					if ((new Double(x).equals(Double.NaN))||(new Double(Math.abs(x)).equals(Double.POSITIVE_INFINITY)))
					{
						throw new ArithmeticException("too large result");
					}
					else
					{
						return BigDecimal.valueOf(x).divide(result, MC_100);
					}
				}
				
				default: //cannot happen
				throw new Error();
			}
		}
	}
	
	static Method getMethod(String functionName)
	{
		for (Method method: MathUtilities.class.getMethods())
		{
			if (method.getName().equals(functionName))
			{
				return method;
			}
		}
		for (Method method: java.lang.Math.class.getDeclaredMethods())
		{
			if (method.getName().equals(functionName))
			{				
				boolean containsNonDouble = false;
				for (Class<?> c: method.getParameterTypes())
				{
					if (!c.getSimpleName().toLowerCase().contains("double"))
					{
						containsNonDouble = true;
					}
				}
				if (!containsNonDouble) return method;
			}
		}
		throw new SyntaxException(functionName);
	}
}
