package particles;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Information about a collision
 * @author rex.ying0923
 *
 */
public class Collision {

	public Vector3d direction = new Vector3d();
	Vector3d reflectedDirection = new Vector3d();
	public double dist = 0;
	public Particle particle;
	
	public Collision(Particle p, Point3d p1, Point3d p2, double dist, Vector3d normal) {
		this.dist = dist;
		this.particle = p;
		direction.sub(p2, p1);
		direction.normalize();
		
		Vector3d v0 = new Vector3d(0, 0, 0);
		if (normal == v0) {
		  reflectedDirection = new Vector3d(direction);
		  reflectedDirection.negate();
		} else {
  		Vector3d v = new Vector3d(normal);
  		v.scale(direction.dot(normal) * 2);
  		reflectedDirection.sub(direction, v);
  		reflectedDirection.normalize();
		}
	}
	
	public Collision(Point3d p1, Point3d p2, double dist, Vector3d normal) {
	  this(null, p1, p2, dist, normal);
	}
}
