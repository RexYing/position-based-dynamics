package particles;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * The face of a triangular mesh. See Mesh.java for details.
 *
 * @author Eston Schweickart, February 2014
 */
public class Triangle {

  /** The first vertex of this triangle. */
  public Vertex v0;

  /** The second vertex of this triangle. */
  public Vertex v1;

  /** The third vertex of this triangle. */
  public Vertex v2;
  
  private static final double EPS = 1e-7;

  /** Constructs a Triangle object from 3 vertices. */
  public Triangle(Vertex v0, Vertex v1, Vertex v2) {
    this.v0 = v0;
    this.v1 = v1;
    this.v2 = v2;
  }

  /** Computes the unit-length normal associated with this triangle. */
  public Vector3d getNormal() {
    Vector3d e0 = new Vector3d();
    Vector3d e1 = new Vector3d();
    e0.sub(v1.x, v0.x);
    e1.sub(v2.x, v1.x);
    Vector3d normal = new Vector3d();
    normal.cross(e1, e0);
    normal.normalize();
    return normal;
  }
  
  /**
   * @return the vertex of this triangle that is neither of the parameters
   */
  public Vertex getDiffVertex(Vertex w0, Vertex w1) {
    if (v0 == w0 || v0 == w1) {
      if (v1 == w0 || v1 == w1) {
        return v2;
      } else {
        return v1;
      }
    } else {
      return v0;
    }
  }
  
  public double area() {
    Vector3d u0 = new Vector3d();
    u0.sub(v1.x, v0.x);
    Vector3d u1 = new Vector3d();
    u1.sub(v2.x, v0.x);
    Vector3d a = new Vector3d();
    a.cross(u0, u1);
    return a.length() / 2;
  }
  
  public Point3d intersectRay(Point3d p0, Point3d p1) {
    Point3d I = new Point3d();
    Vector3d    u, v, n;
    Vector3d    dir, w0, w;
    double     r, a, b;
    
    u = new Vector3d(v1.x);
    u.sub(v0.x);
    v = new Vector3d(v2.x);
    v.sub(v0.x);
    n = new Vector3d(); // cross product
    n.cross(u, v);
    
    if (n.length() == 0) {
        return null;
    }
    
    dir = new Vector3d();
    dir.sub(p1, p0);
    w0 = new Vector3d(p0);
    w0.sub(v0.x);
    a = -(new Vector3d(n).dot(w0));
    b = new Vector3d(n).dot(dir);
    
    if ((float)Math.abs(b) < EPS) {
        return null;
    }
    
    r = a / b;
    if (r < 0.0) {
        return null;
    }
    
    I = new Point3d(p0);
    I.x += r * dir.x;
    I.y += r * dir.y;
    I.z += r * dir.z;
    
    return I;
}

}
