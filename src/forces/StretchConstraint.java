package forces;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;

import particles.Edge;
import particles.Mesh;
import particles.ParticleSystem;

/**
 * C(p1, p2) = |p1 - p2| - d.
 * Applied to all points in mesh
 * @author rex
 *
 */
public class StretchConstraint implements Force {
  
  private ParticleSystem ps;
  private double d;
  private double k;
  private double kIter;
  
  public StretchConstraint(ParticleSystem ps, double distance, double stiffness, int numIter) {
    this.ps = ps;
    this.d = distance;
    this.k = stiffness;
    this.kIter = 1 - Math.pow((1 - k), 1.0 / numIter);
  }

  @Override
  public void applyForce() {
    for (Mesh mesh : ps.M) {
      for (Edge edge : mesh.edges) {
        Vector3d diff = new Vector3d();
        diff.sub(edge.v0.x, edge.v1.x);
        Vector3d dp = new Vector3d(diff);
        dp.scale((Math.abs(diff.length()) - edge.restLength) / diff.length());
        double w0 = 1 / edge.v0.m;
        double w1 = 1 / edge.v1.m;
        Vector3d dp0 = new Vector3d(dp);
        Vector3d dp1 = new Vector3d(dp);
        dp0.scale(-w0 / (w0 + w1) * kIter);
        dp1.scale(w1 / (w0 + w1) * kIter);
        //edge.v0.addPos(dp0);
        //edge.v1.addPos(dp1);
        edge.v0.x.add(dp0);
        edge.v1.x.add(dp1);
        /*if (edge.v0.getHighlight()) {
          System.out.println(dp0);
          System.out.println(edge.v0.x);
        }*/
      }
    }
    //ps.applyChanges();
  }

  @Override
  public void display(GL2 gl) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ParticleSystem getParticleSystem() {
    return ps;
  }

}
