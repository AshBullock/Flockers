/*
  Copyright 2009 by Shan He and the University of Birmingham
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

/**
 * This class defines the arena and the system that controls 
 * predator-prey games    
 *
 * @author Shan He
 */

package boidcoevolution;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.jgap.Population;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;


public class Flockers extends SimState
{
	public Continuous2D flockers;
	public int num_neighborhood;
	public int arenalength = 200;
	public double width = arenalength;
	public double height = arenalength;
	public int Prey_BodyLength = 2;
	public int Predator_BodyLength = 4;
	public double base_speed = 0.7; // how far do we move in a timestep?
	public int maxStepCount = 1000;
	public PreyType preyType;
	public boolean changedPreyType= false;
	public boolean dynamicSwarmComparison = true;
	public int forceChrom = -1;
	public boolean variableSpeeds = false;
	public boolean variableSizes = false;
	
	public int num_predator = 1; 
	public int num_preys = 100; 
	public int numFlockers = num_preys + num_predator;

	public ArrayList<Integer> num_neighborhood_array;
	public ArrayList<Integer> repulsion_distance_array;
	public ArrayList<Double> speed_array = new ArrayList<Double> ();
	public ArrayList<Double> size_array = new ArrayList<Double> ();
	public Integer Predator_maximum_catch=30; //real one

	public ArrayList<ArrayList<Double>> Rule_array;
	public ArrayList<ArrayList<Double>> Predator_Rule_array;
	public int num_ann_output = 2;
	



	public ArrayList<Double> parameterlist;

	public int score=0;
	Hashtable<Integer,ArrayList<Integer>> predator_score_table = new Hashtable<Integer,ArrayList<Integer>>();
	Hashtable<Integer,ArrayList<Integer>> prey_score_table = new Hashtable<Integer,ArrayList<Integer>>();
	Hashtable<Integer,Integer> catched_table = new Hashtable<Integer,Integer>();
	Hashtable<Integer,Integer> pred_catch_table = new Hashtable<Integer,Integer>();
	Hashtable<Integer,Double> pred_energy_table = new Hashtable<Integer,Double>();

	
	public int getNumFlockers() { return numFlockers; }
	public void setNumFlockers(int val) { if (val >= 1) numFlockers = val; }
	public double getWidth() { return width; }
	public void setWidth(double val) { if (val > 0) width = val; }
	public double getHeight() { return height; }
	public void setHeight(double val) { if (val > 0) height = val; }
	public double getNumPredator() { return num_predator; }
	public void setNumPredator(int val) { if (val >= 0) num_predator = val; }

	public void setNumPreys(int val) { if (val >= 1) num_preys = val; }


	public void setScore(int val) {score = val;}
	
	public enum PreyType {
	    DYNAMIC, RANDOM, SWARM 
	}

	/** Creates a Flockers simulation with the given random number seed. */
	public Flockers(long seed, Hashtable<String,Integer> parameters)
	{
		
		super(seed);
		//System.out.println("CREATING NEW FLOCKERS");

		Predator_maximum_catch = parameters.get("MaximumCatch");
		num_preys = parameters.get("PopSize");
		arenalength = parameters.get("ArenaSize");
		try{
			num_predator = parameters.get("PredSize");
		}
		catch(Exception e)
		{
			num_predator = 1;
		}
		 
			
		this.setHeight(arenalength);
		this.setWidth(arenalength);
		this.setNumPreys(num_preys);
		this.setNumFlockers(num_preys+num_predator);
		//System.out.println("width and length in Flockers.java is: " + width);
		//System.out.println("num_preys in Flockers.java is: " + num_preys +"Number of predators: "+ num_predator);

	}
	public void restart()
	{
		super.start();
	}
	public void start()
	{
		
		super.start();

		// set up the flockers field.  It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us.  Hmph,
		// that's 16 hash lookups! I would have guessed that 
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal.  Go figure.
		//System.out.println("width and length in START() Flockers.java is: " + width);
		flockers = new Continuous2D(7*2,width,height);

		// make a bunch of flockers and schedule them.  A few will be dead
		int k=0;
		int preycount=0;

		for(int ii=0; ii<numFlockers; ii++) {
			
			Double speed = 0.0;
			if(variableSpeeds)
			{
			 speed = 0.7 + 0.1*Samplers.sampleGamma(4, 1/3.3);
			}
			else 
			{
				speed = base_speed;
			}
			
			//System.out.println("Speed is: " + speed);
			speed_array.add(speed);	
			
			
						
			
			
		}

		//Double predator_speed = Collections.max(speed_array);
		//System.out.println("width and length in Flockers.java is: " + width);
		//System.out.println("----------------------------------num_preys in Flockers.java is: " + num_preys);
		for(int x=0;x<num_preys;x++)
		{
			//System.out.println("CREATING PREYS");
			Prey prey = new Prey();


			// if (random.nextBoolean(deadFlockerProbability)) flocker.dead = true;

			// define predator, maximum number is num_predator
			Integer prey_box_width = 2*(int)(Math.sqrt(num_preys)); 
			prey.bodyLength = Prey_BodyLength;
			//REMEMBER TO SET MAX CATCH IN super
			//flocker.maximumCatch = Predator_maximum_catch;

			//flockers.setObjectLocation(flocker, 
			//new Double2D(random.nextDouble()*width/2 + width/3, random.nextDouble() * height/2 + height/3));
			
			//set the preys location
			flockers.setObjectLocation(prey, 
					new Double2D((random.nextDouble()*2-1)*prey_box_width+width/2, (random.nextDouble()*2-1)*prey_box_width+height/2));
			prey.lastd = new Double2D(random.nextGaussian(), random.nextGaussian());
			prey.flockID = preycount;
			//initialize score table for all preys
			ArrayList<Integer> life = new ArrayList<Integer>();
			life.add(0);
			prey_score_table.put(preycount, life);
			catched_table.put(preycount, 0);
			
			try
			{
				prey.num_neighborhood = num_neighborhood_array.get(preycount).intValue();
				prey.repulsion_distance = repulsion_distance_array.get(preycount).intValue();	
			}
			catch(Exception e)
			{}
			
			//System.out.println("preycount: " + preycount);
			//System.out.println("Flockers.java repulsion_distance: " + flocker.repulsion_distance);
			prey.speed = speed_array.get(preycount);
			
			if(variableSizes)
			{
			prey.size = 1 + 0.2*Samplers.sampleGamma(4, 1/3.3);
			
			}
			else
			{	
				prey.size = 1;
				
			}
			prey.bodyLength = prey.bodyLength * prey.size;
			
			//System.out.println(prey.bodyLength);
			preycount++;
			
			

			//flocker.lastd = new Double2D(random.nextGaussian(), random.nextGaussian());

			prey.flockers = flockers;
			prey.theFlock = this;
			schedule.scheduleRepeating(prey);                              

		}
		
		for(int ii=0; ii<num_predator; ii++)
		{
			
			Predator pred = new Predator();
			if(forceChrom>=0)
			{
				pred.flockID = forceChrom;
			}
			else
			{
				pred.flockID=ii;
			}
			pred_catch_table.put(ii,0);
			// if (random.nextBoolean(deadFlockerProbability)) flocker.dead = true;

			// define predator, maximum number is num_predator
			Integer prey_box_width = 2*(int)(Math.sqrt(num_preys)); 
			
			pred.maximumCatch = Predator_maximum_catch;
	
			
			pred.bodyLength = Predator_BodyLength;
			k++;
			Double predator_distance = width/2.5 + prey_box_width;
			Double predator_angle = random.nextDouble() * 2* Math.PI;	
			Double loc_x = predator_distance*Math.cos(predator_angle) + width/2;
			Double loc_y = predator_distance*Math.sin(predator_angle) + height/2;
			flockers.setObjectLocation(pred, new Double2D(loc_x, loc_y));
			//flocker.lastd = new Double2D(-predator_distance*Math.cos(predator_angle),  -predator_distance*Math.sin(predator_angle));
			flockers.setObjectLocation(pred, new Double2D(random.nextDouble()*width, random.nextDouble() * height));
			pred.lastd = new Double2D(random.nextGaussian(), random.nextGaussian());
			if(variableSpeeds)
			{
			double predSpeed = (0.7 + 0.1*Samplers.sampleGamma(4, 1/3.3))*1.2;
			pred.speed = predSpeed;
			}
			else 
			{
				 pred.speed = base_speed * 1.2;
			}
			//				System.out.println("Predator_maximum_catch: " + Predator_maximum_catch);
			

			

			//flocker.lastd = new Double2D(random.nextGaussian(), random.nextGaussian());
			pred.size = 2;
			pred.flockers = flockers;
			pred.theFlock = this;
			pred.bodyLength = pred.bodyLength * pred.size;
			schedule.scheduleRepeating(pred);             
		}



	}

	// return the score as fitness. 
	public Hashtable<Integer,Integer> runPredatorEvolution(
			ArrayList<ArrayList<Double>> Flock_Rule_array,
			ArrayList<ArrayList<Double>> Predator_Rule_array, 
			ArrayList<Integer> num_neighborhood_array, 
			ArrayList<Integer> repulsion_array, 
			Hashtable<String,Integer> parameters)
	{
		
		dynamicSwarmComparison = false;
		Flockers flockers = new Flockers(System.currentTimeMillis(), parameters);
		flockers.Rule_array = Flock_Rule_array;
		flockers.Predator_Rule_array = Predator_Rule_array;
		flockers.num_neighborhood_array = num_neighborhood_array;
		flockers.repulsion_distance_array = repulsion_array;

		Iterator itr = flockers.repulsion_distance_array.iterator(); 
		while(itr.hasNext()) {

			Object element = itr.next(); 
			//System.out.println("repulsion_distance is " + (Integer)element);

		} 


		flockers.Predator_maximum_catch = parameters.get("MaximumCatch");
		//System.out.println("Flockers max catch" + flockers.Predator_maximum_catch);

		flockers.start();
		long steps = 0;
		// maximum number of steps
		while(steps < maxStepCount )
		{
			
			if (!flockers.schedule.step(flockers))
				break;

			Integer catched_counter = 0;
			
		
			for (Enumeration e = flockers.pred_catch_table.keys(); e.hasMoreElements();)
			{
				
				
				catched_counter += flockers.pred_catch_table.get(e.nextElement()) ;
			}
			
			steps = flockers.schedule.getSteps();

			if(catched_counter >= Predator_maximum_catch ) {
				
				break;
			}
		}

		
		Hashtable<Integer,Integer> fitness = new Hashtable<Integer,Integer> ();

		for (Enumeration e = flockers.pred_catch_table.keys(); e.hasMoreElements();)
		{
			Object index = e.nextElement();
			int flockID = (int)index;
			System.out.println("INDEX = " + index.toString()+"       pred energy table size = " + flockers.pred_energy_table.size());
			double predEnergy = flockers.pred_energy_table.get(index);
			System.out.println("Predator energy = " + predEnergy);
				
				
			ArrayList<Double> rule = flockers.Predator_Rule_array.get(flockID);
			double dynamicModifier = rule.get(4); //get swarm/dynamic modifier	
			double swarmModifier = rule.get(5);
			int fishCaught = 0;
			fishCaught = flockers.pred_catch_table.get(index);
			System.out.println("Fish Caught = " + fishCaught);
			
			double _value = ((double) ((fishCaught*10) + (predEnergy/20))/150)*100;
			int fitness_value =(int) _value;
			
			System.out.println("Fitness = " + fitness_value);
			//int fintness_value = fishCaught;
				
			fitness.put((Integer)index, fitness_value);
		}

		flockers.finish();

		return fitness;

	}   
	
	
	// return the score as fitness.  NEED TO ADD PREDATOR POPLOADER
		public Hashtable<Integer,Integer> run(ArrayList<ArrayList<Double>> Rule_array, ArrayList<Integer> num_neighborhood_array, ArrayList<Integer> time_wasting_array, Hashtable<String,Integer> parameters)
		{
			//System.out.println("nnetList.size() is " + nnetList.size());
			Flockers flockers = new Flockers(System.currentTimeMillis(), parameters);

	
			flockers.Rule_array = Rule_array;
			flockers.num_neighborhood_array = num_neighborhood_array;
			flockers.repulsion_distance_array = time_wasting_array;

			Iterator itr = flockers.repulsion_distance_array.iterator(); 
			while(itr.hasNext()) {

				Object element = itr.next(); 
				//System.out.println("repulsion_distance is " + (Integer)element);

			} 


			flockers.Predator_maximum_catch = 15;

			flockers.start();
			long steps = 0;
			// maximum number of steps
			int max_steps = 1000; // 1 hour //36000 steps are one hour: 10*10*60*1;
			while(steps < max_steps )
			{
				if (!flockers.schedule.step(flockers))
					break;

				ArrayList<Integer> fitness = new ArrayList<Integer> ();

				Integer catched_counter = 0;
				for (Enumeration e = flockers.catched_table.keys(); e.hasMoreElements();)
				{
					//System.out.println("Catched_Table is " + flockers.catched_table.get(e.nextElement()));
					catched_counter += flockers.catched_table.get(e.nextElement()) ;
				}

				steps = flockers.schedule.getSteps();
				//System.out.println("Steps: " + steps + "catched_counter: " + catched_counter );


				if(catched_counter>=1) {
					System.out.println("Steps: " + steps + "  ------ catched_counter: " + catched_counter );
					break;
				}
			}

			
			Hashtable<Integer,Integer> fitness = new Hashtable<Integer,Integer> ();

		
			for (Enumeration e = flockers.prey_score_table.keys(); e.hasMoreElements();)
			{
				Object index = e.nextElement();

				
				
				int caught_times = 1;
				caught_times = flockers.catched_table.get(index)+1;
				int fitness_value = (100-caught_times);
				//System.out.println(flockers.score_table.get(index));
				//System.out.println("caught_times is ----"+caught_times + "---caught_index" + (Integer)index);
				fitness.put((Integer)index, fitness_value);
			}

			flockers.finish();

			return fitness;

		}   



	public static Integer Median(ArrayList values)
	{
		Collections.sort(values);

		if (values.size() % 2 == 1)
			return (Integer) values.get((values.size()+1)/2-1);
		else
		{
			Integer lower = (Integer) values.get(values.size()/2-1);
			Integer upper = (Integer) values.get(values.size()/2);

			return (lower + upper) / 2;
		}	
	}
	
	public int getMaxStepCount()
	{
		return maxStepCount;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Hashtable<Integer, Integer> runDynamicSwarmComparison(
			ArrayList<ArrayList<Double>> flock_Rule_array,
			ArrayList<ArrayList<Double>> predator_Rule_array2,
			ArrayList<Integer> num_neighborhood_array2,
			ArrayList<Integer> repulsion_array,
			Hashtable<String, Integer> parameters, Population swarmPop, int predatorNumber) {
		System.out.println("             Running dynamic population:");
		
		Flockers flockers = new Flockers(System.currentTimeMillis(), parameters);
		flockers.preyType = PreyType.DYNAMIC;
		flockers.Rule_array = flock_Rule_array;
		flockers.Predator_Rule_array = predator_Rule_array2;
		flockers.num_neighborhood_array = num_neighborhood_array2;
		flockers.repulsion_distance_array = repulsion_array;

		Iterator itr = flockers.repulsion_distance_array.iterator(); 
		while(itr.hasNext()) {

			Object element = itr.next(); 
			//System.out.println("repulsion_distance is " + (Integer)element);

		} 


		flockers.Predator_maximum_catch = parameters.get("MaximumCatch");
		//System.out.println("Flockers max catch" + flockers.Predator_maximum_catch);
		
		flockers.start();
		long steps = 0;
		Integer catched_counter = 0;
		int dynamicFishCaught =0;
		// maximum number of steps
		while(steps < maxStepCount )
		{
			
			if (!flockers.schedule.step(flockers))
				break;

	
			
		
			for (Enumeration e = flockers.pred_catch_table.keys(); e.hasMoreElements();)
			{
				
				catched_counter = 0;
				catched_counter += flockers.pred_catch_table.get(e.nextElement()) ;
			
			}
			
			steps = flockers.schedule.getSteps();
			//System.out.println("Max catch: " + Predator_maximum_catch);
			if(catched_counter >= Predator_maximum_catch ) {
				System.out.println("Max catched reached");
				break;
			}
		}
		
		flockers.schedule.seal();
		//re initialise prey and run with swarm if maximum catch has not been reached
		
		if(catched_counter < Predator_maximum_catch)
		{	
			PopulationFileIO PopLoader = new PopulationFileIO();
			PopLoader.readRule(swarmPop);
			flockers.Rule_array =  PopLoader.Rule_array;
			flockers.num_neighborhood_array = PopLoader.num_neighborhood_array;
			flockers.repulsion_distance_array = PopLoader.repulsion_array;
			
			changedPreyType = true;
			flockers.changedPreyType = true;
			flockers.schedule.reset();
			flockers.preyType = PreyType.SWARM;
			flockers.reinitialisePrey(parameters, flockers);
			
			//System.out.println("Changing to swarm...");
			dynamicFishCaught = catched_counter;
			for (Enumeration e = flockers.pred_catch_table.keys(); e.hasMoreElements();)
			{
				Object index = e.nextElement();
				int flockID = (int)index;
				//System.out.println("INDEX = " + index.toString()+"       pred energy table size = " + flockers.pred_energy_table.size());
				double predEnergy = flockers.pred_energy_table.get(index);
				System.out.println("                         Predator energy = " + predEnergy);
			}
			System.out.println("                         Fish caught in Dynamic = " + catched_counter);
			System.out.println("             Running Swarm Population");
			
			steps = 0;
			// maximum number of steps
			int max_steps = 1000; // 1 hour //36000 steps are one hour: 10*10*60*1;
			
		//	flockers.restart();
			
			while(steps < max_steps )
			{
				//System.out.println("running...");
			
				
				if(flockers.schedule.getSteps()>1)
				{
					changedPreyType = false;
				}
		
				if (!flockers.schedule.step(flockers))
				{
					//System.out.println("breaking because here");
					break;
				}
				ArrayList<Integer> fitness = new ArrayList<Integer> ();

				steps = flockers.schedule.getSteps();
				
				if(catched_counter>=Predator_maximum_catch) {
					System.out.println("Steps: " + steps + "  ------ catched_counter: " + catched_counter );
					break;
				}
				
				
			}
			
			
			  
			}
		
		Hashtable<Integer,Integer> fitness = new Hashtable<Integer,Integer> ();

		for (Enumeration e = flockers.pred_catch_table.keys(); e.hasMoreElements();)
		{
			Object index = e.nextElement();
			int flockID = (int)index;
			//System.out.println("INDEX = " + index.toString()+"       pred energy table size = " + flockers.pred_energy_table.size());
			double predEnergy = flockers.pred_energy_table.get(index);
			System.out.println("                         Predator energy = " + predEnergy);
				
				
			ArrayList<Double> rule = flockers.Predator_Rule_array.get(flockID);
			double dynamicModifier = rule.get(4); //get swarm/dynamic modifier	
			double swarmModifier = rule.get(5);
			int fishCaught = 0;
			//System.out.println("flockers catch table size " + flockers.pred_catch_table.size());
			//System.out.println("flock ID = " + flockID);
			//System.out.println("catched fish: " + flockers.pred_catch_table.get(0));
			fishCaught = flockers.pred_catch_table.get(index);
			System.out.println("                         Fish caught in Swarm = " + (fishCaught-dynamicFishCaught));
			System.out.println();
			System.out.println("                         Fish Caught = " + fishCaught);
			
			double _value = ((double) ((fishCaught*10) + (predEnergy/10))/150)*100;
			int fitness_value =(int) _value;
			
			System.out.println("                         Fitness = " + fitness_value);
			//int fintness_value = fishCaught;
				
			fitness.put((Integer)index, fitness_value);
		}

		flockers.finish();

		return fitness;
		
		
		
		
		
	}
	private void reinitialisePrey(Hashtable<String, Integer> parameters, Flockers flockers2) {
		
		//System.out.println("Reinitialising..");
		super.start();

		Bag population = new Bag(flockers2.flockers.getAllObjects());
		
		
		//System.out.println("population size = " + population.size());
		
		Iterator<Flocker> itr = population.iterator(); 
		
		while(itr.hasNext()) {
			
	
	
			//System.out.println("looking at current fish..");
			Flocker fish =(Flocker) itr.next();
			
			if(fish instanceof Prey)
			{
				//System.out.println("Removing fish from prey..");
				itr.remove();
				
			}
			else
			{
				schedule.scheduleRepeating(fish);
			}
		
		}
		//System.out.println("Size now: " + population.size());
		//System.out.println("Count = " + count);
		
		for(int x=0;x<num_preys;x++)
		{
			Prey prey = new Prey();
			Integer prey_box_width = 2*(int)(Math.sqrt(num_preys)); 
			prey.bodyLength = Prey_BodyLength;
			
		
			//set the preys location
			flockers2.flockers.setObjectLocation(prey, 
					new Double2D((random.nextDouble()*2-1)*prey_box_width+width/2, (random.nextDouble()*2-1)*prey_box_width+height/2));
			prey.lastd = new Double2D(random.nextGaussian(), random.nextGaussian());
			prey.flockID = x;
			//initialize score table for all preys
			ArrayList<Integer> life = new ArrayList<Integer>();
			life.add(0);
			prey_score_table.put(x, life);
			catched_table.put(x, 0);
			
			try
			{
				prey.num_neighborhood = num_neighborhood_array.get(x).intValue();
				prey.repulsion_distance = repulsion_distance_array.get(x).intValue();	
			}
			catch(Exception e)
			{}
			
			//System.out.println("preycount: " + preycount);
			//System.out.println("Flockers.java repulsion_distance: " + flocker.repulsion_distance);
			prey.speed = flockers2.speed_array.get(x);
			
			if(variableSizes)
			{
			prey.size = 1 + 0.2*Samplers.sampleGamma(4, 1/3.3);
			
			}
			else
			{	
				prey.size = 1;
				
			}
			prey.bodyLength = prey.bodyLength * prey.size;
			
			prey.flockers = flockers2.flockers;
			prey.theFlock = flockers2;      
			schedule.scheduleRepeating(prey);  
		}
	}
	
	
	
	
	
	
	public Integer runDynamicSwarmComparisonSinglePredator(
			ArrayList<ArrayList<Double>> flock_Rule_array,
			ArrayList<ArrayList<Double>> predator_Rule_array2,
			ArrayList<Integer> num_neighborhood_array2,
			ArrayList<Integer> repulsion_array,
			Hashtable<String, Integer> parameters, Population swarmPop,
			int predatorIndex) {
		
		
		System.out.println("             Running dynamic population:");
		
		Flockers flockers = new Flockers(System.currentTimeMillis(), parameters);
		flockers.preyType = PreyType.DYNAMIC;
		flockers.Rule_array = flock_Rule_array;
		flockers.Predator_Rule_array = predator_Rule_array2;
		flockers.num_neighborhood_array = num_neighborhood_array2;
		flockers.repulsion_distance_array = repulsion_array;

		Iterator itr = flockers.repulsion_distance_array.iterator(); 
		while(itr.hasNext()) {

			Object element = itr.next(); 
			//System.out.println("repulsion_distance is " + (Integer)element);

		} 


		flockers.Predator_maximum_catch = parameters.get("MaximumCatch");
		//System.out.println("Flockers max catch" + flockers.Predator_maximum_catch);
		
		flockers.start();
		long steps = 0;
		Integer catched_counter = 0;
		int dynamicFishCaught =0;
		// maximum number of steps
		while(steps < maxStepCount )
		{
			
			if (!flockers.schedule.step(flockers))
				break;
	
			
			catched_counter = flockers.pred_catch_table.get(0);
			//System.out.println("Catched counter: " + catched_counter);
			steps = flockers.schedule.getSteps();
			//System.out.println("Max catch: " + Predator_maximum_catch);
		
			if(catched_counter >= Predator_maximum_catch ) {
				System.out.println("Max catch reached");
				break;
			}
		}
		
		flockers.schedule.seal();

		//re initialise prey and run with swarm if maximum catch has not been reached
		if(catched_counter < Predator_maximum_catch)
		{	
			PopulationFileIO PopLoader = new PopulationFileIO();
			PopLoader.readRule(swarmPop);
			flockers.Rule_array =  PopLoader.Rule_array;
			flockers.num_neighborhood_array = PopLoader.num_neighborhood_array;
			flockers.repulsion_distance_array = PopLoader.repulsion_array;
			
			changedPreyType = true;
			flockers.changedPreyType = true;
			flockers.schedule.reset();
			flockers.preyType = PreyType.SWARM;
			flockers.reinitialisePrey(parameters, flockers);
			
			//System.out.println("Changing to swarm...");
			dynamicFishCaught = catched_counter;
			
				
			double predEnergy = flockers.pred_energy_table.get(0);
			System.out.println("                         Predator energy = " + predEnergy);
		
			System.out.println("                         Fish caught in Dynamic = " + dynamicFishCaught);
			System.out.println("             Running Swarm Population");
			
			steps = 0;
			// maximum number of steps
			int max_steps = 1000; // 1 hour //36000 steps are one hour: 10*10*60*1;
			
		//	flockers.restart();
			//System.out.println("PredCatchNumber is " + flockers.pred_catch_table.get(0));
			while(steps < max_steps )
			{
				//System.out.println("running...");
			
				
				if(flockers.schedule.getSteps()>1)
				{
					changedPreyType = false;
				}
				
				catched_counter = flockers.pred_catch_table.get(0) ;
				
				if (!flockers.schedule.step(flockers))
				{
					//System.out.println("breaking because here");
					break;
				}
				

				steps = flockers.schedule.getSteps();
				
				if(catched_counter>=Predator_maximum_catch) {
					System.out.println("                         Max catch reached ------ catched_counter: " + catched_counter );
					break;
				}	
			}
				  
			}
		
		
		int fitness_value = 0;
		//System.out.println("INDEX = " + index.toString()+"       pred energy table size = " + flockers.pred_energy_table.size());
		double predEnergy = flockers.pred_energy_table.get(0);
		System.out.println("                         Predator energy = " + predEnergy);
			
		ArrayList<Double> rule = flockers.Predator_Rule_array.get(0);
		double dynamicModifier = rule.get(4); //get swarm/dynamic modifier	
		double swarmModifier = rule.get(5);
		int fishCaught = 0;
	
		
		fishCaught = flockers.pred_catch_table.get(0);
		System.out.println("                         Fish caught in Swarm = " + (fishCaught-dynamicFishCaught));
		System.out.println();
		System.out.println("                         Fish Caught = " + fishCaught);
			
			double _value = (fishCaught*10) + (predEnergy/5);
			if(_value < 0)
				_value=0;
			
			int maxCatch = parameters.get("MaximumCatch");
			
			int maxFitness = (int) (((maxCatch*10) + ((((maxCatch*50)/5))*0.8)));
			
			
			
			double adjustedFitness = (_value/maxFitness)*100;
			fitness_value = (int) adjustedFitness;
			
			System.out.println("                         Fitness = " + fitness_value);
			//int fintness_value = fishCaught;
			
		

		flockers.finish();

		return fitness_value;
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	public void setForceChromosone(int i)
	{
		this.forceChrom = i;
	}
	
	
}