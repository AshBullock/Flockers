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
	public int repulsion_distance;
	public int flockID;
	public int life;
	public int catched = 0;
	public int num_neighborhood;
	public int Predator_maximum_catch;
	public boolean dead = false;
	public int pause_time = 0; 
	public int Lock_ID = -1;
	public double size;


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




	

	

	/*
	 * Takes a bag b of prey objects, these are all the objects within 10 of the current prey.
	 * If empty return empty array
	 * 
	 * If fish is not current prey and is alive, find the distance and angle of that fish
	 * 
	 * returns a array of indexes with the closest fish first, prioritising predators
	 */
	public ArrayList<Integer> getNearestNeighbours(Bag b)
	{
		//System.out.println("BagSize: "+ b.size());
		ArrayList<Integer> nearestNeighbours = new  ArrayList <Integer>();

		Hashtable<Integer,Integer> distances_Predators = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> distances_WithinRange = new Hashtable<Integer,Integer>();

		Hashtable<Integer,Integer> distances_OutsideRange = new Hashtable<Integer,Integer>();

		//if the bag is empty or no bag is present then return empty array of neighbours
		if (b==null || b.numObjs == 0) return nearestNeighbours;

		// angle of the i_th boid theta_i in polar coordinates  
		double beta_ij = 0; 
		double theta_i = this.getOrientation();
		//System.out.print("new member now \n");
		//System.out.print("theta_i is " + Math.toDegrees(theta_i) + "\n");

		int i=0;
		//System.out.print(b.numObjs + "\n");
		//for each object in the bag
		for(i=0;i<b.numObjs;i++)
		{

			//Class myClass = b.objs[i].getClass();
			//System.out.print(myClass.getName() + "\n");
			
			Flocker other = (Flocker)(b.objs[i]);
			
			//if it does not equal this flocker and is not dead
			if (other != this && other.dead==false)
			{	
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				// Add noise to distance calculation
				//dx *= (theFlock.random.nextDouble()*2-1)/10+1;
				//dy *= (theFlock.random.nextDouble()*2-1)/10+1;
				
				//caculate estimated distance from that flocker
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

				if ( beta_ij < Math.toRadians(150) || (Math.sqrt(lensquared) <= 4 && other.isPredator(other)) ) {
					// predator
					if(other.isPredator(other)) 
						distances_Predators.put(estimated_dis, i);
					else {
					//redundant?
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
		if (distances_Predators.size() != 0 && this.isPredator(this) != true) { 
		//if there is a predator and I am not a predator
			Vector<Integer> v0 = new Vector<Integer>(distances_Predators.keySet());		
			Integer minDis = Collections.min(v0);; //find the closest predator of every predator
			//System.out.print(current  + "\n");
			Integer index = distances_Predators.get(minDis); //get the index of that predator in the bag 
			nearestNeighbours.add(index);      				//add to nearest neighbours
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
	
	
	
	
	
	
	public ArrayList<Integer> getSlowestNeighbours(Bag b)
	{
		//System.out.println("BagSize: "+ b.size());
		ArrayList<Integer> slowestNeighbours = new  ArrayList <Integer>();

		Hashtable<Integer,Integer> distances_Predators = new Hashtable<Integer,Integer>();
		Hashtable<Double,Integer> speeds_WithinRange = new Hashtable<Double,Integer>();

		Hashtable<Double,Integer> speeds_OutsideRange = new Hashtable<Double,Integer>();

		//if the bag is empty or no bag is present then return empty array of neighbours
		if (b==null || b.numObjs == 0) return slowestNeighbours;

		// angle of the i_th boid theta_i in polar coordinates  
		double beta_ij = 0; 
		double theta_i = this.getOrientation();
		//System.out.print("new member now \n");
		//System.out.print("theta_i is " + Math.toDegrees(theta_i) + "\n");

		int i=0;
		//System.out.print(b.numObjs + "\n");
		//for each object in the bag
		for(i=0;i<b.numObjs;i++)
		{

			//Class myClass = b.objs[i].getClass();
			//System.out.print(myClass.getName() + "\n");
			
			Flocker other = (Flocker)(b.objs[i]);
			
			//if it does not equal this flocker and is not dead
			if (other != this && other.dead==false)
			{	
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
			
				double lensquared = dx*dx+dy*dy;
				
				Double speed = other.getSpeed();


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

				if ( beta_ij < Math.toRadians(150) || (Math.sqrt(lensquared) <= 4 && other.isPredator(other)) ) {
					// predator
					if(other.isPredator(other)) 
						distances_Predators.put(estimated_dis, i);
					else {
					//redundant?
						//In front of the front of the i_th boid
						if ( beta_ij < Math.toRadians(90) ) {
							speeds_WithinRange.put(speed,i);
						}
						else
							speeds_OutsideRange.put(speed,i);
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



		Vector<Double> v = new Vector<Double>(speeds_WithinRange.keySet());

		Collections.sort(v);
		Iterator stepper =  v.iterator();


		while (stepper.hasNext()) {			
			if(count>=num_neighborhood) break;	
			Double current = (Double)stepper.next();
			//System.out.print(current  + "\n");
			Integer index = speeds_WithinRange.get(current);
			slowestNeighbours.add(index);
			count++;
			//System.out.println(index);
		}

		if(slowestNeighbours.size()<num_neighborhood) {
			//System.out.print("Not enough boid in front of it\n");
			Vector<Double> v2 = new Vector<Double>(speeds_OutsideRange.keySet());
			Iterator stepper2 =  v2.iterator();

			while (stepper2.hasNext()) {				
				if(count>=num_neighborhood) break;	 
				Double current2 = (Double)stepper2.next();
				//System.out.print(current2  + "\n");
				Integer index2 = speeds_OutsideRange.get(current2);
				slowestNeighbours.add(index2);		
				count++;
				//System.out.println(index2 + "\n");
			}

		}

		return slowestNeighbours;
	}




//////////////////////////////////////
	
	
	
	public void step(SimState state)
	{        
		
	}
	
	/**
	 * Returns whether a fish is a predator or not 
	 * @param fish The fish in question
	 * @return true or false
	 * @author Ashley Bullock
	 */
	public boolean isPredator(Flocker fish)
	{
		//System.out.println("Is: " + (fish instanceof Predator));
		return (fish instanceof Predator);
	
	}
	
	//gets overrided
	public double getBodyLength()
	{
		
		return 1;
	}


	public double getSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
