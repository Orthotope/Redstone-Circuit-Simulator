package Simulator;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.*;

/*
 This is almost a full NBT-reader/writer. However, TAG_compound have no
 payload (it is in the next tags).
 The same with TAG_list, but it have the type and the length as payload.
 see http://www.minecraft.net/docs/NBT.txt for specifications
 */

public class Tag {
	public static ArrayList<Tag> tags = new ArrayList<Tag>();

	private int type;
	private String name;
	private byte[] payload; // NOTE:compounds payloads is not in here, but in
							// the following tags until end. Lits likewise, but
							// without an end.
	private boolean named = true;

	public int getType() throws NullPointerException {
		return type;
	}

	public String getName() throws NullPointerException {
		return name;
	}

	public byte[] getPayload() throws NullPointerException {
		return payload;
	}

	public String toString() {
		String start = super.toString() + "--- Type: " + type + "; Name: "
				+ name + "; Payload: ";
		StringBuffer out = new StringBuffer(start);
		if (nullError(2)) {
			out.append("null");
		} else {
			out.append("[");
			for (byte b : payload) {
				out.append((int) b + ",");
			}
			out.append("]");
		}
		return out.toString();
	}

	// for end(0)
	public Tag(boolean isEnd) {
		if (isEnd) {
			this.type = 0;
		}
	}

	// for byte(1), short(2), int(3), long(4)
	public Tag(int type, String name, int value, boolean named) {
		this.named = named;
		this.type = type;
		this.name = name;
		switch (type) {
		case 1: { // TAG_Byte
			this.payload = new byte[1];
			this.payload[0] = (byte) value;
		}
			break;
		case 2: { // TAG_Short
			this.payload = new byte[2];
			this.payload[0] = (byte) (value >>> 8);
			this.payload[1] = (byte) value;
		}
			break;
		case 3: { // TAG_Int
			this.payload = new byte[4];
			this.payload[0] = (byte) (value >>> 24);
			this.payload[1] = (byte) (value >>> 16);
			this.payload[2] = (byte) (value >>> 8);
			this.payload[3] = (byte) value;
		}
			break;
		case 4: { // TAG_Long
			this.payload = new byte[8];
			this.payload[0] = 0;
			this.payload[1] = 0;
			this.payload[2] = 0;
			this.payload[3] = 0;
			this.payload[4] = (byte) (value >>> 24);
			this.payload[5] = (byte) (value >>> 16);
			this.payload[6] = (byte) (value >>> 8);
			this.payload[7] = (byte) value;
		}
			break;
		}
	}

	// for a true representation of long(4)
	public Tag(int type, String name, long value, boolean named) {
		this.named = named;
		this.type = type;
		this.name = name;
		if (type == 4) { // TAG_Long
			this.payload = new byte[8];
			this.payload[0] = (byte) (value >>> 56);
			this.payload[1] = (byte) (value >>> 48);
			this.payload[2] = (byte) (value >>> 40);
			this.payload[3] = (byte) (value >>> 32);
			this.payload[4] = (byte) (value >>> 24);
			this.payload[5] = (byte) (value >>> 16);
			this.payload[6] = (byte) (value >>> 8);
			this.payload[7] = (byte) value;
		}
	}

	// for float(5), double(6)
	public Tag(int type, String name, double value, boolean named) {
		this.named = named;
		this.type = type;
		this.name = name;
		long bits = Double.doubleToLongBits(value);
		switch (type) {
		case 5: { // TAG_Float
			this.payload = new byte[4];
			this.payload[0] = (byte) (bits >>> 24);
			this.payload[1] = (byte) (bits >>> 16);
			this.payload[2] = (byte) (bits >>> 8);
			this.payload[3] = (byte) bits;
		}
			break;
		case 6: { // TAG_Double
			this.payload = new byte[4];
			this.payload[0] = (byte) (bits >>> 56);
			this.payload[1] = (byte) (bits >>> 48);
			this.payload[2] = (byte) (bits >>> 40);
			this.payload[3] = (byte) (bits >>> 32);
			this.payload[4] = (byte) (bits >>> 24);
			this.payload[5] = (byte) (bits >>> 16);
			this.payload[6] = (byte) (bits >>> 8);
			this.payload[7] = (byte) bits;
		}
			break;
		}
	}

	// for byte array(7) and all other where payload is in a byte array.
	public Tag(int type, String name, byte[] byteArray, boolean named) {
		this.named = named;
		this.type = type;
		this.name = name;
		if (type == 7) { // TAG_Byte_Array
			this.payload = new byte[byteArray.length + 4]; // add the length in
															// short
			this.payload[0] = (byte) (byteArray.length >>> 24);
			this.payload[1] = (byte) (byteArray.length >>> 16);
			this.payload[2] = (byte) (byteArray.length >>> 8);
			this.payload[3] = (byte) byteArray.length; // TAG_Int
			for (int i = 0; i < byteArray.length; i++) {
				this.payload[i + 4] = byteArray[i];
			}
		} else {
			this.payload = byteArray;
		}

	}

	// for string (8)
	public Tag(int type, String name, String string, boolean named) {
		this.named = named;
		this.type = type;
		this.name = name;
		try {
			this.payload = new byte[string.getBytes("UTF-8").length + 2];
			this.payload[0] = (byte) (string.getBytes("UTF-8").length >>> 8);
			this.payload[1] = (byte) (string.getBytes("UTF-8").length);// TAG_Short
			for (int i = 0; i < string.getBytes("UTF-8").length; i++) {
				this.payload[i + 2] = string.getBytes("UTF-8")[i];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// for compound(10) start
	public Tag(int type, String name, boolean named) {
		this.named = named;
		this.type = type;
		this.name = name;
	}

	// for byte(1), short(2), int(3), long(4)
	public int getInt() throws NullPointerException {
		switch (type) {
		case 1:
			return ((int) payload[0]); // TAG_Byte
		case 2:
			return (((int) payload[0]) << 8 | ((int) payload[1])); // TAG_Short
		case 3:
			return (((int) payload[0]) << 24 | ((int) payload[1]) << 16
					| ((int) payload[2]) << 8 | ((int) payload[3])); // TAG_Int
		case 4:
			return (((int) payload[4]) << 24 | ((int) payload[5]) << 16
					| ((int) payload[6]) << 8 | ((int) payload[7])); // TAG_Long
		}
		throw new NullPointerException();
	}

	// for a true representation of long(4)
	public long getLong() throws NullPointerException {
		if (type == 4) { // TAG_Long
			return (((long) payload[0]) << 56 | ((long) payload[1]) << 48
					| ((long) payload[2]) << 40 | ((long) payload[3]) << 32
					| ((long) payload[4]) << 24 | ((long) payload[5]) << 16
					| ((long) payload[6]) << 8 | ((long) payload[6]));
		} else if (type == 1 || type == 2 || type == 3) {
			return (long) this.getInt();
		}
		throw new NullPointerException();
	}

	// for float(5), double(6)
	public double getDouble() throws NullPointerException {
		switch (type) {
		case 5:
			return Double
					.longBitsToDouble((((long) payload[0]) << 24
							| ((long) payload[1]) << 16
							| ((long) payload[2]) << 8 | ((long) payload[3]))); // TAG_Float
		case 6:
			return Double
					.longBitsToDouble((((long) payload[0]) << 56
							| ((long) payload[1]) << 48
							| ((long) payload[2]) << 40
							| ((long) payload[3]) << 32
							| ((long) payload[4]) << 24
							| ((long) payload[5]) << 16
							| ((long) payload[6]) << 8 | ((long) payload[6]))); // TAG_Double
		}
		throw new NullPointerException();
	}

	// for byte array(7) plus all other that need the data.
	public byte[] getData() {
		if (type != 7) {
			return payload;
		}
		byte[] out = new byte[payload.length - 4];
		for (int i = 0; i < out.length; i++) {
			out[i] = payload[i + 4]; // cuts away the length;
		}
		return out;
	}

	// for string(8)
	public String getString() throws NullPointerException {
		if (type == 8) {
			try {
				return new String(payload, 2, payload.length - 2, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		throw new NullPointerException();
	}

	private boolean nullError(int checkID) { // 0:T
		try {
			switch (checkID) { // might trigger an exception
			case 0:
				new Integer(getType());
				break; // getType
			case 1:
				getName().toString();
				break; // getName
			case 2:
				getPayload().toString();
				break; // getPayload
			case 3:
				new Integer(getInt());
				break; // getInt
			case 4:
				new Long(getLong());
				break; // getLong
			case 5:
				new Double(getDouble());
				break; // getDouble
			case 6:
				getString().toString();
				break; // getString
			}
		} catch (NullPointerException e) {
			return true;
		}
		return false;
	}

	public byte[] allInBytes() {
		ArrayList<Byte> all = new ArrayList<Byte>();
		if (named) {
			all.add(new Byte((byte) type)); // type
			if (!nullError(1)) {
				try {
					all.add(new Byte(
							(byte) (name.getBytes("UTF-8").length >>> 8)));
					all.add(new Byte((byte) name.getBytes("UTF-8").length)); // name
																				// length
					for (int i = 0; i < name.getBytes("UTF-8").length; i++) {
						all.add(new Byte(name.getBytes("UTF-8")[i])); // adds
																		// name
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (!nullError(2)) {
			for (int i = 0; i < getPayload().length; i++) {
				all.add(new Byte(payload[i])); // adds payload
			}
		}
		byte[] out = new byte[all.size()];
		for (int i = 0; i < all.size(); i++) {
			out[i] = all.get(i).byteValue();
		}
		return out;
	}

	public static void saveFile(File file) {
		try {
			GZIPOutputStream outGZIP = new GZIPOutputStream(
					new FileOutputStream(file));
			DataOutputStream out = new DataOutputStream(outGZIP);
			for (Tag tag : tags) {
				out.write(tag.allInBytes(), 0, tag.allInBytes().length);
			}
			out.close();
			outGZIP.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readFile(File file) {
		try {
			GZIPInputStream inGZIP = new GZIPInputStream(new FileInputStream(
					file));
			DataInputStream in = new DataInputStream(inGZIP);
			tags.clear();
			readCompound(in, true);
			in.close();
			inGZIP.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readCompound(DataInputStream in, boolean named)
			throws Exception {
		int compoundsOpen = 0;
		Tag tag;
		boolean first = true;
		do {
			if (named || !first) {
				tag = readTag(in, -1);
			} else {
				tag = readTag(in, 10);
				first = false;
			}
			tags.add(tag);
			if (!tag.nullError(0)) {
				if (tag.getType() == 0) { // end tag
					compoundsOpen--;
				} else if (tag.getType() == 10) { // compound
					compoundsOpen++;
				} else if (tag.getType() == 9) { // list
					for (int i = 0; i < ((tag.getPayload()[1] << 24)
							| (tag.getPayload()[2] << 16)
							| (tag.getPayload()[3] << 8) | tag.getPayload()[4]); i++) {
						if (tag.getPayload()[0] != 10) {
							tags
									.add(new Tag(
											tag.getPayload()[0],
											"",
											readPayload(in, tag.getPayload()[0]),
											false));
						} else {
							readCompound(in, false);
						}
					}
				}
			}
		} while (compoundsOpen > 0);
	}

	private static Tag readTag(DataInputStream in, int unnamedType)
			throws IOException {
		if (unnamedType == -1) {
			String name = "";
			int type = (int) in.readByte();
			if (type == 0) { // TAG_End
				return new Tag(true);
			}
			short nameLength = in.readShort();
			byte[] nameBytes = new byte[nameLength];
			for (int i = 0; i < nameLength; i++) {
				try {
					nameBytes[i] = in.readByte(); // read name
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				name = new String(nameBytes, 0, nameBytes.length, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (type == 10) {
				return new Tag(type, name, true);
			} else {
				return new Tag(type, name, readPayload(in, type), true);
			}
		} else {
			return new Tag(unnamedType, "", readPayload(in, unnamedType), false);
		}
	}

	private static byte[] readPayload(DataInputStream in, int type)
			throws IOException {
		byte[] payload = new byte[0];
		switch (type) {
		case 1: { // TAG_Byte
			payload = new byte[1];
			payload[0] = in.readByte();
		}
			break;
		case 2: { // TAG_Short
			payload = new byte[2];
			for (int i = 0; i < 2; i++) {
				payload[i] = in.readByte();
			}
		}
			break;
		case 5: {
		} // TAG_Float
		case 3: { // TAG_Int
			payload = new byte[4];
			for (int i = 0; i < 4; i++) {
				payload[i] = in.readByte();
			}
		}
			break;
		case 6: {
		} // TAG_Double
		case 4: { // TAG_Long
			payload = new byte[8];
			for (int i = 0; i < 8; i++) {
				payload[i] = in.readByte();
			}
		}
			break;
		case 7: { // TAG_Byte_Array
			int arrayLength = in.readInt();
			payload = new byte[arrayLength];
			for (int i = 0; i < arrayLength; i++) {
				payload[i] = in.readByte(); // read actual array
			}
		}
			break;
		case 8: { // TAG_String
			short arrayLength = in.readShort();
			payload = new byte[arrayLength + 2];
			payload[0] = (byte) (arrayLength >>> 8);
			payload[1] = (byte) (arrayLength);
			for (int i = 0; i < arrayLength; i++) {
				payload[i + 2] = in.readByte(); // read actual array
			}
		}
			break;
		case 9: { // Tag_List
			int listType = (int) in.readByte();
			int listLength = in.readInt();
			payload = new byte[5];
			payload[0] = (byte) listType;
			payload[1] = (byte) (listLength >>> 24);
			payload[2] = (byte) (listLength >>> 16);
			payload[3] = (byte) (listLength >>> 8);
			payload[4] = (byte) listLength;
		}
			break;
		}
		return payload;
	}

	public static Tag findTag(Tag[] nestedTags, Tag[] searchArea)
			throws ArrayIndexOutOfBoundsException { // cannot return compound or
													// list. Payload does not
													// required to be right.
													// Only for named tags
		Tag[] nextTags = null;
		if (nestedTags.length >= 2) { // removes this nesting
			nextTags = new Tag[nestedTags.length - 1];
			for (int i = 1; i < nestedTags.length; i++) {
				nextTags[i - 1] = nestedTags[i];
			}
		}

		// actual searching for a tag to be nested in
		for (int i = 0; i < searchArea.length; i++) {
			Tag currentTag = searchArea[i];
			if ((currentTag.getType() == nestedTags[0].getType())
					&& (currentTag.getName().equals(nestedTags[0].getName()))) { // if
																					// this
				if (currentTag.getType() == 10 && nestedTags.length > 1) { // compound
					return findTag(nextTags, getCompoundPayload(i, searchArea));
				} else if (currentTag.getType() == 9 && nestedTags.length > 1) { // list
					return findTag(nextTags, getListPayload(i, searchArea));
				} else if (nestedTags.length == 1) {
					return currentTag; // what is returned in the end
				}
			} else { // not this
				if (currentTag.getType() == 10) { // compound
					return findTag(nestedTags, skipCompound(i, searchArea));
				} else if (currentTag.getType() == 9) { // list
					return findTag(nestedTags, skipList(i, searchArea));
				}
			}
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	private static Tag[] getCompoundPayload(int start, Tag[] area)
			throws ArrayIndexOutOfBoundsException {
		ArrayList<Tag> out = new ArrayList<Tag>();
		for (int i = start + 1; i < area.length; i++) {
			Tag tag = area[i];
			out.add(tag);
			if (tag.getType() == 10) { // containing a subcompound
				Tag[] subCompound = getCompoundPayload(i, area);
				for (int h = 0; h < subCompound.length; h++) {
					out.add(subCompound[h]);
					i++;
				}
				out.add(new Tag(true)); // the end tag of underlying compounds
										// is brought with
				i++;
			} else if (tag.getType() == 9) { // containing a list
				Tag[] subList = getListPayload(i, area);
				for (int h = 0; h < subList.length; h++) {
					out.add(subList[h]);
					i++;
				}
			} else if (tag.getType() == 0) { // end tag reached
				out.remove(out.size() - 1);
				return out.toArray(new Tag[out.size()]);
			}
		}
		return out.toArray(new Tag[out.size()]);
	}

	private static Tag[] getListPayload(int start, Tag[] area)
			throws ArrayIndexOutOfBoundsException {
		ArrayList<Tag> out = new ArrayList<Tag>();
		int listLength = ((area[start].payload[1] << 24)
				| (area[start].payload[2] << 16)
				| (area[start].payload[3] << 8) | area[start].payload[4]);
		for (int i = 0; i < listLength; i++) {
			if (area[start].payload[0] == 9) { // a list of lists
				out.add(area[start + 1 + (out.size())]);
				Tag[] subList = getListPayload(start + (out.size()), area);
				for (int h = 0; h < subList.length; h++) {
					out.add(subList[h]);
				}
			} else if (area[start].payload[0] == 10) { // a list of compounds
				out.add(area[start + 1 + (out.size())]);
				Tag[] subCompound = getCompoundPayload(start + (out.size()),
						area);
				for (int h = 0; h < subCompound.length; h++) {
					out.add(subCompound[h]);
				}
				out.add(new Tag(true)); // the end tag of underlying compounds
										// is brought with
			} else { // all other cases
				out.add(area[start + 1 + i]);
			}
		}
		return out.toArray(new Tag[out.size()]);
	}

	private static Tag[] skipCompound(int start, Tag[] area)
			throws ArrayIndexOutOfBoundsException {
		Tag[] skip = getCompoundPayload(start, area);
		int size = area.length - skip.length - start - 2;
		if (area.length - skip.length - start - 2 < 0) {
			size = 0;
			System.out.println("end");
		}
		Tag[] out = new Tag[size];
		for (int i = 0; i < out.length; i++) {
			out[i] = area[start + skip.length + 2 + i];
		}
		return out;
	}

	private static Tag[] skipList(int start, Tag[] area)
			throws ArrayIndexOutOfBoundsException {
		Tag[] skip = getListPayload(start, area);
		int size = area.length - skip.length - start - 1;
		if (area.length - skip.length - start - 1 < 0) {
			size = 0;
		}
		Tag[] out = new Tag[size];
		for (int i = 0; i < out.length; i++) {
			out[i] = area[start + skip.length + 1 + i];
		}
		return out;
	}

	// for testing
	public static void main(String[] args) {
		String filename = "data.txt";
		/*
		 * readFile(filename); for (Tag tag :tags) { System.out.println(tag); }
		 */
		try {
			GZIPInputStream inGZIP = new GZIPInputStream(new FileInputStream(
					new File(filename)));
			DataInputStream in = new DataInputStream(inGZIP);
			do {
				System.out.println(in.readByte());
			} while (true);
		} catch (Exception e) {
		}

	}

}
