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


import java.util.BitSet;

/** Some static methods for dealing with fine selection unit activeness
 *  values.
 *  TODO: optimise cluster() by keeping an explicit mapping 
 *        from numbers to BitSets.
 */
class FineSelectionUnitActiveness {


    /** Converts <code>activeness</code> into a BitSet with the prefix
     *  of <code>prefixSize</code> obtained by converting 
     *  <code>prefixValue</code>.
     */
    public static BitSet cluster(int prefixValue,
				 int prefixSize,
				 int activeness) {
	

	BitSet result = new BitSet(32);
	
	int currentBit = 0;

	// Write the bit representation of prefixValue in the beginning
	// of result:

	while (currentBit < prefixSize)
	{
	    if (prefixValue % 2 != 0)
		result.set(currentBit);

	    prefixValue = prefixValue/2;

	    ++currentBit;
	};
	

	// Write the bit representation of activeness in the end of result: 
	
	while (activeness != 0)
	{
	    if (activeness % 2 != 0)
		result.set(currentBit);

	    activeness = activeness/2;

	    ++currentBit;
	};


	return result;

    } // cluster(int prefixValue,..)


    /** Units with activeness <code>activeness</code> interact
     *  with units of the complementary activeness
     *  to produce inferences.
     */
    public static int complementaryActiveness(int activeness) {

	assert activeness > 0;

	assert 
	    activeness <= 
	    Kernel.current().maximalSelectionUnitActiveness();

	return 
	    (Kernel.current().maximalSelectionUnitActiveness() - 
	     activeness) + 1;

    } // complementaryActiveness(int activeness)


} //  class FineSelectionUnitActiveness