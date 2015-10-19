/* Copyright (C) 2010 Alexandre Riazanov (Alexander Ryazanov)
 *
 * The copyright owner licenses this file to You under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package logic.is.power.alabai;



import java.util.*;

import logic.is.power.cushion.*;

import logic.is.power.logic_warehouse.*;

/** A collection of simple term feature function objects
 *  to be used in feature vector-based indexes. 
 */
public final class StandardTermFeature {


    public static ScalarTermFeature numberOfSymbols() {
	return _numberOfSymbols;
    }

    public 
	static 
	VectorTermFeature 
	numberOfSymbolsFromCategories(int modulus) {
	
	Integer key = new Integer(modulus);
	
	NumberOfSymbolsFromCategories result = 
	    _numberOfSymbolsFromCategories.get(key);

	if (result == null)
	    {
		result = new NumberOfSymbolsFromCategories(modulus);
		_numberOfSymbolsFromCategories.put(key,result);
	    };
	return result;
    }

	


    private static class NumberOfSymbols 
	implements ScalarTermFeature {

	public int evaluate(Term term) {
	    return term.numberOfSymbols();
	}
	
	public int evaluate(Flatterm term) {
	    return term.numberOfSymbols();
	}
	
    } // class NumberOfSymbols 


    /** Counts the numbers of symbols belonging to all categories from 
     *  0 to <code>modulus - 1</code>; this is a vector function. 
     */
    private static class NumberOfSymbolsFromCategories
	implements VectorTermFeature {
	
	/** Will count the number of nonvariable symbols <code>sym</code>,
	 *  including logical symbols, but excluding pair constructors
	 *  and abstraction operators,
	 *  such that <code>sym.category(modulus) == category</code>,
	 *  for every <code>category</code> in <code>[0,modulus - 1]</code>. 
	 */
	public NumberOfSymbolsFromCategories(int modulus) {
	    _modulus = modulus;
	}

	public int[] evaluate(Term term) {

	    int[] result = new int[_modulus];

	    term.
		addNumberOfNonvariableSymbolsFromCategories(_modulus,
							    result);
	    return result;
	}
	
	public int[] evaluate(Flatterm term) {

	    int[] result = new int[_modulus];
	    
	    term.
		addNumberOfNonvariableSymbolsFromCategories(_modulus,
							    result);
	    return result;
	}
	
	
	public int size() { return _modulus; }

	private final int _modulus;

    } // class NumberOfSymbolsFromCategory  

    private static final ScalarTermFeature _numberOfSymbols = 
	new NumberOfSymbols();

    /** Cache for {@link #NumberOfSymbolsFromCategory} objects. */
    private 
	static 
	final 
	HashMap<Integer,NumberOfSymbolsFromCategories>
	_numberOfSymbolsFromCategories = 
	new HashMap<Integer,NumberOfSymbolsFromCategories>();
	
} // class StandardTermFeature 