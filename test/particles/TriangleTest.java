package particles;

import static org.junit.Assert.*;

import javax.vecmath.Point3d;

import org.junit.Test;

public class TriangleTest {
  
  private static final double EPS = 1e-7;
  private Vertex v0 = new Vertex(new Point3d(0, 0, 1));
  private Vertex v1 = new Vertex(new Point3d(0, 1, 0));
  private Vertex v2 = new Vertex(new Point3d(1, 0, 0));
  private Triangle triangle = new Triangle(v0, v1, v2);
  
  @Test
  public void testArea() {
    assertEquals(Math.sqrt(3) / 2, triangle.area(), EPS);
  }
  
  @Test
  public void testIntersectRay() {
    Point3d p0 = new Point3d(0, 0, 0);
    Point3d p1 = new Point3d(1, 1, 1);
    Point3d intersection = triangle.intersectRay(p0, p1);
    assertEquals(new Point3d(1.0/3, 1.0/3, 1.0/3), intersection);
    
    p1 = new Point3d(1, 1, 0);
    intersection = triangle.intersectRay(p0, p1);
    assertEquals(new Point3d(0.5, 0.5, 0), intersection);
    
  }

}

