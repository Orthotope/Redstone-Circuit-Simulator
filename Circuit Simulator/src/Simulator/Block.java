package Simulator;

/*
 This is the information every block is composed of.
 */

public class Block {

	public BlockType id; // 0:air; 1:block; 2:wire; 3:torch; 4:repeater;
							// 5:buttons;
	// 6:lever; 7:pressure pad; 8:piston; 9:sticky piston; 10:rail; 11:power
	// rail; 12:detector rail
	// 13:door; 14:trapdoor; 15:rail; 16:power rail; 17:detector rail
	public int charge; // 0:off; 16:source; and then down to 0
	public int place; // 0:ground(default); 1:N; 2:E; 3:S; 4:W; NOTE:repeater is
						// the direction pointing
	// When with straight rails: 0:E-W; 1:N-S; 2:/N; 3:/S; 4:/E; 5:/W;
	// When with curved rails (corner points...): 0:NE; 1:SE; 2:SW; 3:NW;
	public int delay; // in logic ticks
	public int ticksPassed; // in logic ticks
	public int x, y, z;

	public boolean pistonExtended;
	public BlockType subType;

	public static final int[][] dirModifier = { { 0, 0, -1 }, // Down
			{ 0, -1, 0 }, // N
			{ 1, 0, 0 }, // E
			{ 0, 1, 0 }, // S
			{ -1, 0, 0 }, // W
			{ 0, 0, 1 }, // Up
	};

	public Block() {
		this(-1, -1, -1);
	}

	public Block(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		id = BlockType.Air;
		charge = 0;
		place = 0;
		delay = 20;
		ticksPassed = 0;
		pistonExtended = false;
		if (id == BlockType.Rail) {
			subType = BlockType.Rail;
		} else {
			subType = BlockType.Block;
		}
	}

	public Block(int x, int y, int z, BlockType id, int charge, int place) {
		this(x, y, z);
		this.id = id;
		this.charge = charge;
		this.place = place;
		this.ticksPassed = 0;
		if (id == BlockType.Repeater || id == BlockType.Torch) {
			this.delay = 2;
		} else {
			this.delay = 20;
		}
	}

	public Block(Block b) {
		this.x = b.x;
		this.id = b.id;
		this.charge = b.charge;
		this.place = b.place;
		this.delay = b.delay;
		this.ticksPassed = b.ticksPassed;
		this.subType = b.subType;
		pistonExtended = false;

	}

	public Block(BlockType id, int charge, int place, int delay, int passed) {
		this();
		this.id = id;
		this.charge = charge;
		this.place = place;
		this.delay = delay;
		this.ticksPassed = passed;
	}

	public void increaseTick() { // for repeater only
		if (id == BlockType.Repeater)
			delay = ((delay % 8) + 2); // 2,4,6,8
	}

	public void nextBlockSubtype() {
		assert id == BlockType.Block;
		switch (subType) {
		case Block:
			subType = BlockType.Glass;
			break;
		case Glass:
			subType = BlockType.Obsidian;
			break;
		case Obsidian:
			subType = BlockType.PinkWool;
			break;
		case PinkWool:
			subType = BlockType.BlueWool;
			break;
		case BlueWool:
			subType = BlockType.Block;
			break;
		}
	}

	public void nextDoorSubtype(int x, int y, int z) {
		assert (id == BlockType.Door);
		switch (subType) {
		case LowerWoodDoor: {
			subType = BlockType.LowerIronDoor;
			Workspace.getBlock(x, y, z + 1).subType = BlockType.UpperIronDoor;
			break;
		}
		case UpperWoodDoor: {
			subType = BlockType.UpperIronDoor;
			Workspace.getBlock(x, y, z - 1).subType = BlockType.LowerIronDoor;
			break;
		}
		case LowerIronDoor: {
			subType = BlockType.LowerWoodDoor;
			Workspace.getBlock(x, y, z + 1).subType = BlockType.UpperWoodDoor;
			break;
		}
		case UpperIronDoor: {
			subType = BlockType.UpperWoodDoor;
			Workspace.getBlock(x, y, z - 1).subType = BlockType.LowerWoodDoor;
			break;
		}
		}
	}

	public void nextRailSubtype() {
		assert id == BlockType.Rail;
		place = 0;
		switch (subType) {
		case Rail:
			subType = BlockType.CornerRail;
			break;
		case CornerRail:
			subType = BlockType.Rail;
			break;
		}
	}

	public boolean waitTick(boolean input) { // returns true if state changes
		ticksPassed++;
		if (ticksPassed == 1) { // first time searching
			if ((id == BlockType.Torch && ((input && charge == 0) || (!input && charge == 16)))
					|| (id == BlockType.Repeater && ((!input && charge == 0) || (input && charge == 16)))) {
				ticksPassed = 0; // no input change, compared to current state
				// and type
			}
		}

		switch (id) {
		case StickyPiston:
		case Piston:
			if (ticksPassed >= delay) {
				ticksPassed = 0;
				if (input) {
					if (charge != 16)
						extendPiston();
					charge = 16;
				} else {
					if (charge != 0)
						retractPiston();
					charge = 0;
				}
				return true;
			}
			return false;

		case Torch:
			if (ticksPassed >= delay) {
				ticksPassed = 0;
				if (input) {
					charge = 0;
				} else {
					charge = 16;
				}
				return true;
			}
			return false;

		case Button:
		case PressurePad:
		case DetectorRail:
			if (charge == 0) { // reset if manually unpowered
				ticksPassed = 0;
			}
			if (ticksPassed >= delay) {
				ticksPassed = 0;
				charge = 0; // can only turn off by time
				return true;
			}
			return false;

		case Repeater:
			if (ticksPassed >= delay) {
				ticksPassed = 0;
				if (charge == 0) { // special repeater behaviour
					charge = 16;
				} else if (!input && charge == 16) {
					charge = 0;
				}
				return true;
			}
			return false;

		}
		return false;
	}

	protected int push(int place, int count) {
		int ret = 0;
		if (!this.id.isMovableType())
			return ret;

		int[] dir = dirModifier[place];

		int nx = x + dir[0];
		int ny = y + dir[1];
		int nz = z + dir[2];

		Block nextBlock = Workspace.getBlock(nx, ny, nz);

		assert (nextBlock.canBeMoved());
		if (count < BlockType.MaxPistonPush)
			ret = nextBlock.push(place, count + 1);

		Workspace.setBlock(nx, ny, nz, new Block(this));
		Workspace.setBlock(x, y, z, BlockType.Air);

		return ret + 1;
	}

	public boolean canBeMoved() {
		if (x == -1 || y == -1 || z == -1)
			return false; // Can't push past extents of map

		if (id == BlockType.PistonPaddle)
			return false;

		if (id == BlockType.Block && subType == BlockType.Obsidian)
			return false;

		if (id.isPistonType())
			return !pistonExtended; // Pistons can only be pushed when not
									// extended

		return true; // everything else can be pushed.
	}

	protected void pull(int place, int count) {
		if (count <= 0)
			return;

		final int[] dir = dirModifier[place];
		final int nx = this.x + dir[0], ny = this.y + dir[1], nz = this.z
				+ dir[2];
		Block pulled = Workspace.getBlock(nx, ny, nz);

		Workspace.setBlock(x, y, z, BlockType.Air);
		if (pulled.canBeMoved()) {
			final boolean shouldPullMore = (pulled.id == BlockType.StickyPiston);

			Workspace.setBlock(x, y, z, new Block(pulled));
			Workspace.setBlock(nx, ny, nz, BlockType.Air);

			if (shouldPullMore)
				Workspace.getBlock(nx, ny, nz).pull(place, count - 1);

		}
	}

	public void retractPiston() {
		assert x != -1 && y != -1 && z != -1;
		assert id.isPistonType();

		if (!pistonExtended)
			return;

		final int[] dir = dirModifier[this.place];
		final int nx = this.x + dir[0], ny = this.y + dir[1], nz = this.z
				+ dir[2];
		final int nx2 = nx + dir[0], ny2 = ny + dir[1], nz2 = nz + dir[2];

		assert Workspace.getBlock(nx, ny, nz).id == BlockType.PistonPaddle;

		Workspace.setBlock(nx, ny, nz, BlockType.Air);
		if (id == BlockType.StickyPiston) {
			Block pulled = Workspace.getBlock(nx2, ny2, nz2);
			if (pulled.canBeMoved()) {
				Workspace.setBlock(nx, ny, nz, new Block(pulled));
				Workspace.setBlock(nx2, ny2, nz2, BlockType.Air);
			}
		}
		pistonExtended = false;

		// Would be better if this was being more intelligent about what to
		// update.
		GUI.thisGUI.updateAllAndRepaint();
	}

	public void extendPiston() {
		assert x != -1 && y != -1 && z != -1;
		assert id.isPistonType();

		final int[] dir = dirModifier[this.place];

		int c;
		for (c = 1; c <= BlockType.MaxPistonPush; c++) {
			final int[] pos = { this.x + dir[0] * c, this.y + dir[1] * c,
					this.z + dir[2] * c };
			Block block = Workspace.getBlock(pos[0], pos[1], pos[2]);
			if (!block.canBeMoved())
				return; // A non-movable block was encountered
			if (!block.id.isMovableType())
				break; // We found the end of our push.
		}
		// If we didn't break out early, we have hit the max number of blocks
		// that can be pushed by one piston and so this piston can't extend.
		if (c > BlockType.MaxPistonPush)
			return;

		int nx = this.x + dir[0], ny = this.y + dir[1], nz = this.z + dir[2];

		Block firstPushed = Workspace.getBlock(nx, ny, nz);
		int length = firstPushed.push(this.place, 0);

		Block pistonExtension = new Block(BlockType.PistonPaddle, 0,
				this.place, 0, 0);
		Workspace.setBlock(nx, ny, nz, pistonExtension);
		pistonExtended = true;

		// Would be better if this was being more intelligent about what to
		// update.
		GUI.thisGUI.updateAllAndRepaint();
	}

	public boolean isPartOfActivePiston() {
		switch (id) {
		case StickyPiston:
		case Piston:
			return pistonExtended;
		case PistonPaddle:
			return true;
		}
		return false;
	}

	public String toString() {
		return super.toString() + "(block) [id:" + this.id + ", charge:"
				+ this.charge + ", place:" + this.place + ", delay:"
				+ this.delay + ", ticksPassed:" + this.ticksPassed + "]";
	}

	public static boolean providesPower(int x, int y, int z, int place) {
		// It would be better if the block knew its own coordinates.
		Block block = Simulator.Logic.getBlock(x, y, z);
		if (block.x == -1 || block.y == -1 || block.z == -1)
			return false;
		if (block.charge == 0)
			return false;

		switch (block.id) {
		case Wire:
		case PowerRail:
			return Workspace.getConnections(x, y, z, false)[place - 1] == true;
		case Block:
		case Torch:
		case Button:
		case Lever:
		case PressurePad:
		case DetectorRail:
			return true;
		case Repeater:
			return block.place == place;
		}

		return false;
	}

	public static boolean connectsHorizontal(int x, int y, int z, int place) {
		// It would be better if the block knew its own coordinates.
		Block block = Workspace.getBlock(x, y, z);

		switch (block.id) {
		case Wire:
		case Torch:
		case Button:
		case Lever:
		case PressurePad:
			return true;
		case Repeater:
			return block.place == place;
		}
		return false;
	}

	public static boolean connectsDiagonal(int x, int y, int z) {
		Block block = Workspace.getBlock(x, y, z);

		switch (block.id) {
		case Wire:
		case Torch:
		case Button:
		case Lever:
		case PressurePad:
			return true;
		}
		return false;
	}

	public static boolean canBeFloating(int x, int y, int z) {
		Block block = Workspace.getBlock(x, y, z);

		switch (block.id) {
		case StickyPiston:
		case Piston:
		case Block:
			return true;
		case Torch:
		case Button:
		case Lever:
			switch (block.place) {
			case 1: // North
				return (Workspace.getBlock(x, y - 1, z).id == BlockType.Block);
			case 2: // East
				return (Workspace.getBlock(x + 1, y, z).id == BlockType.Block);
			case 3: // South
				return (Workspace.getBlock(x, y + 1, z).id == BlockType.Block);
			case 4: // West
				return (Workspace.getBlock(x - 1, y, z).id == BlockType.Block);
			}
		}
		return false;
	}
}