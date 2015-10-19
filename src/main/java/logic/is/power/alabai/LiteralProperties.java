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
 * Encapsulates various properties of literals (as parts of clauses).
 */
public class LiteralProperties {

    
    /** Values of selection status of literals. */
    public static class SelectionStatus {
      
      /** Default value, indicates that the literal has not been
       *  through literal selection process yet.
       */
	public static final int UnknownSelectionStatus = 0;
      
      /** Indicates that the literal must be selected;
       *  roughly, inferences with this literal are generally
       *  nonredundant.
       */
	public static final int Selected = 1;
      
      /** Indicates that the literal can be unselected without harm,
       *  but can also be selected if this is desirable;
       *  roughly, inferences with this literal are redundant but 
       *  not necessarily bad.
       */
	public static final int PossiblyUnselected = 2;

      
      /** Indicates that the literal cannot be selected;
       *  roughly, inferences with this literal are both redundant
       *  and considered bad.
       */
	public static final int Unselected = 3;
      
    }; // class SelectionStatus

    /** <b>post:<b> as after <code>reset()</code>. */
    public LiteralProperties() { reset(); }

    /** Resets all properties to their default values. */
    public void reset() 
    {
      _selectionStatus = SelectionStatus.UnknownSelectionStatus;
    }

    public void setSelectionStatus(int s) {
	_selectionStatus = s;
    }

    public int selectionStatus() { 
	return _selectionStatus;
    }

    

    //                 Data:

    public int _selectionStatus;


}; // class LiteralProperties
