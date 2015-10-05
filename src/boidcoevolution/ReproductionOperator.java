package boidcoevolution;

import java.util.List;

import org.jgap.GeneticOperator;
import org.jgap.Population;

public class ReproductionOperator implements GeneticOperator {

	@Override

	// TODO Auto-generated method stub
	/**
	 * The operate method will be invoked on each of the genetic operators
	 * referenced by the current Configuration object during the evolution
	 * phase. Operators are given an opportunity to run in the order that
	 * they are added to the Configuration. Implementations of this method
	 * may reference the population of Chromosomes as it was at the beginning
	 * of the evolutionary phase or the candidate Chromosomes, which are the
	 * results of prior genetic operators. In either case, only Chromosomes
	 * added to the list of candidate chromosomes will be considered for
	 * natural selection. Implementations should never modify the original
	 * population.
	 *
	 * @param a_population the population of chromosomes from the current
	 * evolution prior to exposure to any genetic operators
	 * @param a_candidateChromosomes the pool of chromosomes that are candidates
	 * for the next evolved population. Any chromosomes that are modified by this
	 * genetic operator that should be considered for natural selection should be
	 * added to the candidate chromosomes
	 */
	public void operate( final Population a_population,
			final List a_candidateChromosomes )
	{
		// Just loop over the chromosomes in the population, make a copy of
		// each one, and then add that copy to the candidate chromosomes
		// pool so that it'll be considered for natural selection during the
		// next phase of evolution.
		// -----------------------------------------------------------------
		int len = a_population.size();
		for ( int i = 0; i < len; i++ )
		{
			a_candidateChromosomes.add( a_population.getChromosome(i).clone() );
		}
	}


}


