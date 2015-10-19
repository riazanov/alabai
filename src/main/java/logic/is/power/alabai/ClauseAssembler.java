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
 * Implements assembling of persistent clauses in the {@link alabai_je.Clause}
 * representation.
 * Partially inspired by VK::ClauseAssebler in 
 *  <a href="{@docRoot}/../references.html#vampire_kernel_6_0">[Vampire kernel v6.0 sources]</a>.
 */
public class ClauseAssembler {


    /**
     *  @param inputSender where the input clauses are read from.
     *  @param outputReceiver where the assembled clauses are passed to.
     *  @param literalOrder to order literals; may be null
     */
    public ClauseAssembler(SimpleSender<NewClause> inputSender,
			   SimpleReceiver<Clause> outputReceiver,
			   Comparator<Literal> literalOrder) {
	_inputSender = inputSender;
	_outputReceiver = outputReceiver;
	_literalOrder = literalOrder;
	_printAssembled = false;
    }
      
      
    /** Switches printing of assembled clauses. */
    public void setPrintAssembled(boolean fl) {
	_printAssembled = fl;
    }
    
    /** Tries to fully process one clause from the input sender.
     *  @return false if no clauses are available for processing.
     */
    public boolean processNext() {

	Ref<NewClause> inputClause = new Ref<NewClause>();
      

	if (!_inputSender.send(inputClause))
	    return false;
  

	Clause assembledClause = new Clause(inputClause.content.number(),
					    inputClause.content.parents());

  
	//    Convert literals from alabai_je.TmpLiteral to alabai_je.Literal:

 
	if (_literalOrder != null)
	    {
		TreeSet<Literal> sortedLiterals =
		    new TreeSet<Literal>(_literalOrder);
		
		
		for (TmpLiteral lit : inputClause.content.literals())
		    {
			
			// Note that sharing is taking place here:
			
			Formula litBody = 
			    TermFactory.current().createSharedFormula(lit.body());
			
			sortedLiterals.add(new Literal(litBody));
		    };

		for (Literal persistentLit : sortedLiterals)	
		    assembledClause.addLiteral(persistentLit); 
		
	    }
	else
	    {
		for (TmpLiteral lit : inputClause.content.literals())
		    {
			
			// Note that sharing is taking place here:
			
			Formula litBody = 
			    TermFactory.current().createSharedFormula(lit.body());
			
			assembledClause.addLiteral(new Literal(litBody));
		    };
	    }; // if (_literalOrder != null)

  
	//  Inherit properties:

	assembledClause.assignFlags(inputClause.content.flags());
	assembledClause.assignInferences(inputClause.content.inferences());


	if (_printAssembled) 
	    System.out.println("Asm: " + assembledClause);
  
	_outputReceiver.receive(assembledClause);
  
	return true;

    } // processNext()
      

    //                 Data:
    
    
    /** Where the input clauses are read from. */
    private final SimpleSender<NewClause> _inputSender;

    /** Where the assembled clauses are passed to. */
    private final SimpleReceiver<Clause> _outputReceiver;

    private final Comparator<Literal> _literalOrder;

    private boolean _printAssembled;

}; // class ClauseAssembler
