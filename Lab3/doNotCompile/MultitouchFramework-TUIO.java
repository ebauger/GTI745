
import java.awt.Container;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
//import javax.swing.BoxLayout;

//import javax.media.opengl.GL;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLAutoDrawable;
// import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
//import com.sun.opengl.util.GLUT;

import TUIO.TuioListener;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioObject;
//import TUIO.TuioPoint;
import TUIO.TuioTime;


public class MultitouchFramework
	extends GLJPanel
	implements KeyListener, MouseListener, MouseMotionListener, GLEventListener, TuioListener
{
	TuioClient tuioClient;
	public static final int TOUCH_EVENT_DOWN = 0;
	public static final int TOUCH_EVENT_MOVE = 1;
	public static final int TOUCH_EVENT_UP = 2;

	private GraphicsWrapper gw = new GraphicsWrapper();
	private SimpleWhiteboard client = null;

	private int preferredWidth, preferredHeight;
	private int width, height;

	public void setPreferredWindowSize( int w, int h ) {
		preferredWidth = w;
		preferredHeight = h;
	}

	private void createClient() {
		client = new SimpleWhiteboard(this,gw);
	}

	public MultitouchFramework( GLCapabilities caps ) {
		super( caps );

		createClient();

		addGLEventListener(this);
		addKeyListener( this );
		addMouseListener( this );
		addMouseMotionListener( this );

		tuioClient = new TuioClient(3333);
		tuioClient.addTuioListener(this);
		tuioClient.connect();
	}
	public Dimension getPreferredSize() {
		return new Dimension( preferredWidth, preferredHeight );
	}

	public void start() {
		client.startBackgroundWork();
	}

	public void stop() {
		client.stopBackgroundWork();
	}

	// NOTE: calling e.consume() within these methods
	// could prevent us from receiving key events,
	// so we don't call it.
	//
	public void keyPressed( KeyEvent e ) {
		client.keyPressed(e);
	}
	public void keyReleased( KeyEvent e ) {
		client.keyReleased(e);
	}
	public void keyTyped( KeyEvent e ) {
		client.keyTyped(e);
	}
	public void mouseEntered( MouseEvent e ) {
		client.mouseEntered(e);
	}
	public void mouseExited( MouseEvent e ) {
		client.mouseExited(e);
	}
	public void mouseClicked( MouseEvent e ) {
		client.mouseClicked(e);
	}
	public void mousePressed( MouseEvent e ) {
		client.mousePressed(e);
	}
	public void mouseReleased( MouseEvent e ) {
		client.mouseReleased( e );
	}
	public void mouseMoved( MouseEvent e ) {
		client.mouseMoved( e );
	}
	public void mouseDragged( MouseEvent e ) {
		client.mouseDragged( e );
	}

	public void requestRedraw() {
		repaint();
	}

	public void init( GLAutoDrawable drawable ) {
		gw.set( drawable );
	}
	public void reshape(
		GLAutoDrawable drawable,
		int x, int y, int width, int height
	) {
		gw.set( drawable );
		gw.resize( width, height );
	}
	public void displayChanged(
		GLAutoDrawable drawable,
		boolean modeChanged,
		boolean deviceChanged
	) {
		// leave this empty
	}
	public void display( GLAutoDrawable drawable ) {
		gw.set( drawable );
		client.draw();
		// gl.glFlush(); // I don't think this is necessary
	}


	public void touchEvent( int id, float x, float y, int type ) {
		// int offsetx = this.getLocation().x + this.getParent().getLocation().x;
		// int offsety = this.getLocation().y + this.getParent().getLocation().y;
		client.processMultitouchInputEvent(
			id,
			x * gw.getWidth(),
			y * gw.getHeight(),
			type
		);
	}

	public void addTuioObject( TuioObject tuioObject ) { }
	public void updateTuioObject( TuioObject tuioObject ) { }
	public void removeTuioObject( TuioObject tuioObject ) { }
	public void refresh( TuioTime tuioTime ) { }
	public void addTuioCursor( TuioCursor tuioCursor ) {
		touchEvent(
			tuioCursor.getCursorID(),
			tuioCursor.getPosition().getX(), tuioCursor.getPosition().getY(),
			TOUCH_EVENT_DOWN
		);
	}
	public void updateTuioCursor( TuioCursor tuioCursor ) {
		touchEvent(
			tuioCursor.getCursorID(),
			tuioCursor.getPosition().getX(), tuioCursor.getPosition().getY(),
			TOUCH_EVENT_MOVE
		);
	}
	public void removeTuioCursor( TuioCursor tuioCursor ) {
		touchEvent(
			tuioCursor.getCursorID(),
			tuioCursor.getPosition().getX(), tuioCursor.getPosition().getY(),
			TOUCH_EVENT_UP
		);
	}


	// For thread safety, this should be invoked
	// from the event-dispatching thread.
	//
	private static void createUI() {
		if ( ! SwingUtilities.isEventDispatchThread() ) {
			System.out.println(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}

		GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		MultitouchFramework mf = new MultitouchFramework( caps );

		JFrame frame = new JFrame( Constant.PROGRAM_NAME );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		if ( Constant.HAS_MENUBAR ) {
			JMenuBar menuBar = mf.client.createMenuBar();
			if ( menuBar != null )
				frame.setJMenuBar(menuBar);
		}

		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		Container pane = frame.getContentPane();
		// We used to use a BoxLayout as the layout manager here,
		// but it caused problems with resizing behavior due to
		// a JOGL bug https://jogl.dev.java.net/issues/show_bug.cgi?id=135
		pane.setLayout( new BorderLayout() );
		if ( Constant.HAS_PANEL_OF_WIDGETS ) {
			JPanel panelOfWidgets = mf.client.createPanelOfWidgets();
			if ( panelOfWidgets != null )
				pane.add( panelOfWidgets, BorderLayout.LINE_START );
		}
		pane.add( mf, BorderLayout.CENTER );

		frame.pack();
		frame.setVisible( true );

		mf.start();
	}

	public static void main( String[] args ) {
		// Schedule the creation of the UI for the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					createUI();
				}
			}
		);
	}

}


