/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util.math;

import java.text.*;
import java.math.*;
import javax.script.*;

public class MathUtilities
{
	private static final ScriptEngine engine = (new ScriptEngineManager()).getEngineByName("javascript");
	private static final DecimalFormat expFormat = new DecimalFormat("0.###############E0");
	/*
	 * methods with upper case would not be used by users
	 * all methods will return a double or a double[]
	 */
	public static double log(double x1, double x2)
	{
		return Math.log(x2)/Math.log(x1);
	}
	
	public static double ln(double x)
	{
		return Math.log(x);
	}
	
	public static double logten(double x)
	{
		return Math.log10(x);
	}
	
	public static double logonep(double x)
	{
		return Math.log1p(x);
	}
	
	public static double atantwo(double x, double y)
	{
		return Math.atan2(x,y);
	}
	
	public static double expmone(double x)
	{
		return Math.expm1(x);
	}
	
	public static BigInteger fact(double x)
	{
		if (Math.round(x) != toFix(x,10))
		{
			throw new IllegalArgumentException(toString(x) + " is not an integer!");
		}
		else if (x < 0)
		{
			throw new IllegalArgumentException("factorial " + toString(x) + " < 0");
		}
		else if (x <= 1)
		{
			return BigInteger.ONE;
		}
		else
		{
			BigInteger product = BigInteger.ONE;
			for (int i=2; i<=x; i++)
			{
				product = product.multiply(BigInteger.valueOf(i));
			}
			return product;
		}
	}
	
	public static BigDecimal stirling(double x)
	{
		if (Math.round(x) != toFix(x,10))
		{
			throw new IllegalArgumentException(toString(x) + " is not an integer!");
		}
		else if (x < 0)
		{
			throw new IllegalArgumentException("factorial " + toString(x) + " < 0");
		}
		else
		{
			int xInt = (int)Math.round(x);
			return BigDecimal.valueOf(Math.sqrt(2*Math.PI*xInt)).multiply(BigDecimal.valueOf(xInt/Math.E).pow(xInt));
		}
	}
	
	public static BigInteger p(double n, double r)
	{
		if (r <= 0)
		{
			throw new IllegalArgumentException(toString(r) + " is not a positive integer!");
		}
		else
		{
			return fact(n).divide(fact(n-r));
		}
	}
	
	public static BigInteger c(double n, double r)
	{
		return fact(n).divide(fact(r).multiply(fact(n-r)));
	}
	
	public static double sum(double start, double end, double step)
	{
		if (start > end)
		{
			throw new IllegalArgumentException("Lower limit > upper limit!");
		}
		else if (start + step > end)
		{
			throw new IllegalArgumentException("Lower limit + step > upper limit!");
		}
		else if (step <= 0)
		{
			throw new IllegalArgumentException("Step = 0!");
		}
		else if (start == end)
		{
			return start;
		}
		else
		{
			double sum = 0;
			for (double i=start; i<=end; i+=step)
			{
				sum+=i;
			}
			return sum;
		}
	}
	
	public static double product(double start, double end, double step)
	{
		if (start > end)
		{
			throw new IllegalArgumentException("Lower limit > upper limit!");
		}
		else if (start + step > end)
		{
			throw new IllegalArgumentException("Lower limit + step > upper limit!");
		}
		else if (step <= 0)
		{
			throw new IllegalArgumentException("Step = 0!");
		}
		else if (start == end)
		{
			return start;
		}
		else
		{
			double product = 1;
			for (double i=start; i<=end; i+=step)
			{
				product*=i;
			}
			return product;
		}
	}
	
	public static double delta(double a, double b, double c)
	{
		return Math.pow(b,2)-4*a*c;
	}
	
	public static double[] quad(double a, double b, double c)
	{
		double delta = toFix(delta(a,b,c),12);
		double vertx = -b/(2*a);
		double x1, x2;
		if (delta >= 0)
		{
			x1 = vertx + Math.sqrt(delta)/(2*a);
			x2 = vertx - Math.sqrt(delta)/(2*a);
		}
		else
		{
			x1 = vertx;
			x2 = Math.sqrt(-delta)/(2*a);
		}
		double verty = a*Math.pow(vertx,2)+b*vertx+c;
		return new double[]{delta,x1,x2,vertx,verty};
	}
	
	public static double[] cubic(double a, double b, double c, double d)
	{
		/*
		 * solve ax^3+bx^2+cx+d=0
		 * using WebCal's algorithm
		 * http://webcal.freetzi.com/casio.fx-50FH/cubic1.htm
		 * program 2
		 */
		double b1 = b, c1 = c;
		b = -b/(3*a);
		d += b*c;
		c = c/a;
		d = Math.pow(b,3)-d/(2*a);
		double x = Math.pow(b,2)-c/3;
		double y = Math.pow(d,2)-Math.pow(x,3);
		if (0 > y)
		{
			y = 2*Math.sqrt(x)*Math.cos(Math.acos(d/Math.sqrt(Math.pow(x,3)))/3);
		}
		else
		{
			d += Math.sqrt(y);
			y = Math.cbrt(d)+Math.cbrt(d-2*Math.sqrt(y));
		}
		double x1 = y+b;
		/*
		 * now reduce the equation to a quadratic equation
		 */
		b1 += x1*a;
		c1 += x1*b1;
		double[] result = quad(a,b1,c1);
		if (result[0] >= 0)
		{
			return new double[]{x1,result[0],result[1],result[2]};
		}
		else
		{
			return new double[]{x1,result[0],result[1],result[2]};
		}
	}
		
	public static double heron(double a, double b, double c)
	{
		if (isTriangle(a, b, c))
		{
			double s = (a+b+c)/2;
			double rt = s*(s-a)*(s-b)*(s-c);
			return Math.sqrt(Math.abs(rt));
		}
		else throw new IllegalArgumentException("No such triangle!");
	}
	
	public static double opside(double b, double c, double angle)
	{
		if ((b<=0)||(c<=0)||(angle<=0)||(angle>=Math.PI))
		{
			throw new IllegalArgumentException("No such triangle!");
		}
		else
		{
			return Math.sqrt(b*b+c*c-2*b*c*Math.cos(angle));
		}
	}
	
	public static double area(double a, double b, double angle)
	{
		if ((a<=0)||(b<=0)||(angle<=0)||(angle>=Math.PI))
		{
			throw new IllegalArgumentException("No such triangle!");
		}
		else
		{
			return a*b*Math.sin(angle);
		}
	}
	
	public static double[] angle(double a, double b, double c)
	{
		if (isTriangle(a,b,c))
		{
			double cos1 = toFix((Math.pow(b,2)+Math.pow(c,2)-Math.pow(a,2))/(2*b*c),12);
			double cos2 = toFix((Math.pow(a,2)+Math.pow(c,2)-Math.pow(b,2))/(2*a*c),12);
			double cos3 = toFix((Math.pow(a,2)+Math.pow(b,2)-Math.pow(c,2))/(2*a*b),12);
			return new double[]{Math.acos(cos1), Math.acos(cos2), Math.acos(cos3)};			
		}
		else throw new IllegalArgumentException("No such triangle!");
	}
	
	public static double csc(double x)
	{
		double value = Math.sin(x);
		if (toFix(x,12) == 0)
		{
			throw new ArithmeticException();
		}
		else return 1/value;
	}
	
	public static double sec(double x)
	{
		double value = Math.cos(x);
		if (toFix(x,12) == 0)
		{
			throw new ArithmeticException();
		}
		else return 1/value;
	}
	
	public static double cot(double x)
	{
		double value = Math.tan(x);
		if (toFix(x,12) == 0)
		{
			throw new ArithmeticException();
		}
		else if (Math.abs(value) > Math.pow(10,10))
		{
			throw new ArithmeticException();
		}
		else return 1/value;
	}
	
	public static double acsc(double x)
	{
		if (x != 0)
		{
			return Math.asin(1/x);
		}
		else
		{
			throw new ArithmeticException();
		}
	}
	
	public static double asec(double x)
	{
		if (x != 0)
		{
			return Math.acos(1/x);
		}
		else
		{
			throw new ArithmeticException();
		}
	}
	
	public static double acot(double x)
	{
		if (x != 0)
		{
			return Math.atan(1/x);
		}
		else
		{
			throw new ArithmeticException();
		}
	}
	
	public static double dist(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
	}
	
	public static double slope(double x1, double y1, double x2, double y2)
	{
		if (x1 == x2)
		{
			throw new IllegalArgumentException("Vertical line x=" + toString(x1) + "!");
		}
		else
		{
			return (y2-y1)/(x2-x1);
		}
	}
	
	public static double yint(double x1, double y1, double x2, double y2)
	{
		return y1-slope(x1,y1,x2,y2)*x1;
	}
	
	public static double distpl(double A, double B, double C, double x, double y)
	{
		return Math.abs(A*x+B*y+C)/Math.sqrt(Math.pow(A,2)+Math.pow(B,2));
	}
	
	public static double det(double x0, double x1, double x2, double x3, double x4, double x5, double x6, double x7, double x8)
	{
		return x0*x4*x8+x1*x5*x6+x2*x3*x7-x6*x4*x2-x7*x5*x0-x8*x3*x1;
	}
	
	public static double[] simutwo(double a1, double b1, double c1, double a2, double b2, double c2)
	{
		double delta = a1*b2-a2*b1;
		if (delta != 0)
		{
			double x = (c1*b2-c2*b1)/delta;
			double y = (a1*c2-a2*c1)/delta;
			return new double[]{delta, x, y};
		}
		else throw new ArithmeticException("delta = 0!");
	}
	
	public static double isprime(double x)
	{
		if ((Math.round(x) != toFix(x,12))||(x < 2))
		{
			throw new IllegalArgumentException("isPrime(n), where n >= 2 is an integer");
		}
		else
		{
			long n = Math.round(x);
			for (long i=2; i<=Math.sqrt(n); i++)
			{
				double factor = n*1.0/i;
				if (Math.round(factor) == toFix(factor,12)) return 0;
			}
			return 1;
		}
	}
	
	public static double ran(double lower, double upper)
	{
		return (upper-lower)*Math.random()+lower;
	}
	
	public static int ranint(int lower, int upper)
	{
		return (int)Math.round(ran(lower, upper));
	}
	
	/*
	 * the following are methods that will not be called by users directly
	 * should contains capital letter(s)
	 */
	
	public static double toFix(double x, int dp)
	{
		String format = "";
		for (int i=0; i<dp; i++)
		{
			format = format + "#";
		}
		return Double.parseDouble((new DecimalFormat("#0."+format)).format(x));
	}
	
	public static String toFix(BigDecimal x, int dp)
	{
		String format = "";
		for (int i=0; i<dp; i++)
		{
			format = format + "#";
		}
		return (new DecimalFormat("#0."+format)).format(x);
	}
	
	public static String toSci(BigDecimal bd, int sf)
	{
		Double x = bd.doubleValue();
		if (x.equals(Double.POSITIVE_INFINITY)||x.equals(Double.NEGATIVE_INFINITY))
		{
			return x.toString();
		}
		else
		{
			try
			{
				return toString(Double.parseDouble(engine.eval("Number(" + x + ").toPrecision(" + sf + ")").toString()));
			}
			catch (Exception ex)
			{
				return toFix(bd,15);
			}
		}
	}
	
	public static boolean isTriangle(double a, double b, double c)
	{
		if ((a<=0)||(b<=0)||(c<=0))
		{
			return false;
		}
		else if ((a+b<=c)||(a+c<=b)||(b+c<=a))
		{
			return false;
		}
		return true;
	}
	
	public static String formatNormalAnswer(BigDecimal lastAns)
	{
		double d = lastAns.doubleValue();
		if (lastAns.abs().compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) >= 0)
		{
			return expFormat.format(lastAns);
		}
		else if ((d != 0)&&((Math.abs(d) >= Math.pow(10,10))||(Math.abs(d) <= Math.pow(10,-10))))
		{
			return expFormat.format(d);
		}
		else 
		{
			return MathUtilities.toSci(lastAns, 15);
		}
	}
	
	public static String toString(double x)
	{
		String str = x+"";
		if (str.matches(".*\\.0"))
		{
			str = str.substring(0, str.indexOf("."));
		}
		return str;
	}
}
