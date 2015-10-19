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


/** Common base for classes representing various kinds of
 *  selection units that have varying degrees of activeness,
 *  such as all resolution- and paramodulation-related units
 *  (but not, eg, equality factoring units).
 *  The initial value of activeness is 0.
 */
abstract class FineSelectionUnitWithActiveness extends FineSelectionUnit {

    public FineSelectionUnitWithActiveness(int kind,
					   Clause clause) {
	super(kind,clause);
	_activeness = 0;
    }
    

    public int activeness() { return _activeness; }

    public void incrementActiveness() {
	++_activeness; 
    }
  
    
    /** Cluster id corresponding to the unit's kind and current activeness. */
    public BitSet cluster() {
	return 
	    FineSelectionUnitActiveness.cluster(kind(),
						_numberOfBitsInKindAsNumber,
						_activeness);
    } 




    public LinkedList<BitSet> complementaryClusters() {

	LinkedList<BitSet> result = new LinkedList<BitSet>();
	
	int complementaryActiveness = 
	    FineSelectionUnitActiveness.complementaryActiveness(_activeness);

	switch (kind())
	{
	case FineSelectionUnit.Kind.ResolutionI:
	    result.
		addLast(FineSelectionUnitActiveness.
			cluster(FineSelectionUnit.Kind.ResolutionG,
				_numberOfBitsInKindAsNumber,
				complementaryActiveness));
	    return result;
	    
	case FineSelectionUnit.Kind.ResolutionG:
	    result.
		addLast(FineSelectionUnitActiveness.
			cluster(FineSelectionUnit.Kind.ResolutionI,
				_numberOfBitsInKindAsNumber,
				complementaryActiveness));
	    return result;
	    
	case FineSelectionUnit.Kind.ResolutionB: 
	    result.
		addLast(FineSelectionUnitActiveness.
			cluster(FineSelectionUnit.Kind.ResolutionB,
				_numberOfBitsInKindAsNumber,
				complementaryActiveness));
	    return result;

	case FineSelectionUnit.Kind.EqLHSForParamodI: // as below
	case FineSelectionUnit.Kind.EqLHSForParamodG: // as below
	case FineSelectionUnit.Kind.EqLHSForParamodB:  
	    result.
		addLast(FineSelectionUnitActiveness.
			cluster(FineSelectionUnit.Kind.RedexForParamod,
				_numberOfBitsInKindAsNumber,
				complementaryActiveness));
	    return result;

	case FineSelectionUnit.Kind.RedexForParamod: 
	    result.
		addLast(FineSelectionUnitActiveness.
			cluster(FineSelectionUnit.Kind.EqLHSForParamodI,
				_numberOfBitsInKindAsNumber,
				complementaryActiveness));
	    result.
		addLast(FineSelectionUnitActiveness.
			cluster(FineSelectionUnit.Kind.EqLHSForParamodG,
				_numberOfBitsInKindAsNumber,
				complementaryActiveness));
	    result.
		addLast(FineSelectionUnitActiveness.
			cluster(FineSelectionUnit.Kind.EqLHSForParamodB,
				_numberOfBitsInKindAsNumber,
				complementaryActiveness));
	    return result;

	} // switch (kind)
	
	assert false;
	return null;

    } // complementaryClusters()






    //                      Data:

    private static final int _numberOfBitsInKindAsNumber = 6; // should be enough

    private int _activeness;
    
    
    
} // class FineSelectionUnitWithActiveness
