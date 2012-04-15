package Simulator;
import java.io.File;

import javax.swing.JFileChooser;

/*
This class is the one that writes and read the NBT structure into the actual program
it uses the data values stated here:
http://www.minecraftwiki.net/wiki/Data_values#Redstone_Wire
NOTE: Buttons differs for some reason from these data
*/

public class SaveLoad {

  public static void save() {
    boolean running = false;
	if (Options.play) {running = true;}
    GUI.run(false);
  
    //set up file GUI
    JFileChooser chooser = new JFileChooser(Options.filePath);
	chooser.setFileFilter(new FileNameExtensionFilter("Schematic files", "schematic"));
	if (!(chooser.showSaveDialog(GUI.thisGUI) == JFileChooser.APPROVE_OPTION)) {
	  GUI.run(running);
      return;
	}
	Options.filePath = chooser.getCurrentDirectory().getAbsolutePath();
	
	//creates the file
    Tag.tags.clear();
	Tag.tags.add(new Tag(10, "Schematic", true));
	Tag.tags.add(new Tag(2, "Width", Workspace.gety(), true));
	Tag.tags.add(new Tag(2, "Length", Workspace.getx(), true));
	Tag.tags.add(new Tag(2, "Height", Workspace.getz(), true));
	Tag.tags.add(new Tag(8, "Materials", "Alpha", true));
	Tag.tags.add(new Tag(7, "Blocks", getData(true), true));
	Tag.tags.add(new Tag(7, "Data", getData(false), true));
	byte[] emptyCompoundList = {10,0,0,0,0};
	Tag.tags.add(new Tag(9, "Entities", emptyCompoundList, true));
	Tag.tags.add(new Tag(9, "TileEntities", emptyCompoundList, true));
	Tag.tags.add(new Tag (true));
	
	//restarts thread if necessary
	GUI.run(running);
	
	//saves the file with the right extension
	int i = chooser.getSelectedFile().getName().lastIndexOf(".");
	if (i != -1) { //not found
	  if ((chooser.getSelectedFile().getName().substring(i)).equals(".schematic")) { //ensures the correct filename
	    Tag.saveFile(chooser.getSelectedFile());
		return;
	  } else if (chooser.getSelectedFile().getName().substring(i).equals(".")) {
	    Tag.saveFile(new File(chooser.getSelectedFile().getAbsolutePath()+"schematic"));
		return;
	  }
	}
	Tag.saveFile(new File(chooser.getSelectedFile().getAbsolutePath()+".schematic"));
  }

  private static byte[] getData(boolean data) {
    byte[] out = new byte[Workspace.getx()*Workspace.gety()*Workspace.getz()];
	for (int z = 0;z < Workspace.getz();z++) {
	  for (int x = 0;x < Workspace.getx();x++) {
	    for (int y = 0;y < Workspace.gety();y++) {
	      out[z*Workspace.getx()*Workspace.gety()+(x+1)*Workspace.gety()-y-1] = getBlockData(x,y,z, data);
	    }
	  }
	}
	return out;
  }
  
  private static byte getBlockData(int x, int y, int z, boolean data) {
    if (data) { //block id
	  switch (Workspace.getBlock(x,y,z).id) {
	    case Air: return 0; //air
		case Block: return Options.nativeBlockId; //native block
		case Wire: return 55; //wire
		case Torch: {
		  if (Workspace.getBlock(x,y,z).charge == 0) {return 75; //torch off
		  } else {return 76;} //torch on
		}
		case Repeater: {
		  if (Workspace.getBlock(x,y,z).charge == 0) {return 93; //repeater off
		  } else {return 94;} //repeater off
		}
		case Piston: return 33;
		case StickyPiston: return 29;
		
		case Button: return 77; //button
		case Lever: return 69; //lever
		case PressurePad: {
			if (Workspace.getBlock(x, y, z).subType == BlockType.StonePad) return 70; //stone pressure plate
			else return 72; //wood pressure plate
		}
		
		case Door: {
		  if (Workspace.getBlock(x, y, z).subType.isWoodDoor()) return 64;
		  else return 71;
		}
		case Trapdoor: return 96; //trapdoor
		
		case Rail: return 66; //rail
		case PowerRail: return 27; //power rail (both states)
		case DetectorRail: return 28; //detector rail
	  }
	} else { //block metadata
	  switch (Workspace.getBlock(x,y,z).id) {
	    case Air: return 0; //air
		case Block: return 0; //native block
		case Wire: return (byte)Workspace.getBlock(x,y,z).charge; //wire
		case Torch: { //torch
		  switch (Workspace.getBlock(x,y,z).place) {
			case 1: return 0x2; //north
			case 2: return 0x4; //east
			case 3: return 0x1; //south
			case 4: return 0x3; //west
		  }
		  return 0x5;//ground
		}
		case Repeater: { //repeaters
		  byte low = 0;
		  switch (Workspace.getBlock(x,y,z).place) {
			case 1: low = 0x1; break; //north
			case 2: low = 0x2; break; //east
			case 3: low = 0x3; break; //south
			case 4: low = 0x0; break; //west
		  }
		  byte high = 0;
		  switch (Workspace.getBlock(x,y,z).delay) {
			case 2: high = 0x0; break; //1
			case 4: high = 0x1; break; //2
			case 6: high = 0x2; break; //3
			case 8: high = 0x3; break; //4
		  }
		  return (byte)((high << 2) | low);
		}
		case Piston: 
		case StickyPiston: 
		{
			// lower 8 is dir
			// higher 1 is piston extended flag (always off for now)
			switch (Workspace.getBlock(x,y,z).place)
			{
				case 0: return 0x01;			
				case 1: return 0x04;
				case 2: return 0x02;
				case 3: return 0x05;
				case 4: return 0x03;
				case 5: return 0x00;
			}
			break;
		}
		case Button: {//button
		  byte low = 0;
		  switch (Workspace.getBlock(x,y,z).place) {
			case 1: low = 0x2; break; //north
			case 2: low = 0x4; break; //east
			case 3: low = 0x1; break; //south
			case 4: low = 0x3; break; //west
		  }
		  if (Workspace.getBlock(x,y,z).charge == 0) { return low;
		  } else {return (byte)(0x8 | low);}
		}
		case Lever: { //lever
		byte low = 0;
		  switch (Workspace.getBlock(x,y,z).place) {
			case 1: low = 0x2; break; //north
			case 2: low = 0x4; break; //east
			case 3: low = 0x1; break; //south
			case 4: low = 0x3; break; //west
			case 0: low = 0x5; break; //on ground E/W(to power block underneath)
		  }
		  if (Workspace.getBlock(x,y,z).charge == 0) { return low;
		  } else {return (byte)(0x8 | low);}
		}
		case PressurePad: { //pressure plates
		  if (Workspace.getBlock(x,y,z).charge > 0) { return 0x1;
		  } else {return 0x0;}
		}
		case Door: {
		  byte low = (byte) (Workspace.getBlock(x, y, z).place - 1);
		  if (Workspace.getBlock(x, y, z).charge > 0) low = (byte)( 0x4 | low );
		  if (Workspace.getBlock(x, y, z).subType.isLowerType()) return low;
		  else return (byte)( 0x8 | low );
		}
		case Trapdoor: {
		  byte low = 0;
		  switch (Workspace.getBlock(x, y, z).place) {
			case 1: low = 0x3; //north
			case 2: low = 0x1; //east
			case 3: low = 0x2; //south
			case 4: low = 0x0; //west
		  }
		  if (Workspace.getBlock(x, y, z).charge > 0) return (byte)( 0x4 | low );
		  else return low;
		}
		case Rail: { //rail
		byte low = 0;
		  if (Workspace.getBlock(x, y, z).subType == BlockType.CornerRail) {
			switch (Workspace.getBlock(x, y, z).place) {
			  case 0: low = 0x6; break;
			  case 1: low = 0x7; break;
			  case 2: low = 0x8; break;
			  case 3: low = 0x9; break;
			}
		  } else {
			switch (Workspace.getBlock(x, y, z).place) {
			  case 0: low = 0x0; break;
			  case 1: low = 0x1; break;
			  case 2: low = 0x3; break; // South and north
			  case 3: low = 0x2; break; // slopes are switched
			  case 4: low = 0x4; break;
			  case 5: low = 0x5; break;
			}
		  }
		  return low;
		}
		case PowerRail: { //power rail
		byte low = 0;
		  switch (Workspace.getBlock(x, y, z).place) {
			case 0: low = 0x0; break;
			case 1: low = 0x1; break;
			case 2: low = 0x3; break; // South and north
			case 3: low = 0x2; break; // slopes are switched
			case 4: low = 0x4; break;
			case 5: low = 0x5; break;
		  }
		  if (Workspace.getBlock(x, y, z).charge != 9) { return low;
		  } else {return (byte)(0x8 | low);} // This could easily be return low only if charge == 0
		}
		case DetectorRail: { //detector rail
		byte low = 0;
		  switch (Workspace.getBlock(x, y, z).place) {
			case 0: low = 0x0; break;
			case 1: low = 0x1; break;
			case 2: low = 0x3; break; // South and north
			case 3: low = 0x2; break; // slopes are switched
			case 4: low = 0x4; break;
			case 5: low = 0x5; break;
		  }
		  return low;
		}
	  }
	}
	return 0;
  }

  public static void load() {
    boolean running = false;
	if (Options.play) {running = true;}
    GUI.run(false);
	
    //set up file GUI
    JFileChooser chooser = new JFileChooser(Options.filePath);
	chooser.setFileFilter(new FileNameExtensionFilter("Schematic files", "schematic"));
	if (!(chooser.showOpenDialog(GUI.thisGUI) == JFileChooser.APPROVE_OPTION)) {
	  GUI.run(running);
      return;
	}
	Options.filePath = chooser.getCurrentDirectory().getAbsolutePath();
	
    //reads the file
    Tag.tags.clear();
    Tag.readFile(chooser.getSelectedFile());
	
	//entire area
	Tag[] area = Tag.tags.toArray(new Tag[Tag.tags.size()]);
	Tag[] pathX = {new Tag(10, "Schematic", true), new Tag(2, "Length", 0, true)};
	Tag[] pathY = {new Tag(10, "Schematic", true), new Tag(2, "Width", 0, true)};
	Tag[] pathZ = {new Tag(10, "Schematic", true), new Tag(2, "Height", 0, true)};
	new Workspace(Tag.findTag(pathX, area).getInt(), Tag.findTag(pathY, area).getInt(), Tag.findTag(pathZ, area).getInt());
	
	//single blocks
	Tag[] pathId = {new Tag(10, "Schematic", true), new Tag(7, "Blocks", 0, true)};
	Tag[] pathMeta = {new Tag(10, "Schematic", true), new Tag(7, "Data", 0, true)};
	byte[] id = Tag.findTag(pathId, area).getData();
	byte[] meta = Tag.findTag(pathMeta, area).getData();
	for (int i = 0;i < id.length;i++) {
	  loadBlock(i, id[i], meta[i]);
	}
	
	//displaying
	Options.level = 0;
	GUI.updateWorkingArea();
	GUI.run(running);
  }

  
  
  private static void loadBlock(int i, byte id, byte meta) {
    Block block = new Block();
	for (int h = 0;h < Options.blockIDs.length;h++) {
	  if (id == Options.blockIDs[h]) { //block is a solid block
	    block.id = BlockType.Block; 
	  }
	}
	if (block.id != BlockType.Block) {
	  switch (id) {
		case 20: {block.id = BlockType.Block;block.subType = BlockType.Glass;} break; //glass
		case 55: {block.id = BlockType.Wire;} break; //wire
		case 75: {block.id = BlockType.Torch;} break; //torch off
		case 76: {block.id = BlockType.Torch;block.charge = 16;} break; //torch on
		case 93: {block.id = BlockType.Repeater;} break; //repeater off
		case 94: {block.id = BlockType.Repeater;block.charge = 16;} break; //repeater on
		case 33: block.id = BlockType.Piston; break;
		case 29: block.id = BlockType.StickyPiston; break;	
		case 77: {block.id = BlockType.Button;} break; //button
		case 69: {block.id = BlockType.Lever;} break; //lever
		case 70: {block.id = BlockType.PressurePad; block.subType = BlockType.StonePad;} break; //stone pressure plate
		case 72: {block.id = BlockType.PressurePad; block.subType = BlockType.PressurePad;} break; //wood pressure plate
		case 64: {block.id = BlockType.Door; block.subType = BlockType.LowerWoodDoor;} break; //wood door
		case 71: {block.id = BlockType.Door; block.subType = BlockType.LowerIronDoor;} break; //iron door
		case 96: block.id = BlockType.Trapdoor; break; //trapdoor
		case 66: block.id = BlockType.Rail; break; //rail
		case 26: block.id = BlockType.PowerRail; break; //power rail
		case 27: block.id = BlockType.DetectorRail; //detector rail
	  }
	}
	
	switch (block.id) {
	  case Wire: {block.charge = meta;} break; //wire
	  case Torch: { //torch
	    block.delay = 2;
	    switch (meta) {
		 case 1: {block.place = 3;} break;
		 case 2: {block.place = 1;} break;
		 case 3: {block.place = 4;} break;
		 case 4: {block.place = 2;} break;
		}
	  } break;
	  case Repeater: { //repeater
	    block.place = 1;
		block.delay = 2;
	    switch (meta & 3) {
		 case 0: {block.place = 4;} break;
		 case 1: {block.place = 1;} break;
		 case 2: {block.place = 2;} break;
		 case 3: {block.place = 3;} break;
		}
		switch ((meta >>> 2) & 3) {
		 case 0: {block.delay = 2;} break;
		 case 1: {block.delay = 4;} break;
		 case 2: {block.delay = 6;} break;
		 case 3: {block.delay = 8;} break;
		}
	  } break;
	  case Piston:
	  case StickyPiston:
		  block.delay = 2; 
		  switch ( ( meta & 0x07 ) )
		  {
		  case 0x01: block.place = 0;break;
		  case 0x04: block.place = 1;break;
		  case 0x02: block.place = 2;break;
		  case 0x05: block.place = 3;break;
		  case 0x03: block.place = 4;break;
		  case 0x00: block.place = 5; break;
		  }
		  break;
		  
	  case Button: { //button
	    switch (meta & 7) {
		 case 1: {block.place = 3;} break;
		 case 2: {block.place = 1;} break;
		 case 3: {block.place = 4;} break;
		 case 4: {block.place = 2;} break;
		}
		block.charge = ((meta >>> 3) & 1)*16;
	  } break;
	  case Lever: { //lever
	    switch (meta & 7) {
		 case 1: {block.place = 3;} break;
		 case 2: {block.place = 1;} break;
		 case 3: {block.place = 4;} break;
		 case 4: {block.place = 2;} break;
		}
		block.charge = ((meta >>> 3) & 1)*16;
	  } break;
	  case PressurePad: {block.charge = meta*16;} break; //pressure plate
	  case Door: {
		block.place = (meta & 3) + 1;
		block.charge = (meta >>> 2) & 1;
		if (((meta >>> 3) & 1) == 1) {
			if (block.subType.isIronDoor()) block.subType = BlockType.UpperIronDoor;
			else block.subType = BlockType.UpperWoodDoor;
		}
	  }
	  case Trapdoor: {
		switch (meta & 3) {
		 case 0: {block.place = 4;} break;
		 case 1: {block.place = 2;} break;
		 case 2: {block.place = 3;} break;
		 case 3: {block.place = 1;} break;
		}
		block.charge = (meta >>> 2) & 1;
	  }
	  case Rail:
	  case PowerRail:
	  case DetectorRail: {
		switch (meta & 7) {
		 case 2: {block.place = 3;} break;
		 case 3: {block.place = 2;} break;
		 case 0:
		 case 1:
		 case 4:
		 case 5:
		 case 6:
		 case 7:
		 case 8:
		 case 9:
			block.place = meta & 7;
		if (block.id == BlockType.PowerRail)
		 block.charge = (meta >>> 3) & 1;
		}
	  }
	}
	
	
	//creates block
	int x = (i%(Workspace.gety()*Workspace.getx()))/Workspace.gety();
	int y = Workspace.gety()-1-((i%(Workspace.gety()*Workspace.getx()))%Workspace.gety());
	int z = i/(Workspace.gety()*Workspace.getx());
    Workspace.setBlock(x, y, z, block);
	
  }
  
}