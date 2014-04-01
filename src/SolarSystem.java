import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import org.omg.CORBA.portable.InputStream;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;
import static javax.media.opengl.GL.*; // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * NeHe Lesson #6 (JOGL 2 Port): Texture
 * 
 * @author Hock-Chuan Chua
 * @version May 2012
 */
@SuppressWarnings("serial")
public class SolarSystem extends GLCanvas implements GLEventListener , KeyListener{
	// Define constants for the top-level container
	private static String TITLE = "NeHe Lesson #6: Texture";
	private static final int CANVAS_WIDTH = 640; // width of the drawable
	private static final int CANVAS_HEIGHT = 480; // height of the drawable
	private static final int FPS = 60; // animator's target frames per second

	/** The entry main() method to setup the top-level container and animator */
	public static void main(String[] args) {
		// Run the GUI codes in the event-dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the OpenGL rendering canvas
				GLCanvas canvas = new SolarSystem();
				canvas.setPreferredSize(new Dimension(CANVAS_WIDTH,
						CANVAS_HEIGHT));

				// Create a animator that drives canvas' display() at the
				// specified FPS.
				final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

				// Create the top-level container
				final JFrame frame = new JFrame(); // Swing's JFrame or AWT's
													// Frame
				frame.getContentPane().add(canvas);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						// Use a dedicate thread to run the stop() to ensure
						// that the
						// animator stops before program exits.
						new Thread() {
							@Override
							public void run() {
								if (animator.isStarted())
									animator.stop();
								System.exit(0);
							}
						}.start();
					}
				});
				frame.setTitle(TITLE);
				frame.pack();
				frame.setVisible(true);
				animator.start(); // start the animation loop
			}
		});
	}

	// Setup OpenGL Graphics Renderer

	private GLU glu; // for the GL Utility

	private static float floorLevel = -.75f;
	private ArrayList<Planet> planets;
	private Texture floorTexture;
	private Camera cam;
	

	// Texture image flips vertically. Shall use TextureCoords class to retrieve
	// the
	// top, bottom, left and right coordinates.
	private float fTextureTop, fTextureBottom, fTextureLeft, fTextureRight;

	/** Constructor to setup the GUI for this Component */
	public SolarSystem() {
		this.addGLEventListener(this);
		this.addKeyListener(this);
	}

	// ------ Implement methods declared in GLEventListener ------

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be
	 * used to perform one-time initialization. Run only once.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL graphics context
		glu = new GLU(); // get GL Utilities
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		gl.glClearDepth(1.0f); // set clear depth value to farthest
		gl.glEnable(GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL_LEQUAL); // the type of depth test to do
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best
																// perspective
																// correction
		gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out
									// lighting

		// Load texture from image
		try {
			// Create a OpenGL Texture object from (URL, mipmap, file suffix)
			// Use URL so that can read from JAR and disk file.

			floorTexture = TextureIO.newTexture(new File(
					"C:\\Users\\ArcticWolf\\Workspace\\JOGL\\Images\\Grid.png"), true);

			// Use linear filter for texture if image is larger than the
			// original texture
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			// Use linear filter for texture if image is smaller than the
			// original texture
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

			// Texture image flips vertically. Shall use TextureCoords class to
			// retrieve
			// the top, bottom, left and right coordinates, instead of using
			// 0.0f and 1.0f.

			TextureCoords fTextureCoords = floorTexture.getImageTexCoords();
			fTextureTop = fTextureCoords.top();
			fTextureBottom = fTextureCoords.bottom();
			fTextureLeft = fTextureCoords.left();
			fTextureRight = fTextureCoords.right();

		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	
		//Create each planet
		Planet Sun = new Planet(0,0,0,0.7f,"C:\\Users\\ArcticWolf\\Workspace\\JOGL\\images\\Sun.png",glu,gl);
		Planet Mercury = new Planet(0.4f,0.3f,1.2f, 0.1f,"C:\\Users\\ArcticWolf\\Workspace\\JOGL\\images\\Mercury.png",glu,gl);
		Planet Earth = new Planet(0.2f,0.3f,1.6f, 0.26f,"C:\\Users\\ArcticWolf\\Workspace\\JOGL\\images\\Earth.png",glu,gl);		
		planets = new ArrayList<Planet>();
		planets.add(Sun);
		planets.add(Mercury);
		planets.add(Earth);		

	}

	/**
	 * Call-back handler for window re-size event. Also called when the drawable
	 * is first set to visible.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context

		if (height == 0)
			height = 1; // prevent divide by zero
		float aspect = (float) width / height;
		cam = new Camera(glu, drawable, 70, aspect, 0.3f, 1000);

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix
		glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear,
														// zFar

		// Enable the model-view transform
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity(); // reset

	}

	
	
	/**
	 * Called back by the animator to perform rendering.
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color
																// and depth
																// buffers

		
		gl.glLoadIdentity(); // reset the model-view matrix
		gl.glTranslatef(0.0f, 0.0f, -5.0f);
		gl.glRotatef(30f, 1.0f, 0.0f, 0.0f);
		
		cam.useView();
		
		
		floorTexture.enable(gl);
		floorTexture.bind(gl);
		// draw floor
		gl.glBegin(GL_QUADS);

		gl.glTexCoord2f(fTextureRight, fTextureTop);
		gl.glVertex3f(20.0f, floorLevel, 20.0f);
		gl.glTexCoord2f(fTextureLeft, fTextureTop);
		gl.glVertex3f(-20.0f, floorLevel, 20.0f);
		gl.glTexCoord2f(fTextureLeft, fTextureBottom);
		gl.glVertex3f(-20.0f, floorLevel, -20.0f);
		gl.glTexCoord2f(fTextureRight, fTextureBottom);
		gl.glVertex3f(20.0f, floorLevel, -20.0f);

		gl.glEnd();
		floorTexture.disable(gl);

		
		for(int i = 0; i < planets.size(); i++) {
			planets.get(i).drawPlanet();
			planets.get(i).drawPath();
		}
		
		update(drawable);
	}

	/**
	 * Called back before the OpenGL context is destroyed. Release resource such
	 * as buffers.
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	private void update(GLAutoDrawable drawable) {
		for(int i = 0; i < planets.size(); i++) {
			planets.get(i).update();
		}
	}

	
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_W) {
			cam.moveForward();
		}
		if (keyCode == KeyEvent.VK_A) {
			cam.moveLeft();
		}
		if (keyCode == KeyEvent.VK_S) {
			cam.moveBack();
		}
		if (keyCode == KeyEvent.VK_D) {
			cam.moveRight();
		}
		if (keyCode == KeyEvent.VK_R) {
			cam.moveUp();
		}
		if (keyCode == KeyEvent.VK_F) {
			cam.moveDown();
		}
		if (keyCode == KeyEvent.VK_Q) {
			cam.rotateLeft();
		}
		if (keyCode == KeyEvent.VK_E) {
			cam.rotateRight();
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}
}
