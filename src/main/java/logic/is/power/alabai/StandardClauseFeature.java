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

/** A collection of simple clause feature function objects
 *  to be used in feature vector-based indexes. 
 */
public final class StandardClauseFeature {


    public static ScalarClauseFeature numberOfLiterals() {
	return _numberOfLiterals;
    }

    public static ScalarClauseFeature numberOfPositiveLiterals() {
	return _numberOfPositiveLiterals;
    }

    public static ScalarClauseFeature numberOfNegativeLiterals() {
	return _numberOfNegativeLiterals;
    }

    public static ScalarClauseFeature numberOfSymbols() {
	return _numberOfSymbols;
    }

    public 
	static 
	ScalarClauseFeature 
	numberOfTopLevelNonVariableArgumentsInPositiveLiterals() {
	return _numberOfTopLevelNonVariableArgumentsInPositiveLiterals;
    }
    
    public 
	static 
	VectorClauseFeature 
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

	
    public 
	static 
	VectorClauseFeature 
	numberOfSymbolsFromCategoriesInPositiveLiterals(int modulus) {

	Integer key = new Integer(modulus);
	NumberOfSymbolsFromCategoriesInPositiveLiterals result = 
	    _numberOfSymbolsFromCategoriesInPositiveLiterals.get(key);

	if (result == null)
	    {
		result = new NumberOfSymbolsFromCategoriesInPositiveLiterals(modulus);
		_numberOfSymbolsFromCategoriesInPositiveLiterals.put(key,result);
	    };
	return result;
    }

	



    public 
	static 
	VectorClauseFeature 
	numberOfPredicatesFromCategoriesInPositiveLiterals(int modulus) {

	Integer key = new Integer(modulus);
	NumberOfPredicatesFromCategoriesInPositiveLiterals result = 
	    _numberOfPredicatesFromCategoriesInPositiveLiterals.get(key);

	if (result == null)
	    {
		result = new NumberOfPredicatesFromCategoriesInPositiveLiterals(modulus);
		_numberOfPredicatesFromCategoriesInPositiveLiterals.put(key,result);
	    };
	return result;
    }



    public 
	static 
	VectorClauseFeature 
	numberOfPredicatesFromCategoriesInNegativeLiterals(int modulus) {

	Integer key = new Integer(modulus);
	NumberOfPredicatesFromCategoriesInNegativeLiterals result = 
	    _numberOfPredicatesFromCategoriesInNegativeLiterals.get(key);

	if (result == null)
	    {
		result = new NumberOfPredicatesFromCategoriesInNegativeLiterals(modulus);
		_numberOfPredicatesFromCategoriesInNegativeLiterals.put(key,result);
	    };
	return result;
    }



    public 
	static 
	VectorClauseFeature 
	numberOfLiteralHeadersFromCategories(int modulus) {

	Integer key = new Integer(modulus);
	NumberOfLiteralHeadersFromCategories result = 
	    _numberOfLiteralHeadersFromCategories.get(key);

	if (result == null)
	    {
		result = new NumberOfLiteralHeadersFromCategories(modulus);
		_numberOfLiteralHeadersFromCategories.put(key,result);
	    };
	return result;
    }





    
    private static class NumberOfLiterals 
	implements ScalarClauseFeature {
	
	public int evaluate(Collection<Literal> cl) {
	    return cl.size();
	}
	
	public int evaluateTemp(Collection<TmpLiteral> cl) {
	    return cl.size();
	}

    } // class NumberOfLiterals 
    

    private static class NumberOfPositiveLiterals 
	implements ScalarClauseFeature {
	
	public int evaluate(Collection<Literal> cl) {
	    int result = 0;
	    for (Literal lit : cl)
		if (lit.isPositive()) ++result;
	    return result;
	}
	
	public int evaluateTemp(Collection<TmpLiteral> cl) {
	    int result = 0;
	    for (TmpLiteral lit : cl)
		if (lit.isPositive()) ++result;
	    return result;
	}

    } // class NumberOfPositiveLiterals 
    
    
    private static class NumberOfNegativeLiterals 
	implements ScalarClauseFeature {

	public int evaluate(Collection<Literal> cl) {
	    int result = 0;
	    for (Literal lit : cl)
		if (lit.isNegative()) ++result;
	    return result;
	}
	
	public int evaluateTemp(Collection<TmpLiteral> cl) {
	    int result = 0;
	    for (TmpLiteral lit : cl)
		if (lit.isNegative()) ++result;
	    return result;
	}
	
    } // class NumberOfNegativeLiterals 


    private static class NumberOfSymbols 
	implements ScalarClauseFeature {

	public int evaluate(Collection<Literal> cl) {
	    int result = 0;
	    for (Literal lit : cl)
		result += lit.numberOfSymbols();
	    return result;
	}
	
	public int evaluateTemp(Collection<TmpLiteral> cl) {
	    int result = 0;
	    for (TmpLiteral lit : cl)
		result += lit.numberOfSymbols();
	    return result;
	}
	
    } // class NumberOfSymbols 



    private static class NumberOfTopLevelNonVariableArgumentsInPositiveLiterals
	implements ScalarClauseFeature {

	public int evaluate(Collection<Literal> cl) {
	    int result = 0;
	    for (Literal lit : cl)
		if (lit.isPositive())
		    {
			Formula atom = lit.atom();
			if (atom instanceof AtomicFormula)
			    {
				int arity = 
				    ((AtomicFormula)atom).predicate().arity();
				for (int n = 0; n < arity; ++n)
				    {
					Term arg = ((AtomicFormula)atom).arg(n);
					if (!arg.isVariable())
					    ++result;
				    };
			    };
		    };
		
	    return result;
	} // evaluate(Collection<Literal> cl)
	

	public int evaluateTemp(Collection<TmpLiteral> cl) {
	    int result = 0;
	    for (TmpLiteral lit : cl)
		if (lit.isPositive() && lit.isSimple())
		    {
			int arity = lit.predicate().arity();
			Flatterm arg = lit.body().nextCell();
			
			for (int n = 0; n < arity; ++n)
			    {
				if (!arg.isVariable())
				   ++result;
				arg = arg.after();
			    };
		    };
	    return result;
	} // evaluateTemp(Collection<TmpLiteral> cl)
	
    } // class NumberOfTopLevelNonVariableArgumentsInPositiveLiterals






    /** Counts the numbers of symbols belonging to all categories from 
     *  0 to <code>modulus - 1</code>; this is a vector function. 
     */
    private static class NumberOfSymbolsFromCategories
	implements VectorClauseFeature {
	
	/** Will count the number of nonvariable symbols <code>sym</code>,
	 *  including logical symbols, but excluding pair constructors
	 *  and abstraction operators,
	 *  such that <code>sym.category(modulus) == category</code>,
	 *  for every <code>category</code> in <code>[0,modulus - 1]</code>. 
	 */
	public NumberOfSymbolsFromCategories(int modulus) {
	    _modulus = modulus;
	}

	public int[] evaluate(Collection<Literal> cl) {
	    int[] result = new int[_modulus];
	    for (Literal lit : cl)
		{
		    lit.
			addNumberOfNonvariableSymbolsFromCategories(_modulus,
								    result);
		};
	    return result;
	}
	
	public int[] evaluateTemp(Collection<TmpLiteral> cl) {
	    int[] result = new int[_modulus];
	    for (TmpLiteral lit : cl)
		{
		    lit.
			addNumberOfNonvariableSymbolsFromCategories(_modulus,
								    result);
		};
	    return result;
	}
	
	public int size() { return _modulus; }

	private final int _modulus;

    } // class NumberOfSymbolsFromCategories



    /** Counts the numbers of symbols belonging to all categories from 
     *  0 to <code>modulus - 1</code>; this is a vector function. 
     */
    private static class NumberOfSymbolsFromCategoriesInPositiveLiterals
	implements VectorClauseFeature {
	
	/** Will count the number of nonvariable symbols <code>sym</code>
         *  in positive literals,
	 *  including logical symbols, but excluding pair constructors
	 *  and abstraction operators,
	 *  such that <code>sym.category(modulus) == category</code>,
	 *  for every <code>category</code> in <code>[0,modulus - 1]</code>. 
	 */
	public NumberOfSymbolsFromCategoriesInPositiveLiterals(int modulus) {
	    _modulus = modulus;
	}

	public int[] evaluate(Collection<Literal> cl) {
	    int[] result = new int[_modulus];
	    for (Literal lit : cl)
		{
		    if (lit.isPositive())
			lit.
			    addNumberOfNonvariableSymbolsFromCategories(_modulus,
									result);
		};
	    return result;
	}
	
	public int[] evaluateTemp(Collection<TmpLiteral> cl) {
	    int[] result = new int[_modulus];
	    for (TmpLiteral lit : cl)
		{
		    if (lit.isPositive())
			lit.
			    addNumberOfNonvariableSymbolsFromCategories(_modulus,
									result);
		};
	    return result;
	}
	
	public int size() { return _modulus; }

	private final int _modulus;

    } // class NumberOfSymbolsFromCategoriesInPositiveLiterals





    /** Counts the numbers of predicates belonging to all categories from 
     *  0 to <code>modulus - 1</code>; this is a vector function. 
     */
    private static class NumberOfPredicatesFromCategoriesInPositiveLiterals
	implements VectorClauseFeature {
	
	/** Will count the number of predicates <code>pred</code>
         *  in positive literals,
	 *  such that <code>pred.category(modulus) == category</code>,
	 *  for every <code>category</code> in <code>[0,modulus - 1]</code>. 
	 */
	public NumberOfPredicatesFromCategoriesInPositiveLiterals(int modulus) {
	    _modulus = modulus;
	}

	public int[] evaluate(Collection<Literal> cl) {
	    int[] result = new int[_modulus];
	    for (Literal lit : cl)
		{
		    if (lit.isPositive())
			{
			    Formula atom = lit.atom();
			    if (atom instanceof AtomicFormula)
				{
				    Predicate pred = 
					((AtomicFormula)atom).predicate();
				    int category = 
					pred.category(_modulus);
				    result[category]++;
				};
			};
		};
	    return result;
	}
	
	public int[] evaluateTemp(Collection<TmpLiteral> cl) {
	    int[] result = new int[_modulus];
	    for (TmpLiteral lit : cl)
		{
		    if (lit.isPositive() && lit.isSimple())
			{
			    Predicate pred = lit.predicate();
			    int category = 
				pred.category(_modulus);
			    result[category]++;
			};
		};
	    return result;
	}
	
	public int size() { return _modulus; }

	private final int _modulus;

    } // class NumberOfPredicatesFromCategoriesInPositiveLiterals





    /** Counts the numbers of predicates belonging to all categories from 
     *  0 to <code>modulus - 1</code>; this is a vector function. 
     */
    private static class NumberOfPredicatesFromCategoriesInNegativeLiterals
	implements VectorClauseFeature {
	
	/** Will count the number of predicates <code>pred</code>
         *  in negative literals,
	 *  such that <code>pred.category(modulus) == category</code>,
	 *  for every <code>category</code> in <code>[0,modulus - 1]</code>. 
	 */
	public NumberOfPredicatesFromCategoriesInNegativeLiterals(int modulus) {
	    _modulus = modulus;
	}

	public int[] evaluate(Collection<Literal> cl) {
	    int[] result = new int[_modulus];
	    for (Literal lit : cl)
		{
		    if (lit.isNegative())
			{
			    Formula atom = lit.atom();
			    if (atom instanceof AtomicFormula)
				{
				    Predicate pred = 
					((AtomicFormula)atom).predicate();
				    int category = 
					pred.category(_modulus);
				    result[category]++;
				};
			};
		};
	    return result;
	}
	
	public int[] evaluateTemp(Collection<TmpLiteral> cl) {
	    int[] result = new int[_modulus];
	    for (TmpLiteral lit : cl)
		{
		    if (lit.isNegative() && lit.isSimple())
			{
			    Predicate pred = lit.predicate();
			    int category = 
				pred.category(_modulus);
			    result[category]++;
			};
		};
	    return result;
	}
	
	public int size() { return _modulus; }

	private final int _modulus;

    } // class NumberOfPredicatesFromCategoriesInNegativeLiterals





    /** Counts the numbers of literal headers belonging to all categories from 
     *  0 to <code>2*modulus - 1</code>; a literal header category is computed
     *  as polarity + 2*category(predicate); this is a vector function. 
     */
    private static class NumberOfLiteralHeadersFromCategories
	implements VectorClauseFeature {
	
	/** Will count the number of literal headers <code>pol,pred</code>
	 *  such that <code>category(modulus,pol,pred) == category</code>,
	 *  for every <code>category</code> in <code>[0,2*modulus - 1]</code>. 
	 */
	public NumberOfLiteralHeadersFromCategories(int modulus) {
	    _modulus = modulus;
	}

	public int[] evaluate(Collection<Literal> cl) {
	    int[] result = new int[_modulus*2];
	    for (Literal lit : cl)
		{
		    Formula atom = lit.atom();
		    if (atom instanceof AtomicFormula)
			{
			    Predicate pred = 
				((AtomicFormula)atom).predicate();
			    int predCategory = 
				pred.category(_modulus);

			    int category;
			    if (lit.isPositive())
				{
				    category = 2*predCategory + 1;
				}
			    else
				category = 2*predCategory;
			    result[category]++;
			}; // if (atom instanceof AtomicFormula)
		};
	    return result;
	} // evaluate(Collection<Literal> cl)


	
	public int[] evaluateTemp(Collection<TmpLiteral> cl) {
	    int[] result = new int[_modulus*2];
	    for (TmpLiteral lit : cl)
		{
		    if (lit.isSimple())
			{
			    Predicate pred = lit.predicate();
			    
			    int predCategory = 
				pred.category(_modulus);

			    int category;
			    if (lit.isPositive())
				{
				    category = 2*predCategory + 1;
				}
			    else
				category = 2*predCategory;

			    result[category]++;
			}; // if (lit.isSimple())

		};
	    return result;
	} // evaluateTemp(Collection<TmpLiteral> cl)


	
	public int size() { return _modulus*2; }

	private final int _modulus;

    } // class NumberOfLiteralHeadersFromCategories





    private static final ScalarClauseFeature _numberOfLiterals = 
	new NumberOfLiterals();
    private static final ScalarClauseFeature _numberOfPositiveLiterals = 
	new NumberOfPositiveLiterals();
    private static final ScalarClauseFeature _numberOfNegativeLiterals = 
	new NumberOfNegativeLiterals();
    private static final ScalarClauseFeature _numberOfSymbols = 
	new NumberOfSymbols();
    private static final ScalarClauseFeature _numberOfTopLevelNonVariableArgumentsInPositiveLiterals = 
	new NumberOfTopLevelNonVariableArgumentsInPositiveLiterals();

    /** Cache for {@link #NumberOfSymbolsFromCategories} objects. */
    private 
	static 
	final 
	HashMap<Integer,NumberOfSymbolsFromCategories>
	_numberOfSymbolsFromCategories = 
	new HashMap<Integer,NumberOfSymbolsFromCategories>();

    /** Cache for {@link #NumberOfSymbolsFromCategoriesInPositiveLiterals} objects. */
    private 
	static 
	final 
	HashMap<Integer,NumberOfSymbolsFromCategoriesInPositiveLiterals>
	_numberOfSymbolsFromCategoriesInPositiveLiterals = 
	new HashMap<Integer,NumberOfSymbolsFromCategoriesInPositiveLiterals>();
	


    /** Cache for {@link #NumberOfPredicatesFromCategoriesInPositiveLiterals} objects. */
    private 
	static 
	final 
	HashMap<Integer,NumberOfPredicatesFromCategoriesInPositiveLiterals>
	_numberOfPredicatesFromCategoriesInPositiveLiterals = 
	new HashMap<Integer,NumberOfPredicatesFromCategoriesInPositiveLiterals>();


    /** Cache for {@link #NumberOfPredicatesFromCategoriesInNegativeLiterals} objects. */
    private 
	static 
	final 
	HashMap<Integer,NumberOfPredicatesFromCategoriesInNegativeLiterals>
	_numberOfPredicatesFromCategoriesInNegativeLiterals = 
	new HashMap<Integer,NumberOfPredicatesFromCategoriesInNegativeLiterals>();
	

    /** Cache for {@link #NumberOfLiteralHeadersFromCategories} objects. */
    private 
	static 
	final 
	HashMap<Integer,NumberOfLiteralHeadersFromCategories>
	_numberOfLiteralHeadersFromCategories = 
	new HashMap<Integer,NumberOfLiteralHeadersFromCategories>();
	
} // class StandardClauseFeature 