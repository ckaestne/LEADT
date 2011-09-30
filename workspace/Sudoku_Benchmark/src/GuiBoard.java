import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class GuiBoard extends JPanel {

	ListenerFactory listenerFactory;

	protected GuiBox[] boxes;

	public GuiBoard(ListenerFactory listenerFactory) {
		super();
		this.listenerFactory = listenerFactory;
		boxes = new GuiBox[Field.POSSIBILITIES];
		this.setLayout(new GridLayout((int) Math.round(Math
				.sqrt(Field.POSSIBILITIES)), (int) Math.round(Math
				.sqrt(Field.POSSIBILITIES))));
		for (int i = 0; i < Field.POSSIBILITIES; i++) {
			boxes[i] = new GuiBox(i, listenerFactory);
			this.add(boxes[i]);
		}
	}

	public void update(Board b) {
		for (int boxNr = 0; boxNr < Field.POSSIBILITIES; boxNr++) {
			for (int fieldNr = 0; fieldNr < Field.POSSIBILITIES; fieldNr++) {
				Field field = b.getField(Structure.BOX, boxNr, fieldNr);
				JLabel label = boxes[boxNr].getLabel(fieldNr);
				label.setOpaque(true);
				if (field.isInitialSet()) {
					label.setText(String.valueOf(field.getValue()));
					label.setBackground(Color.LIGHT_GRAY);
					label.setBackground(Color.BLACK);
					label.setForeground(Color.ORANGE);
				} else if (field.isSet()) {
					label.setText(String.valueOf(field.getValue()));
					label.setBackground(Color.WHITE);
					label.setForeground(Color.BLACK);
					label.setBackground(Color.ORANGE);
				} else {
					label.setText("");
					label.setBackground(Color.WHITE);
					label.setBackground(Color.WHITE);
				}
			}
		}
	}

}
