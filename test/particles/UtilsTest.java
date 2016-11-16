package particles;

import static org.junit.Assert.*;

import javax.vecmath.Point3d;

import org.junit.Test;

public class UtilsTest {
  
  private static final double EPS = 1e-7;

  @Test
  public void test() {
    Point3d l1 = new Point3d(1, 1, 0);
    Point3d l2 = new Point3d(-1, -1, 0);
    Point3d p = new Point3d(1, 0, 0);
    
    assertEquals("point line dist", Math.sqrt(2) / 2, Utils.pointLineDistance(p, l1, l2), EPS);
  }

}
