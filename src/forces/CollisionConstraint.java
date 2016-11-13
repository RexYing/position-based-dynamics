package forces;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;

import particles.Particle;
import particles.ParticleSystem;
import particles.Triangle;

/**
 * Inequality constraint:
 * C(p) = (p - qc) \dot nc
 * 
 * @author rex
 *
 */
public class CollisionConstraint implements Force {

  /** Stiffness = 1.0: perfectly stiff */
  private static final double k = 1.0;
  
  public Particle particle;
  public Point3d entryPoint;
  public Triangle triangle;
  
  public CollisionConstraint(Particle particle, Point3d entryPoint, Triangle triangle) {
    this.particle = particle;
    this.entryPoint = entryPoint;
    this.triangle = triangle;
  }

  @Override
  public void applyForce() {
    Vector3d grad = triangle.getNormal();
    Vector3d diff = new Vector3d();
    diff.sub(particle.x, entryPoint);
    double c = diff.dot(triangle.getNormal());
    if (c >= 0) {
      return;
    }
    Vector3d dp = new Vector3d(grad);
    dp.scale(-c / grad.lengthSquared() * k);
    System.out.println(dp);
    particle.x.add(dp);
  }

  @Override
  public void display(GL2 gl) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ParticleSystem getParticleSystem() {
    return null;
  }
  
  
}
