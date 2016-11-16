package forces;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;

import particles.Mesh;
import particles.ParticleSystem;
import particles.Triangle;
import particles.Vertex;

public class PressureConstraint implements Force {
  
  private ParticleSystem ps;
  private double pressureCoeff;
  private Mesh mesh;
  private double v0;
  
  public PressureConstraint(ParticleSystem ps, double pressureCoeff, Mesh mesh, double v0) {
    this.ps = ps;
    this.pressureCoeff = pressureCoeff;
    this.mesh = mesh;
    this.v0 = v0;
  }
  
  @Override
  public void applyForce() {
    Map<Vertex, Vector3d> dCdpi = new HashMap<>();
    for (Triangle triangle : mesh.triangles) {
      Vector3d c1 = new Vector3d();
      c1.cross(new Vector3d(triangle.v1.x), new Vector3d(triangle.v2.x));
      Vector3d tmp1 = new Vector3d(dCdpi.get(triangle.v0));
      tmp1.add(c1);
      dCdpi.put(triangle.v0, tmp1);
      
      Vector3d c2 = new Vector3d();
      c2.cross(new Vector3d(triangle.v2.x), new Vector3d(triangle.v0.x));
      Vector3d tmp2 = new Vector3d(dCdpi.get(triangle.v0));
      tmp2.add(c2);
      dCdpi.put(triangle.v1, tmp2);
      
      Vector3d c3 = new Vector3d();
      c1.cross(new Vector3d(triangle.v0.x), new Vector3d(triangle.v1.x));
      Vector3d tmp3 = new Vector3d(dCdpi.get(triangle.v0));
      tmp3.add(c3);
      dCdpi.put(triangle.v2, tmp3);
    }
    
    double c = -pressureCoeff * v0;
    for (Triangle triangle : mesh.triangles) {
      Vector3d tmp = new Vector3d();
      tmp.cross(new Vector3d(triangle.v0.x), new Vector3d(triangle.v1.x));
      c += tmp.dot(new Vector3d(triangle.v2.x));
    }
    
    for (Vertex vertex : mesh.vertices) {
      Vector3d dp = dCdpi.get(vertex);
      dp.scale(c / dp.lengthSquared() * vertex.getPositionUpdateInvMass());
      dp.negate();
      vertex.x.add(dp);
    }
    
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
