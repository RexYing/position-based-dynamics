package particles;

/**
 * Default constants. Add your own as necessary.
 *
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 */
public interface Constants
{
    /** Mass of a particle. */
    public static final double PARTICLE_MASS     = 1.0;

    /** Camera rotation speed constants. */
    public static final double CAM_SIN_THETA     = Math.sin(0.2);
    public static final double CAM_COS_THETA     = Math.cos(0.2);
    
    public static final double NUM_SOLVER_ITERATIONS = 10;
    
    public static final double CLOTH_DENSITY     = 10;
    
    public static final int NUM_ITERATIONS       = 10;
    public static final double STRETCH_DIST      = 0.1;
    public static final double STRETCH_STIFF     = 0.8;
    public static final double BEND_STIFF        = 0.2;
    public static final double DAMP_COEFF        = 0.1;
    public static final double PRESSURE_COEFF    = 0.6;
}
