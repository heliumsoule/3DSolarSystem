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

/**
 * NeHe Lesson #6 (JOGL 2 Port): Texture
 * 
 * @author Hock-Chuan Chua
 * @version May 2012
 */
@SuppressWarnings("serial")
public class JOGL2Nehe06Texture extends GLCanvas implements GLEventListener {
	// Define constants for the top-level container
	private static String TITLE = "NeHe Lesson #6: Texture";
	private static final int CANVAS_WIDTH = 600; // width of the drawable
	private static final int CANVAS_HEIGHT = 600; // height of the drawable
	private static final int FPS = 60; // animator's target frames per second

	/** The entry main() method to setup the top-level container and animator */
	public static void main(String[] args) {
		// Run the GUI codes in the event-dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the OpenGL rendering canvas
				GLCanvas canvas = new JOGL2Nehe06Texture();
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

	private static float halfSideLen = 0.5f;
	// Rotational angle about the x, y and z axes in degrees
	private static float angleX = 0.0f;
	private static float angleY = 0.0f;
	private static float angleZ = 0.0f;
	// Rotational speed about x, y, z axes in degrees per refresh
	private static float rotateSpeedX = 0.3f;
	private static float rotateSpeedY = 0.2f;
	private static float rotateSpeedZ = 0.4f;

	private static double translateY = 0;
	private static double theta = 0;

	private static boolean changed = false;

	// Texture
	private Texture texture;
	private Texture earthTexture;

	// Texture image flips vertically. Shall use TextureCoords class to retrieve
	// the
	// top, bottom, left and right coordinates.
	private float textureTop, textureBottom, textureLeft, textureRight;

	/** Constructor to setup the GUI for this Component */
	public JOGL2Nehe06Texture() {
		this.addGLEventListener(this);
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

			texture = TextureIO.newTexture(new File(
					"C:\\Users\\ArcticWolf\\Workspace\\JOGL\\images\\QuestionMark.png"),
					true);

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
			TextureCoords textureCoords = texture.getImageTexCoords();
			textureTop = textureCoords.top();
			textureBottom = textureCoords.bottom();
			textureLeft = textureCoords.left();
			textureRight = textureCoords.right();
		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		try {
            earthTexture = TextureIO.newTexture(new File("C:\\Users\\ArcticWolf\\Workspace\\JOGL\\Images\\earthmap1k.png"),true);
        }
        catch (IOException exc) {
            exc.printStackTrace();
            System.exit(1);
        }
		
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
		update(drawable);
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color
																// and depth
																// buffers
                
		// ------ Render a Cube with texture ------
		gl.glLoadIdentity(); // reset the model-view matrix
		
		gl.glTranslated(0.0f, 0.0f, -5.0f);
		
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f); // rotate about the x-axis
		gl.glRotatef(angleY, 0.0f, 0.0f, -1.0f); // rotate about the y-axis
		// gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f); // rotate about the z-axis

		gl.glTranslatef(0.0f, 0.0f, (float) translateY - halfSideLen); // translate into the screen

		
		
        // Apply texture.
        earthTexture.enable(gl);
        earthTexture.bind(gl);

        // Draw sphere.
        GLUquadric earth = glu.gluNewQuadric();
        glu.gluQuadricTexture(earth, true);
        glu.gluQuadricDrawStyle(earth, GLU.GLU_FILL);
        glu.gluQuadricNormals(earth, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(earth, GLU.GLU_OUTSIDE);
        final float radius = 0.7f;
        final int slices = 16;
        final int stacks = 16;
        glu.gluSphere(earth, radius, slices, stacks);
        glu.gluDeleteQuadric(earth);
		
		
        gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
        gl.glTranslatef(0.0f, halfSideLen + radius, 0.0f);
        gl.glRotated(angleY, 0.0f, 1.0f, 0.0f);
        
		// Enables this texture's target in the current GL context's state.
		texture.enable(gl); // same as gl.glEnable(texture.getTarget());
		// gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
		// GL.GL_REPLACE);
		// Binds this texture to the current GL context.
		texture.bind(gl); // same as gl.glBindTexture(texture.getTarget(),
							// texture.getTextureObject());

		gl.glBegin(GL_QUADS);

		// Front Face
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(-halfSideLen, -halfSideLen, halfSideLen); // bottom-left
																// of the
																// texture and
		// quad
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(halfSideLen, -halfSideLen, halfSideLen); // bottom-right
																// of the
																// texture and
		// quad
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(halfSideLen, halfSideLen, halfSideLen); // top-right of
																// the texture
																// and quad
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(-halfSideLen, halfSideLen, halfSideLen); // top-left of
																// the texture
																// and quad

		// Back Face
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(-halfSideLen, -halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(-halfSideLen, halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(halfSideLen, halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(halfSideLen, -halfSideLen, -halfSideLen);

		// Top Face
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(-halfSideLen, halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(-halfSideLen, halfSideLen, halfSideLen);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(halfSideLen, halfSideLen, halfSideLen);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(halfSideLen, halfSideLen, -halfSideLen);

		// Bottom Face
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(-halfSideLen, -halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(halfSideLen, -halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(halfSideLen, -halfSideLen, halfSideLen);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(-halfSideLen, -halfSideLen, halfSideLen);

		// Right face
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(halfSideLen, -halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(halfSideLen, halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(halfSideLen, halfSideLen, halfSideLen);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(halfSideLen, -halfSideLen, halfSideLen);

		// Left Face
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(-halfSideLen, -halfSideLen, -halfSideLen);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(-halfSideLen, -halfSideLen, halfSideLen);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(-halfSideLen, halfSideLen, halfSideLen);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(-halfSideLen, halfSideLen, -halfSideLen);

		gl.glEnd();

		
		
		
		// Disables this texture's target (e.g., GL_TEXTURE_2D) in the current
		// GL
		// context's state.
		// texture.disable(gl); // same as gl.glDisable(texture.getTarget());

		// Update the rotational angel after each refresh by the corresponding
		// rotational speed
		angleX += rotateSpeedX;
		angleY += rotateSpeedY;
		angleZ += rotateSpeedZ;
	}

	/**
	 * Called back before the OpenGL context is destroyed. Release resource such
	 * as buffers.
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	private void update(GLAutoDrawable drawable) {
		theta += 0.01;
		translateY = Math.sin(theta);
		if (translateY >= .99 && !changed) {
			changed = true;
			
			
			GL2 gl = drawable.getGL().getGL2();
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
			
			
			try {
				texture = TextureIO.newTexture(new File(
						"C:\\Users\\ArcticWolf\\Workspace\\JOGL\\images\\HitBlock.png"),
						true);

				
				// Use linear filter for texture if image is larger than the
				// original texture
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
						GL_LINEAR);
				// Use linear filter for texture if image is smaller than the
				// original texture
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
						GL_LINEAR);

				// Texture image flips vertically. Shall use TextureCoords class
				// to
				// retrieve
				// the top, bottom, left and right coordinates, instead of using
				// 0.0f and 1.0f.
				TextureCoords textureCoords = texture.getImageTexCoords();
				textureTop = textureCoords.top();
				textureBottom = textureCoords.bottom();
				textureLeft = textureCoords.left();
				textureRight = textureCoords.right();
			} catch (GLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(theta > 2 * Math.PI) {
			translateY = 0;
		}
	}
}
