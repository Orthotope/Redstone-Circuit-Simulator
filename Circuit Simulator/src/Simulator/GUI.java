package Simulator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputListener;

/*
 This is the GUI. All user inputs is evaluated here, and this is responsible 
 for showing the right things to the user. This class consists of the main method
 */

@SuppressWarnings("serial")
public class GUI extends JFrame implements MouseInputListener, ActionListener {
	public static GUI thisGUI; // the object that this can be referred to

	private static Container contentPane = new JPanel(new BorderLayout());
	private static javax.swing.Timer timer = new javax.swing.Timer(50, null);
	// first menu
	private static JPanel firstMenu = new JPanel();
	private static JPanel palette;
	private static JPanel current;
	// main area
	private static ArrayList<JPanel> panelWork; // one layer
	private static ArrayList<JPanel> panelWork2; // two layers
	private static JPanel zLayers = new JPanel();
	private static CardLayout overlay = new CardLayout(5, 5);
	private static JPanel zLayers2 = new JPanel();
	private static CardLayout overlay2 = new CardLayout(5, 5);
	private static JPanel nesting;
	private static JScrollPane workArea;
	private static Point coord = new Point(-1, -1);
	// second menu
	private static JPanel secondMenu = new JPanel();
	private static JButton playPause;
	private static JCheckBox logicTicks;
	private static JCheckBox twoView;
	// menu bar
	private static JMenuItem save = new JMenuItem("Save");
	private static JMenuItem load = new JMenuItem("Load");
	private static JMenuItem makeNew = new JMenuItem("New");
	private static JMenuItem expand0 = new JMenuItem("Expand ground");
	private static JMenuItem expand1 = new JMenuItem("Expand top");
	private static JMenuItem expand2 = new JMenuItem("Expand right");
	private static JMenuItem expand3 = new JMenuItem("Expand bottom");
	private static JMenuItem expand4 = new JMenuItem("Expand left");
	private static JMenuItem expand5 = new JMenuItem("Expand sky");
	private static JMenuItem contract0 = new JMenuItem("Contract ground");
	private static JMenuItem contract1 = new JMenuItem("Contract top");
	private static JMenuItem contract2 = new JMenuItem("Contract right");
	private static JMenuItem contract3 = new JMenuItem("Contract bottom");
	private static JMenuItem contract4 = new JMenuItem("Contract left");
	private static JMenuItem contract5 = new JMenuItem("Contract sky");

	public GUI() throws Exception {
	} // creating the GUI

	public static void initGUI() { // actually initializing it
		thisGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		thisGUI.setTitle("Circuit Simulator v0.92");
		Graph canvas = new Graph();
		canvas.preparePaint(new Block(BlockType.Repeater, 16, 1, 8, 0),
				new Block(), 16);
		thisGUI.setIconImage(canvas.getImage(16));

		// workingArea
		setWorkingArea();

		Dimension pals = Palette.getPaletteShape();
		// first panel
		firstMenu.setPreferredSize(new Dimension(
				40 + 40 * Palette.getMaxCols(), 420)); // menu size
		firstMenu.setLayout(new BoxLayout(firstMenu, BoxLayout.Y_AXIS));
		JLabel menuName = new JLabel("Palette:"); // header
		menuName.setFont(new Font("header", 1, 20));
		menuName.setAlignmentX(Component.CENTER_ALIGNMENT);
		firstMenu.add(menuName);
		// actual palette
		palette = Palette.setPalette();
		palette.setAlignmentX(Component.CENTER_ALIGNMENT);
		palette.setMaximumSize(new Dimension(40 * pals.width,
				pals.height * 36 + 4));
		palette.setPreferredSize(new Dimension(40 * pals.width,
				pals.height * 36 + 4));
		palette.setMinimumSize(new Dimension(40 * pals.width,
				pals.height * 36 + 4));
		palette.addMouseListener(thisGUI);
		firstMenu.add(palette);
		if (Options.viewTwoLayer) {
			firstMenu.add(Box.createRigidArea(new Dimension(0, 5)));
		} else {
			firstMenu.add(Box.createRigidArea(new Dimension(0, 25)));
		}
		firstMenu.add(Box.createVerticalGlue());
		JLabel currentName = new JLabel("Selection:");
		currentName.setFont(new Font("header", 1, 14));
		currentName.setAlignmentX(Component.CENTER_ALIGNMENT);
		firstMenu.add(currentName);
		// showing the chosen block
		current = Palette.getCurrent();
		firstMenu.add(current);
		Dimension fillerDims = new Dimension(0, 20);
		firstMenu.add(new Box.Filler(fillerDims, fillerDims, fillerDims));

		// second panel
		secondMenu.setPreferredSize(new Dimension(110, 400));
		secondMenu.setLayout(new BoxLayout(secondMenu, BoxLayout.Y_AXIS));
		JLabel head = new JLabel("Options:");
		head.setFont(new Font("header", 1, 20));
		head.setAlignmentX(Component.CENTER_ALIGNMENT);
		secondMenu.add(head);
		canvas.preparePaint(0, 3, 32);
		JButton up = new JButton(new ImageIcon(canvas.getImage(32))); // up
																		// button
		up.setMaximumSize(new Dimension(40, 40));
		up.setAlignmentX(Component.CENTER_ALIGNMENT);
		up.addActionListener(thisGUI);
		secondMenu.add(up);
		canvas.preparePaint(0, 1, 32);
		JButton down = new JButton(new ImageIcon(canvas.getImage(32))); // down
																		// button
		down.setMaximumSize(new Dimension(40, 40));
		down.setAlignmentX(Component.CENTER_ALIGNMENT);
		down.addActionListener(thisGUI);
		secondMenu.add(down);
		canvas.preparePaint(1, 3, 32);
		JButton tick = new JButton(new ImageIcon(canvas.getImage(32))); // tick
																		// button
		tick.setMaximumSize(new Dimension(40, 40));
		tick.setAlignmentX(Component.CENTER_ALIGNMENT);
		tick.addActionListener(thisGUI);
		secondMenu.add(tick);
		canvas.preparePaint(3, 3, 32);
		playPause = new JButton(new ImageIcon(canvas.getImage(32))); // play
																		// pause
																		// button
		playPause.setMaximumSize(new Dimension(40, 40));
		playPause.setAlignmentX(Component.CENTER_ALIGNMENT);
		playPause.addActionListener(thisGUI);
		secondMenu.add(playPause);
		secondMenu.add(Box.createRigidArea(new Dimension(0, 10)));
		JLabel thisLayer = new JLabel("Layer: " + (Options.level + 1));
		thisLayer.setFont(new Font("header", 1, 20));
		thisLayer.setAlignmentX(Component.CENTER_ALIGNMENT);
		secondMenu.add(thisLayer);
		secondMenu.add(Box.createRigidArea(new Dimension(0, 30)));
		timer.setInitialDelay(0);
		timer.addActionListener(thisGUI);

		// start of options
		logicTicks = new JCheckBox("Use logic ticks");
		logicTicks.setSelected(Options.logicTicks);
		logicTicks.setHorizontalTextPosition(SwingConstants.CENTER);
		logicTicks.setVerticalTextPosition(SwingConstants.TOP);
		;
		logicTicks.setAlignmentX(Component.CENTER_ALIGNMENT);
		logicTicks.addActionListener(thisGUI);
		secondMenu.add(logicTicks);
		twoView = new JCheckBox("Two-layered view");
		twoView.setSelected(Options.viewTwoLayer);
		twoView.setHorizontalTextPosition(SwingConstants.CENTER);
		twoView.setVerticalTextPosition(SwingConstants.TOP);
		;
		twoView.setAlignmentX(Component.CENTER_ALIGNMENT);
		twoView.addActionListener(thisGUI);
		secondMenu.add(twoView);

		// menu bars
		JMenuBar menuBar = new JMenuBar();
		JMenu files = new JMenu("Files"); // files
		makeNew.addActionListener(thisGUI);
		load.addActionListener(thisGUI);
		save.addActionListener(thisGUI);
		files.add(makeNew);
		files.add(load);
		files.add(save);
		// size
		JMenu size = new JMenu("Size");
		expand0.addActionListener(thisGUI);
		expand1.addActionListener(thisGUI);
		expand2.addActionListener(thisGUI);
		expand3.addActionListener(thisGUI);
		expand4.addActionListener(thisGUI);
		expand5.addActionListener(thisGUI);
		contract0.addActionListener(thisGUI);
		contract1.addActionListener(thisGUI);
		contract2.addActionListener(thisGUI);
		contract3.addActionListener(thisGUI);
		contract4.addActionListener(thisGUI);
		contract5.addActionListener(thisGUI);
		size.add(expand1);
		size.add(expand3);
		size.add(expand4);
		size.add(expand2);
		size.add(expand0);
		size.add(expand5);
		size.addSeparator();
		size.add(contract1);
		size.add(contract3);
		size.add(contract4);
		size.add(contract2);
		size.add(contract0);
		size.add(contract5);
		// setting up
		menuBar.add(files);
		menuBar.add(size);
		thisGUI.setJMenuBar(menuBar);

		// setting all up
		contentPane.add(firstMenu, BorderLayout.WEST);
		contentPane.add(workArea, BorderLayout.CENTER);
		contentPane.add(secondMenu, BorderLayout.EAST);
		// displaying
		thisGUI.setContentPane(contentPane);
		thisGUI.pack();
		thisGUI.setVisible(true);
	}

	public static void setWorkingArea() {
		panelWork = Workspace.renderLayer(false); // rendering the one layered
													// view
		zLayers = new JPanel();
		zLayers.setLayout(overlay);
		panelWork2 = Workspace.renderLayer(true); // rendering the two-layered
													// view
		zLayers2 = new JPanel();
		zLayers2.setLayout(overlay2);
		int z = 0;
		for (int i = 0; i < panelWork.size(); i++) {
			panelWork.get(i).addMouseListener(thisGUI);
			panelWork.get(i).addMouseMotionListener(thisGUI);
			panelWork2.get(i).addMouseListener(thisGUI);
			panelWork2.get(i).addMouseMotionListener(thisGUI);
			zLayers.add(panelWork.get(i), String.valueOf(i));
			zLayers2.add(panelWork2.get(i), String.valueOf(i));
			if (i == Options.level) {
				z = i;
			}
		}
		zLayers.setBackground(Color.gray);
		zLayers2.setBackground(Color.gray);
		overlay.show(zLayers, String.valueOf(z)); // showing the right layer
		overlay2.show(zLayers2, String.valueOf(z)); // showing the right layer
		nesting = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // nesting
		if (Options.viewTwoLayer) {
			nesting.add(zLayers2);
		} else {
			nesting.add(zLayers);
		}
		nesting.setBackground(Color.white);
		workArea = new JScrollPane(nesting);
		workArea.setBackground(Color.white);
	}

	public void updateAllAndRepaint() {
		updateAll();
		panelWork.get(Options.level).repaint();
	}

	public void actionPerformed(ActionEvent event) {
		// works for the second menu and the menubar
		if (event.getSource().equals(secondMenu.getComponent(1))) { 
			// up pressed
			if (Options.level < Workspace.getz() - 1) {
				Options.level++;
				overlay.show(zLayers, String.valueOf(Options.level)); 
				overlay2.show(zLayers2, String.valueOf(Options.level));
				setLevelLabel();
			}
		} else if (event.getSource().equals(secondMenu.getComponent(2))) { 
			// down pressed
			if (Options.level > 0) {
				Options.level--;
				overlay.show(zLayers, String.valueOf(Options.level)); 
				overlay2.show(zLayers2, String.valueOf(Options.level));
				setLevelLabel();
			}

		} else if (event.getSource().equals(secondMenu.getComponent(3))) { 
			// step tick pressed
			if (!Options.logicTicks) {
				Logic.tick();
			}
			Logic.tick();
			Logic.noTick();
		} else if (event.getSource().equals(secondMenu.getComponent(4))) {
			// play/pause
			if (!Options.play) {
				Options.play = true;
				int delay = Math.round(1000 / Options.ticksPerSecond);
				if (!Options.logicTicks) {
					delay = delay * 2;
				}
				timer.setDelay(delay);
				timer.start();
			} else {
				Options.play = false;
				timer.stop();
			}
			Graph canvas = new Graph();
			if (Options.play) {
				canvas.preparePaint(2, 3, 32);
			} else {
				canvas.preparePaint(3, 3, 32);
			}
			playPause.setIcon(new ImageIcon(canvas.getImage(32)));
		} else if (event.getSource().equals(logicTicks)) { // tick option
			Options.logicTicks = logicTicks.isSelected();
			int delay = Math.round(1000 / Options.ticksPerSecond);
			if (!Options.logicTicks) {
				delay = delay * 2;
			}
			timer.setDelay(delay);
		} else if (event.getSource().equals(twoView)) { // viewmode
			nesting.remove(0);
			Options.viewTwoLayer = twoView.isSelected();
			if (Options.viewTwoLayer) {
				nesting.add(zLayers2);
				firstMenu.remove(2);
				firstMenu.add(Box.createRigidArea(new Dimension(0, 5)), 2);
			} else {
				nesting.add(zLayers);
				firstMenu.remove(2);
				firstMenu.add(Box.createRigidArea(new Dimension(0, 25)), 2);
			}
			Palette.swapLayer();
			Dimension pals = Palette.getPaletteShape();
			// first panel
			// firstMenu.setPreferredSize(new Dimension(40+40*pals.width, 420));
			// //menu size

			// actual palette
			palette = Palette.setPalette();
			palette.setAlignmentX(Component.CENTER_ALIGNMENT);
			palette.setMaximumSize(new Dimension(40 * pals.width,
					pals.height * 36 + 4));
			palette.setPreferredSize(new Dimension(40 * pals.width,
					pals.height * 36 + 4));
			palette.setMinimumSize(new Dimension(40 * pals.width,
					pals.height * 36 + 4));

			palette.addMouseListener(this);
			firstMenu.remove(1);
			firstMenu.add(palette, 1);
			current = Palette.getCurrent();
			firstMenu.remove(5);
			firstMenu.add(current, 5);
			firstMenu.repaint();
			nesting.repaint();
		} else if (event.getSource().equals(timer)) { // play is updating
			if (Options.play) {
				if (!Options.logicTicks) {
					Logic.tick();
				}
				Logic.tick();
				Logic.noTick();
			}

			// menubar
		} else if (event.getSource().equals(save)) { // saving
			SaveLoad.save();
		} else if (event.getSource().equals(load)) { // loading
			SaveLoad.load();
		} else if (event.getSource().equals(makeNew)) { // new
			new Workspace(Options.startx, Options.starty, Options.startz);
			updateWorkingArea();
			// expand
		} else if (event.getSource().equals(expand0)) { // expand down
			Workspace.expand(0);
		} else if (event.getSource().equals(expand1)) { // expand north
			Workspace.expand(1);
		} else if (event.getSource().equals(expand2)) { // expand east
			Workspace.expand(2);
		} else if (event.getSource().equals(expand3)) { // expand south
			Workspace.expand(3);
		} else if (event.getSource().equals(expand4)) { // expand west
			Workspace.expand(4);
		} else if (event.getSource().equals(expand5)) { // expand up
			Workspace.expand(5);
			// contract
		} else if (event.getSource().equals(contract0)) { // contract down
			Workspace.contract(0);
		} else if (event.getSource().equals(contract1)) { // contract north
			Workspace.contract(1);
		} else if (event.getSource().equals(contract2)) { // contract east
			Workspace.contract(2);
		} else if (event.getSource().equals(contract3)) { // contract south
			Workspace.contract(3);
		} else if (event.getSource().equals(contract4)) { // contract west
			Workspace.contract(4);
		} else if (event.getSource().equals(contract5)) { // contract up
			Workspace.contract(5);
		}

		this.validate();
	}

	public static void setLevelLabel() {
		secondMenu.remove(6);
		JLabel thisLayer = new JLabel("Layer: " + (Options.level + 1));
		thisLayer.setFont(new Font("header", 1, 20));
		thisLayer.setAlignmentX(Component.CENTER_ALIGNMENT);
		secondMenu.add(thisLayer, 6);
	}

	public void mouseExited(MouseEvent event) {
	} // Due to implementation

	public void mouseClicked(MouseEvent event) {
	} // Due to implementation

	public void mouseMoved(MouseEvent event) {
	} // Due to implementation

	public void mouseEntered(MouseEvent event) {
	} // Due to implementation

	public void mouseReleased(MouseEvent event) {
		coord.x = -1;
		coord.y = -1;
	}

	public void mouseDragged(MouseEvent event) {
		if (event.getComponent().equals(panelWork.get(Options.level))
				|| event.getComponent().equals(panelWork2.get(Options.level))) {
			if (!Workspace.getBlockxy(event.getPoint()).equals(coord)
					&& Workspace.getBlockxy(event.getPoint()).x != -1) {
				coord = Workspace.getBlockxy(event.getPoint());
				workingAreaEvent(event, false);
			}
		}
	}

	public void mousePressed(MouseEvent event) {
		// if palette pressed
		if (event.getComponent().equals(palette)) {
			Palette.changeType(event.getPoint()); // actual changing
			current = Palette.getCurrent();
			firstMenu.remove(5);
			firstMenu.add(current, 5);

			// if workingArea pressed
		} else if (event.getComponent().equals(panelWork.get(Options.level))
				|| event.getComponent().equals(panelWork2.get(Options.level))) {
			coord = Workspace.getBlockxy(event.getPoint());
			workingAreaEvent(event, true);
		}
		this.validate();
	}

	public static void retractPistonIfPartOfActive(int x, int y, int z) {
		Block b = Workspace.getBlock(x, y, z);
		if (!b.isPartOfActivePiston())
			return;

		if (b.id == BlockType.PistonPaddle) {
			final int[] oppositeDir = { 5, 3, 4, 1, 2, 0 };
			final int[] dir = Block.dirModifier[oppositeDir[b.place]];
			b = Workspace.getBlock(x + dir[0], y + dir[1], z + dir[2]);
		}

		if (b.id.isPistonType())
			b.retractPiston();
	}

	public static void workingAreaEvent(MouseEvent event, boolean trueClick) {
		int operation = event.getButton();
		if (coord.x < 0 || coord.y < 0) { // if false click
			return;
		}
		BlockType topOne = BlockType.TopLayer; // -1 if top layer
		BlockType lowerOne = Workspace
				.getBlock(coord.x, coord.y, Options.level).id;
		if (Options.level < Workspace.getz() - 1) {
			topOne = Workspace.getBlock(coord.x, coord.y, Options.level + 1).id;
		}

		if (operation == 1 && !event.isShiftDown()) { // main click ( left
														// button )
			if (!Options.viewTwoLayer) { // one layer
				if (lowerOne == Palette.getPal()[Palette.selectId][0]
						&& trueClick) {
					Workspace.replace(coord.x, coord.y, Options.level);
				} else {
					retractPistonIfPartOfActive(coord.x, coord.y, Options.level);
					Workspace.setBlock(coord.x, coord.y, Options.level, Palette
							.getPal()[Palette.selectId][0]);
				}
			} else { // two layers
				if ((Palette.getPal()[Palette.selectId][0] == BlockType.Air || Palette
						.getPal()[Palette.selectId][0] == lowerOne)
						&& (Palette.getPal()[Palette.selectId][1] == BlockType.Air
								|| Palette.getPal()[Palette.selectId][1] == topOne || topOne == BlockType.TopLayer)) {
					if (trueClick) { // replace
						// if (lowerOne.hasMiddleClickAction())
						if (lowerOne.isRightClickReplacableType())
							Workspace.replace(coord.x, coord.y, Options.level);
						else if (topOne.isRightClickReplacableType())
							Workspace.replace(coord.x, coord.y,
									Options.level + 1);
					}
				} else { // set
					if (Palette.getPal()[Palette.selectId][0] != BlockType.Air) {
						retractPistonIfPartOfActive(coord.x, coord.y,
								Options.level);
						Workspace.setBlock(coord.x, coord.y, Options.level,
								Palette.getPal()[Palette.selectId][0]);
					}
					if (Palette.getPal()[Palette.selectId][1] != BlockType.Air
							&& topOne != BlockType.TopLayer) {
						retractPistonIfPartOfActive(coord.x, coord.y,
								Options.level + 1);
						Workspace.setBlock(coord.x, coord.y, Options.level + 1,
								Palette.getPal()[Palette.selectId][1]);
					}
				}
			}
		} else if (operation == 3) { // secondary click ( right mousebutton )
			retractPistonIfPartOfActive(coord.x, coord.y, Options.level);
			if (lowerOne.isTallBlock()) {
				if (Workspace.getBlock(coord.x, coord.y, Options.level).subType
						.isLowerType()
						&& Workspace.getBlock(coord.x, coord.y,
								Options.level + 1).subType.isUpperType())
					Workspace.setBlock(coord.x, coord.y, Options.level + 1,
							BlockType.Air);
				else if (Workspace.getBlock(coord.x, coord.y, Options.level).subType
						.isUpperType()
						&& Workspace.getBlock(coord.x, coord.y,
								Options.level - 1).subType.isLowerType())
					Workspace.setBlock(coord.x, coord.y, Options.level - 1,
							BlockType.Air);
			}
			Workspace.setBlock(coord.x, coord.y, Options.level, BlockType.Air); // deleting
																				// block
			if (Options.viewTwoLayer && Options.level < Workspace.getz() - 1) {
				if (lowerOne == BlockType.Air) {
					retractPistonIfPartOfActive(coord.x, coord.y,
							Options.level + 1);
					Workspace.setBlock(coord.x, coord.y, Options.level + 1,
							BlockType.Air); // deleting block
				}
			}
		} else if ((operation == 2 && trueClick)
				|| (operation == 1 && event.isShiftDown())) { // mousewheel
																// pressed

			if (lowerOne == BlockType.Block
					&& !topOne.isMiddleClickManipulatable()) {
				Workspace.getBlock(coord.x, coord.y, Options.level)
						.nextBlockSubtype();
			} else if (topOne == BlockType.Block) {
				Workspace.getBlock(coord.x, coord.y, Options.level + 1)
						.nextBlockSubtype();
			} else if (lowerOne == BlockType.Door
					&& !topOne.isMiddleClickManipulatable()) {
				Workspace.getBlock(coord.x, coord.y, Options.level)
						.nextDoorSubtype(coord.x, coord.y, Options.level);
			} else if (topOne == BlockType.Door) {
				Workspace.getBlock(coord.x, coord.y, Options.level + 1)
						.nextDoorSubtype(coord.x, coord.y, Options.level);
			} else if (lowerOne == BlockType.Rail
					&& !topOne.isMiddleClickManipulatable()) {
				Workspace.getBlock(coord.x, coord.y, Options.level)
						.nextRailSubtype();
			} else if (topOne == BlockType.Rail) {
				Workspace.getBlock(coord.x, coord.y, Options.level + 1)
						.nextRailSubtype();
			} else if (lowerOne.isMiddleClickManipulatable()) {
				if (lowerOne == BlockType.Repeater) {
					Workspace.getBlock(coord.x, coord.y, Options.level)
							.increaseTick();
				} else if (lowerOne.isPlayerActivated()) {
					if (Workspace.getBlock(coord.x, coord.y, Options.level).charge == 0) {
						Workspace.getBlock(coord.x, coord.y, Options.level).charge = 16;
					} else {
						Workspace.getBlock(coord.x, coord.y, Options.level).charge = 0;
					}
				}
			} else if (topOne.isMiddleClickManipulatable()) {
				if (topOne == BlockType.Repeater) {
					Workspace.getBlock(coord.x, coord.y, Options.level + 1)
							.increaseTick();
				} else if (topOne.isPlayerActivated()) {
					if (Workspace.getBlock(coord.x, coord.y, Options.level + 1).charge == 0) {
						Workspace.getBlock(coord.x, coord.y, Options.level + 1).charge = 16;
					} else {
						Workspace.getBlock(coord.x, coord.y, Options.level + 1).charge = 0;
					}
				}
			}
		}

		// updating all neighborblocks in and the columns for -1 to 2
		int[] xs = { coord.x, coord.x, coord.x, coord.x, coord.x - 1,
				coord.x - 1, coord.x - 1, coord.x - 1, coord.x + 1,
				coord.x + 1, coord.x + 1, coord.x + 1, coord.x, coord.x,
				coord.x, coord.x, coord.x, coord.x, coord.x, coord.x };
		int[] ys = { coord.y, coord.y, coord.y, coord.y, coord.y, coord.y,
				coord.y, coord.y, coord.y, coord.y, coord.y, coord.y,
				coord.y - 1, coord.y - 1, coord.y - 1, coord.y - 1,
				coord.y + 1, coord.y + 1, coord.y + 1, coord.y + 1 };
		int[] zs = { Options.level, Options.level - 1, Options.level + 1,
				Options.level + 2, Options.level, Options.level - 1,
				Options.level + 1, Options.level + 2, Options.level,
				Options.level - 1, Options.level + 1, Options.level + 2,
				Options.level, Options.level - 1, Options.level + 1,
				Options.level + 2, Options.level, Options.level - 1,
				Options.level + 1, Options.level + 2, };
		updateBlocks(xs, ys, zs); // updates surrounding blocks
		if (!Options.play) { // updates the state after one tick, just like
								// ingame, only if playing
			Logic.noTick();
		}
	}

	public static void updateBlocks(int[] xIn, int[] yIn, int[] zIn) { 
		// ought to have same length
		int size;

		// for one layer
		size = xIn.length;
		if (yIn.length < size) {
			size = yIn.length;
		}
		if (zIn.length < size) {
			size = zIn.length;
		}
		for (int i = 0; i < size; i++) {
			if (!(xIn[i] < 0 || yIn[i] < 0 || zIn[i] < 0
					|| xIn[i] >= Workspace.getx() || yIn[i] >= Workspace.gety() || zIn[i] >= Workspace
					.getz())) {
				// rerender the new graphics
				try {
					((Graph) panelWork.get(zIn[i]).getComponent(
							yIn[i] * Workspace.getx() + xIn[i])).preparePaint(
							xIn[i], yIn[i], zIn[i], false);
					((Graph) panelWork2.get(zIn[i]).getComponent(
							yIn[i] * Workspace.getx() + xIn[i])).preparePaint(
							xIn[i], yIn[i], zIn[i], true);
					if (zIn[i] > 0) {
						((Graph) panelWork2.get(zIn[i] - 1).getComponent(
								yIn[i] * Workspace.getx() + xIn[i]))
								.preparePaint(xIn[i], yIn[i], zIn[i] - 1, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void updateAll() {
		int[] x = new int[Workspace.getx() * Workspace.gety()
				* Workspace.getz()];
		int[] y = new int[Workspace.getx() * Workspace.gety()
				* Workspace.getz()];
		int[] z = new int[Workspace.getx() * Workspace.gety()
				* Workspace.getz()];
		for (int i = 0; i < Workspace.getx(); i++) {
			for (int j = 0; j < Workspace.gety(); j++) {
				for (int h = 0; h < Workspace.getz(); h++) {
					x[i * Workspace.gety() * Workspace.getz() + j
							* Workspace.getz() + h] = i;
					y[i * Workspace.gety() * Workspace.getz() + j
							* Workspace.getz() + h] = j;
					z[i * Workspace.gety() * Workspace.getz() + j
							* Workspace.getz() + h] = h;
				}
			}
		}
		thisGUI.validate();
		for (int i = 0; i < x.length; i++) {
			((Graph) panelWork.get(z[i]).getComponent(
					y[i] * Workspace.getx() + x[i])).repaint();
			((Graph) panelWork2.get(z[i]).getComponent(
					y[i] * Workspace.getx() + x[i])).repaint();
		}
	}

	// not used yet. Apparently it bugs whatever I do.
	public static void injectSlice(int dir, boolean twoLayers) { 
		// once false is called, true must always be called
		ArrayList<JPanel> panel;
		if (twoLayers) {
			panel = panelWork2;
		} else {
			panel = panelWork;
		}
		switch (dir) { // for some reason, they should be paired.
		case 1: // north
		case 3: { // south
			for (int i = 0; i < panel.size(); i++) {
				GridLayout grid = (GridLayout) panel.get(i).getLayout();
				grid.setRows(grid.getRows() + 1);
				for (int x = 0; x < Workspace.getx(); x++) {
					Graph canvas = new Graph();
					canvas.setPreferredSize(new Dimension(Options.size,
							Options.size)); // size
					canvas.preparePaint(x, Workspace.gety() - 1, i, twoLayers);
					panel.get(i).add(canvas,
							Workspace.getx() * (Workspace.gety() - 1) + x);
				}
			}
		}
			break;
		case 2: // east
		case 4: { // north
			for (int i = 0; i < panel.size(); i++) {
				GridLayout grid = (GridLayout) panel.get(i).getLayout();
				grid.setColumns(grid.getColumns() + 1);
				for (int y = 0; y < Workspace.gety(); y++) {
					Graph canvas = new Graph();
					canvas.setPreferredSize(new Dimension(Options.size,
							Options.size)); // size
					canvas.preparePaint(Workspace.getx() - 1, y, i, twoLayers);
					panel.get(i).add(canvas, (Workspace.getx() * (y + 1)) - 1);
				}
			}
		}
			break;
		}
	}

	public static void run(boolean b) {
		if (b && !timer.isRunning()) { // shall start
			timer.restart();
			Options.play = true;
		} else if (!b && timer.isRunning()) { // stops
			timer.stop();
			Options.play = false;
		}
	}

	public static void updateWorkingArea() {
		setLevelLabel();
		contentPane.remove(GUI.workArea);
		setWorkingArea();
		contentPane.add(GUI.workArea, BorderLayout.CENTER);
		Logic.noTick(); // clear op all unintended errors
	}

	public static void main(String[] args) {
		new Workspace(Options.startx, Options.starty, Options.startz);
		try {
			thisGUI = new GUI();
			initGUI();
		} catch (Exception e) {
			System.out.println("Error. Exiting");
			e.printStackTrace();
		}
	}
}