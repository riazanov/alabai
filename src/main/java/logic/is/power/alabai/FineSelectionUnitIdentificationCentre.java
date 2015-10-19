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

import logic.is.power.cushion.*;

  /**
   * A common umbrella for all fine selection unit identification
   * modules.
   */
  class FineSelectionUnitIdentificationCentre 
      implements SimpleReceiver<Clause>
  {

      public 
	  FineSelectionUnitIdentificationCentre(SimpleReceiver<FineSelectionUnit> outputReceiver,
						int maxPenalty,
						KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForAxioms,
						KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForHypotheses,
						KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForNegatedConjectures,
						KernelOptions.Paramodulation paramodulation) {
	  _outputReceiver = outputReceiver;
	  _printExtractedUnits = false;
	  _printProcessedClauses = false;
	  _outputInterceptor = new OutputInterceptor(); 

	  if (paramodulation.on)
	      _eqLHSForParamodSelUnIdentification = 
		  new EqLHSForParamodSelUnIdentification(_outputInterceptor,
							 maxPenalty,
							 selectionUnitPenaltyForAxioms,
							 selectionUnitPenaltyForHypotheses,
							 selectionUnitPenaltyForNegatedConjectures); 

	  if (paramodulation.on)
	      _equalityFactoringSelUnIdentification = 
		  new EqualityFactoringSelUnIdentification(_outputInterceptor,
							   maxPenalty,
							   selectionUnitPenaltyForAxioms,
							   selectionUnitPenaltyForHypotheses,
							   selectionUnitPenaltyForNegatedConjectures);

	  if (paramodulation.on)
	      _equalityResolutionSelUnIdentification = 
		  new EqualityResolutionSelUnIdentification(_outputInterceptor,
							    maxPenalty,
							    selectionUnitPenaltyForAxioms,
							    selectionUnitPenaltyForHypotheses,
							    selectionUnitPenaltyForNegatedConjectures); 

	  if (paramodulation.on)
	      _redexForParamodSelUnIdentification = 
		  new RedexForParamodSelUnIdentification(_outputInterceptor,
							 maxPenalty,
							 selectionUnitPenaltyForAxioms,
							 selectionUnitPenaltyForHypotheses,
							 selectionUnitPenaltyForNegatedConjectures);
	  
	  _resolutionSelUnIdentification = 
	      new ResolutionSelUnIdentification(_outputInterceptor,
						maxPenalty,
						selectionUnitPenaltyForAxioms,
						selectionUnitPenaltyForHypotheses,
						selectionUnitPenaltyForNegatedConjectures);

      } // FineSelectionUnitIdentificationCentre(..)


      /** Switches the identification of I- and G-versions of the resolution
       *  selection units: if <code>flag == false</code>, no
       *  I or G units will be created.
       */
      public void setCheckUnifierDirectionsInResolution(boolean flag) {
	  _resolutionSelUnIdentification.
	      setCheckUnifierDirections(flag);
      }

      /** Switches the identification of I- and G-versions of the paramodulation
       *  selection units: if <code>flag == false</code>, no
       *  I or G units will be created.
       */
      public void setCheckUnifierDirectionsInParamodulation(boolean flag) {
	  if (_eqLHSForParamodSelUnIdentification != null)
	      _eqLHSForParamodSelUnIdentification.
		  setCheckUnifierDirections(flag);
      }
      
      public void setPrintProcessedClauses(boolean flag) {
	  _printProcessedClauses = flag;
      } 

      public void setPrintExtractedUnits(boolean flag) {
	  _printExtractedUnits = flag;
      } 

      /** Switches output of penalty computation details. */
      public void setPrintPenaltyComputation(boolean flag) {
	  _resolutionSelUnIdentification.
	      setPrintPenaltyComputation(flag);
	  if (_eqLHSForParamodSelUnIdentification != null)
	      _eqLHSForParamodSelUnIdentification.
		  setPrintPenaltyComputation(flag);
	  if (_redexForParamodSelUnIdentification != null)
	      _redexForParamodSelUnIdentification.
		  setPrintPenaltyComputation(flag);
	  if (_equalityFactoringSelUnIdentification != null)
	      _equalityFactoringSelUnIdentification.
		  setPrintPenaltyComputation(flag);
	  if (_equalityResolutionSelUnIdentification != null)
	      _equalityResolutionSelUnIdentification.
		  setPrintPenaltyComputation(flag);
      }

      /** Processes the clause, ie, extracts all fine selection units
       *  with this clause and sends them to the output receiver(s).
       *  @return true (always)
       */
      public boolean receive(Clause cl) {
	  if (_eqLHSForParamodSelUnIdentification != null)
	      _eqLHSForParamodSelUnIdentification.process(cl);
	  if (_equalityFactoringSelUnIdentification != null)
	      _equalityFactoringSelUnIdentification.process(cl);
	  if (_equalityResolutionSelUnIdentification != null)
	      _equalityResolutionSelUnIdentification.process(cl);
	  if (_redexForParamodSelUnIdentification != null)
	      _redexForParamodSelUnIdentification.process(cl);
	  _resolutionSelUnIdentification.process(cl);
	  
	  if (_printProcessedClauses)
	      System.out.println("Act: " + cl); 

	  return true;
      } 

    
      //                    Private classes:

      
      private class OutputInterceptor 
	  implements SimpleReceiver<FineSelectionUnit> {
	  
	  public OutputInterceptor() {}

	  public boolean receive(FineSelectionUnit selUnit) {
	      if (_printExtractedUnits)
		  System.out.println("SelUn/Extr: " + selUnit);
	      return _outputReceiver.receive(selUnit);
	  }

      } // class OutputInterceptor 


      //                     Data:

    private SimpleReceiver<FineSelectionUnit> _outputReceiver;

    private boolean _printProcessedClauses;

    private boolean _printExtractedUnits;
    
    /** Switches output of penalty computation details. */
    private boolean _printPenaltyComputation;

	/** Intercepts the output from all components and resends
	 *  it to _outputReceiver.
	 */
      private OutputInterceptor _outputInterceptor;

    private EqLHSForParamodSelUnIdentification _eqLHSForParamodSelUnIdentification;

    private EqualityFactoringSelUnIdentification _equalityFactoringSelUnIdentification;

    private EqualityResolutionSelUnIdentification _equalityResolutionSelUnIdentification;

    private RedexForParamodSelUnIdentification _redexForParamodSelUnIdentification;
    
    private ResolutionSelUnIdentification _resolutionSelUnIdentification;

  }; // class FineSelectionUnitIdentificationCentre
