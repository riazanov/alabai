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

/** int-valued feature function objects on collections of literals, to be used
 *  in feature vector-based indexes.
 */
public interface ScalarClauseFeature extends ClauseFeature {

    public int evaluate(Collection<Literal> cl);

    public int evaluateTemp(Collection<TmpLiteral> cl);    

} // interface ScalarClauseFeature