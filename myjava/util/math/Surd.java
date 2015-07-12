/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util.math;

/**
 * Requires the following classes to work:
 * myjava.util.math.MathUtilities
 * myjava.util.math.Fraction
 */
import myjava.util.math.*;

public class Surd
{
	private Fraction fraction;
	private int surd;
	public Surd(double c)
	{
		if (Math.round(c) == MathUtilities.toFix(c,12))
		{
			this.fraction = new Fraction(Math.round(c),1);
			this.surd = 1;
		}
		else
		{
			for (int x=1; x<=500; x++)
			{
				double coeff = c/Math.sqrt(x);
				if (Math.round(coeff) == MathUtilities.toFix(coeff,8))
				{
					//coeff is an integer
					int coeff_int = (int)Math.round(coeff);
					this.fraction = new Fraction(coeff_int,1);
					this.surd = x;
					return;
				}
				else
				{
					//coeff is not an integer
					Fraction fraction = new Fraction(coeff);
					if (fraction.getDenominator() != 1)
					{
						this.fraction = fraction;
						this.surd = x;
						return;
					}
				}
			}
			this.fraction = new Fraction(c);
			this.surd = 1;
		}
	}
	
	@Override
	public String toString()
	{
		String f = this.fraction.toString();
		if (f.equals("1")) f = "";
		else if (f.equals("-1")) f = "-";
		return (f + (surd==1?"":("\u221A"+surd)));
	}
}
