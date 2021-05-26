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

}