
package boidcoevolution;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.JFrame;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.NaturalSelector;
import org.jgap.Population;
import org.jgap.impl.BestChromosomesSelector;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;

import sim.display.Display2D;

public class PredatorRuleEvolution {

	public Display2D display;
	public JFrame displayFrame;
	


	public static void main(String[] args) {
		// used for evolved population
		//create populations of chromosones 
	
		int MaximumCatch = 10;
		String flockRule = "swarm";

		//Allows user to specify the number of runs 
		String run_num = args[0];
		int index = run_num.indexOf('=');
		run_num = run_num.substring(index+1, run_num.length());		

		//Population size - the number of prey
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
		
		//Number of Predators 
		String ReadPredSize = args[3];
		index = ReadPredSize.indexOf('=');
		ReadPredSize = ReadPredSize.substring(index+1, ReadPredSize.length());		
		Integer PredSize = Integer.parseInt(ReadPredSize);
		System.out.println("************************* Number of predators are  " + PredSize);	
				
		//naming directory according to parameters
		String dest_dir2 = run_num + "_PopSize" ;
		dest_dir2 += PopSize;
		dest_dir2 += "_ArenaSize";
		dest_dir2 += ReadArenaSize;
		dest_dir2 += "_NumPredators" + PredSize;
		dest_dir2 += "_FlockRule="+ flockRule;
		
		
		flockRule = flockRule.toLowerCase();
		int flockType = 1;
		if(flockRule.equals("dynamic")){
			flockType = 1;
		}
		else if(flockRule.equals("swarm")){
			flockType = 2;
		}
		else if(flockRule.equals("random")){
			flockType = 3;
		}
		
		
		//create a hash table of parameters 
		Hashtable<String,Integer> parameters = new Hashtable<String,Integer>();
		parameters.put("PopSize", PopSize);
		parameters.put("ArenaSize", ArenaSize);
		parameters.put("PredSize", PredSize);
		parameters.put("MaximumCatch", MaximumCatch);
		parameters.put("FlockType", flockType);

		//create a new file with name dependant on arena and pop size  
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

		try{
			// Create file 
			File dir1 = new File (".");
			FileWriter fstream = new FileWriter(dir1.getCanonicalPath() + "//" + dest_dir2 + "//parameters.txt");
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
		//4 genes in a chromosone  
		int chromeSize = 4;
		
		Gene[] sampleGenes = new Gene[chromeSize];
		
		//try to create a sample chromosone 
		try {
		
			//gaConf.setPreservFittestIndividual(false);
			gaConf.setKeepPopulationSizeConstant(true);
			gaConf.setBulkFitnessFunction(new PredatorFitnessFunction(parameters));
			
			//creating a sample genes for a sample chromosone, each gene is a double 
			sampleGenes[0] = new DoubleGene(gaConf,0,10); //distanceWillChase
			sampleGenes[1] = new DoubleGene(gaConf,0,10); //move_modifier
			sampleGenes[2] = new DoubleGene(gaConf,0,10); // size of group prioritised
			sampleGenes[3] = new DoubleGene(gaConf, 1, 2); //tactic   
			
			//create and set the sample chromosone 
			Chromosome sampleChromosome = new Chromosome(gaConf, sampleGenes );
			gaConf.setSampleChromosome( sampleChromosome );
			gaConf.setPopulationSize(PredSize);
			
			//create a random genotype (a collection of randomised chromosones that follow the
			//format given by the sample chromosone but with randomised values
			
			
			//BestChromosomesSelector natSelector = new BestChromosomesSelector(gaConf,0.5);
			
			
			//gaConf.removeNaturalSelectors(false);
			//System.out.println("NAT SELECTOR SIZE that runs after genetic modification = " + gaConf.getNaturalSelectors(false).size());
			//gaConf.addNaturalSelector(natSelector, false);
			//System.out.println("NAT SELECTOR SIZE  that run before = " + gaConf.getNaturalSelectors(true).size());
			genotype = Genotype.randomInitialGenotype(gaConf);

		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
			System.exit( -2);
		}
		
		
		int progress = 0;
		int percentEvolution = numEvolutions;

		//create a population from the genotype and create a new breeder
		Population population = genotype.getPopulation();
		PredatorGABreeder breeder = new PredatorGABreeder(parameters);
	
		
		try {
			genotype = new Genotype(gaConf, population);
		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
			System.exit( -2);
		}
		
		IChromosome bestSolutionSoFar =null;
		//for each evolution 
		for (int i = 0; i < numEvolutions; i++) {

			Double fitness = 0.0;

			// Print progress.
			// ---------------
			if (percentEvolution > 0 ) {
				progress++;
				
				if(bestSolutionSoFar!=null)
				{
				System.out.println("Progress: " + progress + " / "+ numEvolutions);
				System.out.println("This solution: " + population.determineFittestChromosome().getFitnessValueDirectly()
						+"      Current best: " + bestSolutionSoFar.getFitnessValueDirectly() );
				}
				
				//find best solution from chromosone 
				if(bestSolutionSoFar == null)
				{
					bestSolutionSoFar = population.determineFittestChromosome();
				}

				else if(population.determineFittestChromosome().getFitnessValueDirectly()> bestSolutionSoFar.getFitnessValueDirectly())
				{
					bestSolutionSoFar = population.determineFittestChromosome();		
				}
				fitness = bestSolutionSoFar.getFitnessValueDirectly();
				System.out.println("****************Currently fittest Chromosome has fitness " + fitness);
				//System.out.println("Size of chromosone pop: " + population.size());

				//every interval of 10 	
				if(i%10==0 ) {


					try {
						File dir1 = new File (".");

						// Write the whole population to a .csv file																																																					

						FileWriter writer = new FileWriter(dir1.getCanonicalPath() + "//" + dest_dir2  +"//PopulationRecords" + i + ".csv", true);

						//sometimes we gain more chromosones then the population due to optimisation of PredatorGABreeder, 
						//in this case sort by the fitness
						if(PredSize < population.size())
						{
							///population.sortByFitness();
						}
						
						for(int aa = 0; aa<PredSize; aa++) {
							IChromosome evolving_chromosome = population.getChromosome(aa);
							
							Double chromosome_fitness = evolving_chromosome.getFitnessValue();
							//System.out.println("Chromosone fitness = " + chromosome_fitness);
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
				
				try {
					File dir1 = new File (".");

					// Write the whole population to a .csv file																																																					

					FileWriter writer = new FileWriter(dir1.getCanonicalPath() + "//" + dest_dir2  +"//PopulationRecords-FITTEST.csv", false);
					//sometimes we gain more chromosones then the population due to optimisation of PredatorGABreeder, 
					//in this case sort by the fitness
					for(int aa = 0; aa<PredSize; aa++) {
						IChromosome evolving_chromosome = bestSolutionSoFar;				
						Double chromosome_fitness = evolving_chromosome.getFitnessValue();
						//System.out.println("Chromosone fitness = " + chromosome_fitness);
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


			population = breeder.evolve(population, gaConf);
		}



	}


}
