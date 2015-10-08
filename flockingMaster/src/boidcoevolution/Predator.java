package boidcoevolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public class Predator extends Flocker{
	
	public double bodyLength;
	public Integer maximumCatch;
	
	public int Num_catched_prey=0;
	
	

	public boolean accelation_flag = false;
	public int energy=0;	
	
	
	public Double speed = 0.0;
	
	public int Lock_ID = -1;
	


	public void RelocatePrey(Bag b_prey, Continuous2D flockers, Flockers flock)
	{
		Integer prey_box_width = flock.Prey_BodyLength*(int)(Math.sqrt(flock.num_preys));

		for(int i=0;i<b_prey.numObjs;i++)
		{
			Flocker obj = (Flocker)(b_prey.objs[i]);
			if (!obj.isPredator(obj))
			{
				
				obj.life = 0;
				obj.dead=false;
				Double2D pos = new Double2D((flock.random.nextDouble()*2-1)*prey_box_width+flockers.width/2, (flock.random.nextDouble()*2-1)*prey_box_width+flockers.height/2);
				flockers.setObjectLocation(obj, pos);
				obj.lastd = new Double2D(flock.random.nextGaussian(), flock.random.nextGaussian());
			}
		}

	}

	///

	



	

	
	
	@Override
	public void step(SimState state)
	{        
		//System.out.println("Predator step..");
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

		if (isPredator(this)) {
			pause_time=1001;
			// Pause for 2000 time steps
			if(pause_time>1000) {
				catched = 0;

				if (accelation_flag==true)
					energy--;
				else energy = 0;

				// randomly reinitialise the predator after 6000 steps without catching anything
				if(energy<-10*3000000) {
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

	
	
	
	
	public double predatorDecisionMaker(Bag b, Continuous2D flockers, ArrayList<Integer> nearestNeighbours,  Flockers flock) 
	{
		
		
		/*
		 * Possible actions
		 * -Do nothing
		 * -random walk
		 * -follow predator
		 * 
		 * -chase slowest
		 * -move towards center
		 * -chase largest
		 * -chase closest
		 * 
		 */

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
			if (lensquared <= r1 * r1 && !other.isPredator(other) && !other.dead ) {
				catched =  1;
				int catched_num = flock.catched_table.get(other.flockID) + 1; 
				int predCatchNum = flock.pred_catch_table.get(this.flockID)+1;
				other.dead=true;
				Lock_ID = -1;
				//other.body_length = 0;
				//flockers.remove(other);
				// Move the prey into the blind area				
				//Double2D PreyNewloc = new Double2D(loc.x - dx, loc.y - dy);
				//flock.flockers.setObjectLocation(other, PreyNewloc); 

				flock.catched_table.put(other.flockID, catched_num);
				flock.pred_catch_table.put(this.flockID,predCatchNum);
				//System.out.print("Catched a fish. flockID is " + other.flockID +"\n");
				//System.out.print("Catched a fish. other.repulsion_distance is " + other.repulsion_distance +"\n"); 

			}
			else {// Attraction
				if (lensquared <= r2 * r2 && !other.isPredator(other) && !other.dead)
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
	
	@Override
	public double getBodyLength()
	{
		
		return bodyLength;
	}
	

}
