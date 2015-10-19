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
 * Representation of persistent clauses (aka 
 * <a href="{@docRoot}/../glossary.html#kept_clauses">"kept clauses"</a>).
 */
public class Clause implements Comparable<Clause> {
    
    public static class Role {
      
	public static final int Axiom = 0;
	public static final int Hypothesis = 1;
	public static final int NegatedConjecture = 2;
	
	public static int[] allValues() { return _allValues; }

	private static int[] _allValues = 
	    {Axiom, Hypothesis, NegatedConjecture};

    }; // class Role

    /** Mnemonic identifiers of various flags associated with a clause. */    
    public static class FlagNumber {
	
	public final static int IsDerivedFromAxiom = 0;
	
	public final static int IsDerivedFromHypothesis = 1;

	public final static int IsDerivedFromNegatedConjecture = 2;

	/** Indicates that the clause has been discarded. */
	public final static int IsDiscarded = 3;

	

	// IF YOU ADD MORE FLAGS, DON'T FORGET TO UPDATE _MaxFlagValue BELOW!
	
    }; // class FlagNumber


    /** Represents a set of flags associated with a clause; all the flags
     *  are initially clear.
     */
    public static class Flags extends BitSet {

	/** <b>post:</b> all the flags are clear. */
	public Flags() {
	    super();
	}

	public Flags clone() {

	    Flags result = new Flags();
	    
	    ((BitSet)result).or((BitSet)this); // copies the flag values

	    return result;
	}

    } // class Flags
    
    
    /** Switcheable global context. */
    public static class Context {
	
	public Context() {
	    _markUnselectedLiterals = false;
	}
	
	public static Context current() {
	    return _current;
	}
	
	public static void makeCurrent(Context sd) {
	    _current = sd;
	}

	public boolean markUnselectedLiterals() {
	    return _markUnselectedLiterals;
	}

	public void setMarkUnselectedLiterals(boolean fl) {
	    _markUnselectedLiterals = fl;
	}
	

	private static Context _current = null;
	
	private boolean _markUnselectedLiterals;

    } // class Context

    
    public Clause(long number,Collection<Clause> parents) {
	_number = number; 
	_literals = new LinkedList<Literal>();
	_literalsHashCode = hashCodeOfLiterals(_literals);
	_flags = new Flags();
	_inferences = new InferenceType.Set();
	_parents = new LinkedList(parents);
	_selectionUnits = null;
    }
    
    /** Numeric id of the clause; unique within an Alabai kernel session. */
    public final long number() { return _number; }

    public final boolean flagIsSet(int n) {
	assert n >= 0;
	assert n <= _MaxFlagValue;
	return _flags.get(n);
    }

    public final void setFlag(int n) {  
	assert n >= 0;
	assert n <= _MaxFlagValue;
	_flags.set(n); 
    }

    public final void clearFlag(int n) {   
	assert n >= 0;
	assert n <= _MaxFlagValue;
	_flags.clear(n); 
    }
    
    
    public final void assignFlags(Flags f) { 
	_flags = f.clone();
    }
    
    public final void assignInferences(InferenceType.Set inf) {
	_inferences = inf.clone();
    }
    
    public final InferenceType.Set inferences() { return _inferences; }

    /** Checks if the specified inference was used to derive this clause.
     *  @param inferenceType must be one of the values from 
     *         {@link alabai_je.InferenceType}
     */
    public final boolean inferenceWasUsed(int inferenceType) {
	assert inferenceType >= 0;
	assert inferenceType <= InferenceType.maxValue();
	
	return _inferences.contains(inferenceType);
    }
    

    /** Attaches the configuration-specific info to the symbol. */
    public final void setInfo(Object obj) {
	_info = obj;
    }
    
    /** Configuration-specific info attached to the symbol. */
    public final Object info() {
	return _info;
    }

    public final void markAsDiscarded() { setFlag(FlagNumber.IsDiscarded); }
    
    public final boolean isDiscarded() {
	return flagIsSet(FlagNumber.IsDiscarded);
    }
    
    /** Parents of the clause; may be null, but not necessarily, if there are
     *  no parents. 
     */
    public final Collection<Clause> parents() { return _parents; }

    /** Returns all ancestors of this clause, ie, parents, parents 
     *  of parents, etc; they are sorted by their "age", ie, clauses
     *  with smaller values of {@link #number()} will be greater.
     */
    public final SortedSet<Clause> ancestors() {

	TreeSet<Clause> result = new TreeSet<Clause>();
	
	collectAncestors(result);
	
	return result;
    }

    
    /** Collects all ancestors of this clause in the provided collection,
     *  without duplication.
     *  <b>pre:</b> <code>result != null</code>
     */
    public final void collectAncestors(Collection<Clause> result) {
	for (Clause par : _parents)
		if (!result.contains(par))
		    {
			result.add(par);
			par.collectAncestors(result);
		    };
    }

    
    /** At least one of the ancestors or the clause itself 
     *  has role <code>r</code>. 
     */
    public final boolean isDerivedFrom(int clauseRole) {
	return flagIsSet(roleToFlag(clauseRole));
    }

    /** isDerivedFrom(r) and this is false for any other roles. */ 
    public final boolean isDerivedExclusivelyFrom(int clauseRole) {	
	if (!flagIsSet(roleToFlag(clauseRole))) return false;
	int[] allRoles = Role.allValues();
	for (int i = 0; i < allRoles.length; ++i)
	    if (clauseRole != allRoles[i] && flagIsSet(roleToFlag(allRoles[i])))
		return false;
	return true;
    }

    /** @return isDerivedExclusivelyFrom(Role.Axiom) */
    public final boolean isDerivedExclusivelyFromAxioms() {
	return isDerivedExclusivelyFrom(Role.Axiom);
    }
    
    /** The contents of the returned collection should not be modified! 
     *  Literals can only be added with addLiteral().
     */
    public final Collection<Literal> literals() { return _literals; }

    /** Hash code of literals(); corresponds to 
     *  hashCode(Collection<TmpLiteral>) 
     *  to facilitate quick equality checks between Collection<TmpLiteral>
     *  and Clause.
     */
    public final int literalsHashCode() {
	assert _literalsHashCode == hashCodeOfLiterals(_literals);
	return _literalsHashCode;
    }
    
    /** Hash code of the literal list; corresponds to literalsHashCode()
     *  and hashCodeOfLiterals(Collection<Literal> lits)
     *  to facilitate quick equality checks between Collection<TmpLiteral>
     *  and Clause.
     */
    public static int hashCode(Collection<TmpLiteral> lits) {
	int result = 0;
	for (TmpLiteral lit : lits)
	    result = (result * 2) + lit.body().hashCode();
	return result;
    }




    /** Checks if the initial version of the clause comes from the input;
     *  NOTE that this does not guarantee {@link #isStrictlyInput()}
     *  because some rules could have been applied to obtain the current
     *  form of the clause.
     */
    public final boolean isInput() {
	return _inferences.contains(InferenceType.Input);
    }

    public final boolean isStrictlyInput() {
	return _inferences.containsOnly(InferenceType.Input);
    }

    /** Checks if clausification has to be applied to this (generalised)
     *  clause, ie, at least one of the literals is 
     *  <a href="{@docRoot}/../glossary.html#complex_literal">complex</a>.
     */
    public final boolean isForClausification() {
	for (Literal lit : _literals)
	    if (lit.isGeneral()) return true;
	return false;
    }

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
	for (Literal lit : _literals)
	    if (!lit.isConstraintLiteral() ||
		lit.isAnswer())
		return false;
	return !_literals.isEmpty();
    }
    

    /** Checks if the clause is 
     *  <a href="{@docRoot}/../glossary.html#terminal_clause">terminal</a>.
     */
    public final boolean isTerminal() {
	return isContradiction() || 
	    isConditionalContradiction() ||
	    isAnswer();
    }

    /** Checks if the clause is 
     *  <a href="{@docRoot}/../glossary.html#answer_clause">an answer clause</a>.
     */
    public final boolean isAnswer() {
	// Some answer literals and, possibly, some other constraint literals:
	boolean result = false;
	for (Literal lit : _literals)
	    if (lit.isAnswer())
		{
		    result = true;
		}
	    else if (!lit.isConstraintLiteral())
		return false;
	return result;
    }



    /** Number of all literals of all kinds in this clause. */
    public final int totalNumberOfLiterals() { return _literals.size(); }


    public final void addLiteral(Literal lit) {
	_literals.addLast(lit);
	_literalsHashCode = hashCodeOfLiterals(_literals);
    }

    
    /** Clauses are ordered by "age": a clause with smaller
     *  {@link #number()} will be greater in the ordering.
     */
    public final int compareTo(Clause cl) {
	if (number() == cl.number()) return 0;
	return (number() > cl.number())? 1 : -1;
    }
    
    
    /** Registers the selection unit as based on this clause,
     *  so that it is later available in {@link #selectionUnits()}.
     *  <b>pre:</b> <code>su.clause() == this</code>.
     */
    public final void registerSelectionUnit(FineSelectionUnit su) {
	assert su.clause() == this;
	su.setNext(_selectionUnits);
	_selectionUnits = su;
    }

    /** Simple linked list of selection units based on this clause;
     *  selectionUnits() is the first element in the list (unless it
     *  is null), selectionUnits.next() is the second element, and so on.
     */
    public final FineSelectionUnit selectionUnits() {
	return _selectionUnits;
    }

    /** Releases the references to all selection units associated with
     *  this clause.
     */
    public final void discardSelectionUnits() {
	_selectionUnits = null;
    }


    public final void collectFreeVariables(Collection<Variable> result) {
	for (Literal lit : literals())
	    lit.collectFreeVariables(result);
    }


    public String toString() {

	boolean markUnsel = 
	    Context.current() != null &&
	    Context.current().markUnselectedLiterals();

	// Indicates what kind of literals we have been 
	// printing lately:
	int state = 0; // 0 = just started or selected or unknown,
	               // 1 = unselected, 2 = possibly unselected

	String result = "#[" + _number + "] ";
	
	Iterator<Literal> iter = _literals.iterator(); 
	
	if (iter.hasNext())
	{
	    Literal lit = iter.next();

	    if (markUnsel)
		if (lit.isUnselected())
		    {
			result += "[";
			state = 1;
		    }
		else if (lit.isPossiblyUnselected())
		    {
			result += "{";
			state = 2;
		    }
		else
		    state = 0;

	    result += lit.toString();
	    
	    while (iter.hasNext())
		{
		    lit = iter.next();
		    	
		    if (markUnsel)
			if (lit.isUnselected())
			    {
				if (state == 2) result += "}";	
			    }
			else if (lit.isPossiblyUnselected())
			    {
				if (state == 1) result += "]";
			    }
			else
			    {
				if (state == 2) 
				    {
					result += "}";
				    }
				else if (state == 1) 
				    result += "]";
			    };
		    
		    result += " | ";

		    if (markUnsel)
			if (lit.isUnselected())
			    {
				if (state != 1)
				    {
					result += "[";
					state = 1;
				    };
			    }
			else if (lit.isPossiblyUnselected())
			    {
				if (state != 2)
				    {
					result += "{";
					state = 2;
				    };
			    }
			else
			    state = 0;

		    result += lit.toString();

		}; // while (iter.hasNext())
	}; // if (iter.hasNext())

	
	if (markUnsel)
	    if (state == 1)
		{
		    result += "]";
		}
	    else if (state == 2)
		{
		    result += "}";
		};

	result += " ;";

	
	// Clause properties:
	
	if (flagIsSet(FlagNumber.IsDerivedFromAxiom)) 
	    result += " Ax";
	if (flagIsSet(FlagNumber.IsDerivedFromHypothesis)) 
	    result += " Hyp";
	if (flagIsSet(FlagNumber.IsDerivedFromNegatedConjecture))
	    result += " NegConj";
	

	// Parents and inferences:
	
	result += " {";
	
	for (Clause parent : _parents)
	    result += " " + parent.number();

	result += " ";

	if (!_inferences.isEmpty())
	    result += "|" + _inferences;

	result += "}";

	if (_info != null) result += " <<< " + _info + ">>>";

	return result;
    } // toString()


    //                      Package access methods:

    static int roleToFlag(int clauseRole) {
	switch (clauseRole)
	{
	    case Role.Axiom: return FlagNumber.IsDerivedFromAxiom;
		
	    case Role.Hypothesis: return FlagNumber.IsDerivedFromHypothesis;
		
	    case Role.NegatedConjecture:
		return FlagNumber.IsDerivedFromNegatedConjecture;
	};
	
	assert false;
	return -1000;
    } // roleToFlag(Role r)

    
    //                       Private methods:


    /** Hash code of the literal list; corresponds to 
     *  hashCode(Collection<TmpLiteral> lits)
     *  to facilitate quick equality checks between Collection<TmpLiteral>
     *  and Clause.
     */
    private static int hashCodeOfLiterals(Collection<Literal> lits) {
	int result = 0;
	for (Literal lit : lits)
	    result = (result * 2) + lit.body().hashCode();
	return result;
    }
    

    //                        Data:

    /** Total number of valid flags; <code>fl</code> is valid iff
     *  <code>0 <= fl <= _MaxFlagValue</code>.
     */
    private static final int _MaxFlagValue = 3;

    private long _number;

    private LinkedList<Literal> _literals; 

    /** Hash code for _literals, to facilitate quick equality checks. */
    private int _literalsHashCode;

    private Flags _flags;
    
    private InferenceType.Set _inferences;

    private LinkedList<Clause> _parents;

    /** Simple linked list of selection units based on this clause;
     *  _selectionUnits is the first element in the list,
     *  _selectionUnits.next() is the second element, and so on.
     */
    private FineSelectionUnit _selectionUnits;

    
    /** Configuration-specific info that may be attached to a clause. */
    private Object _info;

    

} // class Clause 
