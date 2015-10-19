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


import java.util.LinkedList;

import logic.is.power.cushion.*;



  /**
   * Implements selection of fine selection for promotion;
   * maintains a database of fine selection units currently
   * available for promotion; selection for promotion is done
   * based on the penalty values associated with the units:
   * the bigger the penalty, the smaller the chances of the corresponding
   * unit to be selected for promotion at the next step. 
   *
   * In general, selection is done as follows. First, we select some
   * penalty value assigned to at least one selection unit. Then, we select 
   * some selection unit with this penalty by taking a selection
   * unit that has waited the longest time since the last promotion.
   *
   * The penalty value selection can be done in the following two modes.
   * In the <b>default mode</b>, the chances of selection are inversely 
   * proportional to the <em>penalty value multiplied by the number of
   * the units with this value</em>. 
   * In the <b>penalty-value-only mode</b>, the chances of selection are 
   * inversely proportional to the <em>penalty value itself</em>,  
   * regardless of how many units with this penalty are present.
   */
  class SelectionUnitPromotion {

      public SelectionUnitPromotion(int maximalSelectionUnitPenalty) { 

	  _maximalSelectionUnitPenalty = maximalSelectionUnitPenalty;
	  _maxPenaltySquare = 
	      maximalSelectionUnitPenalty *
	      maximalSelectionUnitPenalty;

	  _unitsByPenalty = 
	      new SelectionUnitList[_maximalSelectionUnitPenalty + 1];
	  for (int p = 1; 
	       p <= _maximalSelectionUnitPenalty; 
	       ++p)
	      _unitsByPenalty[p] = new SelectionUnitList();
	      
	      
	  _randomNumberGenerator = 
	      new RandomWithDiscreteDynamicDistribution(maximalSelectionUnitPenalty,
							23453);
	  _numberOfUnits = 0;

	  setPenaltyValueOnlyMode(false);
      } // SelectionUnitPromotion()


      /** Sets a flag that switches the selection to the penalty-value-only mode.
       *  <b>pre:</b> the database is empty (eg, just constructed).
       */
      public void setPenaltyValueOnlyMode(boolean flag) {
	  _penaltyValueOnlyMode = flag;
	  if (_penaltyValueOnlyMode)
	  {
	      for (int penalty = 1;
		   penalty <= _maximalSelectionUnitPenalty;
		   ++penalty)
	      {
		  _randomNumberGenerator.setRelativeProbability(penalty - 1,
								_maxPenaltySquare/penalty);
	      };
	      
	  };
      } // setPenaltyValueOnlyMode(boolean flag)

      /** Adds <code>selUnit</code> to the database of units available for promotion. */
      public void add(FineSelectionUnit selUnit) {
	  
	  //System.out.println("SU PROM ADD " + selUnit); 

	  int penalty = selUnit.penalty();	  
	  
	  assert penalty > 0;

	  assert !_unitsByPenalty[penalty].contains(selUnit);

	  if (_unitsByPenalty[penalty].isEmpty())
	  {
	      assert !_randomNumberGenerator.isAvailable(penalty - 1);
	      _randomNumberGenerator.makeAvailable(penalty - 1);
	      _unitsByPenalty[penalty].addLast(selUnit);
	      if (!_penaltyValueOnlyMode)
		  _randomNumberGenerator.setRelativeProbability(penalty - 1,
								_maxPenaltySquare/penalty);
	  }
	  else
	  {
	      assert _randomNumberGenerator.isAvailable(penalty - 1);
	      
	      _unitsByPenalty[penalty].addLast(selUnit);

	      if (!_penaltyValueOnlyMode)
	      {
		  long probability = 
		      ((long)_unitsByPenalty[penalty].size()) * 
		      (_maxPenaltySquare/penalty);

		  assert probability >= 0;

		  _randomNumberGenerator.setRelativeProbability(penalty - 1,
								probability);
	      };
								
	  };

	  ++_numberOfUnits;

      } // add(FineSelectionUnit selUnit)




      /** Removes <code>selUnit</code> from the database of units available 
       *  for promotion. 
       */
      public boolean remove(FineSelectionUnit selUnit) {

	  //System.out.println("SU PROM REMOVE " + selUnit); 


	  int penalty = selUnit.penalty();

	  assert penalty > 0;

	  if (!_unitsByPenalty[penalty].remove(selUnit)) 
	      return false;

	  if (_unitsByPenalty[penalty].isEmpty()) 
	  {
	      _randomNumberGenerator.makeUnavailable(penalty - 1);
	  }
	  else if (_penaltyValueOnlyMode)
	  {
	      long probability =
		  _unitsByPenalty[penalty].size() * (_maxPenaltySquare/penalty);
	      _randomNumberGenerator.setRelativeProbability(penalty - 1,
							    probability);
	  };

	  --_numberOfUnits;

	  assert _numberOfUnits >= 0;

	  return true;

      } // remove(FineSelectionUnit selUnit)




      /** Selects a unit for promotion accoring to the procedure
       *  described in the doc clause of this class 
       *  (see {@link alabai_je.SelectionUnitPromotion}).
       *  @return <code>false</code> if no selection units are available 
       *          for promotion
       */
      public boolean select(Ref<FineSelectionUnit> selUnitForPromotion) {

	  assert _numberOfUnits >= 0;

	  if (_numberOfUnits == 0)
	      // No selection units left.
	      return false;

	  int penalty = _randomNumberGenerator.generate() + 1;

	  assert penalty > 0;

	  assert !_unitsByPenalty[penalty].isEmpty();

	  selUnitForPromotion.content = _unitsByPenalty[penalty].removeFirst();

	  // The selection unit goes to the end of the list:
	  _unitsByPenalty[penalty].addLast(selUnitForPromotion.content);
	  
	  return true;

      } // select(FineSelectionUnit selUnitForPromotion)

      
      /** Workaround for the problem of arrays of instances of generic classes. */
      private static class SelectionUnitList 
	  extends LinkedList<FineSelectionUnit> {
	  
	  public SelectionUnitList() {
	      super();
	  }

      } // class SelectionUnitList


      //                     Data:
      
      private final int _maximalSelectionUnitPenalty;

      private final int _maxPenaltySquare;

      private boolean _penaltyValueOnlyMode;

      private SelectionUnitList[] _unitsByPenalty;

      /** Will generate values from [0,maximalSelectionUnitPenalty). */
      private 
      RandomWithDiscreteDynamicDistribution 
      _randomNumberGenerator;

      /** Number of units currently in this database. */
      private long _numberOfUnits;

  }; // class SelectionUnitPromotion
