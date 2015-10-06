/*
  Copyright 2009 by Shan He and the University of Birmingham
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

/**
 * This class defines the behavioural models controlled by the 
 * evolutionary neural networks  
 *
 * @author Shan He
 */


package boidcoevolution;
import java.util.*;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;

public class Flocker implements Steppable, sim.portrayal.Oriented2D
{
	public Double2D loc = new Double2D(0,0);
	public Double2D lastd = new Double2D(0,0);
	public Double2D lastvelocity = new Double2D(0,0);
	public Continuous2D flockers;
	public Flockers theFlock;

	public int flockID;
	public int life;
	public int repulsion_distance;
	public int num_neighborhood;
	public int body_length;

	public int Num_catched_prey=0;
	public int Predator_maximum_catch;

	public boolean dead = false;
	public boolean ispredator = false;
	public boolean accelation_flag = false;
	public int energy=0;	
	public int catched = 0;
	public int pause_time = 0; 

	public Double speed = 0.0;
	
	public int Lock_ID = -1;


	public double getOrientation() { return orientation2D(); }


	public double orientation2D()
	{
		// Range [0 2\pi]
		if (lastd.x == 0 && lastd.y == 0) return 0;

		if (Math.atan2(lastd.y,  lastd.x)<0.0)
			return Math.atan2(lastd.y,  lastd.x)+Math.PI*2;
		else
			return Math.atan2(lastd.y,  lastd.x);
	}


	public double getangle(double x1, double y1, double x2, double y2)
	{

		double xx = flockers.tdx(x2,x1);
		double yy = flockers.tdx(y2,y1);
		// Range [0 2\pi]
		if (xx == 0 && xx == 0) return 0;

		if (Math.atan2(yy, xx)<0.0)
			return Math.atan2(yy, xx) + Math.PI*2;
		else
			return Math.atan2(yy, xx);

	}


	// get distance of the nearest neighbour 
	public double getNND(Bag b_prey, Continuous2D flockers)
	{
		Vector<Double> distances_array = new  Vector<Double>();
		if (b_prey==null || b_prey.numObjs == 0) return 350;

		for(int i=0;i<b_prey.numObjs;i++)
		{
			Flocker other = (Flocker)(b_prey.objs[i]);
			if (other != this && !other.ispredator)
			{
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				double lensquared = Math.sqrt(dx*dx+dy*dy);
				if ( !other.ispredator ) {
					distances_array.add(lensquared);
				}
			}
		}

		//sort
		if(distances_array.size()>0) {
			Double obj = Collections.min(distances_array);
			//System.out.print(obj + "\n");
			return obj;
		}
		else
			return 350;



	}



	public Double2D EnforceNonPenetrationConstraint(Bag b, Continuous2D flockers)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);
		double x = 0;
		double y = 0;

		int i=0;
		int count = 0;

		for(i=0;i<b.numObjs;i++)
		{

			//Class myClass = b.objs[i].getClass();
			//System.out.print(myClass.getName() + "\n");
			Flocker other = (Flocker)(b.objs[i]);

			if (other != this && other.ispredator != true && other.dead != true)
			{				
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				double DistFromEachOther = Math.sqrt(dx*dx+dy*dy);
				double AmountOfOverlap = 2*theFlock.Prey_BodyLength - DistFromEachOther; 

				if (AmountOfOverlap>0)
				{	
					count++;
					x += dx/DistFromEachOther * AmountOfOverlap;
					y += dy/DistFromEachOther * AmountOfOverlap;
				}
			}
		}

		//return new Double2D(x/b.numObjs,y/b.numObjs);  
		return new Double2D(x,y);
	}


	public void RelocatePrey(Bag b_prey, Continuous2D flockers, Flockers flock)
	{
		Integer prey_box_width = flock.Prey_BodyLength*(int)(Math.sqrt(flock.num_preys));

		for(int i=0;i<b_prey.numObjs;i++)
		{
			Flocker obj = (Flocker)(b_prey.objs[i]);
			if (!obj.ispredator)
			{
				obj.catched = 0;
				obj.life = 0;
				obj.dead=false;
				Double2D pos = new Double2D((flock.random.nextDouble()*2-1)*prey_box_width+flockers.width/2, (flock.random.nextDouble()*2-1)*prey_box_width+flockers.height/2);
				flockers.setObjectLocation(obj, pos);
				obj.lastd = new Double2D(flock.random.nextGaussian(), flock.random.nextGaussian());
			}
		}

	}

	///

	public ArrayList<Integer> getNearestNeighbours(Bag b)
	{
		ArrayList<Integer> nearestNeighbours = new  ArrayList <Integer>();

		Hashtable<Integer,Integer> distances_Predators = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> distances_WithinRange = new Hashtable<Integer,Integer>();

		Hashtable<Integer,Integer> distances_OutsideRange = new Hashtable<Integer,Integer>();


		if (b==null || b.numObjs == 0) return nearestNeighbours;

		// angle of the i_th boid theta_i in polar coordinates  
		double beta_ij = 0; 
		double theta_i = this.getOrientation();
		//System.out.print("new member now \n");
		//System.out.print("theta_i is " + Math.toDegrees(theta_i) + "\n");

		int i=0;
		//System.out.print(b.numObjs + "\n");
		for(i=0;i<b.numObjs;i++)
		{

			//Class myClass = b.objs[i].getClass();
			//System.out.print(myClass.getName() + "\n");
			Flocker other = (Flocker)(b.objs[i]);

			if (other != this && other.dead==false)
			{
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				// Add noise to distance calculation
				//dx *= (theFlock.random.nextDouble()*2-1)/10+1;
				//dy *= (theFlock.random.nextDouble()*2-1)/10+1;
				double lensquared = dx*dx+dy*dy;



				Integer estimated_dis =  Math.round(Math.round(lensquared));

				//System.out.print("lensquared is " + lensquared + "\n");
				//System.out.print("estimated_dis is " + estimated_dis + "\n");

				// The angle between the j_th and i_th boids, i_th is the origin.  
				double positionangle = getangle(loc.x, loc.y, other.loc.x, other.loc.y);

				beta_ij =  Math.abs(positionangle - theta_i);

				if(beta_ij>Math.PI)
					beta_ij = Math.abs(beta_ij - 2*Math.PI);

				//System.out.print("positionangle is " + Math.toDegrees(positionangle) + "\n");
				//System.out.print("//////////////////////////beta is " + Math.toDegrees(beta_ij) + "\n");

				// In the reaction field 

				if ( beta_ij < Math.toRadians(150) || (Math.sqrt(lensquared) <= 4 && other.ispredator) ) {
					// predator
					if(other.ispredator) 
						distances_Predators.put(estimated_dis, i);
					else {
						//In front of the front of the i_th boid
						if ( beta_ij < Math.toRadians(90) ) {
							distances_WithinRange.put(estimated_dis,i);
						}
						else
							distances_OutsideRange.put(estimated_dis,i);
					}
				}
			}
		}

		// Find maximum theFlock.num_neighborhood nearest neighbours in the front area because of the "front priority" 
		//---------------
		// Sort the neighbours according to their distances

		// Put the nearest predator to the top priority
		//-------------
		int count = 0;
		if (distances_Predators.size() != 0 && this.ispredator != true) {
			Vector<Integer> v0 = new Vector<Integer>(distances_Predators.keySet());		
			Integer minDis = Collections.min(v0);;
			//System.out.print(current  + "\n");
			Integer index = distances_Predators.get(minDis);
			nearestNeighbours.add(index);
			count++;
		}


		Vector<Integer> v = new Vector<Integer>(distances_WithinRange.keySet());

		Collections.sort(v);
		Iterator stepper =  v.iterator();


		while (stepper.hasNext()) {			
			if(count>=num_neighborhood) break;	
			Integer current = (Integer)stepper.next();
			//System.out.print(current  + "\n");
			Integer index = distances_WithinRange.get(current);
			nearestNeighbours.add(index);
			count++;
			//System.out.println(index);
		}

		if(nearestNeighbours.size()<num_neighborhood) {
			//System.out.print("Not enough boid in front of it\n");
			Vector<Integer> v2 = new Vector<Integer>(distances_OutsideRange.keySet());
			Iterator stepper2 =  v2.iterator();

			while (stepper2.hasNext()) {				
				if(count>=num_neighborhood) break;	 
				Integer current2 = (Integer)stepper2.next();
				//System.out.print(current2  + "\n");
				Integer index2 = distances_OutsideRange.get(current2);
				nearestNeighbours.add(index2);		
				count++;
				//System.out.println(index2 + "\n");
			}

		}

		return nearestNeighbours;
	}



	public double predatorDecisionMaker(Bag b, Continuous2D flockers, ArrayList<Integer> nearestNeighbours,  Flockers flock) 
	{

		//MersenneTwisterFast randomnum = new MersenneTwisterFast();
		double temp_alpha = 0;

		Double sum_x = 0.0;
		Double sum_y = 0.0;

		temp_alpha = (flock.random.nextDouble()*2-1)*Math.PI/10;
		if (b==null || b.numObjs == 0 || nearestNeighbours.size()==0) {
			//System.out.print(temp_alpha+"\n");
			return temp_alpha;
		}

		double r2 = flock.arenalength;
		double r1 = 1;


		int i=0;


		// angle of the i_th boid theta_i in polar coordinates  
		double beta_ij = 0; 
		double theta_i = this.getOrientation();

		accelation_flag = false;

		//System.out.print(b.numObjs + "\n");
		for(i=0;i<nearestNeighbours.size();i++)
		{

			//Class myClass = b.objs[i].getClass();
			//System.out.print(myClass.getName() + "\n");

			catched = 0;
			
			int index = nearestNeighbours.get(i);
			Flocker temp_other = (Flocker)(b.objs[index]);
			Flocker other =  (Flocker)(b.objs[index]);
			if(Lock_ID!=-1) {
				if(temp_other.flockID == Lock_ID);
					other =  (Flocker)(b.objs[index]);
				//System.out.println("Lock_ID is " + Lock_ID);
			}
			
			


			double dx = flockers.tdx(loc.x,other.loc.x);
			double dy = flockers.tdy(loc.y,other.loc.y);
			double lensquared = dx*dx+dy*dy;			

			//double theta_j = other.getOrientation();
			// The angle between the j_th and i_th boids, i_th is the origin.  
			double positionangle = getangle(loc.x, loc.y, other.loc.x, other.loc.y);

			beta_ij =  positionangle - theta_i;	


			// In the attraction area
			if (lensquared <= r1 * r1 && !other.ispredator && !other.dead ) {
				catched =  1;
				int catched_num = flock.catched_table.get(other.flockID) + 1; 
				other.dead=true;
				Lock_ID = -1;
				//other.body_length = 0;
				//flockers.remove(other);
				// Move the prey into the blind area				
				//Double2D PreyNewloc = new Double2D(loc.x - dx, loc.y - dy);
				//flock.flockers.setObjectLocation(other, PreyNewloc); 

				flock.catched_table.put(other.flockID, catched_num);
				//System.out.print("Catched a fish. flockID is " + other.flockID +"\n");
				//System.out.print("Catched a fish. other.repulsion_distance is " + other.repulsion_distance +"\n"); 

			}
			else {// Attraction
				if (lensquared <= r2 * r2 && !other.ispredator && !other.dead)
				{
					if (Lock_ID==-1)
						Lock_ID = other.flockID;
					
					accelation_flag = true;
					speed = flock.base_speed*1.2;
					return  beta_ij;
				}
				else 
				{
					//if (lensquared >= r3 * r3)
					//temp_alpha = beta_ij;
					temp_alpha = (flock.random.nextDouble()*2-1)*Math.PI/5;	
					speed = flock.base_speed;
					Lock_ID = -1;
				}
			}

			if(temp_alpha<0) 
				temp_alpha += Math.PI*2;

			sum_x += Math.cos(temp_alpha);
			sum_y += Math.sin(temp_alpha);


		}


		Double sumalpha = Math.atan2(sum_y, sum_x);

		if(Math.abs(sumalpha)>(Math.PI/18)*0.8) {
			if(sumalpha>0)
				sumalpha = (Math.PI/18)*0.8;
			else
				sumalpha = -(Math.PI/18)*0.8;
		}

		return sumalpha;
		//return 0;
	}

//////////////////////////////////////
	
	public ArrayList<Double> RuleCalculation(ArrayList<Integer> inputs, ArrayList<Double> rule) {
		//Integer TopologicalRule[] = {3, 3, 3, 3, 3};
		
/*		ArrayList<Integer> Temp_Rule = new ArrayList<Integer> ();
		Temp_Rule.add(2);
		Temp_Rule.add(1);
		Temp_Rule.add(3);
		Rule = Temp_Rule;*/
		
		ArrayList<Double> outputs = new ArrayList<Double>();
		
		Iterator<Integer> it = inputs.iterator();
		int i = 0;
		while (it.hasNext()) {  
			int input_i = (Integer) it.next();
			input_i = input_i>0? 1 : 0;
			//System.out.println("input is " + input_i);
			double output_i = input_i*rule.get(i);
			//System.out.println("output_i is " + output_i);
			outputs.add(output_i);
			i++;
		}
		
		
		return outputs;
	}
	
	
	public double preyRuleBasedDecision( ArrayList<Double>  Rule, Bag b, Continuous2D flockers, ArrayList<Integer> nearestNeighbours, Flockers flock) 
	{ 
		
		
		Double temp_alpha=0.0;

		Double sum_x = 0.0;
		Double sum_y = 0.0;

		double swerve_threshold = 4;				// The raidus for swerve
		double turn_away_threshold = 32;
		double swerve_angle = Math.PI/6;

		Double lensquared = 0.0;

		temp_alpha =  (flock.random.nextDouble()*2-1)*Math.PI/10;
		if (b==null || b.numObjs == 0 || nearestNeighbours.size()==0) {
			return temp_alpha;
		}

		Double theta_i = this.getOrientation();

		ArrayList<Double> beta_ij_List = new ArrayList<Double> ();
		ArrayList<Double> theta_j_List = new ArrayList<Double> ();
		ArrayList<Double> positionangle_List = new ArrayList<Double> ();

		ArrayList<Integer> inputs = new ArrayList<Integer> ();	

		int i=0;

		// Initialise the inputs
		//-----------------------
		for(i=0; i<num_neighborhood; i++)
		{
			theta_j_List.add(0.0);
			positionangle_List.add(0.0);
			beta_ij_List.add(0.0);
			inputs.add(0);
		}

		// Calculate the inputs
		//-----------------------		
		for(i=0;i<nearestNeighbours.size();i++)
		{
			int index = nearestNeighbours.get(i);
			Flocker other = (Flocker)(b.objs[index]);

			Double dx = flockers.tdx(loc.x,other.loc.x);
			Double dy = flockers.tdy(loc.y,other.loc.y);
			lensquared = dx*dx+dy*dy;	


			Double beta_ij=0.0;
			// For repulsion angle calculation, it should be in the range of [-PI,PI] in order to know which vector is in front
			Double vector_ij_x = other.loc.x - loc.x;
			Double vector_ij_y = other.loc.y - loc.y;				
			beta_ij = Math.atan2(vector_ij_y, vector_ij_x) - Math.atan2(lastd.y, lastd.x);


			// Escape when there is a predator
			// Because the inputs of other individuals 
			//(preys and predators) have been sorted according to distance, so the first predator is the closest.
			//----------------------------
			if (other.ispredator)  {
				if(Math.sqrt(lensquared) <= swerve_threshold) {	
					if(beta_ij > 0)
						temp_alpha = beta_ij + Math.PI + swerve_angle;
					else
						temp_alpha = beta_ij - Math.PI - swerve_angle;
					return temp_alpha;

				}
				else {
					if(Math.sqrt(lensquared) <= turn_away_threshold) {	
						temp_alpha = beta_ij + Math.PI;		
						return temp_alpha;
					}
				}
			}

			else {
				//System.out.print("***** beta_ij_List size is" + beta_ij_List.size() + " ************\n");	
				beta_ij_List.set(i, beta_ij);

				// getOrientation returns [0 2pi]
				Double theta_j = other.getOrientation();
				theta_j_List.set(i, theta_j);

				// getangle returns [0 2pi]
				Double positionangle = getangle(loc.x, loc.y, other.loc.x, other.loc.y);
				positionangle_List.set(i, positionangle);

				// Aha! Silly me. We don't need normalization, since we are using threshold activation function. See
				// Generating Neural Networks With Genetic Algorithms Using a Marker Based Encoding (1995)
				//Double normalized_len = -100 + 200 * (lensquared - 4)/(900 - 4); // Normalized the inputs to [-100,100]
				//lensquared += flock.random.nextGaussian()*200;
				Integer estimated_dis = Math.round(Math.round(lensquared));				
				inputs.set(i, estimated_dis);	
			}
		}




		ArrayList<Double> ANNOutput = RuleCalculation(inputs, Rule);

		if(ANNOutput==null) {
			temp_alpha = 0.0;
			return temp_alpha;
		}
		else
		{
			System.out.println("nearestNeighbours size: " + nearestNeighbours.size());
			for(i=0; i<nearestNeighbours.size(); i++) {
				

				double index = ANNOutput.get(i);
				int intIndex = (int) index;
				if (repulsion_distance<0) {
					intIndex = 3;
				}

				Double temp_repulsion_angle = 0.0;	

				if( Math.abs(beta_ij_List.get(i) + Math.PI/2) <= Math.abs(beta_ij_List.get(i) - Math.PI/2) ) {
					temp_repulsion_angle = beta_ij_List.get(i) + Math.PI/2;
				}
				else {
					temp_repulsion_angle = beta_ij_List.get(i) - Math.PI/2;
				}	

				//System.out.println("inputs.get(i) is " + inputs.get(i));
				//System.out.println("repulsion_distance is " + repulsion_distance);
				if( inputs.get(i)  <= 16  || inputs.get(i)  < repulsion_distance * repulsion_distance) {
					if(Math.abs(temp_repulsion_angle)>Math.PI/18) {
						if(temp_repulsion_angle>0)
							temp_repulsion_angle = Math.PI/18;
						else
							temp_repulsion_angle = -Math.PI/18;
					}
					temp_alpha = temp_repulsion_angle;
				}
				else {
					switch (intIndex) {
					case 0: temp_alpha = 0.0;  break; 															// do noting 	
					case 1: temp_alpha = (flock.random.nextDouble()*2-1)*Math.PI/10;  break; 					// random walk
					case 2: temp_alpha = theta_j_List.get(i) - theta_i; break;	   								// in parallel 
					case 3: temp_alpha = positionangle_List.get(i) - theta_i;  break;    						// attraction
					default : temp_alpha = (flock.random.nextDouble()*2-1)*Math.PI/10; break;					// random walk
					}
					if(temp_alpha<0) 
						temp_alpha += Math.PI*2;
				}

					sum_x += Math.cos(temp_alpha);
					sum_y += Math.sin(temp_alpha);
				

			}
		}


		Double sumalpha = Math.atan2(sum_y, sum_x);

		if(Math.abs(sumalpha)>Math.PI/18) {
			if(sumalpha>0)
				sumalpha = Math.PI/18;
			else
				sumalpha = -Math.PI/18;
		}


		//System.out.print("***** alpha_ij is" + alpha_ij + " ************\n");		
		return sumalpha;		
	}
	
//////////////////////////////////////




	public void step(SimState state)
	{        
		final Flockers flock = (Flockers)state;
		loc = flock.flockers.getObjectLocation(this);


		//System.out.print("number of neighbour " + theFlock.num_neighborhood + "\n");

		if (dead) {
			return;
		}

		double alpha = 0;
		//double speed = flock.jump;
		//speed = Samplers.sampleGamma(4, 1/3.3)*flock.jump;


		double sigma = 0.0;
		boolean move_flag = true;
		Double2D OverlapAvoidance = new Double2D();

		if (ispredator) {
			pause_time++;
			// Pause for 2000 time steps
			if(pause_time>1000) {
				catched = 0;

				if (accelation_flag==true)
					energy--;
				else energy = 0;

				// randomly reinitialise the predator after 6000 steps without catching anything
				if(energy<-10*30) {
					energy = 0;
					loc = new Double2D(flock.random.nextDouble()*flock.width, flock.random.nextDouble()*flock.height);
					flock.flockers.setObjectLocation(this, loc);
					lastd = new Double2D(flock.random.nextGaussian(), flock.random.nextGaussian());
					// Relocation prey							
					Bag b_prey = flockers.getAllObjects();
					RelocatePrey( b_prey,  flockers, flock);
					pause_time = 0;					
				} else {
					num_neighborhood = 10;
					Bag b_predator = flockers.getObjectsWithinDistance(loc, 160, true);
					ArrayList<Integer> nearestNeighbours_predator = getNearestNeighbours(b_predator);
					alpha = predatorDecisionMaker(b_predator, flock.flockers, nearestNeighbours_predator, flock);

					//Predator_maximum_catch = 2;
					// Successful						
					if(catched==1) {
						energy = 0;
						Num_catched_prey += catched;
						//System.out.println("Predator_maximum_catch: " + Predator_maximum_catch);
						if(Num_catched_prey >= Predator_maximum_catch)
						{
							loc = new Double2D(flock.random.nextDouble()*flock.width, flock.random.nextDouble()*flock.height);
							flock.flockers.setObjectLocation(this, loc);
							lastd = new Double2D(flock.random.nextGaussian(), flock.random.nextGaussian());
							Num_catched_prey = 0;

							// Relocation prey							
							Bag b_prey = flockers.getAllObjects();
							RelocatePrey( b_prey,  flockers, flock);
							pause_time = 0;

						}
						else
							pause_time = 0;

						//else
						//System.out.println("Total catched: " + Num_catched_prey);
						// pause for 500-x time steps
						//pause_time = 300;
						//loc = new Double2D(loc.x + (1 + flock.random.nextDouble())*160, loc.y + (1 + flock.random.nextDouble())*160);
						//loc = new Double2D(flock.random.nextDouble()*flock.width, flock.random.nextDouble()*flock.height);
						//flock.flockers.setObjectLocation(this, loc);
						//lastd = new Double2D(flock.random.nextGaussian(), flock.random.nextGaussian());

					}
				}
				// Noise
				sigma = (flock.random.nextDouble()*2-1)*Math.PI/36;
			}
			else
				move_flag = false;

		}
		else {

			//Set num_neighborhood to 4 temporarily to enforce overlap avoidance. 	
			//-------------------
			num_neighborhood = 4;
			Bag b_prey = flockers.getObjectsWithinDistance(loc, 10, true);
			ArrayList<Integer> nearestNeighbours_prey = getNearestNeighbours(b_prey);
			OverlapAvoidance = EnforceNonPenetrationConstraint(b_prey,flock.flockers);
			if(OverlapAvoidance.x !=0 || OverlapAvoidance.y !=0) {
				loc = new Double2D(flock.flockers.stx(loc.x + OverlapAvoidance.x), flock.flockers.sty(loc.y + OverlapAvoidance.y));
				flock.flockers.setObjectLocation(this, loc);
			}


			repulsion_distance = flock.repulsion_distance_array.get(flockID);
			//			System.out.print("Catched a fish. Hashcode is " + this.hashCode() +"\n");
			//			System.out.print("Catched a fish. flockID is " + this.flockID +"\n");
			//			System.out.print("Catched a fish. other.repulsion_distance is " + this.repulsion_distance +"\n"); 
			num_neighborhood = flock.num_neighborhood_array.get(flockID);
			// obtain speed from speed_array which is from the gamma distribution 

			if(num_neighborhood!=0 && this.dead!=true) {				
				b_prey = flockers.getObjectsWithinDistance(loc, 15, true);
				nearestNeighbours_prey = getNearestNeighbours(b_prey);
				ArrayList<Double> Rule = flock.Rule_array.get(flockID);
				alpha = preyRuleBasedDecision(Rule, b_prey,flock.flockers, nearestNeighbours_prey, flock);
			}
			else {
				alpha = (flock.random.nextDouble()*2-1)*Math.PI/36;
			}

			sigma = (flock.random.nextDouble()*2-1)*Math.PI/36;


			life++;
			//System.out.println(life);

			ArrayList<Integer> previouslife = new ArrayList<Integer>();
			// Haven't been caught once
			if(flock.catched_table.get(this.flockID)==0) {
				previouslife.add(life);
				flock.score_table.put(this.flockID, previouslife);
			}
			else
			{
				// Only store the minimum time step
				// Only update it when it is caught 
				if(catched==1) {
					previouslife = flock.score_table.get(this.flockID);
					int current_life= life - previouslife.get(previouslife.size()-1);
					previouslife.add(current_life);
					flock.score_table.put(this.flockID, previouslife);
				}

			}

		}



		double orientation = getOrientation();	

		if(move_flag==true) {
			double next_direction = orientation  +  sigma + alpha;

			double dx =  Math.cos(next_direction);
			double dy =  Math.sin(next_direction);

			dx = dx * speed ;
			dy = dy * speed ;

			lastd = new Double2D(dx,dy);
			loc = new Double2D(flock.flockers.stx(loc.x + dx), flock.flockers.sty(loc.y + dy));
			flock.flockers.setObjectLocation(this, loc);
		}


	}

}
