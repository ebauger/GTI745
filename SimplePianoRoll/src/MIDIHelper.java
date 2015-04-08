import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.sound.midi.InvalidMidiDataException;

public class MIDIHelper {

	public static void main(String[] args) throws IOException, InvalidMidiDataException {
		// TODO Auto-generated method stub
		loadMidiFile("/Users/etienne/Music/for_elise_by_beethoven.mid");
	}
	
	public static Score loadMidiFile(String file) throws IOException, InvalidMidiDataException{
		
		File f  =  new File(file) ;

		 // désérialization de l'objet
		try {
			 // ouverture d'un flux sur un fichier
			ObjectInputStream ois =  new ObjectInputStream(new FileInputStream(f));
					
			return (Score)ois.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	
	public static void saveMidiFile(Score s, String file){
		
		File f = new File(file);
		 // sérialization de l'objet
		try {
			 // ouverture d'un flux sur un fichier
			ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(f)) ;
			oos.writeObject(s);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
	}

}
