/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util.math;

import java.util.*;
import java.math.*;

public class Symbol
{
	Type type;
	char operator;
	String function;
	BigDecimal[] value;
	public Symbol(char operator)
	{
		if (operator == '(')
		{
			this.type = Type.OPENING_BRACKET;
		}
		else if (operator == ')')
		{
			this.type = Type.CLOSING_BRACKET;
		}
		else if (operator == ',')
		{
			this.type = Type.ARG_SEPARATOR;
		}
		else
		{
			this.type = Type.OPERATOR;
		}
		this.operator = operator;
	}
	
	public Symbol(String function)
	{
		this.type = Type.FUNCTION;
		this.function = function;
	}
	
	public Symbol(BigDecimal value)
	{
		this.type = Type.VALUE;
		this.value = new BigDecimal[]{value};
	}
	
	public Symbol(BigDecimal[] value)
	{
		this.type = Type.VALUE;
		this.value = value.clone();
	}
	
	@Override
	public String toString()
	{
		switch (this.type)
		{
			case OPERATOR:
			case OPENING_BRACKET:
			case CLOSING_BRACKET:
			return Character.toString(operator);
			
			case FUNCTION:			
			return this.function;
			
			case VALUE:
			default:
			return Arrays.toString(value);
		}
	}
	
	public Object get()
	{
		switch (this.type)
		{
			case OPERATOR:
			return this.operator;
			
			case FUNCTION:
			return this.function;
			
			case VALUE:
			default:
			return this.value;
		}
	}
	
	public BigDecimal getFirst()
	{
		if (this.type == Type.VALUE)
		{
			return this.value[0];
		}
		else
		{
			throw new IllegalStateException("Wrong type " + type.toString());
		}
	}
	
	public Type getType()
	{
		return this.type;
	}
	
	public boolean isLeftAssociative()
	{
		if (this.type == Type.OPERATOR)
		{
			if (this.operator == '^') return false;
			else return true;
		}
		else
		{
			throw new IllegalStateException("Wrong type " + type.toString());
		}
	}
	
	public int getPrecedence()
	{
		if (this.type == Type.OPERATOR)
		{
			switch (this.operator)
			{
				case '^':
				return 3;
				
				case '*':
				case '/':
				return 2;
				
				case '+':
				case '-':
				return 1;
				
				default:
				return -1;
			}
		}
		else if (this.type == Type.FUNCTION)
		{
			return 4;
		}
		else
		{
			throw new IllegalStateException("Wrong type " + type.toString());
		}
	}
	
	public static enum Type
	{
		OPERATOR, FUNCTION, VALUE, OPENING_BRACKET, CLOSING_BRACKET, ARG_SEPARATOR;
	}
	
	public static ArrayList<Symbol> toSymbols(String expression)
	{
		ArrayList<Symbol> symbols = new ArrayList<>();
		for (int i=0; i<expression.length(); i++)
		{
			char c = expression.charAt(i);			
			if (isBracket(c))
			{
				//bracket
				symbols.add(new Symbol(c));
			}
			else if (c == ',')
			{
				symbols.add(new Symbol(c));
			}
			else if (isOperator(c))
			{
				if (c == '-')
				{
					if (i == 0)
					{
						//must be first +/-, so add 0
						symbols.add(new Symbol(BigDecimal.valueOf(0)));
						symbols.add(new Symbol(c));
					}
					else
					{
						char previous = expression.charAt(i-1);
						if ((previous==')')||isNumber(String.valueOf(previous)))
						{
							//must be "minus"
							//e.g. sin(3)-5, 100-5, 
							symbols.add(new Symbol(c));
						}
						else if ((previous=='(')||(previous==','))
						{
							//must be "negative"
							//e.g. (-3+5)/2
							symbols.add(new Symbol(BigDecimal.valueOf(0)));
							symbols.add(new Symbol(c));
						}
						else if ((previous=='*')||(previous=='/')||(previous=='^'))
						{
							int end = Symbol.getNumberEndAt(i+1,expression);
							symbols.add(new Symbol(new BigDecimal(expression.substring(i,end))));
							i = end-1;
						}
					}
				}
				else
				{
					symbols.add(new Symbol(c));
				}
			}
			else if (Character.isLetter(c))
			{
				//function
				int end = Symbol.getFunctionEndAt(i,expression);
				symbols.add(new Symbol(expression.substring(i,end)));
				i = end-1;
			}
			else if (Character.isDigit(c))
			{
				int end = Symbol.getNumberEndAt(i,expression);
				String number = expression.substring(i,end);
				symbols.add(new Symbol(new BigDecimal(number)));	
				i = end-1;
			}
		}
		return symbols;
	}
	
	public static boolean isNumber(String str)
	{
		try
		{
			Double.parseDouble(str);
			return true;
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
	}
	
	public static boolean isFunctionName(String str)
	{
		/*
		 * actually checks if all the chars are letters
		 */
		for (char c: str.toCharArray())
		{
			if (!Character.isLetter(c)) return false;
		}
		return true;
	}
	
	public static int getNumberEndAt(int off, String expression)
	{
		String value = expression.substring(off,off+1);
		char nextChar;
		int i=off+1;
		for (;i<expression.length(); i++)
		{
			nextChar = expression.charAt(i);
			value+=(nextChar+"");
			if (!isNumber(value))
			{
				//now charAt(i) is not a number
				return i;
			}
		}
		return i;
	}
	
	public static int getFunctionEndAt(int off, String expression)
	{
		String value = expression.substring(off,off+1);
		char nextChar;
		int i=off+1;
		for (; i<expression.length(); i++)
		{
			nextChar = expression.charAt(i);
			value+=(nextChar+"");
			if (!isFunctionName(value))
			{
				//now charAt(i) is not a letter
				return i;
			}
		}
		return i;
	}
	
	public static boolean isOperator(char c)
	{
		return (c == '+')||(c == '-')||(c == '*')||(c == '/')||(c == '^')||(c == '&')||(c == '|')||(c == '%');
	}
	
	public static boolean isBracket(char c)
	{
		return (c == '(')||(c == ')');
	}
}
