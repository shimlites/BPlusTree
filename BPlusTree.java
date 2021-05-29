// Searching on a B+ tree in Java

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BPlusTree {
	int index;
	InternalNode rootNode;
	LeafNode firstLeaf;

	public static void main(String[] args) {
		// Ensure correct number of arguments
		if (args.length != constants.DBQUERY_ARG_COUNT) {
			return;
		}
		LocalDateTime startTime = LocalDateTime.now();
		BPlusTree bPlusTree = new BPlusTree(3);

		int recordId = 0;
		int recordSize = Integer.parseInt(args[constants.DBQUERY_PAGE_SIZE_ARG]);
		String datafile = "heap." + recordSize;
		int numBytesInOneRecord = constants.TOTAL_SIZE;
		int numBytesInSdtnameField = constants.STD_NAME_SIZE;
		int numRecordsPerPage = recordSize / numBytesInOneRecord;
		byte[] page = new byte[recordSize];
		try (FileInputStream inStream = new FileInputStream(datafile);) {
			Integer numBytesRead = 0;
			// Create byte arrays for each field
			byte[] sdtnameBytes = new byte[numBytesInSdtnameField];
			// until the end of the binary file is reached
			while ((numBytesRead = inStream.read(page)) != -1) {
				// Process each record in page
				for (int index = 0; index < numRecordsPerPage; index++) {
					byte[] recordBytes = new byte[constants.TOTAL_SIZE];
					// Copy record's SdtName (field is located at multiples of the total record byte
					// length)
					System.arraycopy(page, (index * numBytesInOneRecord), sdtnameBytes, 0, numBytesInSdtnameField);

					// Check if field is empty; if so, end of all records found (packed
					// organisation)
					if (sdtnameBytes[0] == 0) {
						// can stop checking records
						break;
					}

					System.arraycopy(page, (index * numBytesInOneRecord), recordBytes, 0, constants.TOTAL_SIZE);
					recordId++;
					bPlusTree.insert(recordId, recordBytes);
				}
			}
		} catch (FileNotFoundException fnf) {
			System.err.println("FileNotFoundException getting while read page , err msg is : " + fnf.getMessage());
		} catch (IOException ioe) {
			System.err.println("IOException getting while read page , err msg is : " + ioe.getMessage());
		}

	}
	
	// Binary tree search program
	private int binaryTreeSearch(DictionaryPair[] dictionaryPairs, int numPairs, int key) {
		Comparator<DictionaryPair> comparator = new Comparator<DictionaryPair>() {
			@Override
			public int compare(DictionaryPair dictionaryPair1, DictionaryPair dictionaryPair2) {
				Integer keyValue1 = Integer.valueOf(dictionaryPair1.key);
				Integer keyValue2 = Integer.valueOf(dictionaryPair2.key);
				return keyValue1.compareTo(keyValue2);
			}
		};
		byte[] value = null;
		return Arrays.binarySearch(dictionaryPairs, 0, numPairs, new DictionaryPair(key, value), comparator);
	}

	// Find the leaf node
	private LeafNode findLeafNode(int key) {
		Integer[] keys = this.rootNode.keys;
		int i;

		for (i = 0; i < this.rootNode.degree - 1; i++) {
			if (key < keys[i]) {
				break;
			}
		}

		Node child = this.rootNode.childPointers[i];
		if (child instanceof LeafNode) {
			return (LeafNode) child;
		} else {
			return findLeafNode((InternalNode) child, key);
		}
	}

	// Find the leaf node
	private LeafNode findLeafNode(InternalNode node, int key) {

		Integer[] keys = node.keys;
		int i;

		for (i = 0; i < node.degree - 1; i++) {
			if (key < keys[i]) {
				break;
			}
		}
		Node childNode = node.childPointers[i];
		if (childNode instanceof LeafNode) {
			return (LeafNode) childNode;
		} else {
			return findLeafNode((InternalNode) node.childPointers[i], key);
		}
	}
	
	// Get the mid point
	private int getMidpoint() {
		return (int) Math.ceil((this.index + 1) / 2.0) - 1;
	}

	private boolean isEmpty() {
		return firstLeaf == null;
	}

	private int linearNullSearch(DictionaryPair[] dps) {
		for (int i = 0; i < dps.length; i++) {
			if (dps[i] == null) {
				return i;
			}
		}
		return -1;
	}

	private int linearNullSearch(Node[] pointers) {
		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] == null) {
				return i;
			}
		}
		return -1;
	}

	private void sortDictionary(DictionaryPair[] dictionary) {
		Arrays.sort(dictionary, new Comparator<DictionaryPair>() {
			@Override
			public int compare(DictionaryPair o1, DictionaryPair o2) {
				if (o1 == null && o2 == null) {
					return 0;
				}
				if (o1 == null) {
					return 1;
				}
				if (o2 == null) {
					return -1;
				}
				return o1.compareTo(o2);
			}
		});
	}

	private Node[] splitChildPointers(InternalNode in, int split) {
		Node[] pointers = in.childPointers;
		Node[] halfPointers = new Node[this.index + 1];

		for (int i = split + 1; i < pointers.length; i++) {
			halfPointers[i - split - 1] = pointers[i];
			in.removePointer(i);
		}

		return halfPointers;
	}

	private DictionaryPair[] splitDictionary(LeafNode ln, int split) {
		DictionaryPair[] dictionary = ln.dictionary;
		DictionaryPair[] halfDict = new DictionaryPair[this.index];

		for (int i = split; i < dictionary.length; i++) {
			halfDict[i - split] = dictionary[i];
			ln.delete(i);
		}

		return halfDict;
	}

	private void splitInternalNode(InternalNode in) {
		InternalNode parent = in.parent;

		int midpoint = getMidpoint();
		int newParentKey = in.keys[midpoint];
		Integer[] halfKeys = splitKeys(in.keys, midpoint);
		Node[] halfPointers = splitChildPointers(in, midpoint);

		in.degree = linearNullSearch(in.childPointers);

		InternalNode sibling = new InternalNode(this.index, halfKeys, halfPointers);
		for (Node pointer : halfPointers) {
			if (pointer != null) {
				pointer.parent = sibling;
			}
		}

		sibling.rightSibling = in.rightSibling;
		if (sibling.rightSibling != null) {
			sibling.rightSibling.leftSibling = sibling;
		}
		in.rightSibling = sibling;
		sibling.leftSibling = in;

		if (parent == null) {

			Integer[] keys = new Integer[this.index];
			keys[0] = newParentKey;
			InternalNode newRoot = new InternalNode(this.index, keys);
			newRoot.appendChildPointer(in);
			newRoot.appendChildPointer(sibling);
			this.rootNode = newRoot;

			in.parent = newRoot;
			sibling.parent = newRoot;

		} else {

			parent.keys[parent.degree - 1] = newParentKey;
			Arrays.sort(parent.keys, 0, parent.degree);

			int pointerIndex = parent.findIndexOfPointer(in) + 1;
			parent.insertChildPointer(sibling, pointerIndex);
			sibling.parent = parent;
		}
	}

	private Integer[] splitKeys(Integer[] keys, int split) {

		Integer[] halfKeys = new Integer[this.index];

		keys[split] = null;

		for (int i = split + 1; i < keys.length; i++) {
			halfKeys[i - split - 1] = keys[i];
			keys[i] = null;
		}

		return halfKeys;
	}

	private void insert(int key, byte[] value) {
		if (isEmpty()) {

			LeafNode ln = new LeafNode(this.index, new DictionaryPair(key, value));

			this.firstLeaf = ln;

		} else {
			LeafNode ln = (this.rootNode == null) ? this.firstLeaf : findLeafNode(key);

			if (!ln.insert(new DictionaryPair(key, value))) {

				ln.dictionary[ln.numPairs] = new DictionaryPair(key, value);
				ln.numPairs++;
				sortDictionary(ln.dictionary);

				int midpoint = getMidpoint();
				DictionaryPair[] halfDict = splitDictionary(ln, midpoint);

				if (ln.parent == null) {

					Integer[] parent_keys = new Integer[this.index];
					parent_keys[0] = halfDict[0].key;
					InternalNode parent = new InternalNode(this.index, parent_keys);
					ln.parent = parent;
					parent.appendChildPointer(ln);

				} else {
					int newParentKey = halfDict[0].key;
					ln.parent.keys[ln.parent.degree - 1] = newParentKey;
					Arrays.sort(ln.parent.keys, 0, ln.parent.degree);
				}

				LeafNode newLeafNode = new LeafNode(this.index, halfDict, ln.parent);

				int pointerIndex = ln.parent.findIndexOfPointer(ln) + 1;
				ln.parent.insertChildPointer(newLeafNode, pointerIndex);

				newLeafNode.rightSibling = ln.rightSibling;
				if (newLeafNode.rightSibling != null) {
					newLeafNode.rightSibling.leftSibling = newLeafNode;
				}
				ln.rightSibling = newLeafNode;
				newLeafNode.leftSibling = ln;

				if (this.rootNode == null) {

					this.rootNode = ln.parent;

				} else {
					InternalNode in = ln.parent;
					while (in != null) {
						if (in.isOverfull()) {
							splitInternalNode(in);
						} else {
							break;
						}
						in = in.parent;
					}
				}
			}
		}
	}

	private byte[] search(int key) {
		if (isEmpty()) {
			return null;
		}
		LeafNode leafNode = (this.rootNode == null) ? this.firstLeaf : findLeafNode(key);

		DictionaryPair[] dps = leafNode.dictionary;
		int index = binaryTreeSearch(dps, leafNode.numPairs, key);
		if (index < 0) {
			System.out.println("Record not found : " + key);
			return null;
		} else {
			System.out.print("Record found : " + key);
			parseBytes(dps[index].value);
			return dps[index].value;
		}
	}

	private List<byte[]> search(int lowerBound, int upperBound) {
		List<byte[]> values = new ArrayList<>();
		LeafNode currNode = this.firstLeaf;
		while (currNode != null) {

			DictionaryPair dps[] = currNode.dictionary;
			for (DictionaryPair dictionaryPair : dps) {
				if (dictionaryPair == null) {
					break;
				}
				if (lowerBound <= dictionaryPair.key && dictionaryPair.key <= upperBound) {
					System.out.print("Record found : " + dictionaryPair.key);
					parseBytes(dictionaryPair.value);
					values.add(dictionaryPair.value);
				}
			}
			currNode = currNode.rightSibling;
		}
		return values;
	}

	public BPlusTree(int m) {
		this.index = m;
		this.rootNode = null;
	}

	public class Node {
		InternalNode parent;
	}

	private class InternalNode extends Node {
		int maxDegree;
		int minDegree;
		int degree;
		InternalNode leftSibling;
		InternalNode rightSibling;
		Integer[] keys;
		Node[] childPointers;

		private void appendChildPointer(Node pointer) {
			this.childPointers[degree] = pointer;
			this.degree++;
		}

		private int findIndexOfPointer(Node pointer) {
			for (int i = 0; i < childPointers.length; i++) {
				if (childPointers[i] == pointer) {
					return i;
				}
			}
			return -1;
		}

		private void insertChildPointer(Node pointer, int index) {
			for (int i = degree - 1; i >= index; i--) {
				childPointers[i + 1] = childPointers[i];
			}
			this.childPointers[index] = pointer;
			this.degree++;
		}

		private boolean isOverfull() {
			return this.degree == maxDegree + 1;
		}

		private void removePointer(int index) {
			this.childPointers[index] = null;
			this.degree--;
		}

		private InternalNode(int m, Integer[] keys) {
			this.maxDegree = m;
			this.minDegree = (int) Math.ceil(m / 2.0);
			this.degree = 0;
			this.keys = keys;
			this.childPointers = new Node[this.maxDegree + 1];
		}

		private InternalNode(int m, Integer[] keys, Node[] pointers) {
			this.maxDegree = m;
			this.minDegree = (int) Math.ceil(m / 2.0);
			this.degree = linearNullSearch(pointers);
			this.keys = keys;
			this.childPointers = pointers;
		}
	}

	public class LeafNode extends Node {
		int maxNumPairs;
		int minNumPairs;
		int numPairs;
		LeafNode leftSibling;
		LeafNode rightSibling;
		DictionaryPair[] dictionary;

		public void delete(int index) {
			this.dictionary[index] = null;
			numPairs--;
		}

		public boolean insert(DictionaryPair dp) {
			if (this.isFull()) {
				return false;
			} else {
				this.dictionary[numPairs] = dp;
				numPairs++;
				Arrays.sort(this.dictionary, 0, numPairs);

				return true;
			}
		}

		public boolean isDeficient() {
			return numPairs < minNumPairs;
		}

		public boolean isFull() {
			return numPairs == maxNumPairs;
		}

		public boolean isLendable() {
			return numPairs > minNumPairs;
		}

		public boolean isMergeable() {
			return numPairs == minNumPairs;
		}

		public LeafNode(int m, DictionaryPair dp) {
			this.maxNumPairs = m - 1;
			this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
			this.dictionary = new DictionaryPair[m];
			this.numPairs = 0;
			this.insert(dp);
		}

		public LeafNode(int m, DictionaryPair[] dps, InternalNode parent) {
			this.maxNumPairs = m - 1;
			this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
			this.dictionary = dps;
			this.numPairs = linearNullSearch(dps);
			this.parent = parent;
		}
	}

	public class DictionaryPair implements Comparable<DictionaryPair> {
		int key;
		byte[] value;

		public DictionaryPair(int key, byte[] value) {
			this.key = key;
			this.value = value;
		}

		public int compareTo(DictionaryPair o) {
			if (key == o.key) {
				return 0;
			} else if (key > o.key) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	private void parseBytes(byte[] Record) {
		int numBytesInSdtnameField = constants.STD_NAME_SIZE;
		int numBytesIntField = Integer.BYTES;
		byte[] sdtnameBytes = new byte[numBytesInSdtnameField];
		byte[] id_Bytes = new byte[constants.ID_SIZE];
		byte[] date_Bytes = new byte[constants.DATE_SIZE];
		byte[] year_Bytes = new byte[constants.YEAR_SIZE];
		byte[] month_Bytes = new byte[constants.MONTH_SIZE];
		byte[] mdate_Bytes = new byte[constants.MDATE_SIZE];
		byte[] day_Bytes = new byte[constants.DAY_SIZE];
		byte[] time_Bytes = new byte[constants.TIME_SIZE];
		byte[] sensorId_Bytes = new byte[constants.SENSORID_SIZE];
		byte[] sensorName_Bytes = new byte[constants.SENSORNAME_SIZE];
		byte[] counts_Bytes = new byte[constants.COUNTS_SIZE];
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

		System.arraycopy(Record, 0, sdtnameBytes, 0, numBytesInSdtnameField);

		String sdtNameString = new String(sdtnameBytes);

		System.arraycopy(Record, constants.ID_OFFSET, id_Bytes, 0, numBytesIntField);
		System.arraycopy(Record, constants.DATE_OFFSET, date_Bytes, 0, constants.DATE_SIZE);
		System.arraycopy(Record, constants.YEAR_OFFSET, year_Bytes, 0, numBytesIntField);
		System.arraycopy(Record, constants.MONTH_OFFSET, month_Bytes, 0, constants.MONTH_SIZE);
		System.arraycopy(Record, constants.MDATE_OFFSET, mdate_Bytes, 0, numBytesIntField);
		System.arraycopy(Record, constants.DAY_OFFSET, day_Bytes, 0, constants.DAY_SIZE);
		System.arraycopy(Record, constants.TIME_OFFSET, time_Bytes, 0, numBytesIntField);
		System.arraycopy(Record, constants.SENSORID_OFFSET, sensorId_Bytes, 0, numBytesIntField);
		System.arraycopy(Record, constants.SENSORNAME_OFFSET, sensorName_Bytes, 0, constants.SENSORNAME_SIZE);
		System.arraycopy(Record, constants.COUNTS_OFFSET, counts_Bytes, 0, numBytesIntField);

		// Convert long data into Date object
		Date date = new Date(ByteBuffer.wrap(date_Bytes).getLong());

		// Get a string representation of the record for printing to stdout
		String record = sdtNameString.trim() + "," + ByteBuffer.wrap(id_Bytes).getInt() + "," + dateFormat.format(date)
				+ "," + ByteBuffer.wrap(year_Bytes).getInt() + "," + new String(month_Bytes).trim() + ","
				+ ByteBuffer.wrap(mdate_Bytes).getInt() + "," + new String(day_Bytes).trim() + ","
				+ ByteBuffer.wrap(time_Bytes).getInt() + "," + ByteBuffer.wrap(sensorId_Bytes).getInt() + ","
				+ new String(sensorName_Bytes).trim() + "," + ByteBuffer.wrap(counts_Bytes).getInt();
		System.out.println(record);
	}

}
