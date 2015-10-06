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

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;


public class Flockers extends SimState
{
	public Continuous2D flockers;

	public int arenalength = 200;
	public double width = arenalength;
	public double height = arenalength;
	public int Prey_BodyLength = 2;
	public int Predator_BodyLength = 4;
	public double base_speed = 0.7; // how far do we move in a timestep?

	public int num_predator = 1; 
	public int num_preys = 100; 
	public int numFlockers = num_preys + num_predator;

	public ArrayList<Integer> num_neighborhood_array;
	public ArrayList<Integer> repulsion_distance_array;
	public ArrayList<Double> speed_array = new ArrayList<Double> ();
	public Integer Predator_maximum_catch;

	public ArrayList<ArrayList<Double>> Rule_array;
	public int num_ann_output = 2;



	public ArrayList<Double> parameterlist;

	public int score=0;

	Hashtable<Integer,ArrayList<Integer>> score_table = new Hashtable<Integer,ArrayList<Integer>>();
	Hashtable<Integer,Integer> catched_table = new Hashtable<Integer,Integer>();


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

	/** Creates a Flockers simulation with the given random number seed. */
	public Flockers(long seed, Hashtable<String,Integer> parameters)
	{
		super(seed);


		num_preys = parameters.get("PopSize");
		arenalength = parameters.get("ArenaSize");

		this.setHeight(arenalength);
		this.setWidth(arenalength);
		this.setNumPreys(num_preys);
		this.setNumFlockers(num_preys+num_predator);
		System.out.println("width and length in Flockers.java is: " + width);
		System.out.println("num_preys in Flockers.java is: " + num_preys);

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

		// make a bunch of flockers and schedule 'em.  A few will be dead
		int k=0;
		int preycount=0;

		for(int ii=0; ii<numFlockers; ii++) {
			//Double speed = 0.7; // + 0.1*Samplers.sampleGamma(4, 1/3.3);
			//System.out.println("Speed is: " + speed);
			speed_array.add(base_speed);				
		}

		//Double predator_speed = Collections.max(speed_array);
		//System.out.println("width and length in Flockers.java is: " + width);
		//System.out.println("----------------------------------num_preys in Flockers.java is: " + num_preys);
		for(int x=0;x<numFlockers;x++)
		{
			Flocker flocker = new Flocker();


			// if (random.nextBoolean(deadFlockerProbability)) flocker.dead = true;

			// define predator, maximum number is num_predator
			Integer prey_box_width = 2*(int)(Math.sqrt(num_preys)); 
			flocker.body_length = Prey_BodyLength;
			flocker.Predator_maximum_catch = Predator_maximum_catch;

			if (k < num_predator) {
				flocker.ispredator = true;
				flocker.body_length = Predator_BodyLength;
				k++;
				Double predator_distance = width/2.5 + prey_box_width;
				Double predator_angle = random.nextDouble() * 2* Math.PI;	
				Double loc_x = predator_distance*Math.cos(predator_angle) + width/2;
				Double loc_y = predator_distance*Math.sin(predator_angle) + height/2;
				flockers.setObjectLocation(flocker, new Double2D(loc_x, loc_y));
				//flocker.lastd = new Double2D(-predator_distance*Math.cos(predator_angle),  -predator_distance*Math.sin(predator_angle));
				flockers.setObjectLocation(flocker, new Double2D(random.nextDouble()*width, random.nextDouble() * height));
				flocker.lastd = new Double2D(random.nextGaussian(), random.nextGaussian());
				flocker.speed = base_speed * 1.2;
				//				System.out.println("Predator_maximum_catch: " + Predator_maximum_catch);

			}
			else {
				//flockers.setObjectLocation(flocker, 
				//new Double2D(random.nextDouble()*width/2 + width/3, random.nextDouble() * height/2 + height/3));
				flockers.setObjectLocation(flocker, 
						new Double2D((random.nextDouble()*2-1)*prey_box_width+width/2, (random.nextDouble()*2-1)*prey_box_width+height/2));
				flocker.lastd = new Double2D(random.nextGaussian(), random.nextGaussian());
				flocker.flockID = preycount;
				//initialize score table for all preys
				ArrayList<Integer> life = new ArrayList<Integer>();
				life.add(0);
				score_table.put(preycount, life);
				System.out.println("Num neighbourhood array size: " + num_neighborhood_array.size());
				catched_table.put(preycount, 0);
				try
				{
				flocker.num_neighborhood = num_neighborhood_array.get(preycount);
				flocker.repulsion_distance = repulsion_distance_array.get(preycount);	
				}
				catch(Exception e)
				{}
				//System.out.println("preycount: " + preycount);
				//System.out.println("Flockers.java repulsion_distance: " + flocker.repulsion_distance);
				flocker.speed = speed_array.get(preycount);

				preycount++;

			}

			//flocker.lastd = new Double2D(random.nextGaussian(), random.nextGaussian());

			flocker.flockers = flockers;
			flocker.theFlock = this;
			schedule.scheduleRepeating(flocker);                              

		}



	}

	// return the score as fitness. 
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


		flockers.Predator_maximum_catch = 1;

		flockers.start();
		long steps = 0;
		// maximum number of steps
		int max_steps = 36000; // 1 hour //36000 steps are one hour: 10*10*60*1;
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

		//System.out.println("############steps is " + steps);

		Hashtable<Integer,Integer> fitness = new Hashtable<Integer,Integer> ();

		for (Enumeration e = flockers.score_table.keys(); e.hasMoreElements();)
		{
			Object index = e.nextElement();

			int median_life = 0; 
			//ArrayList<Integer> life_list = flockers.score_table.get(index);
			//median_life = Median(life_list);
			//System.out.println("median_life is " +  median_life);
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


}
