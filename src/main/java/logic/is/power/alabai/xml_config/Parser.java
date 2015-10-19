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


package logic.is.power.alabai.xml_config;


import java.io.*;
import java.math.*;
import java.util.*;
import java.net.*;
import javax.xml.bind.*;
import javax.xml.bind.util.*;

import logic.is.power.cushion.*;

import logic.is.power.alabai.jaxb.*;

import javax.xml.validation.*;

import logic.is.power.alabai.ClauseFeatureVector;

import logic.is.power.alabai.ClauseFeature;

import logic.is.power.alabai.TermFeature;

import logic.is.power.alabai.TermFeatureVector;

import logic.is.power.alabai.StandardClauseFeature;

import logic.is.power.alabai.StandardTermFeature;

import logic.is.power.alabai.KernelOptions;

import logic.is.power.alabai.PlugInReference;

import logic.is.power.alabai.LiteralSelectionFunction;

import logic.is.power.alabai.LitSelFunTotal;

import logic.is.power.alabai.LitSelFunAllMaximal;

import logic.is.power.alabai.LitSelFunAllMaximalWRT;

import logic.is.power.alabai.LitSelFunPosHyper;

import logic.is.power.logic_warehouse.LiteralOrdering;

import logic.is.power.logic_warehouse.AdmissibleLiteralOrdering;

import logic.is.power.logic_warehouse.LitOrdByShallowWeight;

import logic.is.power.cushion.FloatFloatLinearInterpolant;

import logic.is.power.cushion.IntFloatLinearInterpolant;

import logic.is.power.cushion.UnaryFunctionObject;

/** Reads Alabai kernel options from config files in XML. */
public class Parser {

    public Parser() throws java.lang.Exception {
	
	JAXBContext jc = 
	    JAXBContext.newInstance("logic.is.power.alabai.jaxb");
	_unmarshaller = jc.createUnmarshaller();
	
	SchemaFactory schemaFactory = 
	    SchemaFactory.
	    newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

	URL schemaURL = 
	    ClassLoader.
	    getSystemResource("options.xsd");

	if (schemaURL == null)
	    throw new RuntimeException("Cannot get a URL for the XML Schema file options.xsd");

	Schema schema = schemaFactory.newSchema(schemaURL);

	_unmarshaller.setSchema(schema);

    } // Parser()

    

    /** Tries to parse the config file and adjusts the values of
     *  the options correspondingly.
     *  @throws java.lang.Exception if the config file does not
     *          comply with the XML Schema for Alabai config files.
     */
    public 
	void 
	parse(File configFile,logic.is.power.alabai.KernelOptions options)
	throws java.lang.Exception {
	
	AlabaiConfig conf;

	try
	    {
		conf =
		    (AlabaiConfig)_unmarshaller.unmarshal(configFile);
	    }
	catch (javax.xml.bind.UnmarshalException ex)
	    {
		throw 
		    new java.lang.Exception("Config file cannot be read: " +
					    ex);
	    };


	for (Object topLevelOption : conf.getTopLevelOption())
	    {
		if (topLevelOption instanceof JAXBElement)
		    {
			parseSimpleOption((JAXBElement)topLevelOption,
					  options);
		    }
		else if (topLevelOption 
			 instanceof 
			 Resolution) 
		    {
			Resolution res = 
			    (Resolution)topLevelOption;

			options.resolution.checkUnifierDirections = 
			    res.isCheckUnifierDirections();			
		    }
		else if (topLevelOption 
			 instanceof 
			 Paramodulation) 
		    {
			Paramodulation par = 
			    (Paramodulation)topLevelOption;

			options.paramodulation.on = par.isOn();	
			options.paramodulation.checkUnifierDirections = 
			    par.isCheckUnifierDirections();			
		    }
		else if (topLevelOption 
			 instanceof 
			 ForwardSubsumption) 
		    {
			ForwardSubsumption fs = 
			    (ForwardSubsumption)topLevelOption;

			options.forwardSubsumption.on = fs.isOn();
			options.forwardSubsumption.equalityPretest = fs.isEqualityPretest();
			options.forwardSubsumption.featureFunctionVector =
			    new ClauseFeatureVector(parseClauseFeatureFunctionVector(fs.getFeatureFunctionVector()));
			
		    }
		else if (topLevelOption 
			 instanceof 
			 ForwardSubsumptionResolution) 
		    {
			ForwardSubsumptionResolution fsr = 
			    (ForwardSubsumptionResolution)topLevelOption;

			options.forwardSubsumptionResolution.on = fsr.isOn();
			
		    }
		else if (topLevelOption 
			 instanceof 
			 BackwardSubsumption) 
		    {
			BackwardSubsumption bs = 
			    (BackwardSubsumption)topLevelOption;

			options.backwardSubsumption.on = bs.isOn();
			options.backwardSubsumption.featureFunctionVector =
			    new ClauseFeatureVector(parseClauseFeatureFunctionVector(bs.getFeatureFunctionVector()));
			
		    }
		else if (topLevelOption 
			 instanceof 
			 ForwardDemodulation) 
		    {
			ForwardDemodulation fd = 
			    (ForwardDemodulation)topLevelOption;

			options.forwardDemodulation.on = fd.isOn();
			options.forwardDemodulation.featureFunctionVector =
			    new TermFeatureVector(parseTermFeatureFunctionVector(fd.getFeatureFunctionVector()));
			
		    }
		else if (topLevelOption 
			 instanceof 
			 BackwardDemodulation) 
		    {
			BackwardDemodulation bd = 
			    (BackwardDemodulation)topLevelOption;

			options.backwardDemodulation.on = bd.isOn();
			options.backwardDemodulation.featureFunctionVector =
			    new TermFeatureVector(parseTermFeatureFunctionVector(bd.getFeatureFunctionVector()));
			
		    }
		else if (topLevelOption 
			 instanceof 
			 SimplifyingEqualityResolution)
		    {
			SimplifyingEqualityResolution ser =
			    (SimplifyingEqualityResolution)topLevelOption;
			options.simplifyingEqualityResolution.on =
			    ser.isOn();
		    }
		else if (topLevelOption 
			 instanceof 
			 LiteralSelection)
		    {
			LiteralSelection ls = 
			    (LiteralSelection)topLevelOption;
			
			List<Object> funcAssignments = 
			    ls.getLiteralSelectionFunctionAssignment();

			for (Object funcAssig : funcAssignments)
			    parseLitSelFuncAssignment(funcAssig,options);
		    }
		else if (topLevelOption 
			 instanceof 
			 UnifiabilityEstimation)
		    {
			UnifiabilityEstimation ue = 
			    (UnifiabilityEstimation)topLevelOption;
			
			BigInteger maxDepth = ue.getMaxDepth();
			Float deepeningCoeff = 
			    ue.getDeepeningCoeff();
			Float duplicateVarUnifiability = 
			    ue.getDuplicateVarUnifiability();
			Float constUnifiability = 
			    ue.getConstUnifiability();
			Float funcUnifiability = ue.getFuncUnifiability();
			Float propConstUnifiability = 
			    ue.getPropConstUnifiability();
			Float predUnifiability = ue.getPredUnifiability();

			if (maxDepth != null)
			    options.unifiabilityEstimation.maxDepth = 
				maxDepth.intValue();

			if (deepeningCoeff != null)
			    options.unifiabilityEstimation.deepeningCoeff = 
				deepeningCoeff.floatValue();
			
			if (duplicateVarUnifiability != null)
			    options.unifiabilityEstimation.duplicateVarUnifiability = 
				duplicateVarUnifiability.floatValue();
			if (constUnifiability != null)
			    options.unifiabilityEstimation.constUnifiability = 
				constUnifiability.floatValue();
			if (funcUnifiability != null)
			    options.unifiabilityEstimation.funcUnifiability = 
				funcUnifiability.floatValue();
			if (propConstUnifiability != null)
			    options.unifiabilityEstimation.propConstUnifiability = 
				propConstUnifiability.floatValue();
			if (predUnifiability != null)
			    options.unifiabilityEstimation.predUnifiability = 
				predUnifiability.floatValue();

		    }
		else if (topLevelOption 
			 instanceof 
			 SelectionUnitPenalty)
		    {
			SelectionUnitPenalty sup = 
			    (SelectionUnitPenalty)topLevelOption;

			InputFormulaRole role = sup.getFor();
			int baseValue = sup.getBaseValue().intValue();
			
			ResolutionSelectionUnitPenalty resB = 
			    sup.getResolutionB();
			ResolutionSelectionUnitPenalty resG = 
			    sup.getResolutionG();
			ResolutionSelectionUnitPenalty resI = 
			    sup.getResolutionI();

			
			EqLHSForParamodSelectionUnitPenalty eqLHS_B = 
			    sup.getEqLHSForParamodB();
			EqLHSForParamodSelectionUnitPenalty eqLHS_G = 
			    sup.getEqLHSForParamodG();
			EqLHSForParamodSelectionUnitPenalty eqLHS_I = 
			    sup.getEqLHSForParamodI();


			RedexForParamodSelectionUnitPenalty redForPar = 
			    sup.getRedexForParamod();
			

			EqualityFactoringSelectionUnitPenalty eqFact = 
			    sup.getEqualityFactoring();
			
			EqualityResolutionSelectionUnitPenalty eqRes = 
			    sup.getEqualityResolution();

			KernelOptions.SelectionUnitPenalty selUnPen = 
			    parseSelectionUnitPenalty(baseValue,
						      resB,
						      resG,
						      resI,
						      eqLHS_B,
						      eqLHS_G,
						      eqLHS_I,
						      redForPar,
						      eqFact,
						      eqRes);

			switch (role)
			    {
			    case AXIOMS:
				options.selectionUnitPenaltyForAxioms = 
				    selUnPen;
				break;
			    case HYPOTHESES:
				options.selectionUnitPenaltyForHypotheses = 
				    selUnPen;
				break;
			    case NEGATED_CONJECTURES:
				options.selectionUnitPenaltyForNegatedConjectures = 
				    selUnPen;
				break;
			    default:
				assert false;
				return;
			    }; // switch (role)
			

		    }
		else if (topLevelOption 
			 instanceof 
			 QueryRewriting)
		    {
			QueryRewriting qr = (QueryRewriting)topLevelOption;
			
			options.queryRewriting.allowFunctionsInConstraints = 
			    qr.isAllowFunctionsInConstraints().booleanValue();

			options.queryRewriting.inconsistenciesAsAnswers = 
			    qr.isInconsistenciesAsAnswers().booleanValue();

			LimitedTransitivePredicateChainingInConstraints lt =
			    qr.getLimitedTransitivePredicateChainingInConstraints();
			options.queryRewriting.limitedTransitivePredicateChainingInConstraints.on = 
			    lt.isOn();
			
			for (LimitedTransitivePredicateChainingInConstraints.PredicateChainLimit lim : 
				 lt.getPredicateChainLimit())
			    {
				String name = lim.getName();
				int arity = lim.getArity().intValue();
				
				if (arity != 2)
				    throw new java.lang.Exception("Chain limit is set on a non-binary predicate " + name + "/" + arity);
				
				int limit = lim.getLimit().intValue();

				Integer previousLimit = 
				    options.
				    queryRewriting.
				    limitedTransitivePredicateChainingInConstraints.
				    limitedChainPredicates.
				    get(new Pair(name,new Integer(arity)));
				
				if (previousLimit == null ||
				    previousLimit > limit)
				    options.
					queryRewriting.
					limitedTransitivePredicateChainingInConstraints.
					limitedChainPredicates.
					put(new Pair(name,new Integer(arity)),
					    new Integer(limit));
				
			    }; // for (LimitedTransitivePredicateChainingInConstraints.PredicateChainLimit lim : 

		    }
		else
		    {
			assert false;
			return;
		    };

	    }; // for (Object topLevelOption : conf.getTopLevelOption())

    } // parse(File configFile,logic.is.power.alabai.KernelOptions options)
	


    private 
	static 
	void parseSimpleOption(JAXBElement topLevelOption,
			       KernelOptions options) {
	
	String optName = topLevelOption.getName().getLocalPart();
	
	if (optName.equals("maximalSelectionUnitActiveness")) {
	    BigInteger value = (BigInteger)topLevelOption.getValue();
	    options.maximalSelectionUnitActiveness = value.intValue();
	}
	else if (optName.equals("maximalSelectionUnitPenalty")) {
	    BigInteger value = (BigInteger)topLevelOption.getValue();
	    options.maximalSelectionUnitPenalty = value.intValue();
	}
	else if (optName.equals("propositionalTautologyTest")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.propositionalTautologyTest = value.booleanValue();
	}
	else if (optName.equals("equationalTautologyTest")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.equationalTautologyTest = value.booleanValue();
	}
	else if (optName.equals("clauseSelectionDelay")) {
	    BigInteger value = (BigInteger)topLevelOption.getValue();
	    options.clauseSelectionDelay = value.intValue();
	}
	else if (optName.equals("printInput")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printInput = value.booleanValue();
	}
	else if (optName.equals("printNew")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printNew = value.booleanValue();
	}
	else if (optName.equals("printKept")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printKept = value.booleanValue();
	}
	else if (optName.equals("printDiscarded")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printDiscarded = value.booleanValue();
	}
	else if (optName.equals("printAssembled")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printAssembled = value.booleanValue();
	}
	else if (optName.equals("printSelectedClauses")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printSelectedClauses = value.booleanValue();
	}
	else if (optName.equals("printActivatedClauses")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printActivatedClauses = value.booleanValue();
	}
	else if (optName.equals("markUnselectedLiterals")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.markUnselectedLiterals = value.booleanValue();
	}
	else if (optName.equals("printPromoted")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printPromoted = value.booleanValue();
	}
	else if (optName.equals("selUnitPromotionWatchList")) {
	    List<BigInteger> value = 
		(List<BigInteger>)topLevelOption.getValue();
	    if (!value.isEmpty())
		{
		    if (options.selUnitPromotionWatchList == null)
			options.selUnitPromotionWatchList = 
			    new HashSet<Long>();
		    for (BigInteger n : value)
			options.selUnitPromotionWatchList.add(new Long(n.longValue()));
		};
	}
	else if (optName.equals("printSelectionUnits")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printSelectionUnits = value.booleanValue();
	}
	else if (optName.equals("printUnifiability")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printUnifiability = value.booleanValue();
	}
	else if (optName.equals("printPenaltyComputation")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.printPenaltyComputation = value.booleanValue();
	}
	else if (optName.equals("otherEqualitySymbols")) {
	    List<String> value = 
		(List<String>)topLevelOption.getValue();
	    options.otherEqualitySymbols = 
		new LinkedList(value);
	}
	else if (optName.equals("answerPredicate")) {
	    AlabaiConfig.AnswerPredicate value =
		(AlabaiConfig.AnswerPredicate)topLevelOption.getValue();
	    String name = value.getName();
	    int arity = value.getArity().intValue();
	    options.addAnswerPredicate(name,arity);
	}
	else if (optName.equals("constraintPredicate")) {
	    AlabaiConfig.ConstraintPredicate value =
		(AlabaiConfig.ConstraintPredicate)topLevelOption.getValue();
	    String name = value.getName();
	    int arity = value.getArity().intValue();
	    options.addConstraintPredicate(name,arity);
	}
	else if (optName.equals("reductionOrdering")) {
	    String value = (String)topLevelOption.getValue();
	    
	    if (value.equals("SubtermRelation"))
		{
		    options.reductionOrdering = 
			KernelOptions.ReductionOrdering.SubtermRelation;
		}
	    else if (value.equals("NonrecursiveKBO"))
		{
		    options.reductionOrdering = 
			KernelOptions.ReductionOrdering.NonrecursiveKBO;
		}
	    else
		assert false;
	}
	else if (optName.equals("mainEqualitySymbol")) {

			AlabaiConfig.MainEqualitySymbol mes =
			    (AlabaiConfig.MainEqualitySymbol)topLevelOption.
			    getValue();

			options.mainEqualitySymbol = mes.getPositive();
			options.mainDisequalitySymbol = mes.getNegative();
			options.mainEqualityIsInfix = mes.isInfix();
	}
	else if (optName.equals("preserveInput")) {
	    Boolean value = (Boolean)topLevelOption.getValue();
	    options.preserveInput = value.booleanValue();
	}
	else
	    {
		
		assert false;
	    };

    } // parseSimpleOption(JAXBElement topLevelOption,..)


    private 
	static
	LinkedList<ClauseFeature> 
	parseClauseFeatureFunctionVector(ClauseFeatureFunctionVector vector) 
    {
	LinkedList<ClauseFeature> result = new LinkedList<ClauseFeature>();

	for (Object featureObj : vector.getClauseFeature())
	    {
		if (featureObj instanceof JAXBElement) {
		    result.addLast(parseClauseFeature((JAXBElement)featureObj));
		}
		else if (featureObj instanceof NumberOfSymbolsFromCategories) {

		    int modulus = 
			((NumberOfSymbolsFromCategories)featureObj).
			getMod().
			intValue();
		    ClauseFeature feature =
			StandardClauseFeature.
			numberOfSymbolsFromCategories(modulus);

		    result.addLast(feature);
		}
		else if (featureObj instanceof PlugInClauseFeature) {
		    String name = 
			((PlugInClauseFeature)featureObj).getName();
		    result.addLast(new PlugInReference.ScalarClauseFeature(name));
		}
	    };
	
	return result;

    } // parseClauseFeatureFunctionVector(..)





    /** For simple features without parameters. */
    private 
	static
	ClauseFeature
	parseClauseFeature(JAXBElement featureElement) {
	
	String name = featureElement.getName().getLocalPart();

	if (name.equals("numberOfLiterals")) {
	    return StandardClauseFeature.numberOfLiterals();
	}
	else if (name.equals("numberOfPositiveLiterals")) {
	    return StandardClauseFeature.numberOfPositiveLiterals();
	}
	else if (name.equals("numberOfNegativeLiterals")) {
	    return StandardClauseFeature.numberOfNegativeLiterals();
	}
	else if (name.equals("numberOfSymbols")) {
	    return StandardClauseFeature.numberOfSymbols();
	};

	assert false;
	return null;

    } // parseClauseFeature(JAXBElement featureElement)




    private 
	static
	LinkedList<TermFeature> 
	parseTermFeatureFunctionVector(TermFeatureFunctionVector vector) 
    {
	LinkedList<TermFeature> result = new LinkedList<TermFeature>();

	for (Object featureObj : vector.getTermFeature())
	    {
		if (featureObj instanceof JAXBElement) {
		    result.addLast(parseTermFeature((JAXBElement)featureObj));
		}
		else if (featureObj instanceof NumberOfSymbolsFromCategories) {

		   
		    int modulus = 
			((NumberOfSymbolsFromCategories)featureObj).
			getMod().
			intValue();
		    TermFeature feature =
			StandardTermFeature.
			numberOfSymbolsFromCategories(modulus);
		    result.addLast(feature);
		}
		else if (featureObj instanceof PlugInTermFeature) {
		    String name = 
			((PlugInTermFeature)featureObj).getName();
		    result.addLast(new PlugInReference.ScalarTermFeature(name));
		}
	    };
	
	return result;

    } // parseTermFeatureFunctionVector(..)






    /** For simple features without parameters. */
    private 
	static
	TermFeature
	parseTermFeature(JAXBElement featureElement) {
	
	String name = featureElement.getName().getLocalPart();

	if (name.equals("numberOfSymbols")) {
	    return StandardTermFeature.numberOfSymbols();
	};

	assert false;
	return null;

    } // parseTermFeature(JAXBElement featureElement)



    private static void parseLitSelFuncAssignment(Object assig,
						  KernelOptions options) {
	
	Empty total;
	Empty allMaximal;
	LiteralSelection.ForNegatedConjectures.AllMaximalWRT allMaximalWRT;
	LiteralSelection.ForNegatedConjectures.PosHyper posHyper;
	
	if (assig 
	    instanceof 
	    LiteralSelection.ForAxioms)
	    {
		total = ((LiteralSelection.ForAxioms)assig).getTotal();
		allMaximal = ((LiteralSelection.ForAxioms)assig).getAllMaximal();
		allMaximalWRT = ((LiteralSelection.ForAxioms)assig).getAllMaximalWRT();
		posHyper = ((LiteralSelection.ForAxioms)assig).getPosHyper();
		options.literalSelection.forAxioms =
		    parseLitSelFun(total,
				   allMaximal,
				   allMaximalWRT,
				   posHyper);
	      
	    }
	else if (assig 
		 instanceof 
		 LiteralSelection.ForHypotheses)
	    {
		total = ((LiteralSelection.ForHypotheses)assig).getTotal();
		allMaximal = ((LiteralSelection.ForHypotheses)assig).getAllMaximal();
		allMaximalWRT = ((LiteralSelection.ForHypotheses)assig).getAllMaximalWRT();
		posHyper = ((LiteralSelection.ForHypotheses)assig).getPosHyper();
		options.literalSelection.forHypotheses = 
		    parseLitSelFun(total,
				   allMaximal,
				   allMaximalWRT,
				   posHyper);
	    }
	else if (assig 
		 instanceof 
		 LiteralSelection.ForNegatedConjectures)
	    {
		total = ((LiteralSelection.ForNegatedConjectures)assig).getTotal();
		allMaximal = 
		    ((LiteralSelection.ForNegatedConjectures)assig).getAllMaximal();
		allMaximalWRT = 
		    ((LiteralSelection.ForNegatedConjectures)assig).getAllMaximalWRT();
		posHyper = ((LiteralSelection.ForNegatedConjectures)assig).getPosHyper();
		options.literalSelection.forNegatedConjectures = 
		    parseLitSelFun(total,
				   allMaximal,
				   allMaximalWRT,
				   posHyper);
	    }
	else
	    {
		assert false;
		return;
	    };


    } // parseLitSelFuncAssignment(Object assig,..)
					   


    private 
	static 
	LiteralSelectionFunction
	parseLitSelFun(Empty total,
		       Empty allMaximal,
		       LiteralSelection.ForNegatedConjectures.AllMaximalWRT allMaximalWRT,
		       LiteralSelection.ForNegatedConjectures.PosHyper posHyper) {
	if (total != null)
	    return new LitSelFunTotal();
	if (allMaximal != null)
	    return new LitSelFunAllMaximal();
	if (allMaximalWRT != null) 
	    return parseLitSelFunAllMaximalWRT(allMaximalWRT);
	if (posHyper != null) 
	    return parseLitSelFunPosHyper(posHyper);
	assert false;
	return null;
    } // parseLitSelFun(Empty total,..)


    private 
	static 
	LitSelFunPosHyper 
	parseLitSelFunPosHyper(LiteralSelection.ForNegatedConjectures.PosHyper posHyper) 
    {
	
	Empty tot = 
	    posHyper.getFilterForNegative().getTotal();
	
	Empty am = 
	    posHyper.getFilterForNegative().getAllMaximal();

	LiteralSelection.ForNegatedConjectures.AllMaximalWRT amw = 
	    posHyper.getFilterForNegative().getAllMaximalWRT();

	LiteralSelection.ForNegatedConjectures.PosHyper ph = 
	    posHyper.getFilterForNegative().getPosHyper();

	LiteralSelectionFunction neg = parseLitSelFun(tot,am,amw,ph);


	
	tot = 
	    posHyper.getFilterForPositive().getTotal();
	am = 
	    posHyper.getFilterForPositive().getAllMaximal();
	amw = 
	    posHyper.getFilterForPositive().getAllMaximalWRT();
	ph = 
	    posHyper.getFilterForPositive().getPosHyper();

	LiteralSelectionFunction pos = parseLitSelFun(tot,am,amw,ph);

	
	return new LitSelFunPosHyper(neg,pos);

    } // parseLitSelFunPosHyper(..) 


    private 
	static 
	LitSelFunAllMaximalWRT
	parseLitSelFunAllMaximalWRT(LiteralSelection.ForNegatedConjectures.AllMaximalWRT allMaximalWRT) 
    {
	Empty adm = allMaximalWRT.getAdmissible();

	LiteralSelection.ForNegatedConjectures.AllMaximalWRT.ByShallowWeight bsw =
	    allMaximalWRT.getByShallowWeight();

	LiteralOrdering litOrd = parseLitOrd(adm,
					     bsw);

	return new LitSelFunAllMaximalWRT(litOrd);

    } // parseLitSelFunAllMaximalWRT(LiteralSelection.ForNegatedConjectures.AllMaximalWRT allMaximalWRT) 
    

    private 
	static 
	LiteralOrdering
	parseLitOrd(Empty admissible,
		    LiteralSelection.ForNegatedConjectures.AllMaximalWRT.ByShallowWeight byShallowWeight) {
	
	if (admissible != null)
	    return AdmissibleLiteralOrdering.some();

	if (byShallowWeight != null)
	    return new LitOrdByShallowWeight(byShallowWeight.getMaxDepth().intValue());
	    
	
	assert false;
	return null;

    } // parseLitOrd(Empty admissible,..)

    
    
    private
	static
	KernelOptions.SelectionUnitPenalty 
	parseSelectionUnitPenalty(int baseValue,

				  ResolutionSelectionUnitPenalty resB,
				  ResolutionSelectionUnitPenalty resG,
				  ResolutionSelectionUnitPenalty resI,

				  EqLHSForParamodSelectionUnitPenalty eqLHS_B,
				  EqLHSForParamodSelectionUnitPenalty eqLHS_G,
				  EqLHSForParamodSelectionUnitPenalty eqLHS_I,

				  RedexForParamodSelectionUnitPenalty redForPar,
				  EqualityFactoringSelectionUnitPenalty eqFact,
				  EqualityResolutionSelectionUnitPenalty eqRes) {

	
	KernelOptions.SelectionUnitPenalty result = 
	    new KernelOptions.SelectionUnitPenalty();

	result.baseValue = baseValue;

	if (resB != null)
	    result.resolutionB = 
		parseResolutionSelectionUnitPenalty(resB);
	if (resG != null)
	    result.resolutionG = 
		parseResolutionSelectionUnitPenalty(resG);
	if (resI != null)
	    result.resolutionI = 
		parseResolutionSelectionUnitPenalty(resI);


	
	if (eqLHS_B != null)
	    result.eqLHSForParamodB = 
		parseEqLHSForParamodSelectionUnitPenalty(eqLHS_B);
	if (eqLHS_G != null)
	    result.eqLHSForParamodG = 
		parseEqLHSForParamodSelectionUnitPenalty(eqLHS_G);
	if (eqLHS_I != null)
	    result.eqLHSForParamodI = 
		parseEqLHSForParamodSelectionUnitPenalty(eqLHS_I);

	
	

	if (redForPar != null)
	    result.redexForParamod = 
		parseRedexForParamodSelectionUnitPenalty(redForPar);
	   

	if (eqFact != null)
	    result.equalityFactoring = 
		parseEqualityFactoringSelectionUnitPenalty(eqFact);
	   
	if (eqRes != null)
	    result.equalityResolution = 
		parseEqualityResolutionSelectionUnitPenalty(eqRes);
	   

	return result;
	
    } // parseSelectionUnitPenalty(int base,..)

    
    private
	static
	KernelOptions.SelectionUnitPenalty.Resolution
	parseResolutionSelectionUnitPenalty(ResolutionSelectionUnitPenalty rsup) {
	
	KernelOptions.SelectionUnitPenalty.Resolution result = 
	    new KernelOptions.SelectionUnitPenalty.Resolution();

	result.generalCoeff = rsup.getGeneralCoeff();

	result.litUnifiabilityCoeff = 
	    parseFloatFloatFunc(rsup.getLitUnifiabilityCoeff());

	result.residualLitCoeff = 
	    parseIntFloatFunc(rsup.getResidualLitCoeff());

	result.residualSizeCoeff = 
	    parseIntFloatFunc(rsup.getResidualSizeCoeff());

	result.litSizeCoeff = 
	    parseIntFloatFunc(rsup.getLitSizeCoeff());

	result.litDepthCoeff = 
	    parseIntFloatFunc(rsup.getLitDepthCoeff());
	
	return result;
	
    } // parseResolutionSelectionUnitPenalty(ResolutionSelectionUnitPenalty rsup)



    
    private
	static
	KernelOptions.SelectionUnitPenalty.EqLHSForParamod
	parseEqLHSForParamodSelectionUnitPenalty(EqLHSForParamodSelectionUnitPenalty rsup) {
	
	KernelOptions.SelectionUnitPenalty.EqLHSForParamod result = 
	    new KernelOptions.SelectionUnitPenalty.EqLHSForParamod();

	result.generalCoeff = rsup.getGeneralCoeff();

	result.lhsUnifiabilityCoeff = 
	    parseFloatFloatFunc(rsup.getLhsUnifiabilityCoeff());

	result.residualLitCoeff = 
	    parseIntFloatFunc(rsup.getResidualLitCoeff());

	result.residualSizeCoeff = 
	    parseIntFloatFunc(rsup.getResidualSizeCoeff());
	
	return result;
	
    } // parseEqLHSForParamodSelectionUnitPenalty(EqLHSForParamodSelectionUnitPenalty rsup)





    
    private
	static
	KernelOptions.SelectionUnitPenalty.RedexForParamod
	parseRedexForParamodSelectionUnitPenalty(RedexForParamodSelectionUnitPenalty rsup) {
	
	KernelOptions.SelectionUnitPenalty.RedexForParamod result = 
	    new KernelOptions.SelectionUnitPenalty.RedexForParamod();

	result.generalCoeff = rsup.getGeneralCoeff();
	result.positiveEqCoeff = rsup.getPositiveEqCoeff();
	result.positiveNonEqCoeff = rsup.getPositiveNonEqCoeff();
	result.negativeEqCoeff = rsup.getNegativeEqCoeff();
	result.negativeNonEqCoeff = rsup.getNegativeNonEqCoeff();

	result.redexUnifiabilityCoeff = 
	    parseFloatFloatFunc(rsup.getRedexUnifiabilityCoeff());

	result.residualLitCoeff = 
	    parseIntFloatFunc(rsup.getResidualLitCoeff());

	result.residualSizeCoeff = 
	    parseIntFloatFunc(rsup.getResidualSizeCoeff());
	
	result.redexDepthCoeff = 
	    parseIntFloatFunc(rsup.getRedexDepthCoeff());
	
	return result;
	
    } // parseRedexForParamodSelectionUnitPenalty(..)


    private
	static
	KernelOptions.SelectionUnitPenalty.EqualityFactoring
	parseEqualityFactoringSelectionUnitPenalty(EqualityFactoringSelectionUnitPenalty rsup) {
	
	KernelOptions.SelectionUnitPenalty.EqualityFactoring result = 
	    new KernelOptions.SelectionUnitPenalty.EqualityFactoring();

	result.generalCoeff = rsup.getGeneralCoeff();

	result.litNumCoeff = 
	    parseIntFloatFunc(rsup.getLitNumCoeff());

	result.clauseSizeCoeff = 
	    parseIntFloatFunc(rsup.getClauseSizeCoeff());
	
	return result;
	
    } // parseEqualityFactoringSelectionUnitPenalty(..)


    private
	static
	KernelOptions.SelectionUnitPenalty.EqualityResolution
	parseEqualityResolutionSelectionUnitPenalty(EqualityResolutionSelectionUnitPenalty rsup) {
	
	KernelOptions.SelectionUnitPenalty.EqualityResolution result = 
	    new KernelOptions.SelectionUnitPenalty.EqualityResolution();

	result.generalCoeff = rsup.getGeneralCoeff();

	result.litNumCoeff = 
	    parseIntFloatFunc(rsup.getLitNumCoeff());

	result.clauseSizeCoeff = 
	    parseIntFloatFunc(rsup.getClauseSizeCoeff());
	
	return result;
	
    } // parseEqualityResolutionSelectionUnitPenalty(..)



    private
	static 
	UnaryFunctionObject<Integer,Float> 
	parseIntFloatFunc(IntFloatFunc discreetMap) {

	IntFloatLinearInterpolant result = 
	    new IntFloatLinearInterpolant();

	for (IntFloatFunc.Map pair : discreetMap.getMap())
	    {
		result.put(pair.getX().intValue(),pair.getY());
	    };

	return result;

    } // parseIntFloatFunc(IntFloatFunc discreetMap)


    private
	static 
	UnaryFunctionObject<Float,Float> 
	parseFloatFloatFunc(FloatFloatFunc discreetMap) {

	FloatFloatLinearInterpolant result = 
	    new FloatFloatLinearInterpolant();

	for (FloatFloatFunc.Map pair : discreetMap.getMap())
	    {
		result.put(pair.getX(),pair.getY());
	    };

	return result;

    } // parseFloatFloatFunc(FloatFloatFunc discreetMap)

    private Unmarshaller _unmarshaller;

} // class Parser