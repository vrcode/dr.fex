package co.vrcode.drfex.test;


import junit.framework.TestCase;
import co.vrcode.util.FileSystem;


public class TestUtils extends TestCase {


	public void testNegativeBytes() {
		assertEquals(
			"Unknown",
			FileSystem.getFormattedBytesString(-1000)
		);
	}


	public void testBytes() {
		assertEquals(
			"1000 bytes",
			FileSystem.getFormattedBytesString(1000)
		);
	}


	public void testKBytes() {
		assertEquals(
			"1 KB",
			FileSystem.getFormattedBytesString(1024)
		);
	}


	public void testKBytesFrac() {
		assertEquals(
			"1 KB",
			FileSystem.getFormattedBytesString(1500)
		);
	}


	public void testMBytes() {
		assertEquals(
			"1.00 MB",
			FileSystem.getFormattedBytesString(1048576)
		);
	}


	public void testMBytesFrac() {
		assertEquals(
			"1.48 MB",
			FileSystem.getFormattedBytesString(1548576)
		);
	}


	public void testGBytes() {
		assertEquals(
			"1.00 GB",
			FileSystem.getFormattedBytesString(1073741824)
		);
	}


	public void testGBytesFrac() {
		assertEquals(
			"1.47 GB",
			FileSystem.getFormattedBytesString(1573741824)
		);
	}


	public void testTBytes() {
		assertEquals(
			"1.00 TB",
			FileSystem.getFormattedBytesString(1099511627776L)
		);
	}


	public void testTBytesFrac() {
		assertEquals(
			"1.45 TB",
			FileSystem.getFormattedBytesString(1599511627776L)
		);
	}

	public void testTBytes3Digit() {
		assertEquals(
			"909.49 TB",
			FileSystem.getFormattedBytesString(999999999999999L)
		);
	}

	public void testGetFileExtension() {
		assertEquals(
			"txt",
			FileSystem.getExtensionForFile("test.txt")
		);
		assertEquals(
			"txt",
			FileSystem.getExtensionForFile("/some/path/test.txt")
		);
	}


	public void testGetFileExtensionEmpty() {
		// Paths with no extension should be handled properly
		assertEquals(
			"",
			FileSystem.getExtensionForFile("/some/path/test")
		);

		// Files that start with ".", such as config files
		// usually have no extension
		assertEquals(
			"",
			FileSystem.getExtensionForFile("/some/path/.config")
		);

		// Make sure files that start with ".", but have
		// an extension are also handled properly
		assertEquals(
			"conf",
			FileSystem.getExtensionForFile("/some/path/.config.conf")
		);
	}


	public void testGetMimeType() {
		assertEquals(
			FileSystem.getMimeType("test.txt"),
			"text/plain"
		);
		assertEquals(
			FileSystem.getMimeType("/some/path/image.jpg"),
			"image/jpeg"
		);		
		assertEquals(
			FileSystem.getMimeType("/file.zip"),
			"application/zip"
		);
		assertEquals(
			FileSystem.getMimeType("file.html"),
			"text/html"
		);
	}


	public void testGetMimeTypeUnknown() {
		// Unknown Mime Types
		assertEquals(
			FileSystem.getMimeType("file.blah"),
			"application/octet-stream"
		);
	}

}
