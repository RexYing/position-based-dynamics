package forces;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;

import particles.Constants;
import particles.Particle;
import particles.ParticleSystem;

public class Gravity implements Force {
  
  private static final double GRAV_CONST = 9.8;
  
  private ParticleSystem ps;
  
  public Gravity(ParticleSystem ps) {
    this.ps = ps;
  }

  @Override
  public void applyForce() {
    for (Particle p : ps.P) {
      if (p.getHighlight()) {
        continue;
      }
      p.addForce(new Vector3d(0, -GRAV_CONST * p.m, 0));
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
