
package boidcoevolution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.TournamentSelector;

import sim.display.Display2D;


public class PredatorRuleEvolution {



	public Display2D display;
	public JFrame displayFrame;



	public static void main(String[] args) {
		// TODO Auto-generated method stub

	
		// used for evolved population
		//create populations of chromosones 
		Population evolved_pop_1 = null;
		Population evolved_pop_2 = null;
		IChromosome mutant = null;
		Integer num_mutant = 1;
		Integer num_re_introduction = -1;



		//Allows user to specify the number of runs 
		String run_num = args[0];
		int index = run_num.indexOf('=');
		run_num = run_num.substring(index+1, run_num.length());		


		//Population size - the number of chromosones in pop
		String ReadPopSize = args[1];
		index = ReadPopSize.indexOf('=');
		ReadPopSize = ReadPopSize.substring(index+1, ReadPopSize.length());		
		Integer PopSize = Integer.parseInt(ReadPopSize);
		System.out.println("************************* PopSize is " + PopSize);				
		
		
		//Arena Size - 
		String ReadArenaSize = args[2];
		index = ReadArenaSize.indexOf('=');
		ReadArenaSize = ReadArenaSize.substring(index+1, ReadArenaSize.length());		
		Integer ArenaSize = Integer.parseInt(ReadArenaSize);
		System.out.println("************************* Arena size is " + ArenaSize);	
		
		
		String dest_dir2 = run_num + "_PopSize";
		dest_dir2 += PopSize;
		dest_dir2 += "_ArenaSize";
		dest_dir2 += ReadArenaSize;
		
		//create a hash table of parameters 
		Hashtable<String,Integer> parameters = new Hashtable<String,Integer>();
		parameters.put("PopSize", PopSize);
		parameters.put("ArenaSize", ArenaSize);

		//create a new file with name dependant on arena and pop size  
		try {			
			File dir1 = new File (".");
			boolean success = (new File(dir1.getCanonicalPath() + "//" + dest_dir2 + "_Predator_Evolution")).mkdir();
			if (success) {
				System.out.println("Directory: " + dir1.getCanonicalPath() + "/" + dest_dir2 + " created");
			}    
		}
		catch (IOException e) {
			System.out.println(e);
		}		

		try{
			// Create file 
			File dir1 = new File (".");
			FileWriter fstream = new FileWriter(dir1.getCanonicalPath() + "//" + dest_dir2 + "_Predator_Evolution//parameters.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(PopSize.toString());
			out.write('\n');
			out.write(ArenaSize.toString());
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}		


	



		
		int numEvolutions = 30000;
		Configuration.reset();
		//add a new default configuration 
		Configuration gaConf = new DefaultConfiguration();
		Genotype genotype = null;
		//3 genes in a chromosone  
		int chromeSize = 3;


		Gene[] sampleGenes = new Gene[ chromeSize ];

		try {

			//gaConf.setPreservFittestIndividual(true);
			gaConf.setKeepPopulationSizeConstant(true);
			//gaConf.removeNaturalSelectors(true);
			//TournamentSelector selector = new TournamentSelector(gaConf, 10, 1);
			//gaConf.addNaturalSelector(selector, true);
			//gaConf.addGeneticOperator( new ReproductionOperator() );

			//gaConf.getGeneticOperators().clear();
			//gaConf.addGeneticOperator(new MutationOperatorNew(gaConf, 500));
				
			//set the fitness function
			gaConf.setBulkFitnessFunction(new RuleDecodingPredatorEvolution(parameters));

			//creating a sample genes for a sample chromosone, each gene is a double 
			for (int i=0; i < chromeSize; i++)
			{
				
				sampleGenes[i] = new DoubleGene(gaConf, 0, 6);  
				
				
			
			}

			//create and set the sample chromosone 
			Chromosome sampleChromosome = new Chromosome(gaConf, sampleGenes );
			
			gaConf.setSampleChromosome( sampleChromosome );
			
			//set the population size 
			gaConf.setPopulationSize(PopSize);
			
			//create a random genotype (a collection of randomised chromosones that follow the
			//format given by the sample chromosone but with randomised values
			genotype = Genotype.randomInitialGenotype(gaConf);


		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
			System.exit( -2);
		}

		int progress = 0;
		int percentEvolution = numEvolutions;


		Population pop2 = genotype.getPopulation();

		//		MarkerDecoding Marker = new MarkerDecoding(Predator_maximum_catch);
		//		Marker.evaluate(pop2);
		//		Hashtable<Integer,Integer> fitness_values = Marker.statistical_results;
		//		for(int j=0; j<pop2.size(); j++) {
		//			IChromosome individual = pop2.getChromosome(j);
		//			individual.setFitnessValueDirectly(fitness_values.get(j));
		//			pop2.setChromosome(j, individual);
		//			System.out.println("Repulsion is .........: " + individual.getGene(1501));	
		//			System.out.println("individual hash code .........: " + individual.hashCode());
		//			System.out.println("Fitness is .........: " + fitness_values.get(j));	
		//			System.out.println("---------------------------------------------");	
		//		}		

		MyGABreeder2 breeder2 = new MyGABreeder2(parameters);
	
		// Initialize the initial population with repulsion distance 32 for each prey

		IChromosome tempChrom = null;
		Chromosome sampleChromosome = null;

/*				for (int j = 0; j<pop2.size(); j++) {
					tempChrom = pop2.getChromosome(j);
					sampleGenes = tempChrom.getGenes();
					sampleGenes[0].setAllele(0);
					sampleGenes[1].setAllele(0);
					sampleGenes[2].setAllele(0);
					try {
						sampleChromosome = new Chromosome(gaConf, sampleGenes ); 
						pop2.setChromosome(j, sampleChromosome);
					}
					catch (InvalidConfigurationException e) {
						e.printStackTrace();
						System.exit( -2);
					}
					
				}*/

		try {
			genotype = new Genotype(gaConf, pop2);
		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
			System.exit( -2);
		}
		
		
		//for each evolution 
		for (int i = 0; i < numEvolutions; i++) {

			Double fitness = 0.0;

			// Print progress.
			// ---------------
			if (percentEvolution > 0 ) {
				progress++;

				//Write the best ANN to a file
				//----------------------


				//				MarkerDecoding Marker = new MarkerDecoding(Predator_maximum_catch);
				//				Marker.evaluate(pop2);
				//				Hashtable<Integer,Integer> fitness_values = Marker.statistical_results;
				//
				
				//for each chromosone from population set chromosone again ? 
				//MAYBE NOT NEEDED 
				for(int j=0; j<pop2.size(); j++) {
					IChromosome individual = pop2.getChromosome(j);
					//individual.setFitnessValueDirectly(fitness_values.get(j));
					pop2.setChromosome(j, individual);
					//System.out.println("Invasion_SwarmVs.java Repulsion is .........: " + individual.getGene(1501));	
					//System.out.println("Invasion_SwarmVs.java Fitness is .........: " + individual.getFitnessValueDirectly());	
					//System.out.println("---------------------------------------------");	
				}	
				//find best solution from chromosone 
				IChromosome bestSolutionSoFar = pop2.determineFittestChromosome();
				fitness = bestSolutionSoFar.getFitnessValueDirectly();
				System.out.println("****************Currently fittest Chromosome has fitness " + fitness);
				

				//every interval of 10 	
				if(i%10==0 ) {


					try {
						File dir1 = new File (".");

						// Write the whole population to a .csv file

						FileWriter writer = new FileWriter(dir1.getCanonicalPath() + "//" + dest_dir2  +"_Predator_Evolution//PopulationRecords" + i + ".csv", true);


						for(int aa = 0; aa<PopSize; aa++) {
							IChromosome evolving_chromosome = pop2.getChromosome(aa);
							Double chromosome_fitness = evolving_chromosome.getFitnessValue();
							writer.append(chromosome_fitness.toString());
							writer.append(',');
							writer.append("3");
							writer.append(',');
							writer.append("5");
							writer.append(',');
							for(int bb=0; bb<evolving_chromosome.size(); bb++) {
								Double allele = (Double) evolving_chromosome.getGene(bb).getAllele();	
								//System.out.println("allele is " + allele);
								writer.append(allele.toString());
								writer.append(',');
							}
							writer.append('\n');

						}
						writer.close();


					}

					catch (IOException e) {
						System.out.println(e);
					}
				}



			}

			pop2 = breeder2.evolve(pop2, gaConf);
		}



	}


}
