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

public class Prey extends Flocker {
	
	public double bodyLength;


	

	public Double speed = 0.0;


	






	
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

			if (other != this && other.isPredator(other) != true && other.dead != true)
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


		
	
	
	public double preyRuleBasedDecision( ArrayList<Double>  rule, Bag b, Continuous2D flockers, ArrayList<Integer> nearestNeighbours, Flockers flock) 
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
		
		//array list containing estimated distances of the nearest neighbours, predators being first
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
			if (other.isPredator(other))  {
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




		ArrayList<Integer> ANNOutput = RuleCalculation(inputs, rule);

		if(ANNOutput==null) {
			//System.out.println("annoOutput is null");
			temp_alpha = 0.0;
			return temp_alpha;
		}
		else
		{
			//System.out.println("nearestNeighbours size: " + nearestNeighbours.size());
			for(i=0; i<nearestNeighbours.size(); i++) {
				

				int index = ANNOutput.get(i);
				if (repulsion_distance<0) {
					index = 3;
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
					switch (index) {
					case 0: temp_alpha = 0.0;  break; 															// do nothing 	
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

	
	public void step(SimState state)
	{        
		//System.out.println("Speed: " + speed);
		//System.out.println("Size: " + size);
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


			repulsion_distance = flock.repulsion_distance_array.get(flockID).intValue();
			//			System.out.print("Catched a fish. Hashcode is " + this.hashCode() +"\n");
			//			System.out.print("Catched a fish. flockID is " + this.flockID +"\n");
			//			System.out.print("Catched a fish. other.repulsion_distance is " + this.repulsion_distance +"\n"); 
			num_neighborhood = flock.num_neighborhood_array.get(flockID).intValue();
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
				flock.prey_score_table.put(this.flockID, previouslife);
			}
			else
			{
				// Only store the minimum time step
				// Only update it when it is caught 
				if(catched==1) {
					

					previouslife = flock.prey_score_table.get(this.flockID);
					int current_life= life - previouslife.get(previouslife.size()-1);
					previouslife.add(current_life);
					flock.prey_score_table.put(this.flockID, previouslife);
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
	
	@Override
	public double getBodyLength()
	{
		
		return bodyLength;
	}

	@Override
	public double getSpeed()
	{	//System.out.println("Overriden speed taken!");
		return speed;
	}
	
	public ArrayList<Integer> RuleCalculation(ArrayList<Integer> inputs, ArrayList<Double> rule) {
		//Integer TopologicalRule[] = {3, 3, 3, 3, 3};
		
/*		ArrayList<Integer> Temp_Rule = new ArrayList<Integer> ();
		Temp_Rule.add(2);
		Temp_Rule.add(1);
		Temp_Rule.add(3);
		Rule = Temp_Rule;*/
		
		ArrayList<Integer> outputs = new ArrayList<Integer>();
		
		Iterator<Integer> it = inputs.iterator();
		int i = 0;
		//for each input (the distance from the nearest neighbour)
		while (it.hasNext()) {  
			int input_i = (Integer) it.next();
			//if input_i is greater than 0 then = 1 else = 0 
			input_i = input_i>0? 1 : 0;
			//System.out.println("input is " + input_i);
			
			
			int output_i = (int) (input_i*rule.get(i));
			//System.out.println("output_i is " + output_i);
			outputs.add(output_i);
			i++;
		}
		
		
		return outputs;
	}
	
}
