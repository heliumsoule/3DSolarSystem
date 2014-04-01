import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;


public class Camera {
	
	private float x;
	private float y;
	private float z;
	private float rx;
	private float ry;
	private float rz;
	
	private float fov;
	private float aspect;
	private float near;
	private float far;
	private GLAutoDrawable drawable;
	private GLU glu;
	
	private float speed;
	private float rotSpeed;
	
	public Camera(GLU glu, GLAutoDrawable drawable, float fov, float aspect, float near, float far) {
		x = y = z = 0;
		rx = ry = rz = 0;
		
		this.fov = fov;
		this.aspect = aspect;
		this.near = near;
		this.far = far;
		this.drawable = drawable;
		this.glu = glu;
		initProjection();
		
		
		speed = 0.5f;
		rotSpeed = 5;
	}
	
	private void initProjection() {
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = new GLU();
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fov, aspect, near, far); // fovy, aspect, zNear,
		gl.glMatrixMode(GL_MODELVIEW);
	}
	
	public void useView() {
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glRotatef(rx, 1, 0, 0);
		gl.glRotatef(ry, 0, 1, 0);
		gl.glRotatef(rz, 0, 0, 1);
		gl.glTranslated(x, y, z);
	}
	
	public void moveForward() {
		z += speed;
	}
	
	public void moveBack() {
		z -= speed;
	}
	
	public void moveRight() {
		x -= speed;
	}
	
	public void moveLeft() {
		x += speed;
	}
	
	public void moveUp() {
		y -= speed;
	}
	
	public void moveDown() {
		y += speed;
	}
	
	public void rotateRight() {
		ry -= rotSpeed;
	}
	
	public void rotateLeft() {
		ry += rotSpeed;
	}
	
	public float getRX() {
		return rx;
	}
	
	public float getRY() {
		return ry;
	}
	
	public float getRZ() {
		return rz;
	}
	
	public void setRX(float rx) {
		this.rx = rx;
	}
	
	public void setRY(float ry) {
		this.ry = ry;
	}
	
	public void setRZ(float rz) {
		this.rz = rz;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
 
	public float getZ() {
		return z;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public void setZ(float z) {
		this.z = z;
	}
	
	
}
