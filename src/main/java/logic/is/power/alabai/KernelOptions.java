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

/**
 * Encapsulates various options of Alabai JE kernel sessions
 * represented by instances of {@link alabai_je.Kernel}.
 * TODO: Separate maximal activeness values for selection units of
 *       different types instead of just 
 *       KernelOptions.maximalSelectionUnitActiveness.
 */
public class KernelOptions {

    public static enum ReductionOrdering {
	SubtermRelation,
	NonrecursiveKBO
    }

    /** Literal selection functions. */
    public static class LiteralSelection {
	
	public LiteralSelection() {
	    forAxioms = new LitSelFunTotal();
	    forHypotheses = new LitSelFunTotal();
	    forNegatedConjectures = new LitSelFunTotal();
	}

	public LiteralSelectionFunction forAxioms;
	
	public LiteralSelectionFunction forHypotheses;

	public LiteralSelectionFunction forNegatedConjectures;

    } // class LiteralSelection


    /** Parameters of resolution. */
    public static class Resolution {
	

	/** Sets default values. */
	public Resolution() {
	    checkUnifierDirections = false;
	}

	/** Copy constructor. */
	public Resolution(Resolution res) {
	    checkUnifierDirections = res.checkUnifierDirections;
	}
	
	/** Switches the identification and separate deployment
	 *  of I- and G-versions of resolution selection units.
	 */
	public boolean checkUnifierDirections;

    } // class Resolution


    /** Parameters of paramodulation. */
    public static class Paramodulation {
	
	/** Sets default values. */
	public Paramodulation() {
	    on = true;
	    checkUnifierDirections = false;
	}

	/** Copy constructor. */
	public Paramodulation(Paramodulation par) {
	    on = par.on;
	    checkUnifierDirections = par.checkUnifierDirections;
	}

	/** Switches paramodulation. */
	public boolean on;
	

	/** Switches the identification and separate deployment
	 *  of I- and G-versions of paramodulation selection units.
	 */
	public boolean checkUnifierDirections;

    } // class Paramodulation



    /** Parameters of forward subsumption. */
    public static class ForwardSubsumption {
	
	/** Sets default values. */
	public ForwardSubsumption() {
	    on = true;
	    equalityPretest = false;
	    featureFunctionVector = new ClauseFeatureVector();
	    featureFunctionVector.
		add(StandardClauseFeature.numberOfLiterals());
	    featureFunctionVector.
		add(StandardClauseFeature.numberOfNegativeLiterals());
	    featureFunctionVector.
		add(StandardClauseFeature.numberOfPositiveLiterals());
	    featureFunctionVector.
		add(StandardClauseFeature.numberOfSymbols());
	}

	/** Copy constructor. */
	public ForwardSubsumption(ForwardSubsumption fs) {
	    on = fs.on;
	    equalityPretest = fs.equalityPretest;
	    featureFunctionVector = 
		new ClauseFeatureVector(fs.featureFunctionVector);
	}

	/** Switches forward subsumption. */
	public boolean on;

	
	/** Switches quick equality pretest in forward subsumption,
	 *  if <code>equalityPretest == true</code>, the subsumption procedure will
	 *  try to quickly find an equal clause before trying other possibilities.
	 */
	public boolean equalityPretest;
	

	/** Functions to compute feature vectors in 
	 *  the forward subsumption index.
	 */
	public 
	    ClauseFeatureVector 
	    featureFunctionVector;

    } // class ForwardSubsumption


    /** Parameters of forward subsumption resolution. */
    public static class ForwardSubsumptionResolution {

	
	/** Sets default values. */
	public ForwardSubsumptionResolution() {
	    on = true;
	}

	/** Copy constructor. */
	public 
	    ForwardSubsumptionResolution(ForwardSubsumptionResolution fsr) {
	    on = fsr.on;
	}


	/** Switches forward subsumption. */
	public boolean on;

    } // class ForwardSubsumptionResolution


    /** Parameters of backward subsumption. */
    public static class BackwardSubsumption {
	
	/** Sets default values. */
	public BackwardSubsumption() {
	    on = false;
	    featureFunctionVector = 
		new ClauseFeatureVector();
	    featureFunctionVector.
		add(StandardClauseFeature.numberOfLiterals());
	    featureFunctionVector.
		add(StandardClauseFeature.numberOfNegativeLiterals());
	    featureFunctionVector.
		add(StandardClauseFeature.numberOfPositiveLiterals());
	    featureFunctionVector.
		add(StandardClauseFeature.numberOfSymbols());
	}

	/** Copy constructor. */
	public BackwardSubsumption(BackwardSubsumption fs) {
	    on = fs.on;
	    featureFunctionVector = 
		new ClauseFeatureVector(fs.featureFunctionVector);
	}

	/** Switches backward subsumption. */
	public boolean on;

	/** Functions to compute feature vectors in 
	 *  the backward subsumption index.
	 */
	public 
	    ClauseFeatureVector
	    featureFunctionVector;

    } // class BackwardSubsumption

    


    /** Parameters of forward demodulation. */
    public static class ForwardDemodulation {
	
	/** Sets default values. */
	public ForwardDemodulation() {
	    on = true;
	    featureFunctionVector = 
		new TermFeatureVector();
	    featureFunctionVector.
		add(StandardTermFeature.numberOfSymbols());
	}

	/** Copy constructor. */
	public ForwardDemodulation(ForwardDemodulation fs) {
	    on = fs.on;
	    featureFunctionVector = 
		new TermFeatureVector(fs.featureFunctionVector);
	}

	/** Switches forward demodulation. */
	public boolean on;

	/** Functions to compute feature vectors in 
	 *  the forward demodulation index.
	 */
	public 
	    TermFeatureVector 
	    featureFunctionVector;

    } // class ForwardDemodulation


    

    /** Parameters of backward demodulation. */
    public static class BackwardDemodulation {
	
	/** Sets default values. */
	public BackwardDemodulation() {
	    on = false;
	    featureFunctionVector = 
		new TermFeatureVector();
	    featureFunctionVector.
		add(StandardTermFeature.numberOfSymbols());
	}

	/** Copy constructor. */
	public BackwardDemodulation(BackwardDemodulation fs) {
	    on = fs.on;
	    featureFunctionVector = 
		new TermFeatureVector(fs.featureFunctionVector);
	}

	/** Switches backward demodulation. */
	public boolean on;

	/** Functions to compute feature vectors in 
	 *  the backward demodulation index.
	 */
	public 
	    TermFeatureVector 
	    featureFunctionVector;

    } // class BackwardDemodulation

    
    /** Parameters of simplifying equality resolution. */
    public static class SimplifyingEqualityResolution {

	/** Sets default values. */
	public SimplifyingEqualityResolution() {
	    on = true;
	}

	/** Copy constructor. */
	public 
	    SimplifyingEqualityResolution(SimplifyingEqualityResolution ser) {
	    on = ser.on;
	}

	/** Switches simplifying equality resolution. */
	public boolean on;


    } // class SimplifyingEqualityResolution






    /** Parameters of heuristic unifiability
     *  estimation used for rating fine inference selection units.
     */
    public static class UnifiabilityEstimation {

	public UnifiabilityEstimation() {
	    maxDepth = 2;
	    deepeningCoeff = 0.5f;
	    duplicateVarUnifiability = 0.5f;
	    constUnifiability = 0.1f;
	    funcUnifiability = 0.1f;
	    propConstUnifiability = 0.1f;
	    predUnifiability = 0.1f;
	}

	/** Copy constructor. */
	public UnifiabilityEstimation(UnifiabilityEstimation ue) {
	    maxDepth = ue.maxDepth;
	    deepeningCoeff = ue.deepeningCoeff;
	    duplicateVarUnifiability = ue.duplicateVarUnifiability;
	    constUnifiability = ue.constUnifiability;
	    funcUnifiability = ue.funcUnifiability;
	    propConstUnifiability = ue.propConstUnifiability;
	    predUnifiability = ue.predUnifiability;
	}

	/** Terms are only examined downto this depth. */
	public int maxDepth;
	/** Adjusts the computation of unifiability of compound terms
	 *  and atomic formulas: 
	 *  <code>u(f(s)) = u(f) * (u(s) + (1 - u(s)) * deepeningCoeff</code>.
	 */
	public float deepeningCoeff;
	/** Unifiability of variables with multiple occurences. */
	public float duplicateVarUnifiability;
	/** Unifiability of constants. */
	public float constUnifiability;
	/** Unifiability of functions. */
	public float funcUnifiability;
	/** Unifiability of propositional constants. */
	public float propConstUnifiability;
	/** Unifiability of predicates. */
	public float predUnifiability;

    } // class UnifiabilityEstimation




    /** Parameters for computing penalties of various
     *  fine inference selection units. 
     */
    public static class SelectionUnitPenalty {
	
	public SelectionUnitPenalty() {
	    baseValue = 100; 

	    resolutionB = new Resolution();
	    resolutionG = new Resolution();
	    resolutionI = new Resolution();


	    eqLHSForParamodB = new EqLHSForParamod();
	    eqLHSForParamodG = new EqLHSForParamod();
	    eqLHSForParamodI = new EqLHSForParamod();

	    redexForParamod = new RedexForParamod();

	    equalityFactoring = new EqualityFactoring();

	    equalityResolution = new EqualityResolution();

	} // SelectionUnitPenalty()
	


	/** Copy constructor. */
	public SelectionUnitPenalty(SelectionUnitPenalty sup) {
	    baseValue = sup.baseValue; 

	    resolutionB = new Resolution(sup.resolutionB);
	    resolutionG = new Resolution(sup.resolutionG);
	    resolutionI = new Resolution(sup.resolutionI);

	    eqLHSForParamodB = new EqLHSForParamod(sup.eqLHSForParamodB);
	    eqLHSForParamodG = new EqLHSForParamod(sup.eqLHSForParamodG);
	    eqLHSForParamodI = new EqLHSForParamod(sup.eqLHSForParamodI);


	    redexForParamod = new RedexForParamod(sup.redexForParamod);
	    
	    equalityFactoring = new EqualityFactoring(sup.equalityFactoring);

	    equalityResolution = new EqualityResolution(sup.equalityResolution);

	} // SelectionUnitPenalty(SelectionUnitPenalty sup)




	/** Parameters for computing penalties of various
	 *  kinds of selection units with literals for resolution.
	 */
	public static class Resolution {

	    /** Copy constructor. */
	    public Resolution(Resolution res) {
		generalCoeff = res.generalCoeff;
		litUnifiabilityCoeff = 
		    (UnaryFunctionObject<Float,Float>)res.litUnifiabilityCoeff.clone();
		residualLitCoeff = 
		    (UnaryFunctionObject<Integer,Float>)res.residualLitCoeff.clone();
		residualSizeCoeff = 
		    (UnaryFunctionObject<Integer,Float>)res.residualSizeCoeff.clone();
		litSizeCoeff = 
		    (UnaryFunctionObject<Integer,Float>)res.litSizeCoeff.clone();
		litDepthCoeff = 
		    (UnaryFunctionObject<Integer,Float>)res.litDepthCoeff.clone();
	    } 
		
	    public Resolution() {
		generalCoeff = 1;
		litUnifiabilityCoeff = new FloatFloatLinearInterpolant();
		((FloatFloatLinearInterpolant)litUnifiabilityCoeff).put(1.0f,1.0f);
		residualLitCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)residualLitCoeff).put(0,1.0f);
		residualSizeCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)residualSizeCoeff).put(0,1.0f);
		litSizeCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)litSizeCoeff).put(0,1.0f);
		litDepthCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)litDepthCoeff).put(0,1.0f);
	    } // Resolution()

	    public float generalCoeff;

	    public UnaryFunctionObject<Float,Float> litUnifiabilityCoeff;

	    public UnaryFunctionObject<Integer,Float> residualLitCoeff;

	    public UnaryFunctionObject<Integer,Float> residualSizeCoeff;

	    public UnaryFunctionObject<Integer,Float> litSizeCoeff;

	    public UnaryFunctionObject<Integer,Float> litDepthCoeff;
	    
	} // class Resolution


	

	/** Parameters for computing penalties of various
	 *  kinds of selection units with LHS of positive equations
	 *  intended for paramodulation.
	 */
	public static class EqLHSForParamod {

	    /** Copy constructor. */
	    public EqLHSForParamod(EqLHSForParamod par) {
		generalCoeff = par.generalCoeff;
		lhsUnifiabilityCoeff = 
		    (UnaryFunctionObject<Float,Float>)par.lhsUnifiabilityCoeff.clone();
		residualLitCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.residualLitCoeff.clone();
		residualSizeCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.residualSizeCoeff.clone();
	    } 
		
	    public EqLHSForParamod() {
		generalCoeff = 1;
		lhsUnifiabilityCoeff = new FloatFloatLinearInterpolant();
		((FloatFloatLinearInterpolant)lhsUnifiabilityCoeff).put(1.0f,1.0f);
		residualLitCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)residualLitCoeff).put(0,1.0f);
		residualSizeCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)residualSizeCoeff).put(0,1.0f);
	    } // EqLHSForParamod()

	    public float generalCoeff;

	    public UnaryFunctionObject<Float,Float> lhsUnifiabilityCoeff;

	    public UnaryFunctionObject<Integer,Float> residualLitCoeff;

	    public UnaryFunctionObject<Integer,Float> residualSizeCoeff;
	    
	} // class EqLHSForParamod


	/** Parameters for computing penalties of selection units with 
	 *  redexes for paramodulation
	 *  ({@link alabai_je#FinSelUnRedexForParamod}).
	 */
	public static class RedexForParamod {

	    /** Copy constructor. */
	    public RedexForParamod(RedexForParamod par) {
		generalCoeff = par.generalCoeff;
		positiveEqCoeff = par.positiveEqCoeff;
		positiveNonEqCoeff = par.positiveNonEqCoeff;
		negativeEqCoeff = par.negativeEqCoeff;
		negativeNonEqCoeff = par.negativeNonEqCoeff;
		redexUnifiabilityCoeff = 
		    (UnaryFunctionObject<Float,Float>)par.redexUnifiabilityCoeff.clone();
		residualLitCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.residualLitCoeff.clone();
		residualSizeCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.residualSizeCoeff.clone();
		redexDepthCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.redexDepthCoeff.clone();
	    } 
		
	    public RedexForParamod() {
		generalCoeff = 1;
		positiveEqCoeff = 1;
		positiveNonEqCoeff = 1;
		negativeEqCoeff = 1;
		negativeNonEqCoeff = 1;
		redexUnifiabilityCoeff = new FloatFloatLinearInterpolant();
		((FloatFloatLinearInterpolant)redexUnifiabilityCoeff).put(1.0f,1.0f);
		residualLitCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)residualLitCoeff).put(0,1.0f);
		residualSizeCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)residualSizeCoeff).put(0,1.0f);
		redexDepthCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)redexDepthCoeff).put(0,1.0f);
	    } // RedexForParamod()

	    public float generalCoeff;
	    
	    public float positiveEqCoeff;
	    
	    public float positiveNonEqCoeff;
	    
	    public float negativeEqCoeff;

	    public float negativeNonEqCoeff;

	    public UnaryFunctionObject<Float,Float> redexUnifiabilityCoeff;

	    public UnaryFunctionObject<Integer,Float> residualLitCoeff;

	    public UnaryFunctionObject<Integer,Float> residualSizeCoeff;

	    public UnaryFunctionObject<Integer,Float> redexDepthCoeff;
	    
	} // class RedexForParamod




	/** Parameters for computing penalties of selection units with 
	 *  clauses for equality factoring
	 *  ({@link alabai_je#FinSelUnEqualityFactoring}).
	 */
	public static class EqualityFactoring {

	    /** Copy constructor. */
	    public EqualityFactoring(EqualityFactoring par) {
		generalCoeff = par.generalCoeff;
		litNumCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.litNumCoeff.clone();
		clauseSizeCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.clauseSizeCoeff.clone();
	    } 
		
	    public EqualityFactoring() {
		generalCoeff = 1;
		litNumCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)litNumCoeff).put(0,1.0f);
		clauseSizeCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)clauseSizeCoeff).put(0,1.0f);
	    } // RedexForParamod()

	    public float generalCoeff;

	    public UnaryFunctionObject<Integer,Float> litNumCoeff;

	    public UnaryFunctionObject<Integer,Float> clauseSizeCoeff;
	    
	} // class EqualityFactoring





	/** Parameters for computing penalties of selection units with 
	 *  clauses for equality resolution
	 *  ({@link alabai_je#FinSelUnEqualityResolution}).
	 */
	public static class EqualityResolution {

	    /** Copy constructor. */
	    public EqualityResolution(EqualityResolution par) {
		generalCoeff = par.generalCoeff;
		litNumCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.litNumCoeff.clone();
		clauseSizeCoeff = 
		    (UnaryFunctionObject<Integer,Float>)par.clauseSizeCoeff.clone();
	    } 
		
	    public EqualityResolution() {
		generalCoeff = 1;
		litNumCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)litNumCoeff).put(0,1.0f);
		clauseSizeCoeff = new IntFloatLinearInterpolant();
		((IntFloatLinearInterpolant)clauseSizeCoeff).put(0,1.0f);
	    } // RedexForParamod()

	    public float generalCoeff;

	    public UnaryFunctionObject<Integer,Float> litNumCoeff;

	    public UnaryFunctionObject<Integer,Float> clauseSizeCoeff;
	    
	} // class EqualityResolution



	public int baseValue;
	
	public Resolution resolutionB;
	public Resolution resolutionG;
	public Resolution resolutionI;

	public EqLHSForParamod eqLHSForParamodB;
	public EqLHSForParamod eqLHSForParamodG;
	public EqLHSForParamod eqLHSForParamodI;

	public RedexForParamod redexForParamod;

	public EqualityFactoring equalityFactoring;

	public EqualityResolution equalityResolution;

    } // class SelectionUnitPenalty 


    
    /** Options configuring the incremental query rewriting
     *  (as described in
     * <a href="{@docRoot}/../references.html#incremental_query_rewriting">[Incremental Query Rewriting chapter]</a>
     *  ).
     */
    public static class QueryRewriting {

	
	/** Configuration of the optimisation based on discarding clauses with 
	 *  too long chains of transitive predicates in the constraint 
	 *  part: see Section 4 of 
	 *  <a href="{@docRoot}/../references.html#incremental_query_rewriting">[Incremental Query Rewriting chapter]</a>.			
	 */
	public static class LimitedTransitivePredicateChainingInConstraints {
	    
	    public LimitedTransitivePredicateChainingInConstraints() {
		on = true;
		 
		limitedChainPredicates = 
		    new HashMap<Pair<String,Integer>,Integer>();
	    }
	    
	    public LimitedTransitivePredicateChainingInConstraints(LimitedTransitivePredicateChainingInConstraints obj) {
		on = obj.on;
		limitedChainPredicates = 
		    new HashMap<Pair<String,Integer>,Integer>(obj.limitedChainPredicates);
	    }

	    /** Switches the optimisation; if false, the optimisation will not 
	     *  be applied to any predicates.
	     */
	    public boolean on;

	    
	    public HashMap<Pair<String,Integer>,Integer> limitedChainPredicates;

	} // class LimitedTransitivePredicateChainingInConstraints



	public QueryRewriting() {
	    allowFunctionsInConstraints = false;
	    inconsistenciesAsAnswers = true;
	    limitedTransitivePredicateChainingInConstraints = 
		new LimitedTransitivePredicateChainingInConstraints();
	}

	/** Copy constructor. */
	public QueryRewriting(QueryRewriting qr) {
	    allowFunctionsInConstraints = qr.allowFunctionsInConstraints;
	    inconsistenciesAsAnswers = qr.inconsistenciesAsAnswers;
	    limitedTransitivePredicateChainingInConstraints = 
		new LimitedTransitivePredicateChainingInConstraints(qr.limitedTransitivePredicateChainingInConstraints);
	}

	/** Switches the discarding of clauses with constraints containing non-nullary
	 *  functions based on Section 4 of 
	 *  <a href="{@docRoot}/../references.html#incremental_query_rewriting">[Incremental Query Rewriting chapter]</a>.
	 */
	public boolean allowFunctionsInConstraints;

	
	/** Switches reporting empty clauses and clauses containing only 
	 *  constraint literals but no answer literals, as answers;
	 *  reporting such clauses is only useful for checking consistency
	 *  of the background KBs with the data in the DB.
	 */
	public boolean inconsistenciesAsAnswers;

	/** Configuration of the optimisation based on discarding clauses with 
	 *  too long chains of transitive predicates in the constraint 
	 *  part: see Section 4 of 
	 *  <a href="{@docRoot}/../references.html#incremental_query_rewriting">[Incremental Query Rewriting chapter]</a>.			
	 */
	public
	    LimitedTransitivePredicateChainingInConstraints
	    limitedTransitivePredicateChainingInConstraints;

    } // class QueryRewriting

    


    /** <b>post:</b> as after <code>clear()</code>. */
    public KernelOptions() { clear(); }

    /** Copy constructor. */
    public KernelOptions(KernelOptions opt) {
	
	maximalSelectionUnitActiveness = opt.maximalSelectionUnitActiveness;

	maximalSelectionUnitPenalty = opt.maximalSelectionUnitPenalty;
	
	reductionOrdering = opt.reductionOrdering;
	
	literalSelection = opt.literalSelection;

	resolution = new Resolution(opt.resolution);

	paramodulation = new Paramodulation(opt.paramodulation);

	forwardSubsumption =
	    new ForwardSubsumption(opt.forwardSubsumption);

	forwardSubsumptionResolution =
	    new ForwardSubsumptionResolution(opt.forwardSubsumptionResolution);

	backwardSubsumption =
	    new BackwardSubsumption(opt.backwardSubsumption);

	forwardDemodulation =
	    new ForwardDemodulation(opt.forwardDemodulation);

	backwardDemodulation =
	    new BackwardDemodulation(opt.backwardDemodulation);

	simplifyingEqualityResolution = 
	    new SimplifyingEqualityResolution(opt.simplifyingEqualityResolution);

	propositionalTautologyTest = opt.propositionalTautologyTest;

	equationalTautologyTest = opt.equationalTautologyTest;

	clauseSelectionDelay = opt.clauseSelectionDelay;

	unifiabilityEstimation = 
	    new UnifiabilityEstimation(opt.unifiabilityEstimation);

	selectionUnitPenaltyForAxioms = 
	    new SelectionUnitPenalty(opt.selectionUnitPenaltyForAxioms);
	selectionUnitPenaltyForHypotheses = 
	    new SelectionUnitPenalty(opt.selectionUnitPenaltyForHypotheses);
	selectionUnitPenaltyForNegatedConjectures = 
	    new SelectionUnitPenalty(opt.selectionUnitPenaltyForNegatedConjectures);
	
	queryRewriting = new QueryRewriting(opt.queryRewriting);

	printInput = opt.printInput;

	printNew = opt.printNew;

	printKept = opt.printKept;

	printDiscarded = opt.printDiscarded;

	printAssembled = opt.printAssembled;

	printSelectedClauses = opt.printSelectedClauses;
	
	printActivatedClauses = opt.printActivatedClauses;

	markUnselectedLiterals = opt.markUnselectedLiterals;

	printPromoted = opt.printPromoted;

	selUnitPromotionWatchList = opt.selUnitPromotionWatchList;

	printSelectionUnits = opt.printSelectionUnits;

	printUnifiability = opt.printUnifiability;

	printPenaltyComputation = opt.printPenaltyComputation;

	mainEqualitySymbol = opt.mainEqualitySymbol;

	mainDisequalitySymbol = opt.mainDisequalitySymbol;	

	mainEqualityIsInfix = opt.mainEqualityIsInfix;
	
	otherEqualitySymbols = opt.otherEqualitySymbols;

	answerPredicates = 
	    new LinkedList<Pair<String,Integer>>(opt.answerPredicates);

	constraintPredicates = 
	    new LinkedList<Pair<String,Integer>>(opt.constraintPredicates);
    
	preserveInput = opt.preserveInput;
	
    } //  KernelOptions(KernelOptions opt)

    /** Resets all options to their default values. */
    public void clear() {

	maximalSelectionUnitActiveness = 1024;
	
	maximalSelectionUnitPenalty = 1000;

	reductionOrdering = ReductionOrdering.NonrecursiveKBO;
		
	literalSelection = new LiteralSelection();

	resolution = new Resolution();

	paramodulation = new Paramodulation();

	forwardSubsumption = new ForwardSubsumption();

	forwardSubsumptionResolution = new ForwardSubsumptionResolution();

	backwardSubsumption = new BackwardSubsumption();

	forwardDemodulation = new ForwardDemodulation();

	backwardDemodulation = new BackwardDemodulation();

	simplifyingEqualityResolution = 
	    new SimplifyingEqualityResolution();

	propositionalTautologyTest = true;

	equationalTautologyTest = true;

	clauseSelectionDelay = 10;

	unifiabilityEstimation = 
	    new UnifiabilityEstimation();

	
	selectionUnitPenaltyForAxioms = new SelectionUnitPenalty();
	selectionUnitPenaltyForHypotheses = new SelectionUnitPenalty();
	selectionUnitPenaltyForNegatedConjectures = new SelectionUnitPenalty();

	queryRewriting = new QueryRewriting();
	

	
	printInput = false;

	printNew = false;

	printKept = false;

	printDiscarded = false;

	printAssembled = false;

	printSelectedClauses = false;

	printActivatedClauses = false;

	markUnselectedLiterals = true;

	printPromoted = false;

	selUnitPromotionWatchList = null;

	printSelectionUnits = false;

	printUnifiability = false;

	printPenaltyComputation = false;

	mainEqualitySymbol = "=";

	mainDisequalitySymbol = "!=";

	mainEqualityIsInfix = true;
	
	otherEqualitySymbols = new LinkedList<String>();


	answerPredicates = 
	    new LinkedList<Pair<String,Integer>>();

	constraintPredicates = 
	    new LinkedList<Pair<String,Integer>>();

	preserveInput = false;

    } // clear() 


    public KernelOptions clone() {
	return new KernelOptions(this);
    }

    
    public void addAnswerPredicate(String name,int arity) {
	answerPredicates.
	    add(new Pair<String,Integer>(name,new Integer(arity)));
    }

    
    public void addConstraintPredicate(String name,int arity) {
	constraintPredicates.
	    add(new Pair<String,Integer>(name,new Integer(arity)));
    }



    public int maximalSelectionUnitActiveness;

    public int maximalSelectionUnitPenalty;

    public ReductionOrdering reductionOrdering;

    public LiteralSelection literalSelection;

    public Resolution resolution;

    public Paramodulation paramodulation;

    public ForwardSubsumption forwardSubsumption;

    public ForwardSubsumptionResolution forwardSubsumptionResolution;

    public BackwardSubsumption backwardSubsumption;

    public ForwardDemodulation forwardDemodulation;

    public BackwardDemodulation backwardDemodulation;

    public SimplifyingEqualityResolution simplifyingEqualityResolution;

    /** Switches propositional tautology test during the forward 
     *  processing of clauses. 
     */
    public boolean propositionalTautologyTest;

    /** Switches equational tautology test during the forward
     *  processing of clauses. 
     */
    public boolean equationalTautologyTest;

    
    /** Specifies how many generating inference cycles will be made
     *  between clause selection attempts.
     */
    public int clauseSelectionDelay;


    /** Encapsulates all parameters of heuristic unifiability
     *  estimation used for rating fine inference selection units.
     */
    public UnifiabilityEstimation unifiabilityEstimation;

    public SelectionUnitPenalty selectionUnitPenaltyForAxioms;
    public SelectionUnitPenalty selectionUnitPenaltyForHypotheses;
    public SelectionUnitPenalty selectionUnitPenaltyForNegatedConjectures;


    public QueryRewriting queryRewriting;



    public boolean printInput;

    /** Switches printing of newly assembled clauses, both input and 
     *  derived.
     */
    public boolean printNew;

    public boolean printKept;

    public boolean printDiscarded;

      /** Switches printing of kept clauses that have been
       *  converted into the persistent clause representation.
       */
    public boolean printAssembled;

    /** Switches printing of clauses that have been selected
     *  for clausification or deduction inferences.
     */
    public boolean printSelectedClauses;

    /** Switches printing of clauses that have been activated
     *  by extracting their selection units.
     */
    public boolean printActivatedClauses;

    
    /** Switches highlighting of unselected literals in 
     *  activated clauses.
     */
    public boolean markUnselectedLiterals;

    /** Switches printing of fine selection units that are being promoted. */
    public boolean printPromoted;

    /** Specifies promotion of which selection units should be printed;
     *  if null, all promotions are printed.
     */
    public Set<Long> selUnitPromotionWatchList;

    /** Switches printing of fine selection units freshly extracted 
     *  from the corresponding clauses. 
     */
    public boolean printSelectionUnits;

    /** Switches logging of term unifiability estimation. */
    public boolean printUnifiability;

    /** Switches logging of selection unit penalty computation, */
    public boolean printPenaltyComputation;


    public String mainEqualitySymbol;

    /** If this is non-null and 
     *  <code>mainEqualityIsInfix == true</code>,
     *  negation of equality will be printed infixly with 
     *  this symbol.
     */
    public String mainDisequalitySymbol;

    /** Specifies whether the equality predicate should be output
     *  as infix. 
     */
    public boolean mainEqualityIsInfix;
				       
    /** Other symbols that should be treated as equality 
     *  when used as binary predicates.
     */
    public Collection<String> otherEqualitySymbols;

    // TODO: should move inside queryRewriting
    public LinkedList<Pair<String,Integer>> answerPredicates;

    // TODO: should move inside queryRewriting
    public LinkedList<Pair<String,Integer>> constraintPredicates;
    
    public boolean preserveInput;

}; // class KernelOptions
