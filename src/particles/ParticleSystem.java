package particles;

import java.util.*;
import javax.vecmath.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.glsl.*;

import forces.CollisionConstraint;
import forces.Force;
import particles.Particle;

import java.io.*;

/**
 * Maintains dynamic lists of Particle and Force objects, and provides access to
 * their state for numerical integration of dynamics.
 *
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 */
public class ParticleSystem // implements Serializable
{
  /** Current simulation time. */
  public double time = 0;

  /** List of Particle objects. */
  public ArrayList<Particle> P = new ArrayList<Particle>();

  /** Mesh list. */
  public ArrayList<Mesh> M = new ArrayList<Mesh>();

  /** Static meshes for collision detection only */
  private List<Mesh> staticMeshes = new ArrayList<>();

  /** List of Force objects. */
  public ArrayList<Force> F = new ArrayList<Force>();

  public List<Force> constraints = new ArrayList<>();

  /**
   * true iff prog has been initialized. This cannot be done in the constructor
   * because it requires a GL2 reference.
   */
  private boolean init = false;

  /** Filename of vertex shader source. */
  public static final String[] VERT_SOURCE = { "vert.glsl" };

  /** Filename of fragment shader source. */
  public static final String[] FRAG_SOURCE = { "frag.glsl" };

  /** The shader program used by the particles. */
  ShaderProgram prog;

  /** Basic constructor. */
  public ParticleSystem() {

  }

  /**
   * Set up the GLSL program. This requires that the current directory (i.e. the
   * package in which this class resides) has a vertex and fragment shader.
   */
  public synchronized void init(GL2 gl) {
    if (init)
      return;

    prog = new ShaderProgram();
    ShaderCode vert_code = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, 1, this.getClass(), VERT_SOURCE, false);
    ShaderCode frag_code = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, 1, this.getClass(), FRAG_SOURCE, false);
    if (!prog.add(gl, vert_code, System.err) || !prog.add(gl, frag_code, System.err)) {
      System.err.println("WARNING: shader did not compile");
      prog.init(gl); // Initialize empty program
    } else {
      prog.link(gl, System.err);
    }

    init = true;
  }

  /** Adds a force object (until removed) */
  public synchronized void addForce(Force f) {
    F.add(f);
  }

  /** Adds a force object (until removed) */
  public synchronized void addConstraint(Force f) {
    constraints.add(f);
  }

  /**
   * Add a mesh to the current particle system.
   */
  public void addMesh(File filename) {
    Mesh objmesh = new Mesh();
    try {
      objmesh = MeshBuilder.buildMesh(filename, this);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.err.println("OOPS: " + e);
      System.exit(1);
    } catch (MeshBuilder.BadMeshException e) {
      e.printStackTrace();
      System.err.println("OOPS: " + e);
      System.exit(-1);
    }
    System.out.println("Mesh file " + filename + " loaded.");
    M.add(objmesh);
  }

  /**
   * Useful for removing temporary forces, such as user-interaction spring
   * forces.
   */
  public synchronized void removeForce(Force f) {
    F.remove(f);
  }

  /**
   * Creates particle and adds it to the particle system.
   * 
   * @param p0
   *          Undeformed/material position.
   * @return Reference to new Particle.
   */
  public synchronized Particle createParticle(Point3d p0) {
    Particle newP = new Particle(p0, P.size());
    P.add(newP);
    return newP;
  }

  /**
   * Helper-function that computes the nearest particle to the specified
   * (deformed) position.
   * 
   * @return Nearest particle, or null if no particles.
   */
  public synchronized Particle getNearestParticle(Point3d x) {
    Particle minP = null;
    double minDistSq = Double.MAX_VALUE;
    for (Particle particle : P) {
      double distSq = x.distanceSquared(particle.x);
      if (distSq < minDistSq) {
        minDistSq = distSq;
        minP = particle;
      }
    }
    return minP;
  }
  
  public synchronized Particle getNearestParticle(Point3d l1, Point3d l2) {
    double minDist = Double.MAX_VALUE;
    Particle minP = null;
    for (Particle particle : P) {
      double dist = Utils.pointLineDistance(particle.x, l1, l2);
      if (dist < minDist) {
        minDist = dist;
        minP = particle;
      }
    }
    return minP;
  }

  /**
   * Moves all particles to undeformed/materials positions, and sets all
   * velocities to zero. Synchronized to avoid problems with simultaneous calls
   * to advanceTime().
   */
  public synchronized void reset() {
    for (Particle p : P) {
      p.x.set(p.x0);
      p.v.set(0, 0, 0);
      p.f.set(0, 0, 0);
      p.setHighlight(false);
    }
    time = 0;
  }

  /**
   * Simple implementation of a first-order time step. TODO: Implement the
   * "Position Based Fluids" integrator here
   */
  public synchronized void advanceTime(double dt) {
    for (Mesh mesh : M) {
      mesh.updateMass();
    }


    /// Clear force accumulators:
    for (Particle p : P) {
      p.f.set(0, 0, 0);
      p.xPrev = new Point3d(p.x);
    }

    for (Force force : F) {
      force.applyForce();
    }

    List<Force> collisionConstraints = new ArrayList<>();

    for (Particle p : P) {
      p.applyChanges();
      Vector3d acc = new Vector3d(p.f);
      acc.scale(1.0 / p.m);
      p.v.scaleAdd(dt, acc, p.v); // p.v += dt * p.f;
      p.x.scaleAdd(dt, p.v, p.x); // p.x += dt * p.v;

      for (Mesh mesh : staticMeshes) {
        CollisionConstraint collisionConstraint = mesh.segmentIntersects(p.xPrev, p);
        if (collisionConstraint != null) {
          collisionConstraints.add(collisionConstraint);
        }
      }
    }

    for (int i = 0; i < Constants.NUM_SOLVER_ITERATIONS; i++) {
      for (Force c : constraints) {
        c.applyForce();
      }

      collisionConstraints = new ArrayList<>();
      for (Particle p : P) {
        for (Mesh mesh : staticMeshes) {
          CollisionConstraint collisionConstraint = mesh.segmentIntersects(p.xPrev, p);
          if (collisionConstraint != null) {
            collisionConstraints.add(collisionConstraint);
          }
        }
      }

      for (Force c : collisionConstraints) {
        c.applyForce();
      }
    }

    for (Particle p : P) {
      Vector3d dp = new Vector3d();
      dp.sub(p.x, p.xPrev);
      dp.scale(0.95 / dt);
      p.v = dp;
    }

    time += dt;
  }
  
  public void globalVelocityDamping(double dampCoeff, Mesh mesh) {
    double sumMass = 0;
    Vector3d xcm = new Vector3d();
    Vector3d vcm = new Vector3d();
    for (Particle p : mesh.vertices) {
      sumMass += p.m;
      vcm.scaleAdd(p.m, p.v);
      xcm.scaleAdd(p.m, p.x);
    }
    xcm.scale(1 / sumMass);
    vcm.scale(1 / sumMass);
    
    Vector3d l = new Vector3d();
    GMatrix i = Utils.zeroMat(3, 3);
    for (Particle p : mesh.vertices) {
      Vector3d r = new Vector3d(p.x);
      r.sub(xcm);
      Vector3d mivi = new Vector3d(p.v);
      mivi.scale(p.m);
      Vector3d li = new Vector3d();
      li.cross(r, mivi);
      l.add(li);
      
      GMatrix rm = Utils.crossToMatrixOp(r);
      GMatrix rmtrsp = new GMatrix(rm);
      rmtrsp.transpose();
      GMatrix ii = new GMatrix(rm); 
      ii.mul(rmtrsp);
      GMatrix idScaled = Utils.identityMatScaled(3, p.m);
      ii.mul(idScaled);
      i.add(ii);
    }
    System.out.println(l);
    System.out.println(i);
    i.invert();
    GVector omega = new GVector(l);
    omega.mul(i, omega);
    Vector3d omega3 = new Vector3d(omega.getElement(0), omega.getElement(1), omega.getElement(2));
    
    for (Particle p : mesh.vertices) {
      Vector3d dv = new Vector3d(vcm);
      Vector3d omegaCr = new Vector3d();
      
      Vector3d r = new Vector3d(p.x);
      r.sub(xcm);
      omegaCr.cross(omega3, r);
      dv.add(omegaCr);
      dv.sub(p.v);
      dv.scale(dampCoeff);
      
      p.v.add(dv);
    }
  }
  
  

  /**
   * Displays Particle and Force objects. Modify how you like.
   */
  public synchronized void display(GL2 gl) {
    for (Force force : F) {
      force.display(gl);
    }
    if (!init)
      init(gl);
    prog.useProgram(gl, true);
    for (Particle particle : P) {
      particle.display(gl);
    }
    for (Mesh mesh : M) {
      mesh.display(gl);
    }
    prog.useProgram(gl, false);
  }

  public void addStaticMesh(Mesh m) {
    staticMeshes.add(m);
  }

  public void applyChanges() {
    for (Particle p : P) {
      p.applyChanges();
    }
  }

}
