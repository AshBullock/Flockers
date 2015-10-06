/*
  Copyright 2009 by Shan He and the University of Birmingham
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package boidcoevolution;
import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;
import javax.swing.*;
import java.awt.*;
import sim.portrayal.simple.*;
import sim.portrayal.SimplePortrayal2D;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;



import java.util.ArrayList;
import java.util.Hashtable;

import org.jgap.impl.*;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;


public class FlockersWithUI_FinalGenObser extends GUIState
{
	public Display2D display;
	public JFrame displayFrame;

	ArrayList<ArrayList<Double>> Rule_array = new ArrayList<ArrayList<Double>>(); 
	ArrayList<Integer> num_neighborhood_array = new ArrayList<Integer>();
	ArrayList<Integer> repulsion_array = new ArrayList<Integer>();
	
	public static Hashtable<String,Integer> parameters = new Hashtable<String,Integer>();
	


	public static void main(String[] args)
	{
		
		parameters.put("PopSize", 200);
		parameters.put("ArenaSize", 400);
		FlockersWithUI_FinalGenObser flocking = new FlockersWithUI_FinalGenObser();  // randomizes by currentTimeMillis        
		Console c = new Console(flocking);
		c.setVisible(true);
	}

	public Object getSimulationInspectedObject() { return state; }  // non-volatile

	ContinuousPortrayal2D flockersPortrayal = new ContinuousPortrayal2D();

	public FlockersWithUI_FinalGenObser()
	{
		super(new Flockers(System.currentTimeMillis(), parameters));
	}

	public FlockersWithUI_FinalGenObser(SimState state) 
	{
		super(state);
	}

	public static String getName() { return "Flockers"; }

	public void start()
	{


		
		Flockers flock = (Flockers)state;




		PopulationFileIO PopLoder = new PopulationFileIO(); 
		Population pop = PopLoder.LoadPopulation("\\1_PopSize100_ArenaSize600_Evolution\\PopulationRecords40", 200);
		
		PopLoder.readRule(pop);

		flock.Rule_array = PopLoder.Rule_array;
		flock.num_neighborhood_array = PopLoder.num_neighborhood_array;
		flock.repulsion_distance_array = PopLoder.repulsion_array;
		flock.Predator_maximum_catch = 20;
		
		super.start();
		setupPortrayals();

	}

	public void load(SimState state)
	{
		super.load(state);
		setupPortrayals();
	}

	public void setupPortrayals()
	{
		Flockers flock = (Flockers)state;
		flock.width = 1000; 
		flock.height = 1000; 
		flock.num_predator = 1;


		// obstacle portrayal needs no setup
		flockersPortrayal.setField(flock.flockers);

		// make the flockers random colors and four times their normal size (prettier)
		for(int x=0;x<flock.flockers.allObjects.numObjs;x++) {
			Flocker f = (Flocker)(flock.flockers.allObjects.objs[x]);
			int flocksize = flock.Prey_BodyLength;
			if(f.ispredator==true) flocksize = flock.Predator_BodyLength;

			flockersPortrayal.setPortrayalForObject(flock.flockers.allObjects.objs[x],
					new OrientedPortrayal2D(new SimplePortrayal2D(),0,flocksize,
							new Color(128 + state.random.nextInt(128),
									128 + state.random.nextInt(128),
									128 + state.random.nextInt(128)),
									OrientedPortrayal2D.SHAPE_COMPASS));
		}

		// update the size of the display appropriately.
		double w = flock.flockers.getWidth();
		double h = flock.flockers.getHeight();
		if (w == h)
		{ display.insideDisplay.width = display.insideDisplay.height = 750; }
		else if (w > h)
		{ display.insideDisplay.width = 750; display.insideDisplay.height = 750 * (h/w); }
		else if (w < h)
		{ display.insideDisplay.height = 750; display.insideDisplay.width = 750 * (w/h); }

		// reschedule the displayer
		display.reset();

		// redraw the display
		display.repaint();
	}

	public void init(Controller c)
	{
		super.init(c);

		// make the displayer
		display = new Display2D(750,750,this,1);
		display.setBackdrop(Color.black);


		displayFrame = display.createFrame();
		displayFrame.setTitle("Flockers");
		c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
		displayFrame.setVisible(true);
		display.attach( flockersPortrayal, "Behold the Flock!" );
	}

	public void quit()
	{
		super.quit();

		if (displayFrame!=null) displayFrame.dispose();
		displayFrame = null;
		display = null;
	}


	
	
	

}
