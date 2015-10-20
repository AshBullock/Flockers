package boidcoevolution;


import java.util.Hashtable;

import org.jgap.BulkFitnessFunction;
import org.jgap.Population;
import org.jgap.BulkFitnessFunction;
import org.jgap.FitnessFunction;
import org.jgap.IChromosome;
import org.jgap.Population;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;


public class PredatorFitnessFunction extends BulkFitnessFunction {

	public Hashtable<Integer,Integer> statistical_results = new Hashtable<Integer,Integer> ();
	public Hashtable<String,Integer> global_parameters;
	public Flockers theFlock; 
	
	//which rule is being used 
	ArrayList<ArrayList<Integer>> Flock_Rule_array = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<Integer>> Predator_Rule_array = new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> num_neighborhood_array = new ArrayList<Integer>();
	ArrayList<Integer> repulsion_array = new ArrayList<Integer>();
	


	
	public PredatorFitnessFunction(Hashtable<String,Integer> parameters) {
	//set chromosone parameters and a new flock 
	theFlock = new Flockers(System.currentTimeMillis(), parameters);
	global_parameters = parameters;

}


	
	
	//read the rules that each chromosone has  
	private void readRule(Population predPop) 
	{
	
		ArrayList<Integer> Rule_j = new ArrayList<Integer>();
	
		for (int i=0;i<predPop.size();i++) {
			IChromosome a_subject = predPop.getChromosome(i);
			int numGenes = a_subject.size();
			//System.out.println("Chromosome hash code is: " + a_subject.hashCode());
	
			// Last allele is the number of neighbourhood
			Integer num_neighborhood = 3;
	
			Integer repulsion_dis = 5;
			
		
		
			//System.out.println("Number of genes is..........." + numGenes);
			//System.out.println("a_subject size ......." + a_subject.size());
			for (int j=0; j<numGenes; j++) {
				//System.out.println("j = " + j);
				Double temp_output = (Double) a_subject.getGene(j).getAllele();
				Integer rule_output = temp_output.intValue();
				Rule_j.add(rule_output);
			}
	
			Predator_Rule_array.add(Rule_j);
			//System.out.println("Predator rule array = ..... " + Rule_j.size());
		}
	
	}
	
	public void evaluate(Population pop) {
	
	
		readRule(pop);
	
		
		/**
		 *Selects the prey population to use here.
		 */
		PopulationFileIO PopLoader = new PopulationFileIO(); 
		//System.out.println("PopSIZE: " + global_parameters.get("PopSize"));
		Population flockerPop = PopLoader.LoadPopulation("\\DynamicRule", global_parameters.get("PopSize"));

		PopLoader.readRule(flockerPop);
		Flock_Rule_array =  PopLoader.Rule_array;
		num_neighborhood_array = PopLoader.num_neighborhood_array;
		repulsion_array = PopLoader.repulsion_array;
		
		//MersenneTwisterFast randomnum = new MersenneTwisterFast();
		//theFlock.run(randomnum.nextInt());
		
		//run the flock with the given rules and retrieve statistical results
		//need to change for predator
		statistical_results = theFlock.runPredatorEvolution(Flock_Rule_array, Predator_Rule_array, num_neighborhood_array, repulsion_array, global_parameters);
	
		//System.out.println(pop.size());
	
	
		for (int i=0;i<pop.size();i++) {
			IChromosome chromosome2 = pop.getChromosome(i);
			chromosome2.setFitnessValue(statistical_results.get(i));
			//pop.setChromosome(i, chromosome2);
			//System.out.println(statistical_results.get(i));
	
		}
	
	}
	
	

}

