/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licensing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package boidcoevolution;

import java.util.Hashtable;

import org.jgap.*;
import org.jgap.audit.*;
import org.jgap.event.*;

public class MyGABreeder2
extends BreederBase {
	/** String containing the CVS revision. Read out via reflection!*/
	private final static String CVS_REVISION = "$Revision: 1.17 $";

	private transient Configuration m_lastConf;

	private transient Population m_lastPop;

	public RuleDecodingEvolution Marker;

	public MyGABreeder2(Hashtable<String,Integer> parameters) {
		super();
		Marker = new RuleDecodingEvolution(parameters);
	}

	/**
	 * Evolves the population of chromosomes within a genotype. This will
	 * execute all of the genetic operators added to the present active
	 * configuration and then invoke the natural selector to choose which
	 * chromosomes will be included in the next generation population.
	 *
	 * @param a_pop the population to evolve
	 * @param a_conf the configuration to use for evolution
	 *
	 * @return evolved population
	 *
	 * @author Klaus Meffert
	 * @since 3.2
	 */
	public Population evolve(Population a_pop, Configuration a_conf) {
		Population pop = a_pop;
		int originalPopSize = a_conf.getPopulationSize();

		IChromosome fittest = null;
		// If first generation: Set age to one to allow genetic operations,
		// see CrossoverOperator for an illustration.
		// ----------------------------------------------------------------
		if (a_conf.getGenerationNr() == 0) {
			int size = pop.size();
			for (int i = 0; i < size; i++) {
				IChromosome chrom = pop.getChromosome(i);
				chrom.increaseAge();
			}
		}
		else {
			// Select fittest chromosome in case it should be preserved and we are
			// not in the very first generation.
			// -------------------------------------------------------------------
			if (a_conf.isPreserveFittestIndividual()) {
				/**@todo utilize jobs. In pop do also utilize jobs, especially for fitness
				 * computation*/
				fittest = pop.determineFittestChromosome(0, pop.size() - 1);
			}
		}
		if (a_conf.getGenerationNr() > 0) {
			// Adjust population size to configured size (if wanted).
			// Theoretically, this should be done at the end of this method.
			// But for optimization issues it is not. If it is the last call to
			// evolve() then the resulting population possibly contains more
			// chromosomes than the wanted number. But this is no bad thing as
			// more alternatives mean better chances having a fit candidate.
			// If it is not the last call to evolve() then the next call will
			// ensure the correct population size by calling keepPopSizeConstant.
			// ------------------------------------------------------------------
			keepPopSizeConstant(pop, a_conf);
		}
		// Ensure fitness value of all chromosomes is udpated.
		// ---------------------------------------------------

		// Apply certain NaturalSelectors before GeneticOperators will be executed.
		// ------------------------------------------------------------------------
		pop = applyNaturalSelectors(a_conf, pop, true);
		// Execute all of the Genetic Operators.
		// -------------------------------------
		applyGeneticOperators(a_conf, pop);
		// Reset fitness value of genetically operated chromosomes.
		// Normally, this should not be necessary as the Chromosome class
		// initializes each newly created chromosome with
		// FitnessFunction.NO_FITNESS_VALUE. But who knows which Chromosome
		// implementation is used...
		// ----------------------------------------------------------------
		int currentPopSize = pop.size();
		for (int i = originalPopSize; i < currentPopSize; i++) {
			IChromosome chrom = pop.getChromosome(i);
			chrom.setFitnessValueDirectly(FitnessFunction.NO_FITNESS_VALUE);
			// Mark chromosome as new-born.
			// ----------------------------
			chrom.resetAge();
			// Mark chromosome as being operated on.
			// -------------------------------------
			chrom.increaseOperatedOn();
		}
		// Increase age of all chromosomes which are not modified by genetic
		// operations.
		// -----------------------------------------------------------------
		int size = Math.min(originalPopSize, currentPopSize);
		for (int i = 0; i < size; i++) {
			IChromosome chrom = pop.getChromosome(i);
			chrom.increaseAge();
			// Mark chromosome as not being operated on.
			// -----------------------------------------
			chrom.resetOperatedOn();
		}
		// If a bulk fitness function has been provided, call it.
		// ------------------------------------------------------
		//BulkFitnessFunction bulkFunction = a_conf.getBulkFitnessFunction();



		// Apply certain NaturalSelectors after GeneticOperators have been applied.
		// ------------------------------------------------------------------------
		pop = applyNaturalSelectors(a_conf, pop, false);
		
	

		// Inject mutant
//		for(int j=0; j<num_injection; j++) {
//			pop.setChromosome(j, mutant);
//		}		

		//    if (bulkFunction != null) {
		//
		//        /**@todo utilize jobs: bulk fitness function is not so important for a
		//         * prototype! */
		//        bulkFunction.evaluate(pop);      
		//      }
		// Assign fitess by simulation
		// Ensure fitness value of all chromosomes is udpated.
		// ---------------------------------------------------
		
		Marker.evaluate(pop);
		Hashtable<Integer,Integer> fitness_values = Marker.statistical_results;
		for(int j=0; j<pop.size(); j++) {
			IChromosome individual = pop.getChromosome(j);
			individual.setFitnessValueDirectly(fitness_values.get(j));
			pop.setChromosome(j, individual);
			//System.out.println("Repulsion is .........: " + individual.getGene(1501));	
			//System.out.println("individual hash code .........: " + individual.hashCode());
			//System.out.println("Fitness is .........: " + fitness_values.get(j));	
			//System.out.println("---------------------------------------------");	
		}	    

		updateChromosomes(pop, a_conf);    

		// Fill up population randomly if size dropped below specified percentage
		// of original size.
		// ----------------------------------------------------------------------
		if (a_conf.getMinimumPopSizePercent() > 0) {
			int sizeWanted = a_conf.getPopulationSize();
			int popSize;
			int minSize = (int) Math.round(sizeWanted *
					(double) a_conf.getMinimumPopSizePercent()
					/ 100);
			popSize = pop.size();
			if (popSize < minSize) {
				IChromosome newChrom;
				IChromosome sampleChrom = a_conf.getSampleChromosome();
				Class sampleChromClass = sampleChrom.getClass();
				IInitializer chromIniter = a_conf.getJGAPFactory().
				getInitializerFor(sampleChrom, sampleChromClass);
				while (pop.size() < minSize) {
					try {
						/**@todo utilize jobs as initialization may be time-consuming as
						 * invalid combinations may have to be filtered out*/
						newChrom = (IChromosome) chromIniter.perform(sampleChrom,
								sampleChromClass, null);

						pop.addChromosome(newChrom);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		}
		IChromosome newFittest = reAddFittest(pop, fittest);



		// Increase number of generations.
		// -------------------------------
		a_conf.incrementGenerationNr();
		// Fire an event to indicate we've performed an evolution.
		// -------------------------------------------------------
		m_lastPop = pop;
		m_lastConf = a_conf;
		a_conf.getEventManager().fireGeneticEvent(
				new GeneticEvent(GeneticEvent.GENOTYPE_EVOLVED_EVENT, this));
		return pop;
	}

	public Configuration getLastConfiguration() {
		return m_lastConf;
	}

	public Population getLastPopulation() {
		return m_lastPop;
	}

	/**
	 * @return deep clone of this instance
	 *
	 * @author Klaus Meffert
	 * @since 3.2
	 */
	public Object clone() {
		return null;
	}

	/**
	 * Cares that population size is kept constant and does not exceed the desired
	 * size.
	 *
	 * @param a_pop Population
	 * @param a_conf Configuration
	 */
	protected void keepPopSizeConstant(Population a_pop, Configuration a_conf) {
		if (a_conf.isKeepPopulationSizeConstant()) {
			try {
				a_pop.keepPopSizeConstant();
			} catch (InvalidConfigurationException iex) {
				throw new RuntimeException(iex);
			}
		}
	}

	protected IChromosome reAddFittest(Population a_pop, IChromosome a_fittest) {
		// Determine if all-time fittest chromosome is in the population.
		// --------------------------------------------------------------
		if (a_fittest != null && !a_pop.contains(a_fittest)) {
			// Re-add fittest chromosome to current population.
			// ------------------------------------------------
			a_pop.addChromosome(a_fittest);
			return a_fittest;
		}
		return null;
	}

	protected void updateChromosomes(Population a_pop, Configuration a_conf) {
		int currentPopSize = a_pop.size();
		// Ensure all chromosomes are updated.
		// -----------------------------------
		BulkFitnessFunction bulkFunction = a_conf.getBulkFitnessFunction();
		boolean bulkFitFunc = (bulkFunction != null);
		if (!bulkFitFunc) {
			for (int i = 0; i < currentPopSize; i++) {
				IChromosome chrom = a_pop.getChromosome(i);
				chrom.getFitnessValue();
			}
		}
	}
}
