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
 * Common base for all classes representing different kinds of 
 * fine selection units.
 */
abstract class FineSelectionUnit {
      
      /** Tag values for different derived classes. */
      public static class Kind {

	  /** See {@link alabai_je.FinSelUnResolutionI}. */
	  public static final int ResolutionI = 0; 

	  /** See {@link alabai_je.FinSelUnResolutionG}. */
	  public static final int ResolutionG = 1;

	  /** See {@link alabai_je.FinSelUnResolutionB}. */
	  public static final int ResolutionB = 2;
	  
	  /** See {@link alabai_je.FinSelUnEqLHSForParamodI}. */
	  public static final int EqLHSForParamodI = 3;

	  /** See {@link alabai_je.FinSelUnEqLHSForParamodG}. */
	  public static final int EqLHSForParamodG = 4;

	  /** See {@link alabai_je.FinSelUnEqLHSForParamodB}. */
	  public static final int EqLHSForParamodB = 5;
	  
	  /** See {@link alabai_je.FinSelUnRedexForParamod}. */
	  public static final int RedexForParamod = 6;

	  /** See {@link alabai_je.FinSelUnEqualityFactoring}. */
	  public static final int EqualityFactoring = 7;

	  /** See {@link alabai_je.FinSelUnEqualityResolution}. */
	  public static final int EqualityResolution = 8;

      }; // class Kind


       
      /** <b>post:</b> <code>next()</code> is undefined */
    public FineSelectionUnit(int kind,Clause clause) {
	_kind = kind;
	_number = Kernel.current().generateFreshNumericId();
	_clause = clause;
	_clause.registerSelectionUnit(this);
    }

    public final int kind() { return _kind; }

    /** Deprecated: numeric id unique within the host 
     *  {@link alabai_je.Kernel} session; may disappear in a future version. 
     */
    public final long number() { return _number; }

      public final Clause clause() { return _clause; }
      
    /** Indicates that the unit should not be used for inferences. */
    public final boolean isDiscarded() {
	return _clause.isDiscarded();
    }


      public final int penalty() { return _penalty; }

    public final void setPenalty(int p) { _penalty = p; }

      /** <b>post</b> next() == su */
      public final void setNext(FineSelectionUnit su) { _next = su; }


    /** Next selection unit in the list of units for the same clause. */
      public final FineSelectionUnit next() { return _next; }


      /** Short printable name for the kind value. */
    public static String spell(int kind) {
	
	switch (kind)
	    {
	    case Kind.ResolutionI: return "ResI";
	    case Kind.ResolutionG: return "ResG";
	    case Kind.ResolutionB: return "ResB";
	    case Kind.EqLHSForParamodI: return "EqLHSFParI";
	    case Kind.EqLHSForParamodG: return "EqLHSFParG";
	    case Kind.EqLHSForParamodB: return "EqLHSFParB";
	    case Kind.RedexForParamod: return "RedFPar";
	    case Kind.EqualityFactoring: return "EqFact";
	    case Kind.EqualityResolution: return "EqRes";
	  
	    default:
		assert false;
		return null;
	    } // switch (kind)

    } // spell(Kind kind)

    public String toString() {
	
	switch (_kind)
	    {
	    case Kind.ResolutionI:
		return ((FinSelUnResolutionI)this).toString();
	    case Kind.ResolutionG:
		return ((FinSelUnResolutionG)this).toString();
	    case Kind.ResolutionB:
		return ((FinSelUnResolutionB)this).toString();
	    case Kind.EqLHSForParamodI:
		return ((FinSelUnEqLHSForParamodI)this).toString();
	    case Kind.EqLHSForParamodG:
		return ((FinSelUnEqLHSForParamodG)this).toString();
	    case Kind.EqLHSForParamodB:
		return ((FinSelUnEqLHSForParamodB)this).toString();
	    case Kind.RedexForParamod:
		return ((FinSelUnRedexForParamod)this).toString();
	    case Kind.EqualityFactoring:
		return ((FinSelUnEqualityFactoring)this).toString();
	    case Kind.EqualityResolution:
		return ((FinSelUnEqualityResolution)this).toString();
	  
	    default: 
		assert false;
		return null;
	
	    } // switch (_kind)
    } // toString()


    //                      Data:
      
      private int _kind;

    /** Numeric id unique within the host {@link alabai_je.Kernel} session. */
    private long _number;

      private Clause _clause;

      /** "Weight" of the unit, which is intuitively the inverse
       *  of the unit's heuristic quality; the higher is the penalty 
       *  value, the smaller is the potential of the unit for promotion.
       */
      private int _penalty;

      /** To arrange selection units for a particular clause as a list. */
      private FineSelectionUnit _next;

  }; // class FineSelectionUnit

