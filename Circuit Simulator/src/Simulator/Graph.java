package Simulator;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/*
 This class renders the graphics for all of the items in the GUI.
 Both in the working area, and all the other buttons too.
 NOTE: This way of rendering graphics is apparently not very effective. 
 It should be updated to a better way.
 */

@SuppressWarnings("serial")
public class Graph extends JPanel { // will modify this object via the
	// paint()-method
	@SuppressWarnings("unused")
	private static BufferedImage image = new BufferedImage(
			128, 128, BufferedImage.TYPE_INT_ARGB); // <----size here; x, y

	private boolean onBoard;
	private boolean twoLayered;
	private boolean special;
	private int x;
	private int y;
	private int z;
	private Block lowerOne; // if not on the board (practically the palette)
	private Block topOne;
	private int size;
	private int specialId; // arrow 0;
	private int specialDir;

	public void paint(Graphics graphics) { 
		// hopefully will be called each time something needs validation. 
		// Not static
		super.paint(graphics);
		if (!special) {
			if (onBoard) {
				renderIcon(Workspace.getBlock(x, y, z), Options.size, Workspace
						.getConnections(x, y, z, false), (Graphics2D) graphics);
				if (z < Workspace.getz() - 1 && twoLayered) {
					renderIcon2(Workspace.getBlock(x, y, z), Workspace
							.getBlock(x, y, z + 1), Options.size, Workspace
							.getConnections(x, y, z + 1, false),
							(Graphics2D) graphics);
				}
			} else {
				boolean[] allTrue = { true, true, true, true, true };
				renderIcon(lowerOne, size, allTrue, (Graphics2D) graphics);
				renderIcon2(lowerOne, topOne, size, allTrue,
						(Graphics2D) graphics);
			}
		} else {
			renderSpecial((Graphics2D) graphics);
		}
		graphics.dispose();
	}

	public void preparePaint(int x, int y, int z, boolean layers) { 
		// for the board
		this.special = false;
		this.onBoard = true;
		this.twoLayered = layers;
		this.x = x;
		this.y = y;
		this.z = z;
		this.setPreferredSize(new Dimension(Options.size, Options.size));
		this.repaint();
	}

	public void preparePaint(Block lowerOne, Block topOne, int size) { 
		// for palette
		this.special = false;
		this.onBoard = false;
		this.lowerOne = lowerOne;
		this.topOne = topOne;
		this.size = size;
		this.setPreferredSize(new Dimension(size, size));
		this.repaint();
	}

	public void preparePaint(int id, int dir, int size) { 
		// for special
		this.special = true;
		this.specialId = id;
		this.specialDir = dir;
		this.size = size;
		this.repaint();
	}

	private void renderIcon(Block block, int size, boolean[] connected,
			Graphics2D graphics) {

		// setting right size and rotating
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.translate((double) size / 2, (double) size / 2); 
		// to rotate around middle point
		switch (block.id) {
		case Lever:
			if (block.place == 0 && block.charge > 0) {
				graphics.rotate(Math.PI); // turns lever down
				break;
			}
		case Torch:
		case Repeater:
		case Button:
		case StickyPiston:
		case Piston:
		case PistonPaddle:
		case Door:
		case Trapdoor:
			place(graphics, block.place);
			break;
		case Rail:
			if (block.subType == BlockType.CornerRail) {
				place(graphics, block.place + 1);
				break;
			}
		case PowerRail:
		case DetectorRail:
			placeRail(graphics, block.place);
			break;
		}

		graphics.translate((double) size / -2, (double) size / -2);
		// scaling
		graphics.scale((double) size / 128, (double) size / 128);

		// Prepares for graphics
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, 128, 128); // Background

		drawing(block, size, connected, graphics);

		// restoring normality
		graphics.scale((double) 128 / size, (double) 128 / size);
		graphics.translate((double) size / 2, (double) size / 2); 
		// to rotate around middle point
		switch (block.id) {
		case Torch: // fall through
		case Repeater:
		case StickyPiston:
		case Piston:
		case PistonPaddle:
		case Button: {
			switch (block.place) {
			case 1: {
				place(graphics, 1);
			}
				break;
			case 2: {
				place(graphics, 4);
			}
				break;
			case 4: {
				place(graphics, 2);
			}
				break;
			}
		}
			break;
		case Lever: {
			if (block.charge > 0 && block.place == 0) {
				graphics.rotate(Math.PI); // turns lever down
			} else {
				switch (block.place) {
				case 1: {
					place(graphics, 1);
				}
					break;
				case 2: {
					place(graphics, 4);
				}
					break;
				case 4: {
					place(graphics, 2);
				}
					break;
				}
			}
		}
			break;
		}
		graphics.translate((double) size / -2, (double) size / -2);

	}

	// rendering the top icon for the two-layered view
	private void renderIcon2(Block lowerBlock, Block block, int size,
			boolean[] connected, Graphics2D graphics) {

		// rotating
		graphics.translate((double) size / 2, (double) size / 2); 
		// to rotate around middle point
		switch (block.id) {
		case Lever:
			if (block.place == 0 && block.charge > 0) {
				graphics.rotate(Math.PI); // turns lever down
				break;
			}
		case Torch:
		case Repeater:
		case Button:
		case StickyPiston:
		case Piston:
		case PistonPaddle:
		case Trapdoor:
			place(graphics, block.place);
			break;
		case Door: // Only draw upper if above a non-door block
			if (block.subType.isUpperType()
					&& !(lowerBlock.subType.isLowerType())) {
				place(graphics, block.place);
			} else if (block.subType.isLowerType()) {
				place(graphics, block.place);
			}
			break;
		case Rail:
			if (block.subType == BlockType.CornerRail) {
				place(graphics, block.place + 1);
				break;
			}
		case PowerRail:
		case DetectorRail:
			placeRail(graphics, block.place);
			break;
		}
		graphics.translate((double) size / -2, (double) size / -2);
		// scaling
		graphics.scale((double) size / 128, (double) size / 128);

		// transparency rules
		if (!(lowerBlock.id == BlockType.Block)) {
			graphics.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.7f));
		}
		if (!(lowerBlock.id == BlockType.Air || lowerBlock.id == BlockType.Block)
				&& !(block.id == BlockType.Air || block.id == BlockType.Block)) {
			graphics.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f));
			graphics.setColor(Color.white);
			graphics.fillRect(0, 0, 128, 128);
			graphics.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 1.0f));
		}

		if (block.id.isPistonType() && block.pistonExtended && block.place == 0
				&& lowerBlock.id == BlockType.PistonPaddle) {
			graphics.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.7f));
		}

		if (block.id == BlockType.Block) {
			graphics.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f));
			switch (block.subType) {
			case Block:
				graphics.setColor(Color.gray);
				graphics.fillRect(0, 0, 128, 128);
				break;
			case Glass:
				graphics.setColor(new Color(100, 170, 200));
				graphics.setStroke(new BasicStroke(20));
				graphics.drawRect(10, 10, 108, 108);

				// graphics.fillRect(0, 0, 128, 128);
				graphics.setColor(Color.gray);
				graphics.fillRect(20, 20, 128 - 20 * 2, 128 - 20 * 2);
				break;
			case Obsidian:
				graphics.setColor(new Color(50, 0, 150));
				graphics.setStroke(new BasicStroke(20));
				graphics.drawRect(10, 10, 108, 108);

				graphics.setColor(Color.gray);
				graphics.fillRect(20, 20, 128 - 20 * 2, 128 - 20 * 2);
				break;
			default:
				drawing(block, size, connected, graphics);
			}
		} else if (block.id == BlockType.PistonPaddle
				&& (block.place == 0 || block.place == 5)) {
			graphics.setColor(Color.red);
			graphics.fillRect(10, 10, 108, 108); // Paddle
			if (block.place != 5) {
				graphics.setColor(Color.red.darker());
				int shaft = 30;
				graphics.fillRect((128 - shaft) / 2, (128 - shaft) / 2, shaft,
						shaft); // Paddle
			}
		} else {
			drawing(block, size, connected, graphics);
		}
	}

	private void place(Graphics2D graphics, int place) { 
		// rotates image to be placed correctly
		switch (place) {
		case 1:
			graphics.rotate(Math.PI);
			break;
		case 2:
			graphics.rotate(Math.PI * 3 / 2);
			break;
		case 4:
			graphics.rotate(Math.PI / 2);
			break;
		default:
			break;
		}
	}

	private void placeRail(Graphics2D graphics, int place) { 
		// rotates rails to be placed correctly
		switch (place) {
		case 1:
		case 5:
			graphics.rotate(Math.PI / 2);
			break;
		case 2:
			graphics.rotate(Math.PI);
			break;
		case 4:
			graphics.rotate(Math.PI * 3 / 2);
			break;
		default:
			break;
		}
	}

	private void drawing(Block block, int size, boolean[] connected,
			Graphics2D graphics) {

		// power tester
		final Color sienna = new Color(160, 82, 45);
		final Color darkGreen = new Color(0, 100, 0);

		Color powerState;
		if (block.charge > 0) { // if on
			powerState = Color.red;
		} else {
			powerState = new Color(128, 0, 0);
		}

		switch (block.id) { // draw the specific item
		case Block: { // Block
			switch (block.subType) {
			case Block:
				graphics.setColor(Color.yellow);
				graphics.fillRect(0, 0, 128, 128);
				break;
			case Glass:
				graphics.setColor(new Color(100, 170, 200));
				graphics.setStroke(new BasicStroke(20));
				graphics.drawRect(10, 10, 108, 108);

				// graphics.fillRect(0, 0, 128, 128);
				graphics.setColor(Color.yellow);
				graphics.fillRect(20, 20, 128 - 20 * 2, 128 - 20 * 2);
				break;
			case Obsidian:
				graphics.setColor(new Color(50, 0, 150));
				graphics.setStroke(new BasicStroke(20));
				graphics.drawRect(10, 10, 108, 108);

				// graphics.fillRect(0, 0, 128, 128);
				graphics.setColor(Color.yellow);
				graphics.fillRect(20, 20, 128 - 20 * 2, 128 - 20 * 2);
				break;
			case PinkWool:
				graphics.setColor(new Color(150, 100, 100));
				graphics.setStroke(new BasicStroke(10));
				graphics.drawRect(5, 5, 118, 118);

				graphics.setColor(new Color(255, 180, 200));
				graphics.fillRect(10, 10, 108, 108);
				break;
			case BlueWool:
				graphics.setColor(new Color(100, 100, 150));
				graphics.setStroke(new BasicStroke(10));
				graphics.drawRect(5, 5, 118, 118);

				graphics.setColor(new Color(150, 220, 250));
				graphics.fillRect(10, 10, 108, 108);
				break;
			}
		}
			break;

		case Wire: { // Wire
			graphics.setColor(powerState);
			graphics.fillRect(48, 48, 32, 32);
			if (connected[0]) {
				graphics.fillRect(48, 0, 32, 48);
			}
			if (connected[1]) {
				graphics.fillRect(80, 48, 48, 32);
			}
			if (connected[2]) {
				graphics.fillRect(48, 80, 32, 48);
			}
			if (connected[3]) {
				graphics.fillRect(0, 48, 48, 32);
			}
		}
			break;

		case Torch: { // Torch place 0 + 3
			if (block.place != 0) {
				graphics.setColor(new Color(80, 50, 25));// wood
				graphics.fillRect(48, 80, 32, 48);
			}
			graphics.setColor(powerState);
			graphics.fillOval(32, 32, 64, 64);
		}
			break;

		case Repeater: { // Repeater place 1
			int[] xArray = { 128, 64, 0 };
			int[] yArray = { 0, 128, 0 };
			graphics.setColor(Color.darkGray);
			graphics.fillPolygon(xArray, yArray, 3);
			graphics.setColor(powerState);
			graphics.fillOval(48, 0, 32, 32);
			graphics.fillOval(48, 64, 32, 32);
			// the timers
			Color input;
			if (block.charge > 0) { // the input, if it should change anything
				input = new Color(128, 0, 0);
			} else {
				input = Color.red;
			}
			if (block.ticksPassed < 2) {
				input = powerState;
			}
			if (block.delay >= 2) {
				graphics.setColor(input);
				graphics.fillRect(96, 96, 32, 16);
			}
			if (block.ticksPassed < 4) {
				input = powerState;
			}
			if (block.delay >= 4) {
				graphics.setColor(input);
				graphics.fillRect(96, 64, 32, 16);
			}
			if (block.ticksPassed < 6) {
				input = powerState;
			}
			if (block.delay >= 6) {
				graphics.setColor(input);
				graphics.fillRect(0, 96, 32, 16);
			}
			if (block.ticksPassed < 8) {
				input = powerState;
			}
			if (block.delay >= 8) {
				graphics.setColor(input);
				graphics.fillRect(0, 64, 32, 16);
			}
		}
			break;

		case Button: { // Button place 3
			if (block.charge == 0) {
				powerState = Color.darkGray;
			}
			graphics.setColor(powerState);
			graphics.fillRect(16, 96, 96, 32);
		}
			break;

		case Lever: { // Lever place 3 + 0
			if (block.place == 0) { // ground
				graphics.setColor(Color.darkGray);
				graphics.fillRect(32, 32, 64, 64);
				graphics.setColor(powerState);
				graphics.fillRect(48, 16, 32, 64);
			} else { // on side
				graphics.setColor(Color.darkGray);
				graphics.fillRect(16, 96, 96, 32);
				graphics.setColor(powerState);
				graphics.fillRect(48, 48, 32, 64);
			}
		}
			break;

		case PressurePad: { // Pressure pad
			if (block.charge == 0) {
				if (block.subType == BlockType.StonePad)
					powerState = Color.darkGray;
				else
					powerState = sienna;
			}
			graphics.setColor(powerState);
			graphics.fillRect(16, 16, 96, 96);
		}
			break;

		case StickyPiston:
		case Piston: {
			if (block.place == 0) {
				int strokeWidth = 30;
				Color pistonColor = Color.darkGray;
				if (block.id == BlockType.StickyPiston)
					pistonColor = darkGreen;

				graphics.setColor(pistonColor);
				graphics.setStroke(new BasicStroke(strokeWidth));
				graphics.drawRect(strokeWidth / 2, strokeWidth / 2,
						128 - strokeWidth, 128 - strokeWidth);

				graphics.setColor(pistonColor.darker());
				if (block.charge > 0)
					graphics.setColor(Color.red);
				graphics.fillRect(strokeWidth, strokeWidth,
						128 - strokeWidth * 2, 128 - strokeWidth * 2);

			} else if (block.place == 5) {
				graphics.setColor(Color.darkGray);
				if (block.id == BlockType.StickyPiston)
					graphics.setColor(darkGreen);
				graphics.fillRect(0, 0, 128, 128); // Base

				graphics.setColor(sienna);
				if (block.charge > 0)
					graphics.setColor(Color.red);
				graphics.fillRect(15, 15, 128 - 15 * 2, 128 - 15 * 2); // Paddle

			} else {
				final int padding = 5;
				final int[] paddle = { 128 - padding * 2, 20 };
				final int[] base = { 128, 128 - 42 };
				int[] shaft = { 20, 128 - padding - paddle[1] - base[1] };

				graphics.setColor(Color.darkGray);
				if (block.id == BlockType.StickyPiston)
					graphics.setColor(darkGreen);
				graphics.fillRect(0, 0, base[0], base[1]); // Base
				graphics.setColor(sienna);
				if (block.charge > 0)
					graphics.setColor(Color.red);

				if (block.pistonExtended)
					shaft[1] = 128 - base[1];
				else
					graphics.fillRect((128 - paddle[0]) / 2, 128 - padding
							- paddle[1], paddle[0], paddle[1]); // Paddle
				graphics.fillRect((128 - shaft[0]) / 2, base[1], shaft[0],
						shaft[1]); // Shaft
			}
			break;
		}

		case PistonPaddle: {
			if (block.place == 0 || block.place == 5) {
				if (Options.viewTwoLayer)
					break;

				graphics.setColor(Color.red);
				graphics.fillRect(10, 10, 108, 108); // Paddle
				if (block.place == 5)
					break;

				graphics.setColor(Color.red.darker());
				int shaft = 30;
				graphics.fillRect((128 - shaft) / 2, (128 - shaft) / 2, shaft,
						shaft); // Paddle

			} else {
				final int padding = 5;
				final int[] paddle = { 128 - padding * 2, 20 };
				int[] shaft = { 20, 128 - padding - paddle[1] };

				graphics.setColor(Color.red);
				graphics.fillRect((128 - shaft[0]) / 2, 0, shaft[0], shaft[1]); // Shaft
				graphics.fillRect(padding, 128 - padding - paddle[1],
						paddle[0], paddle[1]); // Paddle
			}
			break;
		}

		case Door: {
			graphics.setColor(powerState);
			graphics.fillRect(0, 98, 30, 30);
			if (block.subType.isIronDoor())
				graphics.setColor(Color.gray);
			else
				graphics.setColor(sienna);
			if (block.charge == 0)
				graphics.fillRect(30, 98, 98, 30);
			else
				graphics.fillRect(0, 0, 30, 98);
			break;
		}

		case Trapdoor: {
			graphics.setColor(sienna);
			if (block.charge == 0) {
				graphics.fillRect(0, 0, 26, 111);
				graphics.fillRect(26, 0, 25, 23);
				graphics.fillRect(26, 41, 25, 25);
				graphics.fillRect(26, 84, 25, 27);
				graphics.fillRect(51, 0, 26, 111);
				graphics.fillRect(77, 0, 25, 23);
				graphics.fillRect(77, 41, 25, 25);
				graphics.fillRect(77, 84, 25, 27);
				graphics.fillRect(102, 0, 26, 111);
			} else {
				graphics.fillRect(0, 111, 15, 17);
				graphics.fillRect(45, 111, 38, 17);
				graphics.fillRect(113, 111, 15, 17);
			}
			graphics.setColor(powerState);
			graphics.fillRect(15, 111, 30, 17);
			graphics.fillRect(83, 111, 30, 17);
			break;
		}

		case Rail: {
			if (block.subType == BlockType.CornerRail) {
				// North-east
				int[] xPoints = { 98, 85, 30, 30, 43, 98 };
				int[] yPoints = { 30, 30, 85, 98, 98, 43 };
				graphics.setColor(Color.gray);
				graphics.fillRect(10, 0, 20, 98);
				graphics.fillRect(10, 98, 20, 20);
				graphics.fillRect(30, 98, 98, 20);
				graphics.fillRect(98, 0, 20, 10);
				graphics.fillRect(98, 10, 20, 20);
				graphics.fillRect(118, 10, 10, 20);
				graphics.setColor(sienna);
				graphics.fillRect(30, 10, 68, 20);
				graphics.fillRect(98, 30, 20, 68);
				graphics.fillPolygon(xPoints, yPoints, 6);
			} else {
				if (block.place <= 1) // East-west
				{
					graphics.setColor(Color.gray);
					graphics.fillRect(0, 10, 128, 20);
					graphics.fillRect(0, 98, 128, 20);
					graphics.setColor(sienna);
					graphics.fillRect(12, 30, 20, 68);
					graphics.fillRect(54, 30, 20, 68);
					graphics.fillRect(96, 30, 20, 68);
				} else // Ascending north
				{
					graphics.setColor(Color.lightGray);
					graphics.fillRect(10, 0, 20, 43);
					graphics.fillRect(98, 0, 20, 43);
					graphics.setColor(Color.gray);
					graphics.fillRect(10, 43, 20, 42);
					graphics.fillRect(98, 43, 20, 42);
					graphics.setColor(Color.darkGray);
					graphics.fillRect(10, 85, 20, 43);
					graphics.fillRect(98, 85, 20, 43);
					graphics.setColor(new Color(178, 134, 68));
					graphics.fillRect(30, 12, 68, 20);
					graphics.setColor(sienna);
					graphics.fillRect(30, 54, 68, 20);
					graphics.setColor(new Color(91, 37, 27));
					graphics.fillRect(30, 96, 68, 20);
				}
			}
			break;
		}

		case PowerRail: // Power rail
		{
			if (block.place <= 1) // East-west
			{
				graphics.setColor(Color.gray);
				graphics.fillRect(0, 10, 128, 20);
				graphics.fillRect(0, 98, 128, 20);
				graphics.setColor(new Color(170, 170, 0));
				graphics.fillRect(0, 30, 128, 10);
				graphics.fillRect(0, 88, 128, 10);
				graphics.setColor(sienna);
				graphics.fillRect(12, 40, 20, 48);
				graphics.fillRect(54, 40, 20, 48);
				graphics.fillRect(96, 40, 20, 48);
				graphics.setColor(powerState);
				graphics.fillRect(0, 54, 12, 20);
				graphics.fillRect(32, 54, 22, 20);
				graphics.fillRect(74, 54, 22, 20);
				graphics.fillRect(116, 54, 12, 20);
			} else // Ascending north
			{
				graphics.setColor(Color.lightGray);
				graphics.fillRect(10, 0, 20, 43);
				graphics.fillRect(98, 0, 20, 43);
				graphics.setColor(Color.gray);
				graphics.fillRect(10, 43, 20, 42);
				graphics.fillRect(98, 43, 20, 42);
				graphics.setColor(Color.darkGray);
				graphics.fillRect(10, 85, 20, 43);
				graphics.fillRect(98, 85, 20, 43);
				graphics.setColor(new Color(170, 170, 0));
				graphics.fillRect(30, 0, 10, 128);
				graphics.fillRect(88, 0, 10, 128);
				graphics.setColor(new Color(178, 134, 68));
				graphics.fillRect(40, 12, 48, 20);
				graphics.setColor(sienna);
				graphics.fillRect(40, 54, 48, 20);
				graphics.setColor(new Color(91, 37, 27));
				graphics.fillRect(40, 96, 48, 20);
				graphics.setColor(powerState);
				graphics.fillRect(54, 0, 20, 12);
				graphics.fillRect(54, 32, 20, 22);
				graphics.fillRect(54, 74, 20, 22);
				graphics.fillRect(54, 116, 20, 12);
			}
			break;
		}

		case DetectorRail: // Detector rail
		{
			if (block.place <= 1) // East-west
			{
				graphics.setColor(Color.gray);
				graphics.fillRect(0, 10, 128, 20);
				graphics.fillRect(0, 98, 128, 20);
				graphics.setColor(sienna);
				graphics.fillRect(12, 30, 20, 68);
				graphics.fillRect(54, 30, 20, 13);
				graphics.fillRect(54, 85, 20, 13);
				graphics.fillRect(96, 30, 20, 68);
				graphics.setColor(powerState);
				graphics.fillRect(43, 43, 42, 42);
			} else // Ascending north
			{
				graphics.setColor(Color.lightGray);
				graphics.fillRect(10, 0, 20, 43);
				graphics.fillRect(98, 0, 20, 43);
				graphics.setColor(Color.gray);
				graphics.fillRect(10, 43, 20, 42);
				graphics.fillRect(98, 43, 20, 42);
				graphics.setColor(Color.darkGray);
				graphics.fillRect(10, 85, 20, 43);
				graphics.fillRect(98, 85, 20, 43);
				graphics.setColor(new Color(178, 134, 68));
				graphics.fillRect(30, 12, 68, 20);
				graphics.setColor(sienna);
				graphics.fillRect(30, 54, 13, 20);
				graphics.fillRect(85, 54, 13, 20);
				graphics.setColor(new Color(91, 37, 27));
				graphics.fillRect(30, 96, 68, 20);
				graphics.setColor(powerState);
				graphics.fillRect(43, 43, 42, 42);
			}
			break;
		}

		default:
			break; // Air or error
		}

	}

	public BufferedImage getImage(int size) {
		BufferedImage image = new BufferedImage(size, size,
				BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.createGraphics();
		this.paint(graphics);
		graphics.dispose();
		return image;
	}

	private void renderSpecial(Graphics2D graphics) {
		// transforming
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.translate((double) size / 2, (double) size / 2);
		place(graphics, specialDir);
		graphics.translate((double) size / -2, (double) size / -2);
		graphics.scale((double) size / 128, (double) size / 128);
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, 128, 128); // Background
		// comparing ids
		switch (specialId) {
		case 0:
			specialArrow(graphics);
			break;
		case 1:
			specialTick(graphics);
			break;
		case 2:
			specialPause(graphics);
			break;
		case 3:
			specialPlay(graphics);
			break;
		}
	}

	private void specialArrow(Graphics2D graphics) {
		graphics.setColor(Color.darkGray);
		int[] xArray = { 0, 64, 128, 128, 64, 0 };
		int[] yArray = { 80, 16, 80, 112, 48, 112 };
		graphics.fillPolygon(xArray, yArray, 6);
	}

	private void specialTick(Graphics2D graphics) {
		graphics.setColor(Color.darkGray);
		int[] xArray1 = { 32, 48, 48, 32 };
		int[] yArray1 = { 32, 32, 96, 96 };
		graphics.fillPolygon(xArray1, yArray1, 4);
		int[] xArray2 = { 64, 96, 64 };
		int[] yArray2 = { 32, 64, 96 };
		graphics.fillPolygon(xArray2, yArray2, 3);
	}

	private void specialPause(Graphics2D graphics) {
		graphics.setColor(Color.darkGray);
		int[] xArray1 = { 36, 56, 56, 36 };
		int[] yArray1 = { 32, 32, 96, 96 };
		graphics.fillPolygon(xArray1, yArray1, 4);
		int[] xArray2 = { 72, 92, 92, 72 };
		int[] yArray2 = { 32, 32, 96, 96 };
		graphics.fillPolygon(xArray2, yArray2, 4);
	}

	private void specialPlay(Graphics2D graphics) {
		graphics.setColor(Color.darkGray);
		int[] xArray = { 48, 96, 48 };
		int[] yArray = { 24, 64, 104 };
		graphics.fillPolygon(xArray, yArray, 3);
	}

}