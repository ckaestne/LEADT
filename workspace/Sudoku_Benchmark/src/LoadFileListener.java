import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class LoadFileListener extends JFileChooser implements ActionListener {

	protected BoardManager bM;

	public LoadFileListener(BoardManager bM) {
		this.bM = bM;
	}

	public void actionPerformed(ActionEvent e) {
		showOpenDialog(null);
		if (null != getSelectedFile()) {
			try {
				boolean loaded = false;
				loaded = true;
				if (!bM.tryLoadFile(getSelectedFile()))
					JOptionPane.showMessageDialog(null,
							"Invalid sudoku! File was not loaded.");
				if (!loaded)
					bM.loadFile(getSelectedFile());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

}
