
import java.util.ArrayList;
import java.awt.Container;
import java.awt.Component;
import java.awt.Graphics;
// import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;



/*
   The octave:
      pitch class     English name     French name
           0             C             do
           1             C#/Db         do diese / re bemol
           2             D             re
           3             D#/Eb         re diese / mi bemol
           4             E             mi
           5             F             fa
           6             F#/Gb         fa diese / sol bemol
           7             G             sol
           8             G#/Ab         sol diese / la bemol
           9             A             la
          10             A#/Bb         la diese / si bemol
          11             B             si
           0             C             do

   A grand piano keyboard has 88 keys:
                              Note Name     MIDI note number     Pitch class
      lowest key (1st key):       A0            21                     9
      middle C:                   C4            60                     0
      highest key (88th key):     C8           108                     0
*/


class Note {
	public int beat;
	public int midi;
	
	public Note(int beat, int midi)
	{
		this.beat = beat;
		this.midi = midi;
	}
	
	@Override 
	public boolean equals(Object arg)
	{
		if (this == arg) return true;
		if (!(arg instanceof Note)) return false;
		Note n = (Note) arg;
		return beat == n.beat &&
				midi == n.midi;
	}
}
class Score implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int midiNoteNumberOfMiddleC = 60;

	public int numPitches = 88;
	public static final int pitchClassOfLowestPitch = 9; // 9==A==la
	public static final int midiNoteNumberOfLowestPitch = 21;
	public int numBeats = 128;
	public boolean [][] grid;
	private ArrayList<Note> selectedNotes;

	public static final int numPitchesInOctave = 12;
	public String [] namesOfPitchClasses;
	public boolean [] pitchClassesInMajorScale;
	public boolean [] pitchClassesToEmphasizeInMajorScale;
	
	private static final int MAX_NUM_BEATS = 2000;
	private static final int MIN_NUM_BEATS = 30;
	
	public boolean isNoteActive(int x, int y)
	{
		return grid[x][y];
	}
	public void setNote(int x, int y, boolean value)
	{
		if(grid[x][y] != value)
			grid[x][y] = value;
		if(!value)
			selectedNotes.remove(new Note(x, y));
	}

	public Score() {
		grid = new boolean[ numBeats ][ numPitches ];
		selectedNotes = new ArrayList<Note>();

		namesOfPitchClasses = new String[ numPitchesInOctave ];
		namesOfPitchClasses[ 0] = "C";
		namesOfPitchClasses[ 1] = "C#";
		namesOfPitchClasses[ 2] = "D";
		namesOfPitchClasses[ 3] = "D#";
		namesOfPitchClasses[ 4] = "E";
		namesOfPitchClasses[ 5] = "F";
		namesOfPitchClasses[ 6] = "F#";
		namesOfPitchClasses[ 7] = "G";
		namesOfPitchClasses[ 8] = "G#";
		namesOfPitchClasses[ 9] = "A";
		namesOfPitchClasses[10] = "A#";
		namesOfPitchClasses[11] = "B";

		pitchClassesInMajorScale = new boolean[ numPitchesInOctave ];
		pitchClassesInMajorScale[ 0] = true;
		pitchClassesInMajorScale[ 1] = false;
		pitchClassesInMajorScale[ 2] = true;
		pitchClassesInMajorScale[ 3] = false;
		pitchClassesInMajorScale[ 4] = true;
		pitchClassesInMajorScale[ 5] = true;
		pitchClassesInMajorScale[ 6] = false;
		pitchClassesInMajorScale[ 7] = true;
		pitchClassesInMajorScale[ 8] = false;
		pitchClassesInMajorScale[ 9] = true;
		pitchClassesInMajorScale[10] = false;
		pitchClassesInMajorScale[11] = true;

		pitchClassesToEmphasizeInMajorScale = new boolean[ numPitchesInOctave ];
		pitchClassesToEmphasizeInMajorScale[ 0] = true;
		pitchClassesToEmphasizeInMajorScale[ 1] = false;
		pitchClassesToEmphasizeInMajorScale[ 2] = false;
		pitchClassesToEmphasizeInMajorScale[ 3] = false;
		pitchClassesToEmphasizeInMajorScale[ 4] = true;
		pitchClassesToEmphasizeInMajorScale[ 5] = true;
		pitchClassesToEmphasizeInMajorScale[ 6] = false;
		pitchClassesToEmphasizeInMajorScale[ 7] = true;
		pitchClassesToEmphasizeInMajorScale[ 8] = false;
		pitchClassesToEmphasizeInMajorScale[ 9] = false;
		pitchClassesToEmphasizeInMajorScale[10] = false;
		pitchClassesToEmphasizeInMajorScale[11] = false;
	}

	// returns -1 if out of bounds
	public int getMidiNoteNumberForMouseY( GraphicsWrapper gw, int mouse_y ) {
		float y = gw.convertPixelsToWorldSpaceUnitsY( mouse_y );
		int indexOfPitch = (int)(-y);
		if ( 0 <= indexOfPitch && indexOfPitch < numPitches )
			return indexOfPitch + midiNoteNumberOfLowestPitch;
		return -1;
	}

	// returns -1 if out of bounds
	public int getBeatForMouseX( GraphicsWrapper gw, int mouse_x ) {
		float x = gw.convertPixelsToWorldSpaceUnitsX( mouse_x );
		int indexOfBeat = (int)x;
		if ( 0 <= indexOfBeat && indexOfBeat < numBeats )
			return indexOfBeat;
		return -1;
	}

	public void draw(
		GraphicsWrapper gw,
		boolean highlightMajorCScale,
		int midiNoteNumber1ToHilite,
		int beat1ToHilite,
		int beat2ToHilite,
		boolean showMouseHelper
	) {
		for ( int y = 0; y < numPitches; y++ ) {
			int pitchClass = ( y + pitchClassOfLowestPitch ) % numPitchesInOctave;
			int midiNoteNumber = y + midiNoteNumberOfLowestPitch;
			if ( showMouseHelper && midiNoteNumber == midiNoteNumber1ToHilite ) { // mouse cursor
				gw.setColor( 0, 1, 1 );
				gw.fillRect( 0, -y-0.8f, numBeats, 0.6f );
			}

			if ( midiNoteNumber == midiNoteNumberOfMiddleC ) {
				gw.setColor( 1, 1, 1 );
				gw.fillRect( 0, -y-0.7f, numBeats, 0.4f );
			}
			else if ( pitchClass == 0 && highlightMajorCScale ) {
				gw.setColor( 1, 1, 1 );
				gw.fillRect( 0, -y-0.6f, numBeats, 0.2f );
			}
			else if ( pitchClassesToEmphasizeInMajorScale[ pitchClass ] && highlightMajorCScale ) {
				gw.setColor( 0.6f, 0.6f, 0.6f );
				gw.fillRect( 0, -y-0.6f, numBeats, 0.2f );
			}
			else if ( pitchClassesInMajorScale[ pitchClass ] || ! highlightMajorCScale ) {
				gw.setColor( 0.6f, 0.6f, 0.6f );
				gw.fillRect( 0, -y-0.55f, numBeats, 0.1f );
			}
		}
		for ( int x = 0; x < numBeats; x++ ) {
			if ( showMouseHelper && x == beat1ToHilite ) { // mouse cursor
				gw.setColor( 0, 1, 1 );
				gw.fillRect( x+0.2f, -numPitches, 0.6f, numPitches );
			}

			if ( x == beat2ToHilite ) { // time cursor
				gw.setColor( 1, 0, 0 );
				gw.fillRect( x+0.45f, -numPitches, 0.1f, numPitches );
			}
			else if ( x % 4 == 0 ) {
				gw.setColor( 0.6f, 0.6f, 0.6f );
				gw.fillRect( x+0.45f, -numPitches, 0.1f, numPitches );
			}
		}
		for ( int y = 0; y < numPitches; ++y ) {
			for ( int x = 0; x < numBeats; ++x ) {
				gw.setColor( 0, 0, 0 );
				if(selectedNotes.contains(new Note(x, y)))
				{
					gw.setColor( Color.ORANGE );
				}
				if ( grid[x][y] )
				{
					gw.fillRect( x+0.3f, -y-0.7f, 0.4f, 0.4f );
				}
			}
		}
	}

	public AlignedRectangle2D getBoundingRectangle() {
		return new AlignedRectangle2D(
			new Point2D(0,-numPitches),
			new Point2D(numBeats,0)
		);
	}

	public void clearGrid()
	{
		for ( int y = 0; y < numPitches; ++y ) {
			for ( int x = 0; x < numBeats; ++x ) {
				grid[x][y] = false;
			}
		}
		clearSelected();
	}
	
	public void clearSelected()
	{
		selectedNotes.clear();
	}
	
	public void selectNotesInRect(int startBeat, int endBeat, int startMidi, int endMidi)
	{
		for(int x = startBeat; x <= endBeat; x++)
		{
			for(int y = startMidi; y <= endMidi; y++)
			{
				Note n = new Note(x, y);
				if(grid[x][y] && !selectedNotes.contains(n))
				{
					selectedNotes.add(n);
				}
			}
		}
	}
	
	public void transpose(int delta_x, int delta_y)
	{
		synchronized(this)
		{
			for(Note n : selectedNotes)
			{
				int newBeat = n.beat + delta_x;
				int newMidi = n.midi - delta_y;
				
				grid[n.beat][n.midi] = false;
				grid[newBeat][newMidi] = true;
				
				n.beat = newBeat;
				n.midi = newMidi;
			}
		}
	}
	
	public void changeDuration(int variation)
	{
		synchronized(this)
		{
			int newNumBeats = numBeats + variation;
			
			if(newNumBeats < MIN_NUM_BEATS) newNumBeats = MIN_NUM_BEATS;
			if(newNumBeats > MAX_NUM_BEATS) newNumBeats = MAX_NUM_BEATS;
			
			if(newNumBeats != numBeats)
			{
				boolean[][] newGrid = new boolean[ newNumBeats ][ numPitches ];
				
				for ( int y = 0; y < numPitches; ++y ) {
					for ( int x = 0; x < numBeats && x < newNumBeats; ++x ) {
						newGrid[x][y] = grid[x][y];
					}
				}
				grid = newGrid;
				numBeats = newNumBeats;
			}
		}
	}
}

class MyCanvas extends JPanel implements KeyListener, MouseListener, MouseMotionListener, Runnable {

	SimplePianoRoll simplePianoRoll;
	GraphicsWrapper gw = new GraphicsWrapper();

	Score score = new Score();

	Thread thread = null;
	boolean threadSuspended;

	int currentBeat = 0;

	public static final int RADIAL_MENU_PLAY = 0;
	public static final int RADIAL_MENU_STOP = 1;
	public static final int RADIAL_MENU_DRAW = 2;
	public static final int RADIAL_MENU_ERASE = 3;

	public static final int CONTROL_MENU_ZOOM = 0;
	public static final int CONTROL_MENU_PAN = 1;
	public static final int CONTROL_MENU_TEMPO = 2;
	public static final int CONTROL_MENU_TOTAL_DURATION = 3;
	public static final int CONTROL_MENU_TRANSPOSE = 4;
	
	public static final int MAX_BPM = 500;
	public static final int MIN_BPM = 120;
	private int currentBPM = 220;
	private boolean currentBpmIsDirty = true;
	private Metronome metronome = new Metronome();
	
	public int getCurrentBpm() { return currentBPM; }
	public void setCurrentBpm( int newBpm )
	{
		if(newBpm > MAX_BPM) newBpm = MAX_BPM;
		if(newBpm < MIN_BPM) newBpm = MIN_BPM;
		
		if(newBpm == currentBPM) return;
		
		currentBPM = newBpm;
		currentBpmIsDirty = true;
	}

	RadialMenuWidget radialMenu = new RadialMenuWidget();
	ControlMenuWidget controlMenu = new ControlMenuWidget();

	int mouse_x, mouse_y, old_mouse_x, old_mouse_y, 
		startOfSelectRect_x = -1, startOfSelectRect_y = -1;

	boolean isControlKeyDown = false;

	int beatOfMouseCursor = -1; // -1 for none
	int midiNoteNumberOfMouseCurser = -1; // -1 for none

	public MyCanvas( SimplePianoRoll sp ) {
		simplePianoRoll = sp;
		setBorder( BorderFactory.createLineBorder( Color.black ) );
		setBackground( Color.white );
		addKeyListener( this );
		addMouseListener( this );
		addMouseMotionListener( this );

		radialMenu.setItemLabelAndID( RadialMenuWidget.CENTRAL_ITEM, "",            RADIAL_MENU_STOP );
		radialMenu.setItemLabelAndID( 1,                             "Stop Music",  RADIAL_MENU_STOP );
		radialMenu.setItemLabelAndID( 3,                             "Draw Notes",  RADIAL_MENU_DRAW );
		radialMenu.setItemLabelAndID( 5,                             "Play Music",  RADIAL_MENU_PLAY );
		radialMenu.setItemLabelAndID( 7,                             "Erase Notes", RADIAL_MENU_ERASE );

		controlMenu.setItemLabelAndID( ControlMenuWidget.CENTRAL_ITEM, "", -1 );
		controlMenu.setItemLabelAndID( 1, "Tempo", CONTROL_MENU_TEMPO );
		controlMenu.setItemLabelAndID( 2, "Pan", CONTROL_MENU_PAN );
		controlMenu.setItemLabelAndID( 3, "Zoom", CONTROL_MENU_ZOOM );
		controlMenu.setItemLabelAndID( 5, "Total Duration", CONTROL_MENU_TOTAL_DURATION );
		controlMenu.setItemLabelAndID( 7, "Transpose", CONTROL_MENU_TRANSPOSE );

		gw.frame( score.getBoundingRectangle(), false );
	}
	public Dimension getPreferredSize() {
		return new Dimension( Constant.INITIAL_WINDOW_WIDTH, Constant.INITIAL_WINDOW_HEIGHT );
	}
	public void clear() {
		score.clearGrid();
		repaint();
	}
	public void clearSelectedNotes()
	{
		score.clearSelected();
		repaint();
	}
	public void frameAll() {
		gw.frame( score.getBoundingRectangle(), false );
		repaint();
	}
	public void paintComponent( Graphics g ) {
		super.paintComponent( g );
		gw.set( g );
		if ( getWidth() != gw.getWidth() || getHeight() != gw.getHeight() )
			gw.resize( getWidth(), getHeight() );
		gw.clear(0.4f,0.4f,0.4f);
		gw.setupForDrawing();
		gw.setCoordinateSystemToWorldSpaceUnits();
		gw.enableAlphaBlending();

		score.draw(
			gw,
			simplePianoRoll.highlightMajorScale,
			midiNoteNumberOfMouseCurser,
			beatOfMouseCursor,
			currentBeat,
			simplePianoRoll.dragMode != SimplePianoRoll.DM_SELECT_NOTES
		);

		gw.setCoordinateSystemToPixels();

		if ( radialMenu.isVisible() )
			radialMenu.draw( gw );
		if ( controlMenu.isVisible() )
			controlMenu.draw( gw );

		if ( ! radialMenu.isVisible() && ! controlMenu.isVisible() ) {
			// draw datatip
			if ( midiNoteNumberOfMouseCurser >= 0 && beatOfMouseCursor >= 0 ) {
				final int margin = 5;
				final int x_offset = 15;

				String s = score.namesOfPitchClasses[
					( midiNoteNumberOfMouseCurser - score.midiNoteNumberOfLowestPitch + score.pitchClassOfLowestPitch )
					% score.numPitchesInOctave
				];
				int x0 = mouse_x + x_offset;
				int y0 = mouse_y - RadialMenuWidget.textHeight - 2*margin;
				int height = RadialMenuWidget.textHeight + 2*margin;
				int width = Math.round( gw.stringWidth( s ) + 2*margin );
				gw.setColor( 0, 0, 0, 0.6f );
				gw.fillRect( x0, y0, width, height );
				gw.setColor( 1, 1, 1 );
				gw.drawRect( x0, y0, width, height );
				gw.drawString( mouse_x + x_offset + margin, mouse_y - margin, s );
			}
			
			if(startOfSelectRect_x >= 0 && startOfSelectRect_y >= 0)
			{
				int x0 = startOfSelectRect_x > mouse_x ? mouse_x : startOfSelectRect_x;
				int y0 = startOfSelectRect_y > mouse_y ? mouse_y : startOfSelectRect_y;
				int height = Math.abs(startOfSelectRect_y - mouse_y);
				int width = Math.abs(startOfSelectRect_x - mouse_x);
				gw.setColor(Color.GREEN);
				gw.drawRect(x0, y0, width, height);
			}
			
		}
	}

	public void keyPressed( KeyEvent e ) {
		if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) {
			isControlKeyDown = true;
			if (
				beatOfMouseCursor>=0
				&& simplePianoRoll.rolloverMode == SimplePianoRoll.RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN
			)
				playNote( midiNoteNumberOfMouseCurser );
		}
	}
	public void keyReleased( KeyEvent e ) {
		if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) {
			isControlKeyDown = false;
			stopPlayingNote( midiNoteNumberOfMouseCurser );
		}
	}
	public void keyTyped( KeyEvent e ) {
	}


	public void mouseClicked( MouseEvent e ) { }
	public void mouseEntered( MouseEvent e ) { }
	public void mouseExited( MouseEvent e ) { }
	
	public void generateRandomScore(int numberOfNotesToGenerate)
	{
		score.clearGrid();
		
		RandomScoreGenerator r = new RandomScoreGenerator(RandomNote.getMajorScale(), score.midiNoteNumberOfMiddleC);
		for(int i = 0; i < numberOfNotesToGenerate; i++)
		{
			int noteValue = r.getNextNote() - score.midiNoteNumberOfLowestPitch;
			if ( noteValue >= 0 && !score.grid[i][noteValue] ) {
				score.grid[i][noteValue] = true;
			}
		}
		repaint();
	}
	
	public void paintMidiScore(){
		score.clearGrid();
		//Load MidiHelp with file mid
		//loop on grid notes.
		repaint();
	}
	
	public void setScore(Score s){
		this.score = s;
	}

	private void paint( int mouse_x, int mouse_y ) {
		int newBeatOfMouseCursor = score.getBeatForMouseX( gw, mouse_x );
		int newMidiNoteNumberOfMouseCurser = score.getMidiNoteNumberForMouseY( gw, mouse_y );
		if (
			newBeatOfMouseCursor != beatOfMouseCursor
			|| newMidiNoteNumberOfMouseCurser != midiNoteNumberOfMouseCurser
		) {
			beatOfMouseCursor = newBeatOfMouseCursor;
			midiNoteNumberOfMouseCurser = newMidiNoteNumberOfMouseCurser;
			repaint();
		}

		if ( beatOfMouseCursor >= 0 && midiNoteNumberOfMouseCurser >= 0 ) {
			if ( simplePianoRoll.dragMode == SimplePianoRoll.DM_DRAW_NOTES ) {
				if ( score.grid[beatOfMouseCursor][midiNoteNumberOfMouseCurser-score.midiNoteNumberOfLowestPitch] != true ) {
					score.grid[beatOfMouseCursor][midiNoteNumberOfMouseCurser-score.midiNoteNumberOfLowestPitch] = true;
					repaint();
				}
			}
			else if ( simplePianoRoll.dragMode == SimplePianoRoll.DM_ERASE_NOTES ) {
				if ( score.grid[beatOfMouseCursor][midiNoteNumberOfMouseCurser-score.midiNoteNumberOfLowestPitch] != false ) {
					score.grid[beatOfMouseCursor][midiNoteNumberOfMouseCurser-score.midiNoteNumberOfLowestPitch] = false;
					repaint();
				}
			}
		}
	}

	public void mousePressed( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

		isControlKeyDown = e.isControlDown();

		if ( radialMenu.isVisible() || (SwingUtilities.isLeftMouseButton(e) && e.isControlDown()) ) {
			int returnValue = radialMenu.pressEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( controlMenu.isVisible() || (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown()) ) {
			int returnValue = controlMenu.pressEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			if(simplePianoRoll.dragMode == SimplePianoRoll.DM_SELECT_NOTES)
			{
				startOfSelectRect_x = mouse_x;
				startOfSelectRect_y = mouse_y;
			}
			else
			{
				paint( mouse_x, mouse_y );
			}
		}
	}

	public void mouseReleased( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

		isControlKeyDown = e.isControlDown();

		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.releaseEvent( mouse_x, mouse_y );

			int itemID = radialMenu.getIDOfSelection();
			if ( 0 <= itemID ) {
				switch ( itemID ) {
					case RADIAL_MENU_PLAY:
						simplePianoRoll.setMusicPlaying( true );
						break;
					case RADIAL_MENU_STOP:
						simplePianoRoll.setMusicPlaying( false );
						break;
					case RADIAL_MENU_DRAW:
						simplePianoRoll.setDragMode( SimplePianoRoll.DM_DRAW_NOTES );
						break;
					case RADIAL_MENU_ERASE:
						simplePianoRoll.setDragMode( SimplePianoRoll.DM_ERASE_NOTES );
						break;
				}
			}

			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( controlMenu.isVisible() ) {
			metronome.stop();
			
			int returnValue = controlMenu.releaseEvent( mouse_x, mouse_y );

			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if(simplePianoRoll.dragMode == SimplePianoRoll.DM_SELECT_NOTES)
		{
			int x0 = startOfSelectRect_x > mouse_x ? mouse_x : startOfSelectRect_x;
			int y0 = startOfSelectRect_y > mouse_y ? mouse_y : startOfSelectRect_y;
			int height = Math.abs(startOfSelectRect_y - mouse_y);
			int width = Math.abs(startOfSelectRect_x - mouse_x);
			
			int startBeat = score.getBeatForMouseX(gw, x0);
			int endBeat = score.getBeatForMouseX(gw, x0 + width);
			int startMidi = score.getMidiNoteNumberForMouseY(gw, y0 + height) - score.midiNoteNumberOfLowestPitch;
			int endMidi = score.getMidiNoteNumberForMouseY(gw, y0) - score.midiNoteNumberOfLowestPitch;
			
			score.selectNotesInRect(startBeat, endBeat, startMidi, endMidi);
			
			startOfSelectRect_x = -1;
			startOfSelectRect_y = -1;
			repaint();
		}
	}

	private void playNote( int midiNoteNumber ) {
		if ( Constant.USE_SOUND && midiNoteNumber >= 0 ) {
			simplePianoRoll.midiChannels[0].noteOn(midiNoteNumber,Constant.midiVolume);
		}
	}
	private void stopPlayingNote( int midiNoteNumber ) {
		if ( Constant.USE_SOUND && midiNoteNumber >= 0 ) {
			simplePianoRoll.midiChannels[0].noteOff(midiNoteNumber);
		}
	}

	public void mouseMoved( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

		requestFocusInWindow();

		isControlKeyDown = e.isControlDown();

		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.moveEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( controlMenu.isVisible() ) {
			int returnValue = controlMenu.moveEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		else {
			int newBeatOfMouseCursor = score.getBeatForMouseX( gw, mouse_x );
			int newMidiNoteNumberOfMouseCurser = score.getMidiNoteNumberForMouseY( gw, mouse_y );
			if ( newBeatOfMouseCursor != beatOfMouseCursor ) {
				beatOfMouseCursor = newBeatOfMouseCursor;
				repaint();
			}
			if ( newMidiNoteNumberOfMouseCurser != midiNoteNumberOfMouseCurser ) {
				stopPlayingNote( midiNoteNumberOfMouseCurser );
				midiNoteNumberOfMouseCurser = newMidiNoteNumberOfMouseCurser;
				if (
					beatOfMouseCursor>=0
					&& (
						simplePianoRoll.rolloverMode == SimplePianoRoll.RM_PLAY_NOTE_UPON_ROLLOVER
						|| (
							simplePianoRoll.rolloverMode == SimplePianoRoll.RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN
							&& isControlKeyDown
						)
					)
				)
					playNote( midiNoteNumberOfMouseCurser );
				repaint();
			}
		}

	}

	public void mouseDragged( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		int delta_x = mouse_x - old_mouse_x;
		int delta_y = mouse_y - old_mouse_y;

		isControlKeyDown = e.isControlDown();

		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.dragEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( controlMenu.isVisible() ) {
			if ( controlMenu.isInMenuingMode() ) {
				int returnValue = controlMenu.dragEvent( mouse_x, mouse_y );
				if ( returnValue == CustomWidget.S_REDRAW )
					repaint();
				if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
					return;
			}
			else {
				// use the drag event to change the appropriate parameter
				switch ( controlMenu.getIDOfSelection() ) {
				case CONTROL_MENU_PAN:
					gw.pan( delta_x, delta_y );
					break;
				case CONTROL_MENU_ZOOM:
					gw.zoomIn( (float)Math.pow( Constant.zoomFactorPerPixelDragged, delta_x-delta_y ) );
					break;
				case CONTROL_MENU_TRANSPOSE:
					score.transpose(delta_x, delta_y);
					break;
				case CONTROL_MENU_TEMPO:
					if(delta_y != 0)
					{
						int bpmChange = delta_y;
						if (bpmChange > 5) bpmChange = 5;
						if (bpmChange < -5) bpmChange = -5;
						setCurrentBpm(getCurrentBpm() - bpmChange);
						simplePianoRoll.setCurrentBpm(getCurrentBpm());
					}
					
					if(Constant.USE_SOUND)
					{
						if(!metronome.isStarted())
							metronome.start(getCurrentBpm());
						else
							metronome.updateBpm(getCurrentBpm());
					}
					break;
				case CONTROL_MENU_TOTAL_DURATION:
					if(delta_x != 0)
					{
						boolean autoFrame = simplePianoRoll.isAutoFrameActive;
						score.changeDuration(delta_x);
						
						if(autoFrame)
						{
							AlignedRectangle2D rect = score.getBoundingRectangle();
							gw.frame(rect, true);
						}
					}
					break;
				default:
					// TODO XXX
					break;
				}
				repaint();
			}
		}
		else {
			paint( mouse_x, mouse_y );
		}
	}

	public void startBackgroundWork() {
		currentBeat = 0;
		if ( thread == null ) {
			thread = new Thread( this );
			threadSuspended = false;
			thread.start();
		}
		else {
			if ( threadSuspended ) {
				threadSuspended = false;
				synchronized( this ) {
					notify();
				}
			}
		}
	}
	public void stopBackgroundWork() {
		threadSuspended = true;
	}
	public void run() {
		try {
			int sleepIntervalInMilliseconds = calculateSleepIntervalFromBpm(currentBPM);
			currentBpmIsDirty = false;
			while (true) {

				// Here's where the thread does some work
				synchronized( this ) {
					if ( Constant.USE_SOUND ) {
						for ( int i = 0; i < score.numPitches; ++i ) {
							if ( score.grid[currentBeat][i] )
								simplePianoRoll.midiChannels[0].noteOff( i+score.midiNoteNumberOfLowestPitch );
						}
					}
					currentBeat += 1;
					if ( currentBeat >= score.numBeats )
						currentBeat = 0;
					if ( Constant.USE_SOUND ) {
						for ( int i = 0; i < score.numPitches; ++i ) {
							if ( score.grid[currentBeat][i] )
								simplePianoRoll.midiChannels[0].noteOn( i+score.midiNoteNumberOfLowestPitch, Constant.midiVolume );
						}
					}
				}
				repaint();

				// Now the thread checks to see if it should suspend itself
				if ( threadSuspended ) {
					synchronized( this ) {
						while ( threadSuspended ) {
							wait();
						}
					}
				}
				if(currentBpmIsDirty)
				{
					sleepIntervalInMilliseconds = calculateSleepIntervalFromBpm(currentBPM);
					currentBpmIsDirty = false;
				}
				Thread.sleep( sleepIntervalInMilliseconds );  // interval given in milliseconds
			}
		}
		catch (InterruptedException e) { }
	}

	private static int calculateSleepIntervalFromBpm( int bpm )
	{
		return 1000 / (bpm / 60);
	}
}

public class SimplePianoRoll implements ActionListener, ChangeListener {

	static final String applicationName = "Simple Piano Roll";

	JFrame frame;
	Container toolPanel;
	MyCanvas canvas;

	Synthesizer synthesizer;
	MidiChannel [] midiChannels;

	JMenuItem openMenuItem;
	JMenuItem saveMenuItem;
	JMenuItem clearMenuItem;
	JMenuItem quitMenuItem;
	JCheckBoxMenuItem showToolsMenuItem;
	JCheckBoxMenuItem highlightMajorScaleMenuItem;
	JMenuItem frameAllMenuItem;
	JCheckBoxMenuItem autoFrameMenuItem;
	JMenuItem aboutMenuItem;

	JCheckBox playCheckBox;
	JCheckBox loopWhenPlayingCheckBox;

	JRadioButton drawNotesRadioButton;
	JRadioButton eraseNotesRadioButton;
	JRadioButton selectNotesRadioButton;

	JRadioButton doNothingUponRolloverRadioButton;
	JRadioButton playNoteUponRolloverRadioButton;
	JRadioButton playNoteUponRolloverIfSpecialKeyHeldDownRadioButton;
	
	JButton generateRandomScoreButton;
	
	JSpinner currentBpmSpinner;

	public boolean isMusicPlaying = false;
	public boolean isMusicLoopedWhenPlayed = false;
	public boolean highlightMajorScale = true;
	public boolean isAutoFrameActive = true;

	// The DM_ prefix is for Drag Mode
	public static final int DM_DRAW_NOTES = 0;
	public static final int DM_ERASE_NOTES = 1;
	public static final int DM_SELECT_NOTES = 2;
	public int dragMode = DM_DRAW_NOTES;

	// The RM_ prefix is for Rollover Mode
	public static final int RM_DO_NOTHING_UPON_ROLLOVER = 0;
	public static final int RM_PLAY_NOTE_UPON_ROLLOVER = 1;
	public static final int RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN = 2;
	public int rolloverMode = RM_DO_NOTHING_UPON_ROLLOVER;

	public void setMusicPlaying( boolean flag ) {
		isMusicPlaying = flag;
		playCheckBox.setSelected( isMusicPlaying );
		if ( isMusicPlaying )
			canvas.startBackgroundWork();
		else
			canvas.stopBackgroundWork();
	}
	public void setDragMode( int newDragMode ) {
		dragMode = newDragMode;
		canvas.clearSelectedNotes();
		if ( dragMode == DM_DRAW_NOTES )
			drawNotesRadioButton.setSelected(true);
		else if ( dragMode == DM_ERASE_NOTES )
			eraseNotesRadioButton.setSelected(true);
		else if( dragMode == DM_SELECT_NOTES )
			selectNotesRadioButton.setSelected(true);
		else assert false;
	}
	public void setCurrentBpm( int newBpm )
	{
		currentBpmSpinner.setValue(newBpm);
	}

	public void setRolloverMode( int newRolloverMode ) {
		rolloverMode = newRolloverMode;
		if ( rolloverMode == RM_DO_NOTHING_UPON_ROLLOVER )
			doNothingUponRolloverRadioButton.setSelected(true);
		else if ( rolloverMode == RM_PLAY_NOTE_UPON_ROLLOVER )
			playNoteUponRolloverRadioButton.setSelected(true);
		else if ( rolloverMode == RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN )
			playNoteUponRolloverIfSpecialKeyHeldDownRadioButton.setSelected(true);
		else assert false;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if ( source == clearMenuItem ) {
			canvas.clear();
		}
		else if ( source == quitMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really quit?",
				"Confirm Quit",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}
		else if ( source == showToolsMenuItem ) {
			Container pane = frame.getContentPane();
			if ( showToolsMenuItem.isSelected() ) {
				pane.removeAll();
				pane.add( toolPanel );
				pane.add( canvas );
			}
			else {
				pane.removeAll();
				pane.add( canvas );
			}
			frame.invalidate();
			frame.validate();
		}
		else if ( source == highlightMajorScaleMenuItem ) {
			highlightMajorScale = highlightMajorScaleMenuItem.isSelected();
			canvas.repaint();
		}
		else if ( source == frameAllMenuItem ) {
			canvas.frameAll();
			canvas.repaint();
		}
		else if ( source == autoFrameMenuItem ) {
			isAutoFrameActive = autoFrameMenuItem.isSelected();
			canvas.repaint();
		}
		else if ( source == aboutMenuItem ) {
			JOptionPane.showMessageDialog(
				frame,
				"'" + applicationName + "' Sample Program\n"
					+ "Original version written April 2011",
				"About",
				JOptionPane.INFORMATION_MESSAGE
			);
		}
		else if ( source == playCheckBox ) {
			isMusicPlaying = playCheckBox.isSelected();
			if ( isMusicPlaying )
				canvas.startBackgroundWork();
			else
				canvas.stopBackgroundWork();
		}
		else if ( source == loopWhenPlayingCheckBox ) {
			isMusicLoopedWhenPlayed = loopWhenPlayingCheckBox.isSelected();
		}
		else if ( source == drawNotesRadioButton ) {
			canvas.clearSelectedNotes();
			dragMode = DM_DRAW_NOTES;
		}
		else if ( source == eraseNotesRadioButton ) {
			canvas.clearSelectedNotes();
			dragMode = DM_ERASE_NOTES;
		}
		else if ( source == selectNotesRadioButton ) {
			canvas.clearSelectedNotes();
			dragMode = DM_SELECT_NOTES;
		}
		else if ( source == doNothingUponRolloverRadioButton ) {
			rolloverMode = RM_DO_NOTHING_UPON_ROLLOVER;
		}
		else if ( source == playNoteUponRolloverRadioButton ) {
			rolloverMode = RM_PLAY_NOTE_UPON_ROLLOVER;
		}
		else if ( source == playNoteUponRolloverIfSpecialKeyHeldDownRadioButton ) {
			rolloverMode = RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN;
		}
		else if( source == generateRandomScoreButton ) {
			// TODO add numerical up/down for number of notes
			canvas.generateRandomScore(30);
		}
		else if( source == openMenuItem) {
			JFileChooser chooser = new JFileChooser();
		    FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "MID, MIDI & SPR Files", "mid", "midi", "spr");
		    chooser.setFileFilter(filter);
		    int returnVal = chooser.showOpenDialog(openMenuItem);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       System.out.println("You chose to open this file: " +
		            chooser.getSelectedFile().getAbsolutePath());
			    try {
			    	canvas.score = MIDIHelper.loadMidiFile(chooser.getSelectedFile().getAbsolutePath());
				} catch (IOException | InvalidMidiDataException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		       
		    }
		}
		else if( source == saveMenuItem) {
			JFileChooser chooser = new JFileChooser();
		    FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "MID, MIDI & SPR Files", "mid", "midi", "spr");
		    chooser.setFileFilter(filter);
		    int returnVal = chooser.showSaveDialog(saveMenuItem);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       System.out.println("You chose to save this file: " +
		            chooser.getSelectedFile().getAbsolutePath());
			   MIDIHelper.saveMidiFile(canvas.score, chooser.getSelectedFile().getAbsolutePath());
			   canvas.repaint();
		    }
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if( source == currentBpmSpinner ) {
			canvas.setCurrentBpm((int) currentBpmSpinner.getValue());
		}
	}

	// For thread safety, this should be invoked
	// from the event-dispatching thread.
	//
	private void createUI() {
		if ( Constant.USE_SOUND ) {
			try {
				synthesizer = MidiSystem.getSynthesizer();
				synthesizer.open();
				midiChannels = synthesizer.getChannels();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ( ! SwingUtilities.isEventDispatchThread() ) {
			System.out.println(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}

		frame = new JFrame( applicationName );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
				openMenuItem = new JMenuItem("Open");
				openMenuItem.addActionListener(this);
				menu.add(openMenuItem);
				saveMenuItem = new JMenuItem("Save");
				saveMenuItem.addActionListener(this);
				menu.add(saveMenuItem);
				clearMenuItem = new JMenuItem("Clear");
				clearMenuItem.addActionListener(this);
				menu.add(clearMenuItem);

				menu.addSeparator();

				quitMenuItem = new JMenuItem("Quit");
				quitMenuItem.addActionListener(this);
				menu.add(quitMenuItem);
			menuBar.add(menu);
			menu = new JMenu("View");
				showToolsMenuItem = new JCheckBoxMenuItem("Show Options");
				showToolsMenuItem.setSelected( true );
				showToolsMenuItem.addActionListener(this);
				menu.add(showToolsMenuItem);

				highlightMajorScaleMenuItem = new JCheckBoxMenuItem("Highlight Major C Scale");
				highlightMajorScaleMenuItem.setSelected( highlightMajorScale );
				highlightMajorScaleMenuItem.addActionListener(this);
				menu.add(highlightMajorScaleMenuItem);

				menu.addSeparator();

				frameAllMenuItem = new JMenuItem("Frame All");
				frameAllMenuItem.addActionListener(this);
				menu.add(frameAllMenuItem);

				autoFrameMenuItem = new JCheckBoxMenuItem("Auto Frame");
				autoFrameMenuItem.setSelected( isAutoFrameActive );
				autoFrameMenuItem.addActionListener(this);
				menu.add(autoFrameMenuItem);
			menuBar.add(menu);
			menu = new JMenu("Help");
				aboutMenuItem = new JMenuItem("About");
				aboutMenuItem.addActionListener(this);
				menu.add(aboutMenuItem);
			menuBar.add(menu);
		frame.setJMenuBar(menuBar);

		toolPanel = new JPanel();
		toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.Y_AXIS ) );

		canvas = new MyCanvas(this);

		Container pane = frame.getContentPane();
		pane.setLayout( new BoxLayout( pane, BoxLayout.X_AXIS ) );
		pane.add( toolPanel );
		pane.add( canvas );

		playCheckBox = new JCheckBox("Play", isMusicPlaying );
		playCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		playCheckBox.addActionListener(this);
		toolPanel.add( playCheckBox );

		loopWhenPlayingCheckBox = new JCheckBox("Loop when playing", isMusicLoopedWhenPlayed );
		loopWhenPlayingCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		loopWhenPlayingCheckBox.addActionListener(this);
		toolPanel.add( loopWhenPlayingCheckBox );

		toolPanel.add( Box.createRigidArea(new Dimension(1,20)) );
		toolPanel.add( new JLabel("During dragging:") );

		ButtonGroup dragModeButtonGroup = new ButtonGroup();

			drawNotesRadioButton = new JRadioButton( "Draw Notes" );
			drawNotesRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			drawNotesRadioButton.addActionListener(this);
			if ( dragMode == DM_DRAW_NOTES ) drawNotesRadioButton.setSelected(true);
			toolPanel.add( drawNotesRadioButton );
			dragModeButtonGroup.add( drawNotesRadioButton );

			eraseNotesRadioButton = new JRadioButton( "Erase Notes" );
			eraseNotesRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			eraseNotesRadioButton.addActionListener(this);
			if ( dragMode == DM_ERASE_NOTES ) eraseNotesRadioButton.setSelected(true);
			toolPanel.add( eraseNotesRadioButton );
			dragModeButtonGroup.add( eraseNotesRadioButton );
			
			selectNotesRadioButton = new JRadioButton( "Select Notes" );
			selectNotesRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			selectNotesRadioButton.addActionListener(this);
			if ( dragMode == DM_SELECT_NOTES ) selectNotesRadioButton.setSelected(true);
			toolPanel.add( selectNotesRadioButton );
			dragModeButtonGroup.add( selectNotesRadioButton );

		toolPanel.add( Box.createRigidArea(new Dimension(1,20)) );
		toolPanel.add( new JLabel("Upon cursor rollover:") );

		ButtonGroup rolloverModeButtonGroup = new ButtonGroup();

			doNothingUponRolloverRadioButton = new JRadioButton( "Do Nothing" );
			doNothingUponRolloverRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			doNothingUponRolloverRadioButton.addActionListener(this);
			if ( rolloverMode == RM_DO_NOTHING_UPON_ROLLOVER ) doNothingUponRolloverRadioButton.setSelected(true);
			toolPanel.add( doNothingUponRolloverRadioButton );
			rolloverModeButtonGroup.add( doNothingUponRolloverRadioButton );

			playNoteUponRolloverRadioButton = new JRadioButton( "Play Pitch" );
			playNoteUponRolloverRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			playNoteUponRolloverRadioButton.addActionListener(this);
			if ( rolloverMode == RM_PLAY_NOTE_UPON_ROLLOVER ) playNoteUponRolloverRadioButton.setSelected(true);
			toolPanel.add( playNoteUponRolloverRadioButton );
			rolloverModeButtonGroup.add( playNoteUponRolloverRadioButton );

			playNoteUponRolloverIfSpecialKeyHeldDownRadioButton = new JRadioButton( "Play Pitch if Ctrl down" );
			playNoteUponRolloverIfSpecialKeyHeldDownRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			playNoteUponRolloverIfSpecialKeyHeldDownRadioButton.addActionListener(this);
			if ( rolloverMode == RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN )
				playNoteUponRolloverIfSpecialKeyHeldDownRadioButton.setSelected(true);
			toolPanel.add( playNoteUponRolloverIfSpecialKeyHeldDownRadioButton );
			rolloverModeButtonGroup.add( playNoteUponRolloverIfSpecialKeyHeldDownRadioButton );
			
		generateRandomScoreButton = new JButton( "Generate Random Score" );
		generateRandomScoreButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		generateRandomScoreButton.addActionListener(this);
		toolPanel.add( generateRandomScoreButton );
		
		JLabel lbl1 = new JLabel("BPM: ");
		lbl1.setAlignmentX( Component.LEFT_ALIGNMENT );
		toolPanel.add(lbl1);
    
		SpinnerModel m = new SpinnerNumberModel(canvas.getCurrentBpm(), canvas.MIN_BPM, canvas.MAX_BPM, 1);
		currentBpmSpinner = new JSpinner( m );
		currentBpmSpinner.addChangeListener(this);
		currentBpmSpinner.setAlignmentX( Component.LEFT_ALIGNMENT );
		currentBpmSpinner.setMaximumSize(new Dimension(75,30));
		toolPanel.add( currentBpmSpinner );
		
		frame.pack();
		frame.setVisible( true );

		assert canvas.isFocusable();

	}

	public static void main( String[] args ) {
		// Schedule the creation of the UI for the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					SimplePianoRoll sp = new SimplePianoRoll();
					sp.createUI();
				}
			}
		);
	}
}

