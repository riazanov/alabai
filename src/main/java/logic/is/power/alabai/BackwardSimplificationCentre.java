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

  /**
   * Hub for the main backward simplification operations, 
   * including backward subsumption, backward subsumption resolution,
   * and backward demodulation.
   */
final class BackwardSimplificationCentre implements SimpleReceiver<Clause> {

      /** @param clauseDiscarder where to send the discarded clauses 
       *  @param simplifiedClauseAssembler where the simplified versions 
       *         clauses may be assembled
       */
      public 
	  BackwardSimplificationCentre(SimpleReceiver<Clause> clauseDiscarder,
				       NewClauseAssembler simplifiedClauseAssembler)
	  {
	      _printDiscarded = false;
	      _clauseDiscarder = clauseDiscarder;
	      _subsumptionIndex = new BackwardSubsumptionIndex();
	      _subsumptionQueryQueue = new LinkedList<Clause>();
	      _demodulation = 
		  new BackwardDemodulation(clauseDiscarder,
					   simplifiedClauseAssembler); 
	      _demodulationQueryQueue = new LinkedList<Clause>();
	  }

      /** Switches backward subsumption. */
      public void setBackwardSubsumption(boolean fl) {
	  _subsumption = fl;
      }

      /** Switches backward demodulation. */
      public void setBackwardDemodulation(boolean fl) {
	  _demodulationFlag = fl;
      }

      public
	  void 
	  setClauseFeatureFunctionVector(ClauseFeatureVector vector)
      {
	  _subsumptionIndex.setFeatureFunctionVector(vector);
	  
	  _subsumptionRetrieval = _subsumptionIndex.new Retrieval(); 
	  // This is not in the constructor because a retrieval object
	  // can only be created after the feature function vector is set.
      }


      public 
	  void setTermFeatureFunctionVector(TermFeatureVector vector)
      {
	  _demodulation.setTermFeatureFunctionVector(vector);
      }
      
      public void setPrintDiscarded(boolean fl) {
	  _printDiscarded = fl; 
	  _demodulation.setPrintDiscarded(fl);
      }


      /** Puts the clause in the queues for integration in the databases
       *  as well as in the queues for potential simplifiers;
       *  whenever immediate intergration is possible, it is done. 
       *  @return true (always)
       */
      public boolean receive(Clause cl) {
	  
	  if (_subsumption)
	      {
		  assert !_subsumptionIndex.isLocked();
		  _subsumptionIndex.insert(cl);
		  _subsumptionQueryQueue.addLast(cl);
	      };

	  if (_demodulationFlag)
	      {
		  _demodulation.insert(cl);
		  if (canBeUsedForDemodulation(cl))
		      _demodulationQueryQueue.addLast(cl);
	      };

	  return true;
      }

      
      /** Removes the clause from all underlying indexes and sets,
       *  if it is present there.
       */
      public boolean remove(Clause cl) {
	  boolean result = false;
	  if (_subsumption && _subsumptionIndex.erase(cl))
	      result = true;

	  if (_demodulationFlag && _demodulation.remove(cl))
	      result = true;
	  
	  return result;
      }


      /** Tries to perform some work; it can be integration of clauses
       *  in databases, or some part of a simplification task.
       *  Occasionally checks <code>SwitcheableAbortFlag.current()</code>.
       *  @return false if absolutely nothing can be done
       */
      public boolean doSomething() 
	  throws SwitcheableAbortFlag.Exception {

	  if (_subsumption)
	      {
		  // Simple temporary implementation:
		  if (!_subsumptionQueryQueue.isEmpty())
		      {
			  Clause query = 
			      _subsumptionQueryQueue.removeFirst();

			  _subsumptionRetrieval.reset(query);
			  
			  while (_subsumptionRetrieval.hasNext())
			      {
				  Clause subsumedClause =
				      _subsumptionRetrieval.next();
				  
				  ++(Statistics.current().
				     backwardSimplifyingInferences.
				     subsumption);
				  
				  if (_printDiscarded)
				      System.out.println("Discard/Kept/Subs: " +
							 subsumedClause);


				  ++(Statistics.current().
				     keptClauses.discardedBySubsumption);
				  
				  _clauseDiscarder.receive(subsumedClause);

				  SwitcheableAbortFlag.current().check();

			      }; // while (_subsumptionRetrieval.hasNext())

			  _subsumptionRetrieval.clear();

			  return true;

		      }; // if (!_subsumptionQueryQueue.isEmpty())
	      }; // if (_subsumption)



	  if (_demodulationFlag)
	      {		  
		  if (!_demodulationQueryQueue.isEmpty())
		      {
			  Clause query = 
			      _demodulationQueryQueue.removeFirst();
			  
			  if (_demodulation.simplifyBy(query))
			      return true;

		      }; // if (!_demodulationQueryQueue.isEmpty())

	      }; // if (_demodulationFlag)

	  return false; 

      } // doSomething()

      

      
      /** Checks if the clause is an orientable unit equality. */
      private boolean canBeUsedForDemodulation(Clause cl) {

	  if (cl.literals().size() != 1) return false;

	  Literal lit = cl.literals().iterator().next();

	  if (!lit.isEquality() || lit.isNegative()) return false;
	      
	  Term arg1 = lit.atom().firstArg();
	  Term arg2 = lit.atom().secondArg();
	
	  return
	      ReductionOrdering.
	      current().
	      canBeGreaterModuloSubst(arg1,arg2) ||
	      ReductionOrdering.
	      current().
	      canBeGreaterModuloSubst(arg2,arg1);


      } // canBeUsedForDemodulation(Clause cl)




    //                Data:


    private boolean _subsumption;

    private boolean _demodulationFlag;

    private boolean _printDiscarded;

    private SimpleReceiver<Clause> _clauseDiscarder;

    private final LinkedList<Clause> _subsumptionQueryQueue;

    private final BackwardSubsumptionIndex _subsumptionIndex;

    private BackwardSubsumptionIndex.Retrieval _subsumptionRetrieval;

    private final BackwardDemodulation _demodulation;

    private final LinkedList<Clause> _demodulationQueryQueue;

  }; // class BackwardSimplificationCentre
