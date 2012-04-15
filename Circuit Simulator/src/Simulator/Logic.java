package Simulator;

import java.util.*;

/*
 This class in making all calculations. I consists mainly of two public methods. tick and notick
 tick punches the time by one logic tick, and maybe changes the state of repeaters and torches.
 notick spreading the power again.
 */

public class Logic {
	// those who needs to update their animations
	private static ArrayList<Integer> xUpdate = new ArrayList<Integer>();
	private static ArrayList<Integer> yUpdate = new ArrayList<Integer>();
	private static ArrayList<Integer> zUpdate = new ArrayList<Integer>();
	// holding those to be investigated as powered
	private static ArrayList<Integer> xSource = new ArrayList<Integer>();
	private static ArrayList<Integer> ySource = new ArrayList<Integer>();
	private static ArrayList<Integer> zSource = new ArrayList<Integer>();

	private static ArrayList<ArrayList<ArrayList<Block>>> lastTick 
		= new ArrayList<ArrayList<ArrayList<Block>>>();
	// temporary data of last tick

	public static synchronized void tick() {
		// copies all data to temprary
		for (int x = 0; x < Workspace.getx(); x++) {
			lastTick.add(new ArrayList<ArrayList<Block>>());
			for (int y = 0; y < Workspace.gety(); y++) {
				lastTick.get(x).add(new ArrayList<Block>());
				for (int z = 0; z < Workspace.getz(); z++) {
					Block notCopied = Workspace.getBlock(x, y, z);
					lastTick.get(x).get(y).add(new Block(notCopied));
					// copies all data to temprary
				}
			}
		}

		for (int x = 0; x < Workspace.getx(); x++) {
			for (int y = 0; y < Workspace.gety(); y++) {
				for (int z = 0; z < Workspace.getz(); z++) {
					if (getBlock(x, y, z).id == BlockType.Torch) {
						// apply on torches
						boolean isPowered = false;
						switch (getBlock(x, y, z).place) {
						case 0: {
							if (z > 0) {
								if (getBlock(x, y, z - 1).charge > 0) {
									isPowered = true;
								}
							}
						}
							break;
						case 1: {
							if (y > 0) {
								if (getBlock(x, y - 1, z).charge > 0) {
									isPowered = true;
								}
							}
						}
							break;
						case 2: {
							if (x < Workspace.getx() - 1) {
								if (getBlock(x + 1, y, z).charge > 0) {
									isPowered = true;
								}
							}
						}
							break;
						case 3: {
							if (y < Workspace.gety() - 1) {
								if (getBlock(x, y + 1, z).charge > 0) {
									isPowered = true;
								}
							}
						}
							break;
						case 4: {
							if (x > 0) {
								if (getBlock(x - 1, y, z).charge > 0) {
									isPowered = true;
								}
							}
						}
							break;
						}
						if (Workspace.getBlock(x, y, z).waitTick(isPowered)) {
							// makes sure the torch is getting updated
							xUpdate.add(new Integer(x));
							yUpdate.add(new Integer(y));
							zUpdate.add(new Integer(z));
						}
					}

					if (getBlock(x, y, z).id == BlockType.Repeater) {
						// apply on repeaters
						boolean powered = false;
						switch (getBlock(x, y, z).place) {
						case 1: // pointing north
							if (y < Workspace.gety() - 1) {
								if (Block.providesPower(x, y + 1, z, 1))
									powered = true;
							}
							break;
						case 2: // pointing west
							if (x > 0) {
								if (Block.providesPower(x - 1, y, z, 2))
									powered = true;
							}
							break;
						case 3: // pointing south
							if (y > 0) {
								if (Block.providesPower(x, y - 1, z, 3))
									powered = true;
							}
							break;
						case 4: // pointing east
							if (x < Workspace.getx() - 1) {
								if (Block.providesPower(x + 1, y, z, 4))
									powered = true;
							}
							break;
						}
						Workspace.getBlock(x, y, z).waitTick(powered);
						// makes sure the repeater is getting updated
						xUpdate.add(new Integer(x));
						yUpdate.add(new Integer(y));
						zUpdate.add(new Integer(z));
					}

					if (getBlock(x, y, z).id.isPistonType()) {
						// apply on pistons
						Block piston = getBlock(x, y, z);
						boolean powered = false;
						for (int d = 0; d <= 5 && !powered; d++) {
							if (piston.place == d)
								continue;
							switch (d) {
							case 0: {
								powered = Block.providesPower(x, y, z + 1, 5);
								break;
							}
							case 1:
								powered = Block.providesPower(x, y - 1, z, 3)
										|| Block.providesPower(x, y - 1, z + 1,
												3);
								break;
							case 2:
								powered = Block.providesPower(x + 1, y, z, 4)
										|| Block.providesPower(x + 1, y, z + 1,
												4);
								break;
							case 3:
								powered = Block.providesPower(x, y + 1, z, 1)
										|| Block.providesPower(x, y + 1, z + 1,
												1);
								break;
							case 4:
								powered = Block.providesPower(x - 1, y, z, 2)
										|| Block.providesPower(x - 1, y, z + 1,
												2);
								break;
							case 5: {
								powered = Block.providesPower(x, y, z - 1, 0);
								break;
							}
							}
						}

						Workspace.getBlock(x, y, z).waitTick(powered);
						// makes sure the piston is getting updated
						xUpdate.add(new Integer(x));
						yUpdate.add(new Integer(y));
						zUpdate.add(new Integer(z));
					}

					if (getBlock(x, y, z).id.isButtonType()) { // apply on
																// buttons
						if (Workspace.getBlock(x, y, z).waitTick(false)) {
							xUpdate.add(new Integer(x));
							yUpdate.add(new Integer(y));
							zUpdate.add(new Integer(z));
						}
					}
				}
			}
		}
		lastTick.clear(); // clear all temporary data
	}

	public static synchronized void noTick() {

		// copies all data to temporary and reset all non sources
		for (int x = 0; x < Workspace.getx(); x++) {
			lastTick.add(new ArrayList<ArrayList<Block>>());
			for (int y = 0; y < Workspace.gety(); y++) {
				lastTick.get(x).add(new ArrayList<Block>());
				for (int z = 0; z < Workspace.getz(); z++) {
					Block notCopied = Workspace.getBlock(x, y, z);
					lastTick.get(x).get(y).add(new Block(notCopied));
					// copies all data to temprary buffer
					if (Workspace.getBlock(x, y, z).charge == 16
							&& Workspace.getBlock(x, y, z).id != BlockType.Block) { // all
																					// sources
						addBlock(x, y, z);
					} else if (Workspace.getBlock(x, y, z).charge > 0) {
						Workspace.getBlock(x, y, z).charge = 0;
						// unpowers all other
						xUpdate.add(new Integer(x));
						yUpdate.add(new Integer(y));
						zUpdate.add(new Integer(z));
					}
				}
			}
		}

		// spreading the power
		for (int h = 0; h < 20; h++) { // runs long enough
			// currently under investigation
			ArrayList<Integer> xNow = new ArrayList<Integer>();
			ArrayList<Integer> yNow = new ArrayList<Integer>();
			ArrayList<Integer> zNow = new ArrayList<Integer>();
			for (int i = 0; i < xSource.size(); i++) {
				xNow.add(xSource.get(i));
				yNow.add(ySource.get(i));
				zNow.add(zSource.get(i));
			}
			xSource.clear();
			ySource.clear();
			zSource.clear();

			for (int i = 0; i < xNow.size(); i++) {
				int x = xNow.get(i).intValue();
				int y = yNow.get(i).intValue();
				int z = zNow.get(i).intValue();

				Block block = Workspace.getBlock(x, y, z);
				switch (block.id) {
				case Block: {
					if (block.charge == 16) {
						blockPower(x, y, z);
					}
					break; // directly powered
				}
				case Wire:
					wirePower(x, y, z);
					break;
				case Torch:
					torchPower(x, y, z);
					break;
				case Repeater:
					repeaterPower(x, y, z);
					break;
				case PowerRail:
					railPower(x, y, z);
					break;
				case Button: // fall through
				case Lever:
				case PressurePad:
					inputPower(x, y, z);
					break;
				case Door:
					inputDoor(x, y, z);
				case DetectorRail:
					inputRail(x, y, z);
				}
			}
		}

		// updates all not similar to before
		for (int i = 0; i < xUpdate.size(); i++) {
			if (Workspace.getBlock(xUpdate.get(i), yUpdate.get(i), zUpdate
					.get(i)).id == BlockType.Wire) {
				// only wire will can be bypassed
				if (Workspace.getBlock(xUpdate.get(i), yUpdate.get(i), zUpdate
						.get(i)).charge > 0
						&& getBlock(xUpdate.get(i), yUpdate.get(i), zUpdate
								.get(i)).charge > 0) {
					xUpdate.remove(i);
					yUpdate.remove(i);
					zUpdate.remove(i);
					i--;
				} else if (Workspace.getBlock(xUpdate.get(i), yUpdate.get(i),
						zUpdate.get(i)).charge == 0
						&& getBlock(xUpdate.get(i), yUpdate.get(i), zUpdate
								.get(i)).charge == 0) {
					xUpdate.remove(i);
					yUpdate.remove(i);
					zUpdate.remove(i);
					i--;
				}
			}
		}

		int[] xArray = new int[xUpdate.size()];
		int[] yArray = new int[yUpdate.size()];
		int[] zArray = new int[zUpdate.size()];
		for (int k = 0; k < xUpdate.size(); k++) {
			xArray[k] = xUpdate.get(k).intValue();
			yArray[k] = yUpdate.get(k).intValue();
			zArray[k] = zUpdate.get(k).intValue();
		}

		// clear all temporary data
		xUpdate.clear();
		yUpdate.clear();
		zUpdate.clear();
		xSource.clear();
		ySource.clear();
		zSource.clear();
		lastTick.clear();

		GUI.updateBlocks(xArray, yArray, zArray);

	}

	private static void blockPower(int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			power(x, y, z, i, BlockType.Wire, 15);
			power(x, y, z, i, BlockType.Door, 1);
			power(x, y, z, i, BlockType.Trapdoor, 1);
			power(x, y, z, i, BlockType.PowerRail, 9);
		}
	}

	private static void torchPower(int x, int y, int z) {
		BlockType target = BlockType.Wire;
		int charge = 15; // 17-target(2)
		for (int i = 0; i < 6; i++) {
			if (i == 5) {
				target = BlockType.Block;
				charge = 16; // 17-target(1)
			}
			power(x, y, z, i, target, charge);
			power(x, y, z, i, BlockType.Door, 1);
			power(x, y, z, i, BlockType.Trapdoor, 1);
			power(x, y, z, i, BlockType.PowerRail, 9);
		}
	}

	private static void repeaterPower(int x, int y, int z) {
		power(x, y, z, Workspace.getBlock(x, y, z).place, BlockType.Wire, 15);
		power(x, y, z, Workspace.getBlock(x, y, z).place, BlockType.Block, 16);
		power(x, y, z, Workspace.getBlock(x, y, z).place, BlockType.Door, 1);
		power(x, y, z, Workspace.getBlock(x, y, z).place, BlockType.Trapdoor, 1);
		power(x, y, z, Workspace.getBlock(x, y, z).place, BlockType.PowerRail,
				9);
	}

	private static void railPower(int x, int y, int z) {
		int place = Workspace.getBlock(x, y, z).place;

		// up
		if (z < Workspace.getz()) {
			for (int i = 0; i < 4; i++) {
				int targetCharge = 16;
				Block testBlock;
				if (i == 0 && place == 2 && y > 0) { // north
					testBlock = Workspace.getBlock(x, y - 1, z + 1);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 1 || testBlock.place == 2)) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 1 && place == 4 && x < Workspace.getx() - 1) { // east
					testBlock = Workspace.getBlock(x + 1, y, z + 1);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 0 || testBlock.place == 4)) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 2 && place == 3 && y < Workspace.gety() - 1) { // south
					testBlock = Workspace.getBlock(x, y + 1, z + 1);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 1 || testBlock.place == 3)) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 3 && place == 5 && x > 0) { // west
					testBlock = Workspace.getBlock(x - 1, y, z + 1);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 0 || testBlock.place == 5)) {
						targetCharge = testBlock.charge;
					}
				}
				if (Workspace.getBlock(x, y, z).charge - 1 > targetCharge) {
					power(x, y, z + 1, i + 1, BlockType.PowerRail, Workspace
							.getBlock(x, y, z).charge - 1);
				}
			}
		}

		// down
		if (z > 0) {
			for (int i = 0; i < 4; i++) {
				int targetCharge = 16;
				Block testBlock;
				if (place == 0) {
					if (i == 1 && x < Workspace.getx() - 1) { // east
						testBlock = Workspace.getBlock(x + 1, y, z - 1);
						if (testBlock.id == BlockType.PowerRail
								&& testBlock.place == 5) {
							targetCharge = testBlock.charge;
						}
					} else if (i == 3 && x > 0) { // west
						testBlock = Workspace.getBlock(x - 1, y, z - 1);
						if (testBlock.id == BlockType.PowerRail
								&& testBlock.place == 4) {
							targetCharge = testBlock.charge;
						}
					}
				} else if (place == 1) {
					if (i == 0 && y > 0) { // north
						testBlock = Workspace.getBlock(x, y - 1, z - 1);
						if (testBlock.id == BlockType.PowerRail
								&& testBlock.place == 3) {
							targetCharge = testBlock.charge;
						}
					} else if (i == 2 && y < Workspace.gety() - 1) { // south
						testBlock = Workspace.getBlock(x, y + 1, z - 1);
						if (testBlock.id == BlockType.PowerRail
								&& testBlock.place == 2) {
							targetCharge = testBlock.charge;
						}
					}
				} else if (i == 0 && place == 3 && y > 0) { // north
					testBlock = Workspace.getBlock(x, y - 1, z - 1);
					if (testBlock.id == BlockType.PowerRail
							&& testBlock.place == 3) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 1 && place == 5 && x < Workspace.getx() - 1) { // east
					testBlock = Workspace.getBlock(x + 1, y, z - 1);
					if (testBlock.id == BlockType.PowerRail
							&& testBlock.place == 5) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 2 && place == 2 && y < Workspace.gety() - 1) { // south
					testBlock = Workspace.getBlock(x, y + 1, z - 1);
					if (testBlock.id == BlockType.PowerRail
							&& testBlock.place == 2) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 3 && place == 4 && x > 0) { // west
					testBlock = Workspace.getBlock(x - 1, y, z - 1);
					if (testBlock.id == BlockType.PowerRail
							&& testBlock.place == 4) {
						targetCharge = testBlock.charge;
					}
				}
				if (Workspace.getBlock(x, y, z).charge - 1 > targetCharge) {
					power(x, y, z - 1, i + 1, BlockType.PowerRail, Workspace
							.getBlock(x, y, z).charge - 1);
				}
			}
		}

		// same level
		for (int i = 0; i < 4; i++) {
			int targetCharge = 16;
			Block testBlock;
			if (place == 0) {
				if (i == 1 && x < Workspace.getx() - 1) { // east
					testBlock = Workspace.getBlock(x + 1, y, z);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 0 || testBlock.place == 4)) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 3 && x > 0) { // west
					testBlock = Workspace.getBlock(x - 1, y, z);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 0 || testBlock.place == 5)) {
						targetCharge = testBlock.charge;
					}
				}
			} else if (place == 1) {
				if (i == 0 && y > 0) { // north
					testBlock = Workspace.getBlock(x, y - 1, z);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 1 || testBlock.place == 2)) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 2 && y < Workspace.gety() - 1) { // south
					testBlock = Workspace.getBlock(x, y + 1, z);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 1 || testBlock.place == 3)) {
						targetCharge = testBlock.charge;
					}
				}
			} else if (place == 2) {
				if (i == 0 && y > 0) { // north
					testBlock = Workspace.getBlock(x, y - 1, z);
					if (testBlock.id == BlockType.PowerRail
							&& testBlock.place == 3) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 2 && y < Workspace.gety() - 1) { // south
					testBlock = Workspace.getBlock(x, y + 1, z);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 1 || testBlock.place == 3)) {
						targetCharge = testBlock.charge;
					}
				}
			} else if (place == 3) {
				if (i == 0 && y > 0) { // north
					testBlock = Workspace.getBlock(x, y - 1, z);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 1 || testBlock.place == 2)) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 2 && y < Workspace.gety() - 1) { // south
					testBlock = Workspace.getBlock(x, y + 1, z);
					if (testBlock.id == BlockType.PowerRail
							&& testBlock.place == 2) {
						targetCharge = testBlock.charge;
					}
				}
			} else if (place == 4) {
				if (i == 1 && x < Workspace.getx() - 1) { // east
					testBlock = Workspace.getBlock(x + 1, y, z);
					if (testBlock.id == BlockType.PowerRail
							&& testBlock.place == 5) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 3 && x > 0) { // west
					testBlock = Workspace.getBlock(x - 1, y, z);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 0 || testBlock.place == 5)) {
						targetCharge = testBlock.charge;
					}
				}
			}
			if (place == 5) {
				if (i == 1 && x < Workspace.getx() - 1) { // east
					testBlock = Workspace.getBlock(x + 1, y, z);
					if (testBlock.id == BlockType.PowerRail
							&& (testBlock.place == 0 || testBlock.place == 4)) {
						targetCharge = testBlock.charge;
					}
				} else if (i == 3 && x > 0) { // west
					testBlock = Workspace.getBlock(x - 1, y, z);
					if (testBlock.id == BlockType.PowerRail
							&& testBlock.place == 4) {
						targetCharge = testBlock.charge;
					}
				}
			}
			if (Workspace.getBlock(x, y, z).charge - 1 > targetCharge) {
				power(x, y, z, i + 1, BlockType.PowerRail, Workspace.getBlock(
						x, y, z).charge - 1);
			}
		}
	}

	private static void inputPower(int x, int y, int z) {
		power(x, y, z, Workspace.getBlock(x, y, z).place, BlockType.Block, 16);
		for (int i = 0; i < 5; i++) {
			power(x, y, z, i, BlockType.Wire, 15);
			power(x, y, z, i, BlockType.PowerRail, 9);
		}
	}

	private static void inputDoor(int x, int y, int z) {
		if (Workspace.getBlock(x, y, z).charge > 0) {
			if (Workspace.getBlock(x, y, z).subType.isLowerType())
				power(x, y, z, 5, BlockType.Door, 1);
			else if (Workspace.getBlock(x, y, z).subType.isUpperType())
				power(x, y, z, 0, BlockType.Door, 1);
		}
	}

	private static void inputRail(int x, int y, int z) {
		power(x, y, z, 0, BlockType.Block, 16);
		for (int i = 1; i < 5; i++) {
			power(x, y, z, i, BlockType.Wire, 15);
			power(x, y, z, i, BlockType.Door, 1);
			power(x, y, z, i, BlockType.Trapdoor, 1);
			power(x, y, z, i, BlockType.PowerRail, 9);
		}
		switch (getBlock(x, y, z).place) {
		case 2:
			power(x, y, z + 1, 1, BlockType.PowerRail, 9);
			power(x, y, z - 1, 3, BlockType.PowerRail, 9);
			break;
		case 3:
			power(x, y, z + 1, 3, BlockType.PowerRail, 9);
			power(x, y, z - 1, 1, BlockType.PowerRail, 9);
			break;
		case 4:
			power(x, y, z + 1, 2, BlockType.PowerRail, 9);
			power(x, y, z - 1, 4, BlockType.PowerRail, 9);
			break;
		case 5:
			power(x, y, z + 1, 4, BlockType.PowerRail, 9);
			power(x, y, z - 1, 2, BlockType.PowerRail, 9);
			break;
		}
	}

	private static void wirePower(int x, int y, int z) {
		boolean[] connections = Workspace.getConnections(x, y, z, false);

		// up
		if (z < Workspace.getz()) {
			if (Workspace.getBlock(x, y, z + 1).id != BlockType.Block) { // can
																			// be
																			// lead
																			// up
				for (int i = 0; i < 4; i++) {
					if (connections[i]) {
						int targetCharge = 0;
						switch (i) {
						case 0: { // north
							if (y > 0) {
								targetCharge = Workspace.getBlock(x, y - 1,
										z + 1).charge;
							}
						}
							break;
						case 1: { // east
							if (x < Workspace.getx() - 1) {
								targetCharge = Workspace.getBlock(x + 1, y,
										z + 1).charge;
							}
						}
							break;
						case 2: { // south
							if (y < Workspace.gety() - 1) {
								targetCharge = Workspace.getBlock(x, y + 1,
										z + 1).charge;
							}
						}
							break;
						case 3: { // west
							if (x > 0) {
								targetCharge = Workspace.getBlock(x - 1, y,
										z + 1).charge;
							}
						}
							break;
						}
						if (Workspace.getBlock(x, y, z).charge - 1 > targetCharge) {
							power(x, y, z + 1, i + 1, BlockType.Wire, Workspace
									.getBlock(x, y, z).charge - 1);
						}
					}
				}
			}
		}

		// down
		if (z > 0) {
			if (Workspace.getBlock(x, y, z).charge > Workspace.getBlock(x, y,
					z - 1).charge) {
				// block directly under
				power(x, y, z, 0, BlockType.Block,
						Workspace.getBlock(x, y, z).charge);
			}
			for (int i = 0; i < 4; i++) {
				boolean down = false;
				switch (i) {
				case 0: { // north
					if (y > 0) {
						if (Workspace.getBlock(x, y - 1, z).id != BlockType.Block
								&& Workspace.getBlock(x, y, z).charge - 1 > Workspace
										.getBlock(x, y - 1, z - 1).charge) {
							down = true;
						}
					}
				}
					break;
				case 1: { // east
					if (x < Workspace.getx() - 1) {
						if (Workspace.getBlock(x + 1, y, z).id != BlockType.Block
								&& Workspace.getBlock(x, y, z).charge - 1 > Workspace
										.getBlock(x + 1, y, z - 1).charge) {
							down = true;
						}
					}
				}
					break;
				case 2: { // south
					if (y < Workspace.gety() - 1) {
						if (Workspace.getBlock(x, y + 1, z).id != BlockType.Block
								&& Workspace.getBlock(x, y, z).charge - 1 > Workspace
										.getBlock(x, y + 1, z - 1).charge) {
							down = true;
						}
					}
				}
					break;
				case 3: { // west
					if (x > 0) {
						if (Workspace.getBlock(x - 1, y, z).id != BlockType.Block
								&& Workspace.getBlock(x, y, z).charge - 1 > Workspace
										.getBlock(x - 1, y, z - 1).charge) {
							down = true;
						}
					}
				}
					break;
				}
				if (connections[i] && down) { // can be lead down
					power(x, y, z - 1, i + 1, BlockType.Wire, Workspace
							.getBlock(x, y, z).charge - 1);
				}
			}
		}

		// same level
		for (int i = 0; i < 4; i++) {
			if (connections[i]) {
				int targetCharge = 0;
				switch (i) {
				case 0: { // north
					if (y > 0) {
						targetCharge = Workspace.getBlock(x, y - 1, z).charge;
					}
				}
					break;
				case 1: { // east
					if (x < Workspace.getx() - 1) {
						targetCharge = Workspace.getBlock(x + 1, y, z).charge;
					}
				}
					break;
				case 2: { // south
					if (y < Workspace.gety() - 1) {
						targetCharge = Workspace.getBlock(x, y + 1, z).charge;
					}
				}
					break;
				case 3: { // west
					if (x > 0) {
						targetCharge = Workspace.getBlock(x - 1, y, z).charge;
					}
				}
					break;
				}
				if (Workspace.getBlock(x, y, z).charge - 1 > targetCharge) {
					power(x, y, z, i + 1, BlockType.Wire, Workspace.getBlock(x,
							y, z).charge - 1);
				}
				// powering blocks
				if (Workspace.getBlock(x, y, z).charge > targetCharge) {
					if (Workspace.getConnections(x, y, z, true)[i]) {
						power(x, y, z, i + 1, BlockType.Block, Workspace
								.getBlock(x, y, z).charge);
						power(x, y, z, i + 1, BlockType.Door, 1);
						power(x, y, z, i + 1, BlockType.Trapdoor, 1);
						power(x, y, z, i + 1, BlockType.PowerRail, 9);
					}
				}
			}
		}
	}

	private static void power(int x, int y, int z, int dir, BlockType target,
			int charge) {
		boolean[] end = new boolean[6]; // exceptions preventer
		if (z == 0) {
			end[0] = true;
		}
		if (y == 0) {
			end[1] = true;
		}
		if (x == Workspace.getx() - 1) {
			end[2] = true;
		}
		if (y == Workspace.gety() - 1) {
			end[3] = true;
		}
		if (x == 0) {
			end[4] = true;
		}
		if (z == Workspace.getz() - 1) {
			end[5] = true;
		}

		if (!end[dir]) {
			switch (dir) {
			case 0: {
				if (Workspace.getBlock(x, y, z - 1).id == target
						&& Workspace.getBlock(x, y, z - 1).subType != BlockType.Glass) {
					Workspace.getBlock(x, y, z - 1).charge = charge;
					addBlock(x, y, z - 1);
				}
			}
				break;
			case 1: {
				if (Workspace.getBlock(x, y - 1, z).id == target
						&& Workspace.getBlock(x, y - 1, z).subType != BlockType.Glass) {
					Workspace.getBlock(x, y - 1, z).charge = charge;
					addBlock(x, y - 1, z);
				}
			}
				break;
			case 2: {
				if (Workspace.getBlock(x + 1, y, z).id == target
						&& Workspace.getBlock(x + 1, y, z).subType != BlockType.Glass) {
					Workspace.getBlock(x + 1, y, z).charge = charge;
					addBlock(x + 1, y, z);
				}
			}
				break;
			case 3: {
				if (Workspace.getBlock(x, y + 1, z).id == target
						&& Workspace.getBlock(x, y + 1, z).subType != BlockType.Glass) {
					Workspace.getBlock(x, y + 1, z).charge = charge;
					addBlock(x, y + 1, z);
				}
			}
				break;
			case 4: {
				if (Workspace.getBlock(x - 1, y, z).id == target
						&& Workspace.getBlock(x - 1, y, z).subType != BlockType.Glass) {
					Workspace.getBlock(x - 1, y, z).charge = charge;
					addBlock(x - 1, y, z);
				}
			}
				break;
			case 5: {
				if (Workspace.getBlock(x, y, z + 1).id == target
						&& Workspace.getBlock(x, y, z + 1).subType != BlockType.Glass) {
					Workspace.getBlock(x, y, z + 1).charge = charge;
					addBlock(x, y, z + 1);
				}
			}
				break;
			}
		}
	}

	public static Block getBlock(int x, int y, int z) {
		if (x >= lastTick.size() || y >= lastTick.get(0).size()
				|| z >= lastTick.get(0).get(0).size() || x < 0 || y < 0
				|| z < 0) {
			return new Block();
		} else {
			return lastTick.get(x).get(y).get(z);
		}
	}

	private static void addBlock(int x, int y, int z) {
		xSource.add(new Integer(x));
		ySource.add(new Integer(y));
		zSource.add(new Integer(z));
		xUpdate.add(new Integer(x));
		yUpdate.add(new Integer(y));
		zUpdate.add(new Integer(z));
	}

}