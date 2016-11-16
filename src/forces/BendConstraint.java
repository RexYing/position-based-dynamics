package forces;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;

import particles.Edge;
import particles.Mesh;
import particles.ParticleSystem;
import particles.Vertex;

public class BendConstraint implements Force {

  private static final double EPS = 1e-7;
  /** Default rest bending angle is pi/2: planar constraint. */
  private static final double PHI0 = Math.PI;
  private ParticleSystem ps;
  private double k;
  private double kIter;


  public BendConstraint(ParticleSystem ps, double stiffness, int numIter) {
    this.ps = ps;
    this.k = stiffness;
    this.kIter = 1 - Math.pow((1 - k), 1.0 / numIter);
  }

  @Override
  public void applyForce() {
    for (Mesh mesh : ps.M) {
      for (Edge edge : mesh.edges) {
        if (edge.t0 == null || edge.t1 == null) {
          continue;
        }
        Vector3d n2 = edge.t0.getNormal();
        Vector3d n1 = edge.t1.getNormal();
        //n2.negate();
        double d = n1.dot(n2);

        // vertex positions and inverse masses
        Vertex particle1 = edge.v0;
        Vertex particle2 = edge.v1;
        Vertex particle3 = edge.t1.getDiffVertex(particle1, particle2);
        Vertex particle4 = edge.t0.getDiffVertex(particle1, particle2);

        Point3d p1 = particle1.x;
        double w1 = 1 / particle1.m;
        Vector3d p2 = new Vector3d(particle2.x);
        p2.sub(p1);
        double w2 = 1 / particle2.m;
        Vector3d p3 = new Vector3d(particle3.x);
        p3.sub(p1);
        double w3 = 1 / particle3.m;
        Vector3d p4 = new Vector3d(particle4.x);
        p4.sub(p1);
        double w4 = 1 / particle4.m;
        
        n1.cross(p2, p3);
        n1.normalize();
        n2.cross(p2, p4);
        n2.normalize();
        d = n1.dot(n2);

        Vector3d p2Cp3 = new Vector3d();
        p2Cp3.cross(p2, p3);
        Vector3d p2Cp4 = new Vector3d();
        p2Cp4.cross(p2, p4);

        Vector3d q3 = new Vector3d();
        q3.cross(p2, n2);
        Vector3d n1Cp2 = new Vector3d();
        n1Cp2.cross(n1, p2);
        n1Cp2.scale(d);
        q3.add(n1Cp2);
        q3.scale(1 / p2Cp3.length());

        Vector3d q4 = new Vector3d();
        q4.cross(p2, n1);
        Vector3d n2Cp2 = new Vector3d();
        n2Cp2.cross(n2, p2);
        n2Cp2.scale(d);
        q4.add(n2Cp2);
        q4.scale(1 / p2Cp4.length());

        Vector3d q21 = new Vector3d();
        q21.cross(p3, n2);
        Vector3d n1Cp3 = new Vector3d();
        n1Cp3.cross(n1, p3);
        n1Cp3.scale(d);
        q21.add(n1Cp3);
        q21.scale(1 / p2Cp3.length());
        Vector3d q22 = new Vector3d();
        q22.cross(p4, n1);
        Vector3d n2Cp4 = new Vector3d();
        n2Cp4.cross(n2, p4);
        n2Cp4.scale(d);
        q22.add(n2Cp4);
        q22.scale(1 / p2Cp4.length());
        Vector3d q2 = new Vector3d();
        q2.add(q21, q22);
        q2.negate();

        Vector3d q1 = new Vector3d(q2);
        q1.negate();
        q1.sub(q3);
        q1.sub(q4);

        double c = Math.acos(d) - PHI0;
        double s = -Math.sqrt(1 - d * d) * c;
        double denominator = w1 * q1.lengthSquared() + w2 * q2.lengthSquared() + w3 * q3.lengthSquared()
            + w4 * q4.lengthSquared();
        if (denominator < EPS) {
          continue;
        }

        Vector3d dp1 = new Vector3d(q1);
        dp1.scale(w1 * s / denominator * kIter);
        Vector3d dp2 = new Vector3d(q2);
        dp2.scale(w2 * s / denominator * kIter);
        Vector3d dp3 = new Vector3d(q3);
        dp3.scale(w3 * s / denominator * kIter);
        Vector3d dp4 = new Vector3d(q4);
        dp4.scale(w4 * s / denominator * kIter);

        /*
        if (edge.v0.getHighlight()) {
          
          System.out.println(dp1);
          System.out.println(dp2);
          System.out.println(dp3);
          System.out.println(dp4);
        }
        */

        particle1.x.add(dp1);
        particle2.x.add(dp2);
        particle3.x.add(dp3);
        particle4.x.add(dp4);

      }
    }
  }

  @Override
  public void display(GL2 gl) {
    // TODO Auto-generated method stub

  }

  @Override
  public ParticleSystem getParticleSystem() {
    // TODO Auto-generated method stub
    return null;
  }
}
