package boidcoevolution;


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

public class RuleDecodingEvolution extends BulkFitnessFunction {

	//ArrayList<ArrayList<Double>> trainingData = new ArrayList<ArrayList<Double>>();


	public Hashtable<Integer,Integer> statistical_results = new Hashtable<Integer,Integer> ();
	public Hashtable<String,Integer> global_parameters;
	public Flockers theFlock; 

	ArrayList<ArrayList<Double>> Rule_array = new ArrayList<ArrayList<Double>>();
	ArrayList<Integer> num_neighborhood_array = new ArrayList<Integer>();
	ArrayList<Integer> repulsion_array = new ArrayList<Integer>();

	public RuleDecodingEvolution() {

	}

	public RuleDecodingEvolution(Hashtable<String,Integer> parameters) {
		theFlock = new Flockers(System.currentTimeMillis(), parameters);
		global_parameters = parameters;

	}





	private void readRule(Population pop) 
	{

		ArrayList<Double> Rule_j = new ArrayList<Double>();

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
				Double rule_output = temp_output.doubleValue();
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
		statistical_results = theFlock.run(Rule_array, num_neighborhood_array, repulsion_array, global_parameters);

		System.out.println(pop.size());


		for (int i=0;i<theFlock.num_preys;i++) {
			IChromosome chromosome2 = pop.getChromosome(i);
			chromosome2.setFitnessValue(statistical_results.get(i));
			//pop.setChromosome(i, chromosome2);
			//System.out.println(statistical_results.get(i));

		}

	}



}
