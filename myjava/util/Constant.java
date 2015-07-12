/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util;

public class Constant
{
	private String name;
	private String value;
	private String[] varName;
	public Constant(String name, String value, String... varName)
	{
		super();
		this.name = name;
		this.value = value;
		this.varName = new String[varName.length];
		for (int i=0; i<varName.length; i++)
		{
			if (varName[i].startsWith("[")&&varName[i].endsWith("]"))
			{
				this.varName[i] = varName[i];
			}
			else
			{
				this.varName[i] = "[" + varName[i] + "]";
			}
		}
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	public String[] getVarNames()
	{
		return this.varName.clone();
	}
	
	public String getVarString()
	{
		StringBuilder builder = new StringBuilder();
		for (String v: varName)
		{
			builder.append(v);
			builder.append(", ");
		}
		return builder.substring(0,builder.length()-2);
	}
	
	@Override
	public String toString()
	{
		return this.value.replace("*[pi]","\u03C0").replace("*","\u00D7").replaceAll("[\\(\\)]","");
	}
}
