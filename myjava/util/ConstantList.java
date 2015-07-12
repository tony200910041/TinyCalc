/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util;

import java.util.*;
import myjava.util.*;

public final class ConstantList extends ArrayList<Constant>
{
	private static final ConstantList INSTANCE = new ConstantList();
	private ConstantList()
	{
		super(17);
		this.add(new Constant("Golden ratio", String.valueOf((1+Math.sqrt(5))/2), "[gr]")); 
		this.add(new Constant("Square root of 2", String.valueOf(Math.sqrt(2)), "[rt2]")); 
		this.add(new Constant("Square root of 3", String.valueOf(Math.sqrt(3)), "[rt3]")); 
		this.add(new Constant("Mass of proton", "1.672621777*10^(-27)", "[mp]")); 
		this.add(new Constant("Mass of electron", "9.10938291*10^(-31)", "[me]")); 
		this.add(new Constant("Charge of electron", "1.602176565*10^(-19)", "[eV]", "[C]")); 
		this.add(new Constant("Magnetic constant", "4*[pi]*10^(-7)", "[mc]", "[u0]")); 
		this.add(new Constant("Electric constant", "8.854187817*10^(-12)", "[ec]", "[e0]")); 
		this.add(new Constant("Speed of light", "299792458", "[c]"));
		this.add(new Constant("Gravitational constant", "6.67384*10^(-11)", "[G]")); 
		this.add(new Constant("Acceleration due to gravity", "9.80665", "[g]")); 
		this.add(new Constant("Gas constant", "8.3144621", "[R]")); 
		this.add(new Constant("Avogadro's constant", "6.02214129*10^23", "[NA]")); 
		this.add(new Constant("Atmospheric pressure", "101325", "[atm]")); 
		this.add(new Constant("Atomic mass unit", "1.660538921*10^(-27)", "[u]")); 
		this.add(new Constant("Absolute zero in Degree Celsius", "-273.15", "[t]")); 
		this.add(new Constant("Planck constant", "6.62606957*10^(-34)", "[h]"));
		this.add(new Constant("\u03C0", String.valueOf(Math.PI), "[pi]"));
		this.add(new Constant("e", String.valueOf(Math.E), "[e]"));
	}
	
	public static ConstantList getInstance()
	{
		return INSTANCE;
	}
	
	@Override
	public ConstantList clone() //overriden method does not throw CloneNotSupportedException
	{
		throw new RuntimeException(new CloneNotSupportedException());
	}
}
