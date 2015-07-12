/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util.math;

/**
 * Requires the following classes to work:
 * myjava.util.math.MathUtilities
 */
import java.math.*;
import myjava.util.math.*;

public class Fraction
{
	private BigDecimal numerator;
	private BigInteger denominator;
	public Fraction(BigDecimal numerator, BigInteger denominator)
	{
		super();
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public Fraction(double numerator, int denominator)
	{
		this(BigDecimal.valueOf(numerator),new BigInteger(Integer.toString(denominator)));
	}
	
	public Fraction(double original)
	{
		this(BigDecimal.valueOf(original));
	}
	
	public Fraction(BigDecimal original)
	{
		super();
		double A,C,D,X,Y,M;
		C = original.doubleValue();
		A = C;
		D = C;
		X = 1;
		Y = 0;
		do
		{
			M = Y;
			Y = X+Y*Math.round(C);
			X = M;
			if (C != Math.round(C))
			{
				C = 1/(C-Math.round(C));
			}
			M = Math.round(D*Y);
		} while (M != MathUtilities.toFix(D*Y,10));
		if ((Math.abs(M)>100000)||(Math.abs(Y)>100000)||(Math.abs(Y)==1))
		{
			this.numerator = original;
			this.denominator = BigInteger.ONE;
		}
		else
		{
			this.numerator = BigDecimal.valueOf(Math.round((Math.abs(M)/M)*(Math.abs(Y)/Y)*Math.abs(M)));
			this.denominator = new BigInteger(Long.toString(Math.round(Math.abs(Y))));
		}
	}
	
	public int getDenominator()
	{
		return this.denominator.intValue();
	}
	
	public double getNumerator()
	{
		return this.numerator.doubleValue();
	}
	
	@Override
	public String toString()
	{
		if (this.denominator.equals(BigInteger.ONE))
			return MathUtilities.formatNormalAnswer(this.numerator);
		else
			return this.numerator + "/" + this.denominator;
	}
}
