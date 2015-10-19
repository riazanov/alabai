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
   * Buffer storage of clauses waiting to be selected for finer
   * selection units identification, and the selection mechanism
   * itself.
   */
  class ClauseSelection 
      implements SimpleReceiver<Clause>, SimpleSender<Clause> {


      public ClauseSelection() {
	  _database = new LinkedList<Clause>(); 
      }


      /** Accomodates the clause in the internal database
       *  to make it available for selection.
       *  @return always true
       */
      public boolean receive(Clause cl) {
	  _database.addLast(cl);
	  return true;
      }

    /** Selects a clause (unless the database is empty) and deletes it
     *  from the internal database.
     *  @return false if the database is empty and, therefore, no clause
     *          can be selected
     */
      public boolean send(Ref<Clause> cl) {
	  if (_database.isEmpty()) return false;
	  cl.content = _database.removeFirst();
	  return true;
      }


      /** Removes the clause from the buffer, if it is present. */
      public boolean erase(Clause cl) {
	  // VERY INEFFICIENT!
	  return _database.remove(cl);
      }


      //                   Data:

      /** Temporary representation of the database. */
      private LinkedList<Clause> _database; 

  }; // class ClauseSelection
