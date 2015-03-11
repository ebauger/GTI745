import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class RandomScoreGenerator {

	private final int midiNoteStartOfOctave;
	private List<RandomNote> notes;
	private Random r;
	private final int totalWeight;

	public RandomScoreGenerator(List<RandomNote> notes, int midiNoteStartOfOctave)
	{
		this.notes = notes;
		this.midiNoteStartOfOctave = midiNoteStartOfOctave;
		
		int tempWeight = 0;
		for(RandomNote note : this.notes)
		{
			tempWeight += note.getWeight();
		}
		totalWeight = tempWeight;
		
		r = new Random();
	}
	
	public int getNextNote()
	{
		int rand = r.nextInt(totalWeight);
		
		for(RandomNote note : notes)
		{
			if (rand <= 0) return midiNoteStartOfOctave + note.getValue();
			rand -= note.getWeight();
		}
		return midiNoteStartOfOctave;
	}
}

class RandomNote
{
	public static List<RandomNote> getMajorScale()
	{
		return Collections.unmodifiableList(Arrays.asList(new RandomNote[] { C, D, E, F, G, A, B, C2 }));
	}
	
	private static final RandomNote C = new RandomNote("C", 0);
	private static final RandomNote CSharp = new RandomNote("C#", 1);
	private static final RandomNote D = new RandomNote("D", 2);
	private static final RandomNote DSharp = new RandomNote("D#", 3);
	private static final RandomNote E = new RandomNote("E", 4);
	private static final RandomNote F = new RandomNote("F", 5);
	private static final RandomNote FSharp = new RandomNote( "F#", 6);
	private static final RandomNote G = new RandomNote("G", 7);
	private static final RandomNote GSharp = new RandomNote("G#", 8);
	private static final RandomNote A = new RandomNote("A", 9);
	private static final RandomNote ASharp = new RandomNote("A#", 10);
	private static final RandomNote B = new RandomNote("B", 11);
	private static final RandomNote C2 = new RandomNote("C", 12);
	
	private String name;
	private int weight = 1;
	private int value;
	
	public String getName() { return name; }
	public int getWeight() { return weight; }
	public int getValue() { return value; }
	
	private RandomNote(String name, int value)
	{
		this.name = name;
		this.value = value;
	}
}