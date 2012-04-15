package Simulator;

import java.util.*;
import java.awt.*;
import javax.swing.*;

/*
 This class is responsible for keeping all data in the working area.
 Any changes to the board (except the power), will be done here.
 */
public class Workspace {
	private static ArrayList<ArrayList<ArrayList<Block>>> boardxyz 
		= new ArrayList<ArrayList<ArrayList<Block>>>(); // area
																													// Data

	public Workspace(int xSize, int ySize, int zSize) {
		boardxyz.clear();
		for (int x = 0; x < xSize; x++) {
			boardxyz.add(new ArrayList<ArrayList<Block>>());
			for (int y = 0; y < ySize; y++) {
				boardxyz.get(x).add(new ArrayList<Block>());
				for (int z = 0; z < zSize; z++) {
					boardxyz.get(x).get(y).add(new Block(x, y, z));
				}
			}
		}
	}

	public static Block getBlock(int x, int y, int z) {
		if (x >= getx() || y >= gety() || z >= getz() || x < 0 || y < 0
				|| z < 0) {
			return new Block();
		} else {
			return boardxyz.get(x).get(y).get(z);
		}
	}

	public static Point getBlockxy(Point p) { // returns -1,-1 if not legal
		int x = 0;
		int y = 0;

		while (p.x > Options.size) {
			p.x = p.x - (int) Math.round(Options.size * 1.125);
			x++;
		}
		while (p.y > Options.size) {
			p.y = p.y - (int) Math.round(Options.size * 1.125);
			y++;
		}
		if (p.x < 0 || p.y < 0 || x >= getx() || y >= gety()) {
			return new Point(-1, -1);
		}
		return new Point(x, y);
	}

	public static ArrayList<JPanel> renderLayer(boolean layers) {
		ArrayList<JPanel> out = new ArrayList<JPanel>();
		for (int i = 0; i < getz(); i++) {
			GridLayout grid = new GridLayout(gety(), getx());
			grid.setHgap((int) Math.round(Options.size * 0.125));
			grid.setVgap((int) Math.round(Options.size * 0.125));
			JPanel panel = new JPanel(grid);
			for (int y = 0; y < gety(); y++) {
				for (int x = 0; x < getx(); x++) {
					Graph canvas = new Graph();
					canvas.setPreferredSize(new Dimension(Options.size,
							Options.size)); // size
					canvas.preparePaint(x, y, i, layers);
					panel.add(canvas);
				}
			}
			panel.setBackground(Color.gray);
			out.add(panel);
		}
		return out;
	}

	public static void setBlock(int x, int y, int z, BlockType id) {
		Block block = getBlock(x, y, z);
		block.id = id;
		block.charge = 0;
		block.place = 0;
		block.delay = 0;
		block.ticksPassed = 0;
		switch (id) {
		case Block:
			block.subType = BlockType.Block;
			break;
		case Rail:
			block.subType = BlockType.Rail;
			break;
		case Torch: {
			block.charge = 16;
			block.delay = 2;
		}
			break;
		case Repeater: {
			block.place = 1;
			block.delay = 2;
		}
			break;
		case Button: {
			block.place = 1;
			block.delay = 20;
		}
			break;
		case Lever: {
			block.delay = 20;
		}
			break;
		case PressurePad: {
			block.delay = 20;
			block.subType = BlockType.PressurePad;
		}
			break;
		case Trapdoor: {
			block.place = 3;
			block.delay = 20;
		}
			break;
		case DetectorRail: {
			block.delay = 20;
		}
			break;

		case Door: {
			Block block2 = getBlock(x, y, z - 1);
			if (block2.subType.isLowerType()) {
				block.subType = BlockType.UpperWoodDoor;
				block2.subType = BlockType.LowerWoodDoor;
			} else {
				block2 = getBlock(x, y, z + 1);
				block.subType = BlockType.LowerWoodDoor;
				setBlock(x, y, z + 1, BlockType.Door);
			}
			block.place = 3;
			block2.place = 3;
		}
			break;

		case StickyPiston:
		case Piston: {
			block.place = 1;
			block.delay = 2;
			break;
		}
		}
	}

	public static void setBlock(int x, int y, int z, Block block) {
		boardxyz.get(x).get(y).remove(z);
		boardxyz.get(x).get(y).add(z, block);
		block.x = x;
		block.y = y;
		block.z = z;
	}

	public static void replace(int x, int y, int z) {
		Block block = getBlock(x, y, z);
		switch (block.id) {
		case Repeater:
			block.charge = 0;
			// fall through
		case Button:
		case Trapdoor:
			block.place = (block.place % 4) + 1; // wraps 1,2,3,4
			break;

		case PressurePad:
			if (block.subType == BlockType.PressurePad)
				block.subType = BlockType.StonePad;
			else if (block.subType == BlockType.StonePad)
				block.subType = BlockType.PressurePad;
			break;

		case Torch:
			block.charge = 16;
			// fall through
		case Lever:
			block.place = (block.place + 1) % 5; // wraps 0,1,2,3,4
			break;

		case StickyPiston:
		case Piston:
			if (block.pistonExtended)
				block.retractPiston();

			block.place = (block.place + 1) % 6; // wraps 0,1,2,3,4,5
			block.charge = 0;
			break;

		case Door:
			block.place = (block.place % 4) + 1; // wraps 1,2,3,4
			if (block.subType.isLowerType()) {
				if (z < getz() - 1
						&& getBlock(x, y, z + 1).subType.isUpperType())
					getBlock(x, y, z + 1).place = block.place;
			} else if (block.subType.isUpperType()) {
				if (z > 0 && getBlock(x, y, z - 1).subType.isLowerType())
					getBlock(x, y, z - 1).place = block.place;
			}
			break;

		case Rail:
			if (block.subType == BlockType.CornerRail) {
				block.place = (block.place + 1) % 4; // wraps 0,1,2,3
				break;
			}
		case PowerRail:
		case DetectorRail:
			block.place = (block.place + 1) % 6; // wraps 0,1,2,3,4,5
			break;
		}
	}

	public static boolean[] getConnections(int x, int y, int z, boolean powering) { 
		// note: powering is true if it defines who it should power
		boolean[] falseOut = { false, false, false, false };
		if (getBlock(x, y, z).id != BlockType.Wire) {
			return falseOut;
		}

		boolean[] out = new boolean[4];
		for (int i = 0; i >= 3; i++) { // sets all false
			out[i] = false;
		}
		boolean[] end = new boolean[6];
		if (z == 0) {
			end[0] = true;
		}
		if (y == 0) {
			end[1] = true;
		}
		if (x == getx() - 1) {
			end[2] = true;
		}
		if (y == gety() - 1) {
			end[3] = true;
		}
		if (x == 0) {
			end[4] = true;
		}
		if (z == getz() - 1) {
			end[5] = true;
		}

		// actual checking
		if (!end[1]) { // north
			if (getBlock(x, y - 1, z).id == BlockType.Air) {
				if (!end[0]) {
					if (Block.connectsDiagonal(x, y - 1, z - 1))
						out[0] = true;
				}
			} else if (Block.connectsHorizontal(x, y - 1, z, 1)) {
				out[0] = true;
			}
			if (!end[5]) {
				if (getBlock(x, y, z + 1).id != BlockType.Block
						&& getBlock(x, y - 1, z).id == BlockType.Block) {
					if (Block.connectsDiagonal(x, y - 1, z + 1))
						out[0] = true;
				}
			}
		}

		if (!end[2]) { // east
			if (getBlock(x + 1, y, z).id == BlockType.Air) {
				if (!end[0]) {
					if (Block.connectsDiagonal(x + 1, y, z - 1))
						out[1] = true;
				}
			} else if (Block.connectsHorizontal(x + 1, y, z, 2)) {
				out[1] = true;
			}
			if (!end[5]) {
				if (getBlock(x, y, z + 1).id != BlockType.Block
						&& getBlock(x + 1, y, z).id == BlockType.Block) {
					if (Block.connectsDiagonal(x + 1, y, z + 1))
						out[1] = true;
				}
			}
		}

		if (!end[3]) { // south
			if (getBlock(x, y + 1, z).id == BlockType.Air) {
				if (!end[0]) {
					if (Block.connectsDiagonal(x, y + 1, z - 1))
						out[2] = true;
				}
			} else if (Block.connectsHorizontal(x, y + 1, z, 3)) {
				out[2] = true;
			}
			if (!end[5]) {
				if (getBlock(x, y, z + 1).id != BlockType.Block
						&& getBlock(x, y + 1, z).id == BlockType.Block) {
					if (Block.connectsDiagonal(x, y + 1, z + 1))
						out[2] = true;
				}
			}
		}

		if (!end[4]) { // west
			if (getBlock(x - 1, y, z).id == BlockType.Air) {
				if (!end[0]) { // down
					if (Block.connectsDiagonal(x - 1, y, z - 1))
						out[3] = true;
				}
			} else if (Block.connectsHorizontal(x - 1, y, z, 4)) {
				out[3] = true;
			}
			if (!end[5]) { // up
				if (getBlock(x, y, z + 1).id != BlockType.Block
						&& getBlock(x - 1, y, z).id == BlockType.Block) {
					if (Block.connectsDiagonal(x - 1, y, z + 1))
						out[3] = true;
				}
			}
		}

		// in special cases
		int no = 0;
		int lastFound = 0;
		for (int i = 0; i <= 3; i++) {
			if (out[i] == true) {
				no++;
				lastFound = i;
			}
		}

		if (no == 0) { // not connected
			for (int i = 0; i <= 3; i++) {
				out[i] = true;
			}
		} else if (no == 1) { // one connected
			if (powering) {
				out[lastFound] = false; // not powering block in the direction
										// that is the reason for the alignment.
			}
			switch (lastFound) {
			case 0:
				out[2] = true;
				break;
			case 1:
				out[3] = true;
				break;
			case 2:
				out[0] = true;
				break;
			case 3:
				out[1] = true;
				break;
			}
		} else if (no == 2) {
			if (!((out[0] && out[2]) || (out[1] && out[3])) && powering) { 
				// not straight should not power
				return falseOut;
			}
		} else {
			if (powering) { // not powering if more that two
				return falseOut;
			}
		}
		return out;
	}

	public static void expand(int dir) {
		int x = getx();
		int y = gety();
		int z = getz();
		switch (dir) {
		case 0: { // down
			for (int i = 0; i < x; i++) {
				for (int j = 0; j < y; j++) {
					boardxyz.get(i).get(j).add(0, new Block(i, j, 0));
				}
			}
			Options.level++;
		}
			break;
		case 1: { // north
			for (int i = 0; i < x; i++) {
				boardxyz.get(i).add(0, new ArrayList<Block>());
				for (int k = 0; k < z; k++) {
					boardxyz.get(i).get(0).add(new Block(i, 0, k));
				}
			}
		}
			break;
		case 2: { // east
			boardxyz.add(new ArrayList<ArrayList<Block>>());
			for (int j = 0; j < y; j++) {
				boardxyz.get(x).add(new ArrayList<Block>());
				for (int k = 0; k < z; k++) {
					boardxyz.get(x).get(j).add(new Block(x, j, k));
				}
			}
		}
			break;
		case 3: { // south
			for (int i = 0; i < x; i++) {
				boardxyz.get(i).add(new ArrayList<Block>());
				for (int k = 0; k < z; k++) {
					boardxyz.get(i).get(y).add(new Block(i, y, k));
				}
			}
		}
			break;
		case 4: { // west
			boardxyz.add(0, new ArrayList<ArrayList<Block>>());
			for (int j = 0; j < y; j++) {
				boardxyz.get(0).add(new ArrayList<Block>());
				for (int k = 0; k < z; k++) {
					boardxyz.get(0).get(j).add(new Block(0, j, k));
				}
			}
		}
			break;
		case 5: { // up
			for (int i = 0; i < x; i++) {
				for (int j = 0; j < y; j++) {
					boardxyz.get(i).get(j).add(new Block(i, j, z));
				}
			}
		}
			break;
		}
		/*
		 * GUI.injectSlice(dir, false); GUI.injectSlice(dir, true);
		 */
		GUI.updateWorkingArea();
	}

	public static void contract(int dir) {
		int x = getx();
		int y = gety();
		int z = getz();
		if (((dir == 0 || dir == 5) && z == 1)
				|| ((dir == 1 || dir == 3) && y == 1)
				|| ((dir == 2 || dir == 4) && x == 1)) {
			return;
		}
		switch (dir) {
		case 0: { // down
			for (int i = 0; i < x; i++) {
				for (int h = 0; h < y; h++) {
					boardxyz.get(i).get(h).remove(0);
				}
			}
			if (Options.level != 0) {
				Options.level--;
			}
		}
			break;
		case 1: { // north
			for (int i = 0; i < x; i++) {
				boardxyz.get(i).remove(0);
			}
		}
			break;
		case 2: { // east
			boardxyz.remove(x - 1);
		}
			break;
		case 3: { // south
			for (int i = 0; i < x; i++) {
				boardxyz.get(i).remove(y - 1);
			}
		}
			break;
		case 4: { // west
			boardxyz.remove(0);
		}
			break;
		case 5: { // up
			for (int i = 0; i < x; i++) {
				for (int h = 0; h < y; h++) {
					boardxyz.get(i).get(h).remove(z - 1);
				}
			}
			if (Options.level == z - 1) {
				Options.level--;
			}
		}
			break;
		}
		GUI.updateWorkingArea();
	}

	public static int getx() {
		return boardxyz.size();
	}

	public static int gety() {
		return boardxyz.get(0).size();
	}

	public static int getz() {
		return boardxyz.get(0).get(0).size();
	}

}