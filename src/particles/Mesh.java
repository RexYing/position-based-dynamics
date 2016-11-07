package particles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Tuple2i;
import javax.vecmath.Tuple3i;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.glsl.*;

import particles.Collision;
import particles.Mesh;
import pqp.PQPHelper;
import pqp.PQP_Model;

/**
 * A mesh data structure which may be displayed to the screen. The mesh is
 * assumed to be manifold and triangular, and all triangles are assumed to have
 * coherent orientation. The structure works as follows, where -> means "has
 * pointers to": </br>
 *
 * Mesh -> Triangle, Vertex, Edge </br>
 * Vertex -> Edge </br>
 * Edge -> Vertex, Triangle </br>
 * Triangle -> Vertex </br>
 *
 * @author Eston Schweickart, February 2014
 */
public class Mesh {

  private boolean init = false;
  private boolean fallback = false;

  // TODO: you may not need to explicitly store vertices or edges here; remove
  // these if you so choose.
  /** The vertices of the mesh. */
  public List<Vertex> vertices = new ArrayList<Vertex>();

  /** The edges of the mesh. */
  public List<Edge> edges = new ArrayList<Edge>();

  /** The faces of the mesh. */
  public List<Triangle> triangles = new ArrayList<Triangle>();

  /** Filename of the vertex shader source. */
  private static final String[] VERT_SOURCE = { "mesh-vert.glsl" };

  /** Filename of the fragment shader source. */
  private static final String[] FRAG_SOURCE = { "mesh-frag.glsl" };

  /**
   * True if rendering using glsl shaders. Set to false to use fixed-function
   * rendering.
   */
  private boolean useGLSL = true;

  /** The shader program used by the mesh. */
  private ShaderProgram prog;
  
  private PQP_Model pqpModel;

  /** Gets ready to display the mesh; compiles programs, etc. */
  private void initDisplay(GL2 gl) {
    if (init || !useGLSL)
      return;

    prog = new ShaderProgram();
    ShaderCode vertCode = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, 1, this.getClass(),
            VERT_SOURCE, false);
    ShaderCode fragCode = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, 1, this.getClass(),
            FRAG_SOURCE, false);
    if (!prog.add(gl, vertCode, System.err) || !prog.add(gl, fragCode, System.err)) {
      System.err.println("WARNING: shader did not compile");
      useGLSL = false;
    } else {
      prog.link(gl, System.err);
    }

    init = true;
  }

  /** Displays the mesh to the screen. */
  public void display(GL2 gl) {
    if (!init)
      initDisplay(gl);

    float[] cFront = { 0f, 0.8f, 0.3f };
    float[] cBack = { 0.8f, 0f, 0.3f };

    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, cFront, 0);
    gl.glMaterialfv(GL2.GL_BACK, GL2.GL_DIFFUSE, cBack, 0);

    if (useGLSL) {
      prog.useProgram(gl, true);
    } else {
      gl.glEnable(GL2.GL_LIGHTING);
      gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
    }

    for (Triangle t : triangles) {
      javax.vecmath.Vector3d n = t.getNormal();
      gl.glNormal3d(n.x, n.y, n.z);
      gl.glBegin(GL2.GL_TRIANGLES);
      gl.glVertex3d(t.v0.x.x, t.v0.x.y, t.v0.x.z);
      gl.glVertex3d(t.v1.x.x, t.v1.x.y, t.v1.x.z);
      gl.glVertex3d(t.v2.x.x, t.v2.x.y, t.v2.x.z);
      gl.glEnd();
    }

    if (useGLSL) {
      prog.useProgram(gl, false);
    } else {
      gl.glDisable(GL2.GL_LIGHTING);
    }

    // For debugging purposes- displays the edges of the mesh
    /*
     * gl.glColor3f(0f, 0.8f, 0.3f); for(Edge e : edges) {
     * gl.glBegin(GL2.GL_LINES); gl.glVertex3d(e.v0.x.x, e.v0.x.y, e.v0.x.z);
     * gl.glVertex3d(e.v1.x.x, e.v1.x.y, e.v1.x.z); gl.glEnd(); }
     */
  }
  
  private PQP_Model buildPQPModel(List<Vertex> vertices, List<Triangle> triangles) {
    PQP_Model model = new PQP_Model();
    model.BeginModel();
   
    List<Point3d> pts = new ArrayList<>();
    List<Tuple3i> faces = new ArrayList<>();
    
    for (Vertex vertex : vertices) {
      pts.add(vertex.x);
    }
    
    for (Triangle triangle : triangles) {
      faces.add(new Point3i(
          triangle.v0.getIndex(), triangle.v1.getIndex(), triangle.v2.getIndex()));
    }
    
    return PQPHelper.buildPQPModel(pts, faces);
  }
  
  /**
   * Build internal structures for collision detection after updating vertices and triangular faces
   */
  public void update() {
    pqpModel = buildPQPModel(vertices, triangles);
  }

  public Collision segmentIntersects(Point3d p1, Point3d p2) {
    List<Point3d> tmpV = new ArrayList<>();
    tmpV.add(p1);
    tmpV.add(p1);
    tmpV.add(p2);
    List<Tuple3i> tmpF = new ArrayList<>();
    tmpF.add(new Point3i(0, 1, 2));
    
    Tuple2i faceIdxPair = PQPHelper.simpleCollide(pqpModel, PQPHelper.buildPQPModel(tmpV, tmpF));
    
    if (faceIdxPair == null) {
      return null;
    } else {
      // index does not make sense
      if (faceIdxPair.x >= triangles.size() || faceIdxPair.x < 0) {
        System.out.println("index");
        //return null;
        return new Collision(p1, p2, 0, new Vector3d());
      }
      return new Collision(p1, p2, 0, triangles.get(faceIdxPair.x).getNormal());
    }
  }
  
  /**
   * Collision with a static mesh
   * @param staticMesh
   * @return
   */
  public Map<Vertex, Triangle> collideWithStaticMesh(Mesh staticMesh) {
    List<Tuple2i> faceIdxPairs = PQPHelper.simpleCollideAll(pqpModel, staticMesh.pqpModel);
    if (faceIdxPairs.isEmpty()) {
      return new HashMap<>();
    }
    Map<Vertex, Triangle> potentialCollisions = new HashMap<>();
    for (Tuple2i faceIdxPair : faceIdxPairs) {
      Triangle f1 = triangles.get(faceIdxPair.x); 
      Triangle f2 = staticMesh.triangles.get(faceIdxPair.y);
      potentialCollisions.put(f1.v0, f2);
      potentialCollisions.put(f1.v1, f2);
      potentialCollisions.put(f1.v2, f2);
    }
    return potentialCollisions;
  }
  
  public static Mesh CubeMesh(Point3d lowV, Point3d highV) {
    List<Vertex> verts = new ArrayList<>();
    verts.add(new Vertex(new Point3d(lowV), 0));
    verts.add(new Vertex(new Point3d(lowV.x, lowV.y, highV.z), 1));
    verts.add(new Vertex(new Point3d(lowV.x, highV.y, lowV.z), 2));
    verts.add(new Vertex(new Point3d(lowV.x, highV.y, highV.z), 3));
    verts.add(new Vertex(new Point3d(highV.x, lowV.y, lowV.z), 4));
    verts.add(new Vertex(new Point3d(highV.x, lowV.y, highV.z), 5));
    verts.add(new Vertex(new Point3d(highV.x, highV.y, lowV.z), 6));
    verts.add(new Vertex(new Point3d(highV), 7));
    
    List<Triangle> faces = new ArrayList<>();
    faces.add(new Triangle(verts.get(0), verts.get(1), verts.get(2)));
    faces.add(new Triangle(verts.get(2), verts.get(1), verts.get(3)));
    faces.add(new Triangle(verts.get(0), verts.get(4), verts.get(5)));
    faces.add(new Triangle(verts.get(0), verts.get(5), verts.get(1)));
    faces.add(new Triangle(verts.get(0), verts.get(2), verts.get(4)));
    faces.add(new Triangle(verts.get(2), verts.get(6), verts.get(4)));
    faces.add(new Triangle(verts.get(1), verts.get(5), verts.get(7)));
    faces.add(new Triangle(verts.get(1), verts.get(7), verts.get(3)));
    faces.add(new Triangle(verts.get(2), verts.get(7), verts.get(6)));
    faces.add(new Triangle(verts.get(2), verts.get(3), verts.get(7)));
    faces.add(new Triangle(verts.get(4), verts.get(7), verts.get(5)));
    faces.add(new Triangle(verts.get(4), verts.get(6), verts.get(7)));
    
    Mesh mesh = new Mesh();
    mesh.vertices = verts;
    mesh.triangles = faces;
    mesh.update();
    return mesh;
  }
}
