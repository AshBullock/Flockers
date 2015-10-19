package TestSuite;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import boidcoevolution.Flocker;
import boidcoevolution.Predator;
import boidcoevolution.Prey;

public class FlockerTest {
  @Test
  public void isPredatorTest() {
	  Flocker preyFish = new Prey();
	  Flocker predatorFish = new Predator();
	  Boolean ispredator = preyFish.isPredator(preyFish);
	  Assert.assertTrue(!ispredator);
	  ispredator = predatorFish.isPredator(predatorFish);
	  Assert.assertTrue(ispredator); 
  }
  
  
  @BeforeMethod
  public void beforeMethod() {
	 
  }

}
