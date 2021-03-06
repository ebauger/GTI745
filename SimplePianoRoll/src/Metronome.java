import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;

/*
 * Code found online
 * By: tkojitu
 * From: https://gist.github.com/tkojitu/1751867
 */
 
public class Metronome implements MetaEventListener {
    private Sequencer sequencer;
    private int bpm;
 
    public void start(int bpm) {
        try {
            this.bpm = bpm;
            openSequencer();
            Sequence seq = createSequence();
            startSequence(seq);
        } catch (InvalidMidiDataException | MidiUnavailableException ex) {
            Logger.getLogger(Metronome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stop()
    {
    	if(isStarted())
    	{
	        sequencer.stop();
	        sequencer.removeMetaEventListener(this);
	        sequencer.close();
	        sequencer = null;
    	}
    }
    
    public boolean isStarted()
    {
    	return sequencer != null && sequencer.isOpen() && sequencer.isRunning();
    }
    
    public void updateBpm(int newBpm)
    {
    	if(bpm == newBpm) return;
    	bpm = newBpm;
    	if(isStarted())
    		sequencer.setTempoInBPM(bpm);
    }
 
    private void openSequencer() throws MidiUnavailableException {
        sequencer = MidiSystem.getSequencer();
        sequencer.open();
        sequencer.addMetaEventListener(this);
    }
 
    private Sequence createSequence() {
        try {
            Sequence seq = new Sequence(Sequence.PPQ, 1);
            Track track = seq.createTrack();
 
            ShortMessage msg = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 9, 1, 0);
            MidiEvent evt = new MidiEvent(msg, 0);
            track.add(evt);
 
            addNoteEvent(track, 0);
            addNoteEvent(track, 1);
            addNoteEvent(track, 2);
            addNoteEvent(track, 3);
 
            msg = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 9, 1, 0);
            evt = new MidiEvent(msg, 4);
            track.add(evt);
            return seq;
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(Metronome.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
 
    private void addNoteEvent(Track track, long tick) throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage(ShortMessage.NOTE_ON, 9, 37, 100);
        MidiEvent event = new MidiEvent(message, tick);
        track.add(event);
    }
 
    private void startSequence(Sequence seq) throws InvalidMidiDataException {
        sequencer.setSequence(seq);
        sequencer.setTempoInBPM(bpm);
        sequencer.start();
    }
 
    public static void main(String[] args) throws Exception {
        int bpm = 60;
        if (args.length > 0) {
            try {
                bpm = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                bpm = 0;
            }
            if (bpm == 0)
                bpm = 60;
        }
        new Metronome().start(bpm);
    }
 
    @Override
    public void meta(MetaMessage message) {
        if (message.getType() != 47) {  // 47 is end of track
            return;
        }
        doLoop();
    }
 
    private void doLoop() {
        if (sequencer == null || !sequencer.isOpen()) {
            return;
        }
        sequencer.setTickPosition(0);
        sequencer.start();
        sequencer.setTempoInBPM(bpm);
    }
}
