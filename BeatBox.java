import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

/**
 * 
 * @author 		Oleg Hnashuk
 * @since		2014-01-08
 * @version		1.0
 */
public class BeatBox {
	JPanel mainPanel;
	ArrayList<JCheckBox> checkboxList;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JFrame theFrame;
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
			"Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
			"High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
			"Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
			"Open Hi Conga"};
	int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

	public static void main(String[] args) {
		new BeatBox().buildGUI();
	}
	
	/**
	 * Function that builds interface of the program and calls setUpMidi function
	 */
	public void buildGUI() {
		theFrame = new JFrame("Cyber BeatBox");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		checkboxList = new ArrayList<JCheckBox>();
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
		JButton start = new JButton("Start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo = new JButton("Tempo Up");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo Down");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton serializeIt = new JButton("serializeIt");
		serializeIt.addActionListener(new MySendListener());
		buttonBox.add(serializeIt);
		
		JButton restore = new JButton("restore");
		restore.addActionListener(new MyReadInListener());
		buttonBox.add(restore);
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for(int i = 0; i < 16; i++) {
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		theFrame.getContentPane().add(background);
		
		GridLayout grid = new GridLayout(16, 16);
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);
		
		for(int i = 0; i < 256; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkboxList.add(c);
			mainPanel.add(c);
		}
		
		setUpMidi();
		
		theFrame.setBounds(50, 50, 300, 300);
		theFrame.pack();
		theFrame.setVisible(true);	
	}
	/**
	 * Creates and opens MIDI sequencer, then creates sequence and new track
	 */
	public void setUpMidi() {
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
		} catch(Exception e) {e.printStackTrace();}
	}
	/**
	 * Deletes old track and builds new one, then starts to play it in continuous loop
	 */
	public void buildTrackAndStart() {
		int[] trackList = null;
		
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		
		for(int i = 0; i < 16; i++) {
			trackList = new int[16];
			
			int key = instruments[i];
			
			for(int j = 0; j < 16; j++) {
				JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));
				if(jc.isSelected()) {
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}
			}
			
			makeTracks(trackList);
			track.add(makeEvent(176, 1, 127, 0, 16));
		}
		
		track.add(makeEvent(192, 9, 1, 0, 15));
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch(Exception e) {e.printStackTrace();}
	}
	/**
	 * Inner calss
	 * @author 		Oleg Hnashuk
	 * @since		2014-01-08
	 * @version		1.0
	 */
	public class MyStartListener implements ActionListener {
		/**
		 * @param	a	ActionEvent
		 */
		public void actionPerformed(ActionEvent a) {
			buildTrackAndStart();
		}
	}
	/**
	 * Inner class
	 * @author 		Oleg Hnashuk
	 * @since		2014-01-08
	 * @version		1.0
	 */
	public class MyStopListener implements ActionListener {
		/**
		 * @param	a	ActionEvent
		 */
		public void actionPerformed(ActionEvent a) {
			sequencer.stop();
		}
	}
	/**
	 * Inner class
	 * @author 		Oleg Hnashuk
	 * @since		2014-01-08
	 * @version		1.0
	 */
	public class MyUpTempoListener implements ActionListener {
		/**
		 * @param	a	ActionEvent
		 */
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * 1.03));
		}
	}
	/**
	 * Inner class
	 * @author 		Oleg Hnashuk
	 * @since		2014-01-08
	 * @version		1.0
	 */
	public class MyDownTempoListener implements ActionListener {
		/**
		 * @param	a	ActionEvent
		 */
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * .97));
		}
	}
	/**
	 * Inner class
	 * @author 		Oleg Hnashuk
	 * @since		2014-01-08
	 * @version		1.0
	 */
	public class MySendListener implements ActionListener {
		/**
		 * @param	a	ActionEvent
		 */
		public void actionPerformed(ActionEvent a) {
			
			boolean[] checkboxState = new boolean[256];
			for(int i = 0; i < 256; i++) {
				JCheckBox check = (JCheckBox) checkboxList.get(i);
				if(check.isSelected()) {
					checkboxState[i] = true;
				}
			}
			
			JFileChooser saveFile = new JFileChooser();
			saveFile.showSaveDialog(theFrame);
			try {
				 FileOutputStream fileStream = new FileOutputStream(saveFile.getSelectedFile());
				 ObjectOutputStream os = new ObjectOutputStream(fileStream);
				 os.writeObject(checkboxState);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	/**
	 * Inner class
	 * @author 		Oleg Hnashuk
	 * @since		2014-01-08
	 * @version		1.0
	 */
	public class MyReadInListener implements ActionListener {
		/**
		 * @param	a	ActionEvent
		 */
		public void actionPerformed(ActionEvent a) {
			boolean[] checkboxState = null;
			JFileChooser readFile = new JFileChooser();
			readFile.showOpenDialog(theFrame);
			try {
				FileInputStream fileIn = new FileInputStream(readFile.getSelectedFile());
				ObjectInputStream is = new ObjectInputStream(fileIn);
				checkboxState = (boolean[]) is.readObject();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
			for(int i = 0; i < 256; i++) {
				JCheckBox check = (JCheckBox) checkboxList.get(i);
				if(checkboxState[i]) {
					check.setSelected(true);
				} else {
					check.setSelected(false);
				}
			}
			sequencer.stop();
			buildTrackAndStart();
		}
	}
	/**
	 * Adds MidiEvents to track created by calling makeEvent function for each instrument
	 * 
	 * @param list	Array of integers representing numbers of instruments to pass them as arguments in makeEvent call
	 */
	public void makeTracks(int[] list) {
		for(int i = 0; i < 16; i++) {
			int key = list[i];
			
			if(key != 0) {
				track.add(makeEvent(144, 9, key, 100, i));
				track.add(makeEvent(128, 9, key, 100, i + 1));
			}
		}
	}
	/**
	 * Creates MidiEvent
	 * 
	 * @param comd	number of command in setMessage call
	 * @param chan	number of channel in setMessage call
	 * @param one	number of instrument in setMessage call
	 * @param two	integer representing velocity
	 * @param tick	the time-stamp for the event, in MIDI ticks
	 * @return	new MidiEvent to add to track
	 */
	public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
		MidiEvent event = null;
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent(a, tick);
		} catch(Exception e) {e.printStackTrace(); }
		return event;
	}
}
