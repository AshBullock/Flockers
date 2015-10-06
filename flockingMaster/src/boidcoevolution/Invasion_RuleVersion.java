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
import org.jgap.impl.IntegerGene;
import org.jgap.impl.TournamentSelector;

import sim.display.Display2D;


public class Invasion_RuleVersion {
	public Display2D display;
	public JFrame displayFrame;



	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// used for evolved population 
		Population evolved_pop_1 = null;
		Population evolved_pop_2 = null;
		IChromosome mutant = null;
		//Integer num_mutant = 1;
		Integer num_re_introduction = -1;




		String run_num = args[0];
		int index = run_num.indexOf('=');
		run_num = run_num.substring(index+1, run_num.length());		



		String ReadPopSize = args[1];
		index = ReadPopSize.indexOf('=');
		ReadPopSize = ReadPopSize.substring(index+1, ReadPopSize.length());		
		Integer PopSize = Integer.parseInt(ReadPopSize);
		System.out.println("************************* PopSize is " + PopSize);				

		String ReadArenaSize = args[2];
		index = ReadArenaSize.indexOf('=');
		ReadArenaSize = ReadArenaSize.substring(index+1, ReadArenaSize.length());		
		Integer ArenaSize = Integer.parseInt(ReadArenaSize);
		System.out.println("************************* Arena size is " + ArenaSize);	

		String dest_dir2 = "PopSize";
		dest_dir2 += PopSize;
		dest_dir2 += "_ArenaSize";
		dest_dir2 += ReadArenaSize;

		Hashtable<String,Integer> parameters = new Hashtable<String,Integer>();
		parameters.put("PopSize", PopSize);
		parameters.put("ArenaSize", ArenaSize);

		try {			
			File dir1 = new File (".");
			boolean success = (new File(dir1.getCanonicalPath() + "//" + dest_dir2)).mkdir();
			if (success) {
				System.out.println("Directory: " + dir1.getCanonicalPath() + "/" + dest_dir2 + " created");
			}    
		}
		catch (IOException e) {
			System.out.println(e);
		}		



		// which_is_mutant: Swarm or Transitionwith2Neighbors
		String temp_Mutant_file = args[3];
		index = temp_Mutant_file.indexOf('=');
		temp_Mutant_file = temp_Mutant_file.substring(index+1, temp_Mutant_file.length());				
		String Mutant_file = "//" + temp_Mutant_file;
		System.out.println("************************* Mutant_file is " + Mutant_file);	

		// Population file Swarm or Transitionwith2Neighbors
		String temp_Pop_file = args[4];
		index = temp_Pop_file.indexOf('=');
		temp_Pop_file = temp_Pop_file.substring(index+1, temp_Pop_file.length());		
		String Pop_file = "//" + temp_Pop_file;
		System.out.println("************************* Pop_file is " + Pop_file);		




		int numEvolutions = 5000;
		Configuration.reset();
		Configuration gaConf = new DefaultConfiguration();
		Genotype genotype = null;
		int chromeSize = 7;


		Gene[] sampleGenes = new Gene[ chromeSize ];

		try {


			gaConf.setKeepPopulationSizeConstant(true);


			gaConf.setBulkFitnessFunction(new RuleDecoding(parameters));

			for (int i=0; i < chromeSize; i++)
			{
				sampleGenes[i] = new IntegerGene(gaConf, 100, 100 );  
			}


			Chromosome sampleChromosome = new Chromosome(gaConf, sampleGenes );

			gaConf.setSampleChromosome( sampleChromosome );

			gaConf.setPopulationSize(PopSize);           
			genotype = Genotype.randomInitialGenotype(gaConf);

			//gaConf.getGeneticOperators().clear();

			Population solitary_pop = genotype.getPopulation();

			PopulationFileIO PopLoader = new PopulationFileIO();
			// Load evolved solitary population 			
			evolved_pop_2 = PopLoader.LoadPopulation(Pop_file, PopSize);		
			IChromosome solitary_individual = evolved_pop_2.getChromosome(0);
			for(int j=0; j<PopSize; j++) {
				solitary_pop.setChromosome(j, solitary_individual);
			}

			// Load evolved aggregation population 
			evolved_pop_1 = PopLoader.LoadPopulation(Mutant_file, PopSize);
			// Insert num_mutant evolved individuals to the Transitionwith2Neighbors population
			mutant = evolved_pop_1.getChromosome(0);



			genotype = new Genotype(gaConf, solitary_pop);

			for(int j=0; j<PopSize; j++) {
				IChromosome individual = solitary_pop.getChromosome(j);
				//System.out.println("Repulsion is .........: " + individual.getGene(1501));	
				//System.out.println("individual hash code .........: " + individual.hashCode());
			}

		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
			System.exit( -2);
		}

		int progress = 0;
		int percentEvolution = numEvolutions;


		Population pop2 = genotype.getPopulation();



		MyGABreeder breeder2 = new MyGABreeder(parameters);
		breeder2.mutant = mutant;
		breeder2.num_injection = 0;


		for (int i = 0; i < numEvolutions; i++) {

			Double fitness = 0.0;

			// Print progress.
			// ---------------
			if (percentEvolution > 0 ) {
				progress++;



				//IChromosome bestSolutionSoFar = pop2.determineFittestChromosome();
				//fitness = bestSolutionSoFar.getFitnessValueDirectly();
				//System.out.println("****************Currently fittest Chromosome has fitness " + fitness);

				System.out.println("************************* Previous generation mutant number is " + breeder2.Mutant_count);
				//System.out.println("************************* avg_fitness is " + avg_fitness);

				if(i==0) {
					System.out.println("-----------No mutant . Introduce mutants---------");
					breeder2.num_injection = 1;
				}
				else {

					breeder2.num_injection = 0;
					// If mutants extinct, then re-introduce mutants
					if(breeder2.Mutant_count == 0 && i>1) {

						System.out.println("-----------Mutant extincted. Invasion failed. Terminate!---------");
						break;


					}
					else {
						if(breeder2.Mutant_count>=PopSize-1) {
							System.out.println("-----------Mutant occupied the whole population---------");
							break;
						}
					}
				}
				

							

				try {				
					File dir1 = new File (".");
					FileWriter writer = new FileWriter(dir1.getCanonicalPath() + "//" + dest_dir2 + "//" + temp_Mutant_file + 
							"2" + temp_Pop_file + "_run_" + run_num + ".csv", true);
					writer.append(fitness.toString());
					writer.append(',');
					writer.append(num_re_introduction.toString());
					writer.append(',');
					writer.append(breeder2.Mutant_count.toString());
					writer.append('\n');
					writer.close();
				}
				catch (IOException e) {
					System.out.println(e);
				}


			}

			pop2 = breeder2.evolve(pop2, gaConf);
		}

		// Record the number or re-introduction and the final evolved population
		//----------------------
		try {
			File dir1 = new File (".");
			FileWriter writer = new FileWriter(dir1.getCanonicalPath() + "//" + dest_dir2 + "//" + temp_Mutant_file + 
					"2" + temp_Pop_file + "_run_" + run_num + ".csv", true);
			writer.append('0');
			writer.append(',');
			writer.append(num_re_introduction.toString());
			writer.append(',');
			writer.append(breeder2.Mutant_count.toString());
			writer.append('\n');
			writer.close();


		}

		catch (IOException e) {
			System.out.println(e);
		}

	}


}
