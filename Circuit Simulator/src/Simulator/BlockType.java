package Simulator;

public enum BlockType {
	TopLayer, Air, Block, Wire, Torch, Repeater, Button, Lever, PressurePad, 
	Piston, StickyPiston, Door, Trapdoor, Rail, PowerRail, DetectorRail,

	// All Blocktypes below should not ever be added to the palette
	StonePad, PistonPaddle, LowerWoodDoor, UpperWoodDoor, LowerIronDoor, 
	UpperIronDoor, CornerRail, Glass, Obsidian, PinkWool, BlueWool;

	static final int MaxPistonPush = 13;

	public boolean isPistonType() {
		switch (this) {
		case Piston:
		case StickyPiston:
			return true;
		}
		return false;
	}

	public boolean isPlayerActivated() {
		switch (this) {
		case Button:
		case Lever:
		case PressurePad:
		case Trapdoor:
		case DetectorRail:
			return true;
		}
		return false;
	}

	public boolean isButtonType() {
		switch (this) {
		case Button:
		case PressurePad:
		case DetectorRail:
			return true;
		}
		return false;
	}

	public boolean isMovableType() {
		switch (this) {
		case StickyPiston:
		case Piston:
		case Block:
		case Rail:
		case PowerRail:
		case DetectorRail:
			return true;
		}
		return false;
	}

	public boolean isTallBlock() {
		switch (this) {
		case Door:
			return true;
		}
		return false;
	}

	public boolean isWoodDoor() {
		switch (this) {
		case LowerWoodDoor:
		case UpperWoodDoor:
			return true;
		}
		return false;
	}

	public boolean isIronDoor() {
		switch (this) {
		case LowerIronDoor:
		case UpperIronDoor:
			return true;
		}
		return false;
	}

	public boolean isLowerType() {
		switch (this) {
		case LowerWoodDoor:
		case LowerIronDoor:
			return true;
		}
		return false;
	}

	public boolean isUpperType() {
		switch (this) {
		case UpperWoodDoor:
		case UpperIronDoor:
			return true;
		}
		return false;
	}

	public boolean isRightClickReplacableType() {
		switch (this) {
		case Torch:
		case Repeater:
		case Button:
		case Lever:
		case PressurePad:
		case Piston:
		case StickyPiston:
		case Door:
		case Trapdoor:
		case Rail:
		case PowerRail:
		case DetectorRail:
			return true;
		}
		return false;
	}

	public boolean isMiddleClickManipulatable() {
		switch (this) {
		case Repeater:
		case Button:
		case Lever:
		case PressurePad:
		case Door:
		case Trapdoor:
		case DetectorRail:
			return true;
		}
		return false;
	}
}
