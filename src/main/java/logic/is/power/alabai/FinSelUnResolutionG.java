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
 * Represents selection units carrying a literal intended
 * for resolution inferences in which the unifier is 
 * <a href="{@docRoot}/../glossary.html#generalising_unifier">generalising</a>
 * wrt the literal.
 * Note that selection units of this type can only interact
 * with {@link alabai_je.FinSelUnResolutionI} units to produce resolution
 * inferences. 
 */
class FinSelUnResolutionG extends FinSelUnResolution {


    public FinSelUnResolutionG(Clause clause,Literal literal) {
	super(FineSelectionUnit.Kind.ResolutionG,clause,literal);
    }

}; // class FinSelUnResolutionG






