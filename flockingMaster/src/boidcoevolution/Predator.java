package boidcoevolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import ec.util.MersenneTwisterFast;
import boidcoevolution.Flockers.PreyType;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public class Predator extends Flocker{
	
	public final int CHASE_SPEED_MOD = 3;
	public double bodyLength;
	public Integer maximumCatch=30;
	public int Num_catched_prey=0;
	public boolean accelation_flag = false;
	public int energy=0;	
	public Double speed = 0.0;
	public int Lock_ID = -1;
	public double predEnergy = 500;
	public int wait= 0;
	public int chasing= 0;
	
	public Predator(){//System.out.println("New predator created");}
	}
	

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

	
	@Override
	public void step(SimState state)
	{        
		
	
		
		//System.out.println("Predator step..");
		final Flockers flock = (Flockers)state;
		loc = flock.flockers.getObjectLocation(this);
		//System.out.println(this.flockID);
		
		
		//need to fix
		updateEnergy(this, flock);
		

		//System.out.print("number of neighbour " + theFlock.num_neighborhood + "\n");

		if (dead) {
			return;
		}

		double alpha = 0;
		

		double sigma = 0.0;
		boolean move_flag = true;
		Double2D OverlapAvoidance = new Double2D();

		
		pause_time=1001;
		// Pause for 2000 time steps
		if(pause_time>1000) 
		{
			catched = 0;

			if (accelation_flag==true) 
				energy--;
			else 
				energy = 0;

				// randomly reinitialise the predator after 6000 steps without catching anything
				if(energy<-10*3000000) 
				{
					//System.out.println("Resetting....");
					energy = 0;
					loc = new Double2D(flock.random.nextDouble()*flock.width, flock.random.nextDouble()*flock.height);
					flock.flockers.setObjectLocation(this, loc);
					lastd = new Double2D(flock.random.nextGaussian(), flock.random.nextGaussian());
					// Relocation prey							
					Bag b_prey = flockers.getAllObjects();
					RelocatePrey( b_prey,  flockers, flock);
					pause_time = 0;					
				} 
				else 
				{
					num_neighborhood = 10;
					Bag b_predator = flockers.getObjectsWithinDistance(loc, 160, true);
					
					ArrayList<Integer> nearestNeighbours_predator = getNearestNeighbours(b_predator);
					//System.out.println("Pred ID:" + flockID);
					
					ArrayList<Double> rule = flock.Predator_Rule_array.get(flockID);
					ArrayList<Double> ruleOutput = RuleCalculation(nearestNeighbours_predator, rule);
					
					
					
					
					
					
					
					alpha = predatorMoveSelector(b_predator, flock.flockers, nearestNeighbours_predator, flock, ruleOutput);
					double speedMod = speed/0.7;
					//System.out.println("speedMod = " + speedMod);
					predEnergy = predEnergy - (1*speedMod);
					//System.out.println("PredEnergy =" + predEnergy);
				}
			// Noise
			sigma = (flock.random.nextDouble()*2-1)*Math.PI/36;			}
		else
		{
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

	public double predatorMoveSelector(Bag b, Continuous2D flockers, ArrayList<Integer> nearestNeighbours,  Flockers flock, ArrayList<Double> ruleOutput)
	{	
		
		Double sum_x = 0.0;
		Double sum_y = 0.0;
		// angle of the i_th boid theta_i in polar coordinates  
		double beta_ij = 0; 
		double theta_i = this.getOrientation();
		double chaseRange = ruleOutput.get(0);
		double moveModifier = ruleOutput.get(1);
		double groupPriority = ruleOutput.get(2);
		double tactic = ruleOutput.get(3);
		
		if(tactic >= 1.5)
			tactic = 2.0;
		else
			tactic = 1.0;
		double r2 = flock.arenalength;
		double r1 = 1;
		
		
		//reset wait and chasing variables if the type of prey has changed
		if(this.theFlock.changedPreyType)
		{
		
			this.theFlock.changedPreyType = false;
			wait = 0;
			chasing=0;
		}
		
		
	
		
		//1st: check if can eat a fish or should definitely chase a fish 
		for(int i=0;i<nearestNeighbours.size();i++)
		{
			
			catched = 0;
		
			int index = nearestNeighbours.get(i);
			Flocker temp_other = (Flocker)(b.objs[index]);
			Flocker other =  (Flocker)(b.objs[index]);
		
			//evaluate if should chase nearest or not
			if(Lock_ID!=-1) {
				if(temp_other.flockID == Lock_ID);
					other =  (Flocker)(b.objs[index]);
			}
		
			double dx = flockers.tdx(loc.x,other.loc.x);
			double dy = flockers.tdy(loc.y,other.loc.y);
			double lensquared = dx*dx+dy*dy;	
				
			// The angle between the j_th and i_th boids, i_th is the origin.  
			double positionangle = getangle(loc.x, loc.y, other.loc.x, other.loc.y);
			beta_ij =  positionangle - theta_i;	

			//caught a fish procedure
			// In the attraction area
			if (lensquared <= r1 * r1 && !other.isPredator(other) && !other.dead ) {
				catchedFish(other, flock);
			}
		
			else { // Attraction if the fish is close enough
					
				if (lensquared <= (((r2 * r2)/1000)*chaseRange*CHASE_SPEED_MOD) && !other.isPredator(other) && !other.dead)
				{
				//System.out.println("Chasing closest...");
				//System.out.println("Chase range: " + chaseRange);
				//System.out.println("lensquared:" +lensquared + "   ChaseRangeVal: " +(((r2 * r2)/100)*chaseRange));
					if (Lock_ID==-1)
						Lock_ID = other.flockID;
				
					accelation_flag = true;
					speed = flock.base_speed*1.2;
					return  beta_ij;
				}
			}
			//break the loop early if a fish that is not dead and is not a predator is not close enough
			//as list is ordered by closest anyway
			if(!other.isPredator(other) && !other.dead)
					break;
		}
		
		
		//2nd: fish is not in chase range so decide if wants to chase or not
	
	
		//Predator checks if it should attack or wait 
		double modifier=0.0;
		if(flock.preyType == PreyType.DYNAMIC )
		{
			 modifier = ruleOutput.get(4);
		}
		else if(flock.preyType == PreyType.SWARM)
		{
			modifier = ruleOutput.get(5);
		}
		
				
				
		//System.out.println("modifier = "+ modifier);
		MersenneTwisterFast ran = new MersenneTwisterFast();
		if(!ran.nextBoolean(modifier))
		{
			//wants to wait
			
			Double waitTime = ran.nextDouble();	
			//System.out.println((1.2-waitTime-modifier) + " > " + 0.0 + " ? ? ? ");
			//System.out.println("Wait time would be " + (Math.abs(waitTime - modifier))*100);
			if(0.7-waitTime-modifier >0.0)
				wait = (int) (wait + ((Math.abs(waitTime - modifier))*20)); 
		}
		else
			{
				//wants to chase
				//System.out.println("swarm modifier * 50 = " + (int) (modifier*100));
				chasing = (int) (chasing + modifier*100);
				//System.out.println("Chasinh = " + chasing);
			}
				
		
		
		
		if(chasing >0)
		{
			chasing -- ;
		}
		

		
		
		
		
		//has predator has decided to wait this time
		if(wait>0)
		{	
			//System.out.println("chasing = " + chasing);
			//System.out.println("Waiting");
					
			speed=0.0;
			wait--;
			return Math.atan2(sum_y, sum_x);
		}
			
		
		
		//else go into normal move selection
		else
		{
		
			//MersenneTwisterFast randomnum = new MersenneTwisterFast();
			double temp_alpha = 0;
			
		
			//if no fish nearby
			temp_alpha = (flock.random.nextDouble()*2-1)*Math.PI/10;
			if (b==null || b.numObjs == 0 || nearestNeighbours.size()==0) {
				//System.out.print(temp_alpha+"\n");
				//System.out.println("moveMod: " + moveModifier);
				double speedPercent = (double)moveModifier/10;
				//System.out.println("Speed modifier = " + speedPercent);
				speed = flock.base_speed * speedPercent;
				//System.out.println("speed = " + speed);
				return temp_alpha;
			}
	
			
	
			
	
	
			accelation_flag = false;
	
			//System.out.print(b.numObjs + "\n");
			//for each nearest neighbour
			for(int i=0;i<nearestNeighbours.size();i++)
			{
				catched = 0;

			
				
				int index = nearestNeighbours.get(i);
				Flocker temp_other = (Flocker)(b.objs[index]);
				Flocker other =  (Flocker)(b.objs[index]);
				
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				double lensquared = dx*dx+dy*dy;	
				
				if(isLargeGroup(other,b,groupPriority))
				{
					//System.out.println("Found a large enough group");
					//using anticipated location
					//System.out.println("tactic = " + tactic);
					if(tactic == 1.0)//if (lensquared >= r3 * r3)
					{
						//System.out.println("Using anticipated loc");
						//System.out.println("Chase range: " + chaseRange);
						//System.out.println("lensquared:" +lensquared + "   ChaseRangeVal: " +(((r2 * r2)/100)*chaseRange));
						Double orientation = other.getOrientation();
						double antX =  Math.cos(orientation);
						double antY =  Math.sin(orientation);
		
						antX = antX * other.getSpeed() ;
						antY = antY * other.getSpeed() ;
						//System.out.println("Speed = "+  antX + ", " + antY);
								
						Double2D antloc = new Double2D(flock.flockers.stx(loc.x + antX), flock.flockers.sty(loc.y + antY));
								
								
						double posAngle = getangle(loc.x, loc.y, antloc.x, antloc.y);
								
						//get angle needed to follow fish
						beta_ij =  posAngle - theta_i;	
								
						if (Lock_ID==-1)
							Lock_ID = other.flockID;
								
						accelation_flag = true;
						speed = flock.base_speed*1.2;
						return  beta_ij;
					}
							
					//prioritises inner fish
					else if(tactic == 2.0)
					{	
						//System.out.println("Using chase inner");
						Double distance = Math.sqrt(Math.pow(dx, 2) +Math.pow(dy,2));
						if (lensquared <= r2 * r2 && !other.isPredator(other) && !other.dead && (isInnerFish(other,b) || distance > 30 || distance < 20 ))
						{
										
										
							//System.out.println("Found inner ! chasing him now..");
							//System.out.println("Distance : " + Math.sqrt(Math.pow(dx, 2) +Math.pow(dy,2)));
							//System.out.println("Nearest Neigbour size : " + nearestNeighbours.size());
							if (Lock_ID==-1)
								Lock_ID = other.flockID;
										
							accelation_flag = true;
							speed = flock.base_speed*1.2;
							return  beta_ij;
						}		
					}
				}
						
				else
				{
					//use random
					//temp_alpha = beta_ij;
					//System.out.println("Using random");
					temp_alpha = (flock.random.nextDouble()*2-1)*Math.PI/5;					
					speed = flock.base_speed;
					Lock_ID = -1;
					
				}
				
			if(temp_alpha<0) 
				temp_alpha += Math.PI*2;

			sum_x += Math.cos(temp_alpha);
			sum_y += Math.sin(temp_alpha);

		}
		
	}
	//if havent found a new move yet
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
		
	

	
	
	
	public double predatorChaseSlowest(Bag b, Continuous2D flockers, ArrayList<Integer> nearestNeighbours,  Flockers flock) 
	{	
		nearestNeighbours = getSlowestNeighbours(b);
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
		//for each nearest neighbour
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

			//caught a fish procedure
			// In the attraction area
			if (lensquared <= r1 * r1 && !other.isPredator(other) && !other.dead ) {
				catchedFish(other, flock);
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
	

	
	
	
	
	
	

	private boolean isLargeGroup(Flocker other, Bag b, double groupPriority) {
		ArrayList<Integer> nearestNeighbours = other.getNearestNeighbours(b);
		double netDistance = 0;
		for(int i=0;i<nearestNeighbours.size();i++)
		{
			Flocker neighbour = (Flocker) b.get(i);
			double dx = flockers.tdx(other.loc.x,neighbour.loc.x);
			double dy = flockers.tdy(other.loc.y,neighbour.loc.y);
			netDistance += Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			
		}
		//System.out.println("net Distance of group = " + netDistance + "     will chase: " + 50*groupPriority);
		if(netDistance < 50*groupPriority)
			return true;
	
		else 
			return false;
	}
	


	
	
	
	
	private boolean isInnerFish(Flocker fish, Bag b) {
		ArrayList<Integer> nearestNeighbours = fish.getNearestNeighbours(b);
		
		boolean xleft =false;
		boolean xright = false;
		boolean yup = false;
		boolean ydown = false;
		
		for(int i=0;i<nearestNeighbours.size();i++)
		{
		
			Flocker neighbour = (Flocker) b.get(i);
			
			if(neighbour.isPredator(neighbour)==false)
					{

						if(fish.loc.x < neighbour.loc.x)
						{
							xleft = true;
						}
						if(fish.loc.x > neighbour.loc.x)
						{
							xright = true;
						}
						if(fish.loc.y < neighbour.loc.y)
						{
							yup = true;
						}
						if(fish.loc.y > neighbour.loc.x)
						{
							ydown = true;
						}
						
						if(xleft && xright && yup &&ydown  )
							return true;
					} 
		}
		return false;
			
	}

	private void updateEnergy(Predator predator, Flockers flock)
	{
		//System.out.println("Updating energy for " + predator.flockID);
		
		flock.pred_energy_table.put(predator.flockID, predator.predEnergy-1);
	}
	private void catchedFish(Flocker other, Flockers flock) {
		
		chasing = 0;
		Num_catched_prey += 1;
		int catched_num = flock.catched_table.get(other.flockID) + 1; 
		other.dead=true;
		Lock_ID = -1;
		//other.body_length = 0;
		//flockers.remove(other);
		// Move the prey into the blind area				
		//Double2D PreyNewloc = new Double2D(loc.x - dx, loc.y - dy);
		//flock.flockers.setObjectLocation(other, PreyNewloc); 
		flock.catched_table.put(other.flockID, catched_num);
		//System.out.println("added fish to catch table");
		
		flock.pred_catch_table.put(this.flockID, Num_catched_prey);
		
		
		energy = 0;
		predEnergy+=50;
		
		
		//System.out.println("Num_Catched prey = " + Num_catched_prey);
		//System.out.println("max = " + maximumCatch);
		//check if max catch has been reached
		if(getTotalCatchedPrey(flock) >= maximumCatch)
		{
			
			//System.out.println("Predator_maximum_catch: " + Predator_maximum_catch);
			
			
				//System.out.println("Maximum catch reached ! Resetting");
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
		}
		
		
	

	private int getTotalCatchedPrey(Flockers flock)
	{
		int count = 0;
		
		for (Enumeration e = flock.pred_catch_table.elements(); e.hasMoreElements();)
		{
	
			count += (int) e.nextElement();
		}
		return count; 
		
	} 

	
	@Override
	public double getBodyLength()
	{
		
		return bodyLength;
	}
	
	
	public ArrayList<Double> RuleCalculation(ArrayList<Integer> inputs, ArrayList<Double> rule) {
		// currently do not use inputs to affect rules

		return rule;
	}

}
