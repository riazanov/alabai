
package logic.is.power.alabai.sample_plugins;

import java.util.*;

import logic.is.power.alabai.*;

import logic.is.power.logic_warehouse.*;



/** Example of an Alabai plug-in. */
public class SamplePlugIn1 implements PlugIn {
    
    public SamplePlugIn1() {
    }
    
    public ScalarClauseFeature getScalarClauseFeature(String name) {
	if (name.equals("sampleClauseFeature1"))
	    return _sampleClauseFeature1;
	return null;
    }

    public ScalarTermFeature getScalarTermFeature(String name) {
	if (name.equals("sampleTermFeature1"))
	    return _sampleTermFeature1;
	return null;
    }

    /** Counts the number of binary functions in a clause. */
    private static class SampleClauseFeature1 
	implements ScalarClauseFeature {
	
	
	public int evaluate(Collection<logic.is.power.alabai.Literal> cl) {
	    int result = 0;
	    for (logic.is.power.alabai.Literal lit : cl)
		{
		    Term.LeanIterator iter = new Term.LeanIterator(lit.atom());
		    while (iter.hasNext())
			{
			    Term subterm = iter.next();
			    if (subterm.kind() == Term.Kind.CompoundTerm &&
				((CompoundTerm)subterm).function().arity() == 2)
				++result;
			};
		};
	    return result;
	}
	
	public int evaluateTemp(Collection<TmpLiteral> cl) {


	    int result = 0;
	    for (logic.is.power.alabai.TmpLiteral lit : cl)
		{
		    for (Flatterm subterm = lit.atom(); 
			 subterm != lit.atom().after();
			 subterm = subterm.nextCell())
			if (subterm.isCompound() &&
			    subterm.function().arity() == 2)
			    ++result;
		};
	    return result;
	}
	

    } // static class SampleClauseFeature1 




    /** Counts the number of binary functions in a term. */
    private static class SampleTermFeature1 
	implements ScalarTermFeature {
	
	
	public int evaluate(Term term) {
	    int result = 0;
	    Term.LeanIterator iter = new Term.LeanIterator(term);
	    while (iter.hasNext())
		{
		    Term subterm = iter.next();
		    if (subterm.kind() == Term.Kind.CompoundTerm &&
			((CompoundTerm)subterm).function().arity() == 2)
			++result;
		};
	    return result;
	}
	
	public int evaluate(Flatterm term) {
	    int result = 0;
	    for (Flatterm subterm = term; 
		 subterm != term.after();
		 subterm = subterm.nextCell())
		if (subterm.isCompound() &&
		    subterm.function().arity() == 2)
		    ++result;
	    return result;
	}
	

    } // static class SampleTermFeature1 


    
    private static SampleClauseFeature1 _sampleClauseFeature1 =
	new SampleClauseFeature1();

    private static SampleTermFeature1 _sampleTermFeature1 =
	new SampleTermFeature1();

} // class SamplePlugIn1