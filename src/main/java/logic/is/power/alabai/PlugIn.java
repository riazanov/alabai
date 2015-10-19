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



/** Specification for programmable extensions of Alabai;
 *  the user can provide custom classes and methods by 
 *  implementing this interface; such classes are compiled and
 *  the corresponding class files are given to the command line
 *  shell as parameters.
 */
public interface PlugIn {

    /** Finds a user-defined {@link alabai_je#ScalarClauseFeature} object 
     *  associated with the specified name.
     *  @return null if <code>this</code> does not contain such object.
     */
    public ScalarClauseFeature getScalarClauseFeature(String name);


    /** Finds a user-defined {@link alabai_je#TermFeature} object 
     *  associated with the specified name.
     *  @return null if <code>this</code> does not contain such object.
     */
    public ScalarTermFeature getScalarTermFeature(String name);


} // interface PlugIn