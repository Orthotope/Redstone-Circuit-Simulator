package Simulator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/*
 This class is the palette, from which you can chose blocks.
 */

public class Palette {
	public static final int MAX_PER_COL = 12;
	public static int selectId = 0;
	public static int oldSelectId = 0;

	public static Block setupPaletteBlock(BlockType id) {
		int state = 0;
		int dir = 3;
		switch (id) {
		case Torch:
			state = 1;
			break;
		case Repeater:
		case Piston:
		case StickyPiston:
			dir = 1;
			break;
		case Rail:
		case PowerRail:
		case DetectorRail:
			dir = 0;
			break;
		}
		return new Block(id, state, dir, 8, 0);
	}

	public static int getMaxCols() {
		return Math.max(getPaletteShape(Options.palette).width,
				getPaletteShape(Options.palette2).width);
	}

	public static Dimension getPaletteShape() {
		return getPaletteShape(getPal());
	}

	public static Dimension getPaletteShape(BlockType[][] pal) {
		for (int i = 1; i < 3; i++)
			if (pal.length / i <= MAX_PER_COL)
				return new Dimension(i, pal.length / i
						+ (pal.length % 2 == 0 ? 0 : 1));

		return new Dimension((pal.length + MAX_PER_COL - 1) / MAX_PER_COL, Math
				.min(pal.length, MAX_PER_COL));
	}

	public static JPanel setPalette() { // only for one level view
		Dimension pals = Palette.getPaletteShape();

		BlockType[][] pal = getPal();
		int palIndex = 0;

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JPanel[] cols = new JPanel[pals.width];
		for (int c = 0; c < pals.width; c++) {
			cols[c] = new JPanel();
			cols[c].setLayout(new BoxLayout(cols[c], BoxLayout.Y_AXIS));
			cols[c].setBorder(BorderFactory.createLineBorder(Color.gray, 2));

			int i;
			for (i = 0; i < pals.height && palIndex < pal.length; ++i, ++palIndex) {
				Graph canvas = new Graph();
				canvas.preparePaint(setupPaletteBlock(pal[palIndex][0]),
						setupPaletteBlock(pal[palIndex][1]), 32);
				canvas.setAlignmentX(Component.CENTER_ALIGNMENT);
				canvas.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

				JPanel temp = new JPanel();
				temp.setBackground(Color.gray);
				temp.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
				temp.add(canvas);
				cols[c].add(temp);
			}
			if (c != 0 && i < pals.height) {
				Dimension dims = new Dimension(36, 36 * (pals.height - i));
				cols[c].add(new Box.Filler(dims, dims, dims));
			}
			panel.add(cols[c]);
		}
		return panel;
	}

	public static void changeType(Point p) {
		BlockType[][] pal = Palette.getPal();
		Dimension pals = Palette.getPaletteShape();
		Rectangle r = new Rectangle(pals);
		p.x = (p.x - 3) / 36;
		p.y = (p.y - 5) / 36;

		if (!r.contains(p))
			return; // false click

		int numInLastCol = pal.length;
		while (numInLastCol > pals.height)
			numInLastCol -= pals.height;

		if (p.x == pals.width - 1 && p.y >= numInLastCol)
			return; // false click

		selectId = (p.x * pals.height) + p.y;
	}

	public static JPanel getCurrent() {
		BlockType[][] pal = getPal();

		Block[] block = { setupPaletteBlock(pal[selectId][0]),
				setupPaletteBlock(pal[selectId][1]) };

		Graph canvas = new Graph();
		canvas.preparePaint(block[0], block[1], 48);
		canvas.setPreferredSize(new Dimension(48, 48));
		canvas.setBorder(BorderFactory.createLineBorder(Color.gray, 4));
		JPanel out = new JPanel();
		out.add(canvas);
		out.setAlignmentX(Component.CENTER_ALIGNMENT);
		out.setBackground(Color.gray);
		out.setMaximumSize(new Dimension(60, 60));
		out.setPreferredSize(new Dimension(60, 60));
		out.setMinimumSize(new Dimension(60, 60));
		return out;
	}

	public static void swapLayer() {
		BlockType[][] pal = (Options.viewTwoLayer ? Options.palette2
				: Options.palette);
		BlockType[] oldbrush = (Options.viewTwoLayer ? Options.palette2[oldSelectId]
				: Options.palette[oldSelectId]);
		BlockType[] brush = (Options.viewTwoLayer ? Options.palette[selectId]
				: Options.palette2[selectId]);

		if (Options.viewTwoLayer) {
			// Hanging block
			if (oldbrush[1] == BlockType.Block && oldbrush[0] == BlockType.Air
					&& brush[0] == BlockType.Block && brush[1] == BlockType.Air) {
				int temp = selectId;
				selectId = oldSelectId;
				oldSelectId = temp;
				return;
			}
		}

		int bestmatch = 0;
		float bestmatchval = 0;
		for (int i = 0; i < pal.length; i++) {
			float val = 0;
			if (pal[i][0] == brush[0])
				val += 1;
			if (pal[i][1] == brush[1])
				val += 1;
			if (!Options.viewTwoLayer || oldbrush[1] != BlockType.Air) {
				// Better deal with two layer tools
				if (pal[i][0] == BlockType.Block && pal[i][1] == brush[0])
					val += 3;
				if (brush[0] == BlockType.Block && brush[1] == pal[i][0])
					val += 3;
			}

			if (val > bestmatchval) {
				bestmatch = i;
				bestmatchval = val;
			}
		}

		oldSelectId = selectId;
		selectId = bestmatch;
	}

	public static BlockType[][] getPal() {
		BlockType[][] pal;
		if (Options.viewTwoLayer) {
			pal = Options.palette2;
		} else {
			pal = Options.palette;
		}
		return pal;
	}

}