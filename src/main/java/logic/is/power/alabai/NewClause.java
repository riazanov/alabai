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

import logic.is.power.logic_warehouse.*;


/**
 * Representation of newly created clauses intended to support
 * efficient forward processing.
 * Roughly corresponds to the variable "new" in Figure 3.1 in 
 *      and Figure 3.2 in 
 * <a href="{@docRoot}/../references.html#Riazanov_PhD_thesis">[PhD thesis]</a>
 * See also {@link alabai_je.TmpLiteral}.
 */
public class NewClause {
    
    /** Switcheable global context. */
    public static class Context {
	
	public Context(NumericIdSource numericIdSource) {
	    _numericIdSource = numericIdSource;
	}
	
	public static Context current() {
	    return _current;
	}
	
	public static void makeCurrent(Context sd) {
	    _current = sd;
	}

	public long generateFreshNumericId() {
	    return _numericIdSource.generateFreshNumericId();
	}
	

	private static Context _current = null;

	private NumericIdSource _numericIdSource;
	
    } // class Context



    /** <b>post:</b> the object is prepared for creation of
     *  a new temporary clause in it. 
     *  @param variableBank When a new clause is collected in this object,
     *         the variables will be taken from this bank.
     */
    NewClause(Variable.Bank variableBank) {
	//_number = Kernel.current().generateFreshNumericId();
	_number = Context.current().generateFreshNumericId();
	_literals = new LinkedList<TmpLiteral>();
	_flags = new Clause.Flags();
	_parents = new LinkedList<Clause>();
	_variableBank = variableBank;
	_inferences = new InferenceType.Set();
    }

    /** Numeric id of the clause; unique within an Alabai kernel session. */
    public long number() { return _number; }

    public Clause.Flags flags() { return _flags; }

    public boolean flagIsSet(int n) { return _flags.get(n); }

    public void setFlag(int n) { _flags.set(n); }
    
    public void clearFlag(int n) { _flags.clear(n); }

    public void markAsDerivedFrom(int clauseRole) {
      setFlag(Clause.roleToFlag(clauseRole));
    }


    public Collection<TmpLiteral> literals() { return _literals; }
    
    public void addLiteral(TmpLiteral lit) { _literals.addLast(lit); }
    
    public void addParent(Clause parentClause) {
	
	_parents.addLast(parentClause);

	for (int n = 0; n < Clause.Role.allValues().length; ++n)
	    if (parentClause.isDerivedFrom(Clause.Role.allValues()[n]))
		markAsDerivedFrom(Clause.Role.allValues()[n]);
	
    } // addParent(Clause parentClause)


    /** Marks the clause as obtained by using the specified inference
     *  (possibly, among other inferences).
     *  @param infType must be a value from {@link alabai_je.InferenceType}
     */
    public void addInference(int infType) {
	
	assert infType >= 0;
	assert infType <= InferenceType.maxValue();
	
	_inferences.add(infType);

    } // addInference(int infType)


    public final InferenceType.Set inferences() { return _inferences; }


    /** Variable bank associated with this <code>NewClause</code> object. */    
    public final Variable.Bank variableBank() { return _variableBank; }

    /** Parent clauses. */
    public final Collection<Clause> parents() { return _parents; }



    /** Checks if the clause is 
     *  <a href="{@docRoot}/../glossary.html#contradiction_clause">a contradiction</a>.
     */
    public final boolean isContradiction() {
	return _literals.isEmpty();
    }
    
     
    /** Checks if the clause is 
     *  <a href="{@docRoot}/../glossary.html#conditional_contradiction_clause">a conditional contradiction</a>.
     */
    public final boolean isConditionalContradiction() {
	// Some constraint literals but no answer literals:
	for (TmpLiteral lit : _literals)
	    if (!lit.isConstraintLiteral() ||
		lit.isAnswer())
		return false;
	return !_literals.isEmpty();
    }


    public String toString() {

	String result = "#[" + _number + "] ";
	
	Iterator<TmpLiteral> lit = _literals.iterator(); 
	
	if (lit.hasNext())
	{
	    result += lit.next().toString();
	    
	    while (lit.hasNext()) 
		result += " | " + lit.next();
	};

	result += " ;";

	
	// Clause properties:
	
	if (flagIsSet(Clause.FlagNumber.IsDerivedFromAxiom)) 
	    result += " Ax";
	if (flagIsSet(Clause.FlagNumber.IsDerivedFromHypothesis)) 
	    result += " Hyp";
	if (flagIsSet(Clause.FlagNumber.IsDerivedFromNegatedConjecture))
	    result += " NegConj";
	

	// Parents and inferences:
	
	result += " {";
	
	for (Clause parent : _parents)
	    result += " " + parent.number();

	result += " ";

	if (!_inferences.isEmpty())
	    result += "|" + _inferences;

	result += "}";

	return result;
	
    } // toString()

 

    //                       Data:

    private long _number; 

    private LinkedList<TmpLiteral> _literals;

    private Clause.Flags _flags;

    private LinkedList<Clause> _parents;

    /** When a new clause is collected in this object,
     *  the variables will be taken from this bank.
     */
    private Variable.Bank _variableBank;

    private InferenceType.Set _inferences;

}; // class NewClause
