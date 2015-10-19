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

/** Consequtive int constants, ranging from 0 to InferenceType.maxValue(),
 *  that serve as labels for various inference types; NOTE that the concrete
 *  values may change in later versions.
 */
public class InferenceType {

    /** Relatively compact representation of sets of inference
     *  type labels, based on java.util.BitSet.
     *  TODO: agressive sharing for values.
     */
    public static class Set extends BitSet {
	
	/** Empty set. */
	public Set() { super(); }
	
	public final void add(int infType) {
	    assert infType >= 0;
	    assert infType <= maxValue();
	    set(infType);
	}
	
	
	public final void remove(int infType) {
	    assert infType >= 0;
	    assert infType <= maxValue();
	    clear(infType);
	}
	
	public final boolean contains(int infType) {
	    assert infType >= 0;
	    assert infType <= maxValue();
	    return get(infType);
	}

	public final boolean containsOnly(int infType) {
	    assert infType >= 0;
	    assert infType <= maxValue();
	    
	    return get(infType) && cardinality() == 1;
	}

	public Set clone() {
	    Set result = new Set();
	    ((BitSet)result).or((BitSet)this);
	    return result;
	}

	public String toString() {
	    return toString(" ");
	}
	
	public final String toString(String separator) {
	    String result = "";
	    int printed = 0;
	    for (int n = 0; n <= maxValue(); ++n)
		if (get(n))
		    {
			result += toAbbreviatedString(n);
			++printed;
			if (printed < cardinality()) result += separator;
		    }
	    return result;
	}
	


    } // class Set


    
    //               Values:
    

    public static final int Input = 0;
    public static final int Copy = 1;
    
    
    // Inferences on clauses:

    public static final int Resolution = 2;
    public static final int ForwardParamodulation = 3;
    public static final int BackwardParamodulation = 4;
    public static final int EqualityFactoring = 5;
    public static final int EqualityResolution = 6;
    public static final int ForwardDemodulation = 7;
    public static final int BackwardDemodulation = 8;
    public static final int ForwardSubsumptionResolution = 9;
    public static final int BackwardSubsumptionResolution = 10;

    // Clausification inferences:
    
    public static final int DoubleNegationCancellation = 11;
    public static final int NegatedConjunction = 12;
    public static final int NegatedDisjunction = 13;
    public static final int NegatedEquivalence = 14;
    public static final int NegatedImplication = 15;
    public static final int NegatedReverseImplication = 16;
    public static final int NegatedNotEquivalent = 17;
    public static final int NegatedNotOr = 18;
    public static final int NegatedNotAnd = 19;
    public static final int NegatedForAll = 20;
    public static final int NegatedExist = 21;
    public static final int ConjunctionElimination = 22;
    public static final int DisjunctionElimination = 23;
    public static final int EquivalenceElimination = 24;
    public static final int ImplicationElimination = 25;
    public static final int ReverseImplicationElimination = 26;
    public static final int NotEquivalentElimination = 27;
    public static final int NotOrElimination = 28;
    public static final int NotAndElimination = 29;
    public static final int ForAllElimination = 30;
    public static final int ExistElimination = 31;
    public static final int NegatedTruthConstant = 32;



    public static int maxValue() { return 32; } 


    /** Full name for the given label. */
    public static String toString(int infType) {

	switch (infType)
	    {		
	    case Input: return "Input";
	    case Copy: return "Copy";

	    case Resolution: return "Resolution";
	    case ForwardParamodulation: return "ForwardParamodulation";
	    case BackwardParamodulation: return "BackwardParamodulation";
	    case EqualityFactoring: return "EqualityFactoring";
	    case EqualityResolution: return "EqualityResolution";
	    case ForwardDemodulation: return "ForwardDemodulation"; 
	    case BackwardDemodulation: return "BackwardDemodulation"; 
	    case ForwardSubsumptionResolution: 
		return "ForwardSubsumptionResolution";		
	    case BackwardSubsumptionResolution: 
		return "BackwardSubsumptionResolution";	
		
		
	    case DoubleNegationCancellation:
		return "DoubleNegationCancellation";
	    case NegatedConjunction:
		return "NegatedConjunction";
	    case NegatedDisjunction:
		return "NegatedDisjunction";
	    case NegatedEquivalence: 
		return "NegatedEquivalence";
	    case NegatedImplication: 
		return "NegatedImplication";
	    case NegatedReverseImplication:
		return "NegatedReverseImplication";
	    case NegatedNotEquivalent:
		return "NegatedNotEquivalent";
	    case NegatedNotOr: 
		return "NegatedNotOr";
	    case NegatedNotAnd:
		return "NegatedNotAnd";
	    case NegatedForAll:
		return "NegatedForAll";
	    case NegatedExist: 
		return "NegatedExist";
	    case ConjunctionElimination: 
		return "ConjunctionElimination";
	    case DisjunctionElimination:
		return "DisjunctionElimination";
	    case EquivalenceElimination:
		return "EquivalenceElimination";
	    case ImplicationElimination: 
		return "ImplicationElimination";
	    case ReverseImplicationElimination: 
		return "ReverseImplicationElimination";
	    case NotEquivalentElimination: 
		return "NotEquivalentElimination";
	    case NotOrElimination: 
		return "NotOrElimination";
	    case NotAndElimination: 
		return "NotAndElimination";
	    case ForAllElimination: 
		return "ForAllElimination";
	    case ExistElimination: 
		return "ExistElimination";
	    case NegatedTruthConstant: 
		return "NegatedTruthConstant";
		
	    }; // switch (infType)
	
	throw new Error("Bad value of infType.");

    } // toString(int infType)


    /** Abbreviation for the given label. */
    public static String toAbbreviatedString(int infType) {

	switch (infType)
	    {		
	    case Input: return "In";
	    case Copy: return "Cp";


	    case Resolution: return "Res";
	    case ForwardParamodulation: return "FPar";
	    case BackwardParamodulation: return "BPar";
	    case EqualityFactoring: return "EqF";
	    case EqualityResolution: return "EqRes";
	    case ForwardDemodulation: return "FDem"; 
	    case BackwardDemodulation: return "BDem"; 
	    case ForwardSubsumptionResolution: 
		return "FSubsRes";		
	    case BackwardSubsumptionResolution: 
		return "BSubsRes";	


	    case DoubleNegationCancellation:
		return "DNegCanc";
	    case NegatedConjunction:
		return "NegConj";
	    case NegatedDisjunction:
		return "NegDisj";
	    case NegatedEquivalence: 
		return "NegEq";
	    case NegatedImplication: 
		return "NegImpl";
	    case NegatedReverseImplication:
		return "NegRImpl";
	    case NegatedNotEquivalent:
		return "NegNEq";
	    case NegatedNotOr: 
		return "NegNOr";
	    case NegatedNotAnd:
		return "NegNAnd";
	    case NegatedForAll:
		return "NegFAll";
	    case NegatedExist: 
		return "NegEx";
	    case ConjunctionElimination: 
		return "ConjElim";
	    case DisjunctionElimination:
		return "DisjElim";
	    case EquivalenceElimination:
		return "EqElim";
	    case ImplicationElimination: 
		return "ImplElim";
	    case ReverseImplicationElimination: 
		return "RImplElim";
	    case NotEquivalentElimination: 
		return "NEqElim";
	    case NotOrElimination: 
		return "NOrElim";
	    case NotAndElimination: 
		return "NAndElim";
	    case ForAllElimination: 
		return "FAllElim";
	    case ExistElimination: 
		return "ExElim";
	    case NegatedTruthConstant: 
		return "NegTrConst";
		

	
	    }; // switch (infType)
	
	throw new Error("Bad value of infType.");

    } // toAbbreviatedString(int infType)
    

} // class InferenceType
