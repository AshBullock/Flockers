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


public class RuleDecodingPredatorEvolution extends BulkFitnessFunction {

	public Hashtable<Integer,Integer> statistical_results = new Hashtable<Integer,Integer> ();
	public Hashtable<String,Integer> global_parameters;
	public Flockers theFlock; 
	
	//which rule is being used 
	ArrayList<ArrayList<Integer>> Rule_array = new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> num_neighborhood_array = new ArrayList<Integer>();
	ArrayList<Integer> repulsion_array = new ArrayList<Integer>();


	
	public RuleDecodingPredatorEvolution(Hashtable<String,Integer> parameters) {
	//set chromosone parameters and a new flock 
	theFlock = new Flockers(System.currentTimeMillis(), parameters);
	global_parameters = parameters;

}


	
	
	//read the rules that each chromosone has  
	private void readRule(Population pop) 
	{
	
		ArrayList<Integer> Rule_j = new ArrayList<Integer>();
	
		for (int i=0;i<pop.size();i++) {
			IChromosome a_subject = pop.getChromosome(i);
			//System.out.println("Chromosome hash code is: " + a_subject.hashCode());
	
			// Last allele is the number of neighbourhood
			Integer num_neighborhood = 3;
	
			Integer repulsion_dis = 5;
			
			
			num_neighborhood_array.add(num_neighborhood);
			repulsion_array.add(repulsion_dis);
	
			for (int j=0; j<num_neighborhood; j++) {
	
				Double temp_output = (Double) a_subject.getGene(j).getAllele();
				Integer rule_output = temp_output.intValue();
				Rule_j.add(rule_output);
			}
	
			Rule_array.add(Rule_j);
		}
	
	}
	
	public void evaluate(Population pop) {
	
	
		readRule(pop);
	
		//MersenneTwisterFast randomnum = new MersenneTwisterFast();
		//theFlock.run(randomnum.nextInt());
		System.out.println("theFlock.getWidth() in Marker deconding is: " + theFlock.getWidth());
		//run the flock with the given rules and retrieve statistical results
		//need to change for predator
		statistical_results = theFlock.runPredatorEvolution(Rule_array, num_neighborhood_array, repulsion_array, global_parameters);
	
		System.out.println(pop.size());
	
	
		for (int i=0;i<theFlock.num_predator;i++) {
			IChromosome chromosome2 = pop.getChromosome(i);
			chromosome2.setFitnessValue(statistical_results.get(i));
			//pop.setChromosome(i, chromosome2);
			//System.out.println(statistical_results.get(i));
	
		}
	
	}
	
	

}

