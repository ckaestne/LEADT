import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class Gui extends JFrame {

	protected GuiBoard guiBoard;

	protected ListenerFactory listenerFactory;

	protected JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createOptionsMenu());
		return menuBar;
	}

	protected JPanel createContentPane() {
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		// pane.add(guiBoard);
		return pane;
	}

	protected JMenu createFileMenu() {
		JMenu fileMenu = new JMenu();
		fileMenu.setText("File");
		fileMenu.add(createLoadMenuItem());
		fileMenu.add(createExitMenuItem());
		fileMenu.add(createOpenStateMenuItem());
		fileMenu.add(createSaveStateMenuItem());
		fileMenu.add(createGenerateSudokuMenuItem());
		return fileMenu;
	}

	protected JMenu createOptionsMenu() {
		JMenu optionsMenu = new JMenu();
		optionsMenu.setText("Options");
		optionsMenu.add(createUndoMenuItem());
		optionsMenu.add(createSolutionHintMenuItem());
		optionsMenu.add(createSetPossibilitiesMenuItem());
		return optionsMenu;
	}

	protected JMenuItem createExitMenuItem() {
		JMenuItem exitMenuItem = new JMenuItem();
		exitMenuItem.setText("Exit");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				Event.CTRL_MASK, true));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		return exitMenuItem;
	}

	protected JMenuItem createLoadMenuItem() {
		JMenuItem loadMenuItem = new JMenuItem();
		loadMenuItem.setText("Load");
		loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				Event.CTRL_MASK, true));
		loadMenuItem.addActionListener(listenerFactory.getLoadFileListener());
		return loadMenuItem;
	}

	public Gui(ListenerFactory listenerFactory) {
		super();
		// guiBoard = new GuiBoard(listenerFactory);
		this.listenerFactory = listenerFactory;
		setContentPane(createContentPane());
		setJMenuBar(createMenuBar());

		JPanel glassPane = new JPanel(new BorderLayout());
		glassPane.setBackground(new Color(Color.BLACK.getRed(), Color.BLACK
				.getGreen(), Color.BLACK.getBlue(), GLASS_PANE_ALPHA));
		glassPane.addMouseListener(new MouseListener() {

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		glassPane.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		JLabel label = new JLabel("Busy...", JLabel.CENTER);
		label.setForeground(Color.white);
		glassPane.add(label, BorderLayout.CENTER);
		setGlassPane(glassPane);
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	protected void createAndShowGUI() {
		setTitle("Sudoku");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setResizable(false);
		setVisible(true);
	}

	protected void update(Board b) {
		getGlassPane().setVisible(true);
		if (null != b) {
			if (null == guiBoard) {
				guiBoard = new GuiBoard(listenerFactory);
				getContentPane().add(guiBoard);
			}
			guiBoard.update(b);
		} else {
			if (null != guiBoard) {
				getContentPane().remove(guiBoard);
				guiBoard = null;
			}

		}
		pack();
		getGlassPane().setVisible(false);
	}

	protected JMenuItem createSaveStateMenuItem() {
		JMenuItem saveStateMenuItem = new JMenuItem();
		saveStateMenuItem.setText("Save State");
		saveStateMenuItem.addActionListener(listenerFactory
				.getSaveStateListener());
		saveStateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Event.CTRL_MASK, true));
		return saveStateMenuItem;
	}

	protected JMenuItem createOpenStateMenuItem() {
		JMenuItem openStateMenuItem = new JMenuItem();
		openStateMenuItem.setText("Open State");
		openStateMenuItem.addActionListener(listenerFactory
				.getLoadStateListener());
		openStateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				Event.CTRL_MASK, true));
		return openStateMenuItem;
	}

	protected JMenuItem createUndoMenuItem() {
		JMenuItem loadMenuItem = new JMenuItem();
		loadMenuItem.setText("Undo");
		loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
				Event.CTRL_MASK, true));
		loadMenuItem.addActionListener(listenerFactory.getUndoListener());
		return loadMenuItem;
	}

	private static final int GLASS_PANE_ALPHA = 180;

	public void setBusy(boolean busy) {
		if (busy) {
			getGlassPane().setVisible(true);
		} else {
			getGlassPane().setVisible(false);
		}
	}

	protected JMenuItem createSolutionHintMenuItem() {
		JMenuItem solutionMenuItem = new JMenuItem();
		solutionMenuItem.setText("Solution Hint");
		solutionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
				Event.CTRL_MASK, true));
		solutionMenuItem.addActionListener(listenerFactory
				.getSolutionHintListener());
		return solutionMenuItem;
	}

	protected JMenuItem createGenerateSudokuMenuItem() {
		JMenuItem generateSudokuMenuItem = new JMenuItem();
		generateSudokuMenuItem.setText("Generate Sudoku");
		generateSudokuMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_G, Event.CTRL_MASK, true));
		generateSudokuMenuItem.addActionListener(listenerFactory
				.getGenerateSudokuListener());
		return generateSudokuMenuItem;
	}

	protected JMenuItem createSetPossibilitiesMenuItem() {
		JMenuItem setPossibilitiesMenuItem = new JMenuItem();
		setPossibilitiesMenuItem.setText("Set Possibilities");
		setPossibilitiesMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P, Event.CTRL_MASK, true));
		setPossibilitiesMenuItem.addActionListener(listenerFactory
				.getSetPossibilitiesListener());
		return setPossibilitiesMenuItem;
	}

}
