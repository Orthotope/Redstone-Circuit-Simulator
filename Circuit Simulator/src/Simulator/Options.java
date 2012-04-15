package Simulator;

/*
 This is where all general variables are stored.
 Both those editable by the clint, and some who want be changed.
 You can change things as the startup size and speed of the ticks here
 */

public class Options {
	public static int startx = 30;
	public static int starty = 20;
	public static int startz = 7; // initialization values
	public static int level = 0;
	public static int size = 24;
	public static boolean logicTicks = false;
	public static boolean play = false;
	public static int ticksPerSecond = 20;
	public static BlockType[][] palette = { 
			{ BlockType.Block, BlockType.Air },
			{ BlockType.Torch, BlockType.Air },
			{ BlockType.Lever, BlockType.Air },
			{ BlockType.PressurePad, BlockType.Air },
			{ BlockType.Piston, BlockType.Air },
			{ BlockType.StickyPiston, BlockType.Air },
			{ BlockType.Door, BlockType.Air },
			{ BlockType.Wire, BlockType.Air },
			{ BlockType.Repeater, BlockType.Air },
			{ BlockType.Button, BlockType.Air },
			{ BlockType.Rail, BlockType.Air },
			{ BlockType.PowerRail, BlockType.Air },
			{ BlockType.DetectorRail, BlockType.Air },
			{ BlockType.Trapdoor, BlockType.Air }, };
	public static BlockType[][] palette2 = { // palette for two layered view
			{ BlockType.Block, BlockType.Air }, 
			{ BlockType.Block, BlockType.Block },
			{ BlockType.Air, BlockType.Block },
			{ BlockType.Wire, BlockType.Air },
			{ BlockType.Torch, BlockType.Air },
			{ BlockType.Repeater, BlockType.Air },
			{ BlockType.Piston, BlockType.Air },
			{ BlockType.StickyPiston, BlockType.Air },
			{ BlockType.Rail, BlockType.Air },
			{ BlockType.PowerRail, BlockType.Air },
			{ BlockType.DetectorRail, BlockType.Air },
			{ BlockType.Door, BlockType.Air },
			{ BlockType.Button, BlockType.Air },
			{ BlockType.Lever, BlockType.Air },
			{ BlockType.PressurePad, BlockType.Air },
			{ BlockType.Block, BlockType.Wire },
			{ BlockType.Block, BlockType.Torch },
			{ BlockType.Block, BlockType.Repeater },
			{ BlockType.Block, BlockType.Piston },
			{ BlockType.Block, BlockType.StickyPiston },
			{ BlockType.Block, BlockType.Rail },
			{ BlockType.Block, BlockType.PowerRail },
			{ BlockType.Block, BlockType.DetectorRail },
			{ BlockType.Trapdoor, BlockType.Air }, };
	public static boolean viewTwoLayer = true;
	public static byte nativeBlockId = 1;
	public static byte[] blockIDs = { 1, 2, 3, 4, 5, 7, 12, 13, 14, 15, 16, 17,
			19, 21, 22, 23, 24, 25, 35, 41, 42, 43, 45, 46, 47, 48, 49, 56, 57,
			61, 62, 80, 84, 86, 87, 88, 89, 91 }; 
	// those data values that should be changed to blocks
	public static String filePath = System.getProperty("user.dir");
}