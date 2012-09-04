/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.formulaeditor;

import java.util.LinkedList;
import java.util.List;

public class XMLStringToInternGenerator {

	public XMLStringToInternGenerator() {
	}

	public static List<InternToken> generateInternRepresentationByString(String stringInternFormulaRepresentation) {
		List<InternToken> internTokenList = new LinkedList<InternToken>();

		while (stringInternFormulaRepresentation.length() > 0) {
			InternToken tokenToAdd = getNextToken(stringInternFormulaRepresentation);
			if (tokenToAdd == null) {
				return null;
			}
			internTokenList.add(tokenToAdd);
			stringInternFormulaRepresentation = stringInternFormulaRepresentation.substring(tokenToAdd.getTokenSring()
					.length());
		}

		return internTokenList;
	}

	private static InternToken getNextToken(String stringInternFormulaRepresentation) {

		int internTokenTypeNameEndIndex = stringInternFormulaRepresentation.indexOf(":", 1);
		String internalTokenTypeName = stringInternFormulaRepresentation.substring(0, internTokenTypeNameEndIndex);

		InternTokenType internTokenType = InternTokenType.getInternTokenTypeByString(internalTokenTypeName);

		return null;
	}

}
