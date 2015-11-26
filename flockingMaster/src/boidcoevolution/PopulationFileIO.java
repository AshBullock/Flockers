/**
 * 
 */
package boidcoevolution;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;

/**
 * @author luser
 *
 */
public class PopulationFileIO {
	ArrayList<ArrayList<Double>> Rule_array = new ArrayList<ArrayList<Double>>(); 
	ArrayList<Integer> num_neighborhood_array = new ArrayList<Integer>();
	ArrayList<Integer> repulsion_array = new ArrayList<Integer>();
	
	public ArrayList<Integer> Get_num_neighborhood_array()
	{
		return num_neighborhood_array;
	}
	
	public ArrayList<Integer> Get_repulsion_array()
	{
		return repulsion_array;
	}
	
	public Population LoadPopulation(String name, Integer pop_num)
	{
		Population pop = null;
		Configuration.reset();
		Configuration gaConf = new DefaultConfiguration();
		gaConf.setPreservFittestIndividual(true);
		gaConf.setKeepPopulationSizeConstant(true);


		try{
			pop = new Population(gaConf, pop_num);
			// Open the file that is the first 
			// command line parameter
			File dir1 = new File (".");
			File destinationFile = new File(dir1.getCanonicalPath()  + name + ".csv");
			//System.out.println (dir1.getCanonicalPath()  + name + ".csv");
			FileInputStream fstream = new FileInputStream(destinationFile);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Parse the line
				String delims = "[,]";
				String[] tokens = strLine.split(delims);
				//System.out.println (tokens);
				ArrayList<Double> temparray = new ArrayList<Double>();
				//The first value is fitness, but for loaded population, set it to -1
				//Double Chromosome_fitness = Double.parseDouble(tokens[0]);
				Double Chromosome_fitness = -1.0;
				for (int a = 1; a < tokens.length; a++) {
					String tempalle = tokens[a];
					double allevalue = 0;
					try
					{
						allevalue = Double.parseDouble(tempalle);
						//allevalue = Integer.parseInt(tempalle);
					}
					catch(Exception e)
					{
						//System.out.println("I am here");
						Double value = Double.parseDouble(tempalle);
						System.out.println("allevalue = " + value);
						allevalue = (int)Math.round(value);	
						System.out.println("rounded = " + allevalue );
					}
					//System.out.println ("the allele vaule is " + allevalue);
					temparray.add(allevalue);
				}
				IChromosome chrom = ParseChromosome(gaConf,temparray, Chromosome_fitness);
				pop.addChromosome(chrom);

			}
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		return pop;
	}

	public IChromosome ParseChromosome(Configuration gaConf, ArrayList<Double> allele_array, Double Fitness)
	{

		Chromosome sampleChromosome = null;
		int chromeSize = allele_array.size();

		try {
			Gene[] sampleGenes = new Gene[ chromeSize ];
			//gaConf.setFitnessFunction(new MarkerDecoding());

			for (int i=0; i < chromeSize; i++)
			{			
				Double allele_value = allele_array.get(i);
				//System.out.println("the allele_arry value is " + allele_value + "**** i is " + i);
				sampleGenes[i] = new DoubleGene(gaConf, allele_value, allele_value);  
				sampleGenes[i].setAllele(allele_value);
			}

			sampleChromosome = new Chromosome(gaConf, sampleGenes );  
			sampleChromosome.setFitnessValue(Fitness);


		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
			System.exit( -2);
		}
		return sampleChromosome;


	}

	public void readRule(Population pop) 
	{
		
		ArrayList<Double> Rule_j = new ArrayList<Double>();
		
		for (int i=0;i<pop.size();i++) {
			IChromosome a_subject = pop.getChromosome(i);
			int numGenes = a_subject.size();
			//System.out.println("Num of genes: " + numGenes);
			//System.out.println("Chromosome hash code is: " + a_subject.hashCode());
			
			// Last allele is the number of neighbourhood
			Double num_neighborhood = (Double) a_subject.getGene(0).getAllele();
			
			Double repulsion_dis =
				(Double) a_subject.getGene(1).getAllele();
			
			num_neighborhood_array.add(3);
			repulsion_array.add(5);

			for (int j=0; j<numGenes-2; j++) {
					
					Double rule_output = (Double) a_subject.getGene(j+2).getAllele();
					Rule_j.add(rule_output);
			}
			

			Rule_array.add(Rule_j);
		}
		
	}
	
}
