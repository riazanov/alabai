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



/**
 * Objects accumulating various statistics on inferences, both deduction
 * and simplifying ones, as well as on clauses and fine selection units.
 * Supports switcheable static access.
 */
public class Statistics {


    public class ClausificationInferences {
	
	/** <b>post:</b> as after <code>reset()</code>. */
	public ClausificationInferences() { reset(); }

	/** Sets all counters to the initial values. */ 
	public void reset() {
  
	    skolemisation = 0;
	    negationDistribution = 0;
	    doubleNegationCancellation = 0;
	    conjunctionElimination = 0;
	    explicitUniversalQuantifierElimination = 0;
	    explicitDisjunctionElimination = 0;
	    secondaryConnectiveElimination = 0;
	    negatedTruthConstantRewriting = 0;
	}
	
	/** Adds the values from <code>stat</code> to the corresponing counters
	 *  in this object.
	 */
	public void absorb(ClausificationInferences stat) {
  
	    skolemisation += stat.skolemisation;
	    negationDistribution += stat.negationDistribution;
	    doubleNegationCancellation += stat.doubleNegationCancellation;
	    conjunctionElimination += stat.conjunctionElimination;
	    explicitUniversalQuantifierElimination += 
		stat.explicitUniversalQuantifierElimination;
	    explicitDisjunctionElimination += stat.explicitDisjunctionElimination;
	    secondaryConnectiveElimination += stat.secondaryConnectiveElimination;
	    
	    negatedTruthConstantRewriting += stat.negatedTruthConstantRewriting;
	}
	
      
	/** Checks if some of the counters have non-initial values. */
	public boolean somethingRegistered() {
	    return 
		skolemisation != 0 ||
		negationDistribution != 0 ||
		doubleNegationCancellation != 0 ||
		conjunctionElimination != 0 ||
		explicitUniversalQuantifierElimination != 0 ||
		explicitDisjunctionElimination != 0 ||
		secondaryConnectiveElimination != 0 ||
		negatedTruthConstantRewriting != 0;
	}
            
	/** Total number of accomplished clausification inferences. */
	public long accomplished() {
	    return 
		skolemisation +
		negationDistribution +
		doubleNegationCancellation +
		conjunctionElimination +
		explicitUniversalQuantifierElimination +
		explicitDisjunctionElimination +
		secondaryConnectiveElimination +
		negatedTruthConstantRewriting;
	}
            
      
	/** Checks if this clausification statistics is consistent
	 *  with the specified statistics on clauses.
	 */
	public boolean isConsistentWith(GeneratedClauses stat) {
	    return accomplished() == stat.byClausification;
	}
            

	public String toString() {


	    String result = "%  Clausification inferences " + accomplished() + "\n";
	    if (skolemisation != 0)
		result += "%    skolemisation        " + skolemisation + "\n";
	    if (negationDistribution != 0)
		result += "%    neg. distribution    " + negationDistribution + "\n";
	    if (doubleNegationCancellation != 0)
		result += "%    double neg. canc.    " + doubleNegationCancellation + "\n";
	    if (conjunctionElimination != 0)
		result += "%    conj. elimination    "  + conjunctionElimination + "\n";
	    if (explicitUniversalQuantifierElimination != 0)
		result += "%    univ. quant. elim.   " + explicitUniversalQuantifierElimination 
		    + "\n";
	    if (explicitDisjunctionElimination != 0)
		result += "%    disj. elimination    " + explicitDisjunctionElimination + "\n";
	    if (secondaryConnectiveElimination != 0)
		result += "%    sec. con. elim.      " + secondaryConnectiveElimination + "\n";
	    if (negatedTruthConstantRewriting != 0)
		result += "%    neg. tr. const. rew. " + negatedTruthConstantRewriting + "\n";
    
	    return result;

	} // toString()
            
      
	//                       Counters:


	/** Counts skolemisation inferences. */
	public long skolemisation;
       
	/** Counts negation distribution inferences like 
	 *  <code>~(p & q) --> ~p , ~q</code> or 
	 *  <code>~(p | q) --> ~p ;  ~q</code>, etc.
	 */
	public long negationDistribution;
      
	/** Counts inferences like <code>~(~p) --> p</code>. */
	public long doubleNegationCancellation;


	public long conjunctionElimination;

	/** Counts inferences like <code>forall x (p(x)) --> p(x)</code>. */
	public long explicitUniversalQuantifierElimination;

	/** Counts inferences like <code>p | q --> p, q</code>. */
	public long explicitDisjunctionElimination;

	/** Counts inferences like <code>p => q --> ~p, q</code>,
	 *  or <code>p <=> q --> ~p, q ;  p, ~q</code>, etc.
	 */
	public long secondaryConnectiveElimination;
  

	/** Counts inferebces like <code>~$true --> $false</code> and
	 *  <code>~$false --> $true</code>.
	 */
	public long negatedTruthConstantRewriting;

    }; // class ClausificationInferences





    public static class DeductionInferences {
      
      
	/** Represents statistics on one particular rule. */
	public static class RuleProfile {
				
	    public RuleProfile() { reset(); }

	    /** Sets all counters to the initial values. */ 
	    public void reset() {
		accomplished = 0;
		redundantDiscarded = 0;
	    }
	
	
	    /** Adds the values from <code>stat</code> to the corresponing counters
	     *  in this object.
	     */
	    public void absorb(RuleProfile stat) {  
		accomplished += stat.accomplished;
		redundantDiscarded += stat.redundantDiscarded;
	    }
	
	
	    /** Checks if some of the counters have non-initial values. */
	    public boolean somethingRegistered() {
		return accomplished != 0 || redundantDiscarded != 0;
	    }
            


	    public String toString() { 
		return accomplished + ", redundant " + redundantDiscarded;
	    }
            
      
	
	    public long accomplished;

	    public long redundantDiscarded;

	}; // class RuleProfile
      


    
	/** <b>post:</b> as after <code>reset()</code>. */
	public DeductionInferences() { 

	    resolution = new RuleProfile();
	    forwardParamodulation = new RuleProfile();
	    backwardParamodulation = new RuleProfile();
	    equalityFactoring = new RuleProfile();
	    equalityResolution = new RuleProfile();

	}

	/** Sets all counters to the initial values. */ 
	public void reset() { 
	    resolution.reset();
	    forwardParamodulation.reset();
	    backwardParamodulation.reset();
	    equalityFactoring.reset();
	    equalityResolution.reset();
	}
	

	/** Adds the values from <code>stat</code> to the corresponing counters
	 *  in this object.
	 */
	public void absorb(DeductionInferences stat) {
	    resolution.absorb(stat.resolution);
	    forwardParamodulation.absorb(stat.forwardParamodulation);
	    backwardParamodulation.absorb(stat.backwardParamodulation);
	    equalityFactoring.absorb(stat.equalityFactoring);
	    equalityResolution.absorb(stat.equalityResolution);
	}
	
      
	
	/** Total number of accomplished deduction inferences. */
	public long accomplished() {
	    return
		resolution.accomplished +
		forwardParamodulation.accomplished +
		backwardParamodulation.accomplished +
		equalityFactoring.accomplished +
		equalityResolution.accomplished;
	}
            

	public boolean isConsistentWith(GeneratedClauses stat) { 
	    return
		resolution.accomplished == stat.byResolution &&
		forwardParamodulation.accomplished == stat.byForwardParamodulation &&
		backwardParamodulation.accomplished == stat.byBackwardParamodulation &&
		equalityFactoring.accomplished == stat.byEqualityFactoring &&
		equalityResolution.accomplished == stat.byEqualityResolution;
	}
            
      
	public String toString() {

	    String result = "%  Deduction inferences " + accomplished() + "\n";

	    if (resolution.somethingRegistered())
		result +="%    resolution              " + resolution + "\n";
	    if (forwardParamodulation.somethingRegistered())
		result +="%    forward paramadulation  " + forwardParamodulation + "\n";
	    if (backwardParamodulation.somethingRegistered())
		result +="%    backward paramadulation " + backwardParamodulation  + "\n";
	    if (equalityFactoring.somethingRegistered())
		result +="%    eq. factoring           " + equalityFactoring  + "\n";
	    if (equalityResolution.somethingRegistered())
		result +="%    eq. resolution          " +  equalityResolution + "\n";
  
	    return result;
	}
            
      

    
      
	public RuleProfile resolution;
      
	public RuleProfile forwardParamodulation;
      
	public RuleProfile backwardParamodulation;
	      
	public RuleProfile equalityFactoring;
	      
	public RuleProfile equalityResolution;
      
    }; // class DeductionInferences






    public static class ForwardSimplifyingInferences {
      
    
      
	/** <b>post:</b> as after <code>reset()</code>. */
	public ForwardSimplifyingInferences() { reset(); }
      
      
	/** Sets all counters to the initial values. */ 
	public void reset() {
	    propositionalTautologyDeletion = 0;
	    equationalTautologyDeletion = 0;
	    identicalLiteralFactoring = 0;
	    answerFactoring = 0;
	    simplifyingEqualityResolution = 0;
	    subsumption = 0;
	    subsumptionResolution = 0;
	    demodulation = 0;
	}
	
	
	/** Adds the values from <code>stat</code> to the corresponing counters
	 *  in this object.
	 */
	public void absorb(ForwardSimplifyingInferences stat) {
	    propositionalTautologyDeletion = stat.propositionalTautologyDeletion;
	    equationalTautologyDeletion = stat.equationalTautologyDeletion;
	    identicalLiteralFactoring = stat.identicalLiteralFactoring;
	    answerFactoring = stat.answerFactoring;
	    simplifyingEqualityResolution = stat.simplifyingEqualityResolution;
	    subsumption = stat.subsumption;
	    subsumptionResolution = stat.subsumptionResolution;
	    demodulation = stat.demodulation;
	}
	
      
	/** Checks if some of the counters have non-initial values. */
	public boolean somethingRegistered() {
	    return
		propositionalTautologyDeletion != 0 ||
		equationalTautologyDeletion != 0 ||
		identicalLiteralFactoring != 0 ||
		answerFactoring != 0 ||
		simplifyingEqualityResolution != 0 ||
		subsumption != 0 ||
		subsumptionResolution != 0 ||
		demodulation != 0 ;
	}
            
            
	/** Total number of accomplished clausification inferences. */
	public long accomplished() {    
	    return
		propositionalTautologyDeletion +
		equationalTautologyDeletion +
		identicalLiteralFactoring +
		answerFactoring +
		simplifyingEqualityResolution +
		subsumption +
		subsumptionResolution +
		demodulation;
	}
            
      
	/** Checks if this forward simplification statistics is consistent
	 *  with the specified statistics on clauses.
	 */
	public boolean isConsistentWith(GeneratedClauses stat) {
	    return
		propositionalTautologyDeletion == stat.discardedAsPropositionalTautologies &&
		equationalTautologyDeletion == stat.discardedAsEquationalTautologies &&
		subsumption == stat.discardedBySubsumption;
	}
            

	public String toString() {

	    String result = "%  Forward simplification inferences " + accomplished() + "\n";

	    if (propositionalTautologyDeletion != 0)
		result += "%    prop. taut. del.  " + propositionalTautologyDeletion + "\n";
	    if (equationalTautologyDeletion != 0)
		result += "%    eq. taut. del.    " + equationalTautologyDeletion + "\n";
	    if (identicalLiteralFactoring != 0)
		result += "%    ident. lit. fact. " + identicalLiteralFactoring + "\n";
	    if (answerFactoring != 0)
		result += "%    answer factoring  " + answerFactoring + "\n";
	    if (simplifyingEqualityResolution != 0)
		result += "%    simpl. eq. res.   " + simplifyingEqualityResolution + "\n";
	    if (subsumption != 0)
		result += "%    subsumption       " + subsumption + "\n";
	    if (subsumptionResolution != 0)
		result += "%    subs. resolution  " + subsumptionResolution + "\n";
	    if (demodulation != 0)
		result += "%    demodulation      " + demodulation + "\n";

	    return result;

	} // toString()
            
      
    

	public long propositionalTautologyDeletion;

	public long equationalTautologyDeletion;
      
	/** Counts inferences like <code>p | p | C --> p | C</code>. */
	public long identicalLiteralFactoring;

	/** Counts answer factoring inferences. */
	public long answerFactoring;

	/** Counts inferences like <code>s != s | C --> C</code>. */
	public long simplifyingEqualityResolution;

	public long subsumption;

	public long subsumptionResolution;
      
	public long demodulation;

    }; // class ForwardSimplifyingInferences





    public static class BackwardSimplifyingInferences {
      
    
      
	/** <b>post:</b> as after <code>reset()</code>. */
	public BackwardSimplifyingInferences() { reset(); }
      
      
	/** Sets all counters to the initial values. */ 
	public void reset() {    
	    subsumption = 0;
	    subsumptionResolution = 0;
	    demodulation = 0;
	}
	
	
	/** Adds the values from <code>stat</code> to the corresponing counters
	 *  in this object.
	 */
	public void absorb(BackwardSimplifyingInferences stat) {    
	    subsumption += stat.subsumption;
	    subsumptionResolution += stat.subsumptionResolution;
	    demodulation += stat.demodulation;
	}
	
      
	/** Checks if some of the counters have non-initial values. */
	public boolean somethingRegistered() {    
	    return 
		subsumption != 0 ||
		subsumptionResolution != 0 ||
		demodulation != 0;
	}
            
            
	/** Total number of accomplished clausification inferences. */
	public long accomplished() {    
	    return 
		subsumption +
		subsumptionResolution +
		demodulation;
	}
            
      
	/** Checks if this backward simplification statistics is consistent
	 *  with the specified statistics on clauses.
	 */
	public boolean isConsistentWith(KeptClauses stat) {    
	    return
		subsumption == stat.discardedBySubsumption &&
		subsumptionResolution == stat.discardedAsSimplifiedBySubsumptionResolution &&
		demodulation == stat.discardedAsSimplifiedByDemodulation;
	}
            

	/** Checks if this backward simplification statistics is consistent
	 *  with the specified statistics on clauses.
	 */
	public boolean isConsistentWith(GeneratedClauses stat) {    
	    return
		subsumptionResolution == stat.byBackwardSubsumptionResolution &&
		demodulation == stat.byBackwardDemodulation;
	}
            

	public String toString() {
	    
	    String result = "%  Backward simplification inferences " + accomplished() + "\n";
    
	    if (subsumption != 0)
		result += "%    subsumption      " + subsumption + "\n";
	    if (subsumptionResolution != 0)
		result += "%    subs. resolution " + subsumptionResolution + "\n";
	    if (demodulation != 0)
		result += "%    demodulation     " + demodulation + "\n";

	    return result;    

	} // toString()
            
     

	public long subsumption;

	public long subsumptionResolution;
      
	public long demodulation;

    }; // class BackwardSimplifyingInferences





    public static class GeneratedClauses {
      
    

	/** <b>post:</b> as after <code>reset()</code>. */
	public GeneratedClauses() { reset(); }

	/** Sets all counters to the initial values. */ 
	public void reset() {
	    input = 0;
	    copied = 0;
	    byClausification = 0;
	    byResolution = 0;
	    byForwardParamodulation = 0;	
	    byBackwardParamodulation = 0;	
	    byEqualityFactoring = 0;
	    byEqualityResolution = 0;
	    byBackwardDemodulation = 0;
	    byBackwardSubsumptionResolution = 0;
	    discardedAsPropositionalTautologies = 0;
	    discardedAsEquationalTautologies = 0;
	    discardedBySubsumption = 0;
	    discardedAsUnfactorableAnswers = 0;
	    discardedDueToUnsatisfiableConstraints = 0;
	    discardedAsAbsoluteContradictions = 0;
	    discardedAsConditionalContradictions = 0;
	} // reset()
	
	
	/** Adds the values from <code>stat</code> to the corresponing counters
	 *  in this object.
	 */
	public void absorb(GeneratedClauses stat) {    
	    input += stat.input; 
	    copied += stat.copied;
	    byClausification += stat.byClausification;
	    byResolution += stat.byResolution;
	    byForwardParamodulation += stat.byForwardParamodulation;	
	    byBackwardParamodulation += stat.byBackwardParamodulation;	
	    byEqualityFactoring += stat.byEqualityFactoring;
	    byEqualityResolution += stat.byEqualityResolution;
	    byBackwardDemodulation += stat.byBackwardDemodulation;
	    byBackwardSubsumptionResolution += stat.byBackwardSubsumptionResolution;
	    discardedAsPropositionalTautologies += stat.discardedAsPropositionalTautologies;
	    discardedAsEquationalTautologies += stat.discardedAsEquationalTautologies;
	    discardedBySubsumption += stat.discardedBySubsumption;
	    discardedAsUnfactorableAnswers += 
		stat.discardedAsUnfactorableAnswers;
	    discardedDueToUnsatisfiableConstraints += 
		stat.discardedDueToUnsatisfiableConstraints;
	    discardedAsAbsoluteContradictions += 
		stat.discardedAsAbsoluteContradictions;
	    discardedAsConditionalContradictions += 
		stat.discardedAsConditionalContradictions;
	} // absorb(GeneratedClauses stat)
	
      
	
	public long totalGenerated() {    
	    return 
		input +
		copied +
		byClausification +
		byResolution +
		byForwardParamodulation +	
		byBackwardParamodulation +	
		byEqualityFactoring +
		byEqualityResolution +
		byBackwardDemodulation +
		byBackwardSubsumptionResolution;
	}
            

	/** The number of clauses discarded as redundant. */
	public long redundantDiscarded() {    
	    return 
		discardedAsPropositionalTautologies +
		discardedAsEquationalTautologies +
		discardedBySubsumption +
		discardedAsUnfactorableAnswers +
		discardedDueToUnsatisfiableConstraints +
		discardedAsAbsoluteContradictions +
		discardedAsConditionalContradictions;
	}
            

	public String toString() {

	    String result = "%  Generated clauses " + totalGenerated() + "\n";
	    result += "%      input                 " + input + "\n";
	    result += "%      copied                " + copied + "\n";
	    if (byClausification != 0)
		result += "%      by clausification     " + byClausification + "\n";
	    if (byResolution != 0)
		result += "%      by resolution         " + byResolution + "\n";
	    if (byForwardParamodulation != 0)
		result += "%      by forward paramod.   " + byForwardParamodulation + "\n";
	    if (byBackwardParamodulation != 0)
		result += "%      by backward paramod.  " + byBackwardParamodulation + "\n";
	    if (byEqualityFactoring != 0)
		result += "%      by eq. factoring      " + byEqualityFactoring + "\n";
	    if (byEqualityResolution != 0)
		result += "%      by eq. resolution     " + byEqualityResolution + "\n";
	    if (byBackwardDemodulation != 0)
		result += "%      by backward demod.    " + byBackwardDemodulation + "\n";
	    if (byBackwardSubsumptionResolution != 0)
		result += "%      by back. subs. resol. " + byBackwardSubsumptionResolution + "\n";
    
	    result += "%    Redundant discarded " + redundantDiscarded() + "\n";
	    if (discardedAsPropositionalTautologies != 0)
		result += "%      prop. tautologies " + discardedAsPropositionalTautologies + "\n"; 
	    if (discardedAsEquationalTautologies != 0)
		result += "%      eq. tautologies   " + discardedAsEquationalTautologies + "\n"; 
	    if (discardedBySubsumption != 0)
		result += "%      subsumed          " + discardedBySubsumption + "\n"; 
	    if (discardedAsUnfactorableAnswers != 0)
		result += "%      unfact. answers   " + discardedAsUnfactorableAnswers + "\n"; 
	    if (discardedDueToUnsatisfiableConstraints != 0)
		result += "%      unsat. constr.  " + discardedDueToUnsatisfiableConstraints + "\n";
	    if (discardedAsAbsoluteContradictions != 0)
		result += "%      abs. contr.       " + discardedAsAbsoluteContradictions + "\n"; 
	    if (discardedAsConditionalContradictions != 0)
		result += "%      cond. contr.      " + discardedAsConditionalContradictions + "\n";
	    return result;

	} // toString()
            
      

    

	// How generated:

	public long input;

	public long copied;

	public long byClausification;

	public long byResolution;

	public long byForwardParamodulation;
	
	public long byBackwardParamodulation;
	
	public long byEqualityFactoring;

	public long byEqualityResolution;

	public long byBackwardDemodulation;

	public long byBackwardSubsumptionResolution;

	
	// Why discarded:
		    
	public long discardedAsPropositionalTautologies;

	public long discardedAsEquationalTautologies;

	public long discardedBySubsumption;
	
	public long discardedAsUnfactorableAnswers;

	public long discardedDueToUnsatisfiableConstraints;

	public long discardedAsAbsoluteContradictions;

	public long discardedAsConditionalContradictions;

	

    }; // class GeneratedClauses




    public static class KeptClauses {


    

	/** <b>post:</b> as after <code>reset()</code>. */
	public KeptClauses() { reset(); }

	/** Sets all counters to the initial values. */ 
	public void reset() {
	    total = 0;
	    selected = 0;
	    terminal = 0;
	    discardedBySubsumption = 0;
	    discardedAsSimplifiedBySubsumptionResolution = 0;
	    discardedAsSimplifiedByDemodulation = 0;   
	}
	
	
	/** Adds the values from <code>stat</code> to the corresponing counters
	 *  in this object.
	 */
	public void absorb(KeptClauses stat) {
	    total += stat.total;
	    selected += stat.selected;
	    terminal += stat.terminal;
	    discardedBySubsumption += stat.discardedBySubsumption;
	    discardedAsSimplifiedBySubsumptionResolution += 
		stat.discardedAsSimplifiedBySubsumptionResolution;
	    discardedAsSimplifiedByDemodulation += 
		stat.discardedAsSimplifiedByDemodulation;    
	}
	
      
	/** The total number of clauses that have been retained. */
	public long totalKept() { return total; }
            

	/** The number of clauses discarded as redundant. */
	public long redundantDiscarded() {
	    return
		discardedBySubsumption +
		discardedAsSimplifiedBySubsumptionResolution +
		discardedAsSimplifiedByDemodulation;  
	}
            

	public String toString() {
	    String result = "%  Kept clauses\n";
	    result += "%      total   " + total + "\n";
	    result += "%        selected   " + selected + "\n";
	    result += "%        terminal   " + terminal + "\n";
	    result += "%      current " + (totalKept() - redundantDiscarded()) + "\n";
	    result += "%      redundant discarded " + redundantDiscarded() + "\n";
	    if (discardedBySubsumption != 0)
		result += "%        subsumed                 " + discardedBySubsumption + "\n";
	    if (discardedAsSimplifiedBySubsumptionResolution != 0)
		result += "%        simplified by subs. res. " + discardedAsSimplifiedBySubsumptionResolution + "\n";
	    if (discardedAsSimplifiedByDemodulation != 0)
		result += "%        simplified by demod.     " + discardedAsSimplifiedByDemodulation + "\n";

	    return result;

	} // toString()
            
      

    

	public long total;

	public long selected;

	public long terminal;

	// Why discarded:
	  
	public long discardedBySubsumption;

	public long discardedAsSimplifiedBySubsumptionResolution;

	public long discardedAsSimplifiedByDemodulation;

    }; // class KeptClauses 






    public static class FineSelectionUnits {
      
    

	/** Statistics on units of a particular kind. */
	public static class KindProfile {

	
	    
	    /** <b>post:</b> as after <code>reset()</code>. */
	    public KindProfile() { reset(); }

	    /** Sets all counters to the initial values. */ 
	    public void reset() {
		extracted = 0;
		deleted = 0;
		promoted = 0;
	    }
	
	    
	    /** Adds the values from <code>stat</code> to the corresponing counters
	     *  in this object.
	     */
	    public void absorb(KindProfile stat) {    
		extracted += stat.extracted;
		deleted += stat.deleted;
		promoted += stat.promoted;
	    }
	

	    /** Checks if some of the counters have non-initial values. */
	    public boolean somethingRegistered() {
		return extracted != 0 || deleted != 0 || promoted != 0;
	    }
            
	    
	    public String toString() {    
		return extracted + ", deleted " + deleted + ", promoted " + promoted;
	    }
            
      
	
	
	    
	    /** Total number of identified units of the corresponding kind. */ 
	    public long extracted;

	    /** Total number of units of the corresponding kind that were
	     *  destroyed.
	     */
	    public long deleted;
	    
	    /** Total number of promotions of units of the corresponding kind. */ 
	    public long promoted;
	    
	}; // class KindProfile
      
    

	/** <b>post:</b> as after <code>reset()</code>. */
	public FineSelectionUnits() { 

	    resolution = new KindProfile();
	    eqLHSForParamodulation = new KindProfile();
	    redexForParamodulation = new KindProfile();
	    equalityFactoring = new KindProfile();
	    equalityResolution = new KindProfile();
	    
	}

	/** Sets all counters to the initial values. */ 
	public void reset() {
	    resolution.reset();
	    eqLHSForParamodulation.reset();
	    redexForParamodulation.reset();
	    equalityFactoring.reset();
	    equalityResolution.reset();
	}
	

	/** Adds the values from <code>stat</code> to the corresponing counters
	 *  in this object.
	 */
	public void absorb(FineSelectionUnits stat) {
	    resolution.absorb(stat.resolution);
	    eqLHSForParamodulation.absorb(stat.eqLHSForParamodulation);
	    redexForParamodulation.absorb(stat.redexForParamodulation);
	    equalityFactoring.absorb(stat.equalityFactoring);
	    equalityResolution.absorb(stat.equalityResolution);
	}
	
      

	/** Total number of identified units of all kinds. */ 
	public long extracted() {    
	    return 
		resolution.extracted +
		eqLHSForParamodulation.extracted +
		redexForParamodulation.extracted +
		equalityFactoring.extracted +
		equalityResolution.extracted;
	}
            

	/** Total number of destroyed units of all kinds. */
	public long deleted() {    
	    return 
		resolution.deleted +
		eqLHSForParamodulation.deleted +
		redexForParamodulation.deleted +
		equalityFactoring.deleted +
		equalityResolution.deleted;
	}
            

	/** Total number of promotions of units of all kinds. */
	public long promoted() {    
	    return 
		resolution.promoted +
		eqLHSForParamodulation.promoted +
		redexForParamodulation.promoted +
		equalityFactoring.promoted +
		equalityResolution.promoted;
	}
            
      
	public String toString() {
	    String result = "%  Selection units " + (extracted() - deleted()) + " = " 
		+ extracted() + " extracted - " + deleted() + " deleted, promoted " + promoted() +"\n";
	    if (resolution.somethingRegistered())
		result += "%     resolution " + resolution + "\n";
	    if (eqLHSForParamodulation.somethingRegistered())
		result += "%     eq. LHS for paramod. " + eqLHSForParamodulation + "\n";
	    if (redexForParamodulation.somethingRegistered())
		result += "%     redex for paramod.   " + redexForParamodulation + "\n";
	    if (equalityFactoring.somethingRegistered())
		result += "%     eq. factoring        " + equalityFactoring + "\n";
	    if (equalityResolution.somethingRegistered())
		result += "%     eq. resolution       " + equalityResolution + "\n";

	    return result;
	} // toString()
            

    

	/** Statistics on SUs for resolution. */
	public KindProfile resolution;
	
	/** Statistics on SUs representing heads of equations
	 *  to paramodulate from.
	 */
	public KindProfile eqLHSForParamodulation;

	/** Statistics on SUs representing subterms of clauses to paramodulate
	 *  into.
	 */
	public KindProfile redexForParamodulation;

	/** Statistics on SUs representing clauses for equality factoring. */
	public KindProfile equalityFactoring;
	      
	/** Statistics on SUs representing clauses for equality resolution. */
	public KindProfile equalityResolution;

    }; // class FineSelectionUnits





  
    /** <b>post:</b> as after <code>reset()</code>. */
    public Statistics() {

	clausificationInferences = new ClausificationInferences();
	deductionInferences = new DeductionInferences();
	forwardSimplifyingInferences = new ForwardSimplifyingInferences();
	backwardSimplifyingInferences = new BackwardSimplifyingInferences();
	generatedClauses = new GeneratedClauses();
	keptClauses = new KeptClauses();
	fineSelectionUnits = new FineSelectionUnits();
    }
            

    /** Sets all counters to the initial values. */ 
    public void reset() {
	clausificationInferences.reset();
	deductionInferences.reset();
	forwardSimplifyingInferences.reset();
	backwardSimplifyingInferences.reset();
	generatedClauses.reset();
	keptClauses.reset();
	fineSelectionUnits.reset();
    }
	

    /** Current active object; may be null if no object is active 
     *  at the moment.
     */
    public static Statistics current() { return _current; }

    /** Sets <b>obj</b> as the current active object. */
    public static void makeCurrent(Statistics obj) {
	_current = obj;
    }

    /** Adds the values from <code>stat</code> to the corresponing counters
     *  in this object.
     */
    public void absorb(Statistics stat) {
	clausificationInferences.absorb(stat.clausificationInferences);
	deductionInferences.absorb(stat.deductionInferences);
	forwardSimplifyingInferences.absorb(stat.forwardSimplifyingInferences);
	backwardSimplifyingInferences.absorb(stat.backwardSimplifyingInferences);
	generatedClauses.absorb(stat.generatedClauses);
	keptClauses.absorb(stat.keptClauses);
	fineSelectionUnits.absorb(stat.fineSelectionUnits);
    }
	


    /** Checks the consistency of statistics. */
    public boolean isConsistent() {
	return 
	    clausificationInferences.isConsistentWith(generatedClauses) &&
	    deductionInferences.isConsistentWith(generatedClauses) &&
	    forwardSimplifyingInferences.isConsistentWith(generatedClauses) &&
	    backwardSimplifyingInferences.isConsistentWith(keptClauses) &&
	    backwardSimplifyingInferences.isConsistentWith(generatedClauses);
    }
            
      

    public String toString() {
	return
	    clausificationInferences.toString() +
	    deductionInferences +
	    forwardSimplifyingInferences +
	    backwardSimplifyingInferences +
	    generatedClauses +
	    keptClauses +
	    fineSelectionUnits;
    }
            
      

  

    public ClausificationInferences clausificationInferences;
    
    public DeductionInferences deductionInferences;
  
    public ForwardSimplifyingInferences  forwardSimplifyingInferences;

    public BackwardSimplifyingInferences  backwardSimplifyingInferences;

    public GeneratedClauses generatedClauses;

    public KeptClauses keptClauses;

    public FineSelectionUnits fineSelectionUnits;



    
    /** Current active object; null if no object is active. */
    private static Statistics _current = null;


}; // class Statistics 
