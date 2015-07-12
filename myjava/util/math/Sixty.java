/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util.math;

public class Sixty
{
	private int hour;
	private int minute;
	private double second;
	private int sign;
	public Sixty(double x)
	{
		super();
		double mx = Math.abs(x);
		this.hour = (int)(Math.floor(mx));
		this.minute = (int)Math.floor((mx-hour)*60);
		this.second = ((mx-hour)*60-minute)*60;
		this.sign = (int)Math.signum(x);
		//calibration:
		if (MathUtilities.toFix(second,5)==60)
		{
			minute++;
			second=0;
		}
		if (minute==60)
		{
			hour++;
			minute=0;
		}
	}
	
	@Override
	public String toString()
	{
		return (sign==-1?"-":"") + hour + "\u00B0" + minute + "\'" + MathUtilities.toString(MathUtilities.toFix(second,5)) + "\"";
	}
}
