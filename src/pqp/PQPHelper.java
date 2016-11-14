package pqp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2i;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple2i;
import javax.vecmath.Tuple3i;

import particles.Triangle;

/** 
 * Provide static helper methods for simple construction and usage of PQP 
 * @author rexy
 *
 */
public class PQPHelper {
  
  private static WrapperUtil util = new WrapperUtil();
  
  private static SWIGTYPE_p_p_double I3 = util.newI3();
  private static SWIGTYPE_p_double vec0 = util.newPoint(0, 0, 0);

  public static PQP_Model buildPQPModel(List<Point3d> vertices, List<Tuple3i> faces) {
    PQP_Model model = new PQP_Model();
    model.BeginModel();
    
    List<SWIGTYPE_p_double> pts = new ArrayList<>();
    for (Point3d p : vertices) {
      pts.add(newPoint(p));
    }
    
    for (int i = 0; i < faces.size(); i++) {
      Tuple3i fIdx = faces.get(i);
      model.AddTri(pts.get(fIdx.x), pts.get(fIdx.y), pts.get(fIdx.z), i);
    }
    
    model.EndModel();
    return model;
  }
  
  public static SWIGTYPE_p_double newPoint(Point3d pt) {
    return util.newPoint(pt.x, pt.y, pt.z);
  }
  
  /**
   * Collision assuming no rotation and translation.
   * @return the indices of first 2 triangles in each model that collide
   */
  public static Tuple2i simpleCollide(PQP_Model m1, PQP_Model m2) {
    PQP_CollideResult collideResult = new PQP_CollideResult();
    PQP.PQP_Collide(collideResult, I3, vec0, m1, I3, vec0, m2, PQP.getPQP_FIRST_CONTACT());
    if (collideResult.Colliding() == 0) {
      return null;
    } else {
      return new Point2i(collideResult.Id1(0), collideResult.Id2(0));
    }
  }
  
  /**
   * Collision assuming no rotation and translation.
   * @return the indices of all triangles in each model that collide
   */
  public static List<Tuple2i> simpleCollideAll(PQP_Model m1, PQP_Model m2) {
    PQP_CollideResult collideResult = new PQP_CollideResult();
    PQP.PQP_Collide(collideResult, I3, vec0, m1, I3, vec0, m2, PQP.getPQP_ALL_CONTACTS());
    if (collideResult.Colliding() == 0) {
      return new ArrayList<Tuple2i>();
    } else {
      List<Tuple2i> collideList = new ArrayList<>();
      for (int i = 0; i < collideResult.NumPairs(); i++) {
        collideList.add(new Point2i(collideResult.Id1(i), collideResult.Id2(i)));
      }
      return collideList;
    }
  }
  
}
