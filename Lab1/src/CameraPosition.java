
public class CameraPosition {
	public String name;
	
	// point of view, or center of camera; the ego-center; the eye-point
	public Point3D position = new Point3D();

	// point of interest; what the camera is looking at; the exo-center
	public Point3D target = new Point3D();

	// This is the up vector for the (local) camera space
	public Vector3D up = new Vector3D();
	
	@Override
	public String toString()
	{
		return name;
	}
}
