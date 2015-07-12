/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util.math;

public class SyntaxException extends RuntimeException
{
	public SyntaxException(String functionName)
	{
		super("The function " + functionName + " does not exist, or you have entered invalid argument(s).");
	}
}
