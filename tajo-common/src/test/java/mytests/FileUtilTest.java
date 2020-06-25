package mytests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.tajo.util.FileUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class FileUtilTest {

	/*
	 * Category Partition
	 * String path {valid, notExsisting, null}
	 * String textToWrite {len > 0 ; len = 0 ; null}
	 * */
	@Test
	@Parameters({
		"test.txt,prova", // path = valid ; textToWrite > 0
		"test.txt,", // path = valid ; textToWrite = 0
		"./newFolder/test.txt,prova", // path = notExisting ; textToWrite > 0
		"test.txt,0", // path = valid ; textToWrite = null
		"0,prova", // path = null ; textToWrite > 0
	})
	public void writeAndReadTest(String path,String textToWrite) throws IOException {
		boolean t = false;
		if(textToWrite.equals("0")) {
			textToWrite = null;
		}
		if (path.equals("0")) {
			path = null;
		}
		try {
			Path filepath  = new Path(path);
			FileUtil.writeTextToFile(textToWrite, filepath);
			String read = FileUtil.readTextFile(new File(path));
			assertEquals(textToWrite,read);
		} catch(NullPointerException e) {
			t = true;
			assertTrue(t);
		}catch (IllegalArgumentException e) {
			t = true;
			assertTrue(t);
		}	
	}
	
	/*
	 * Category Partition
	 * String path : {valid,notExisting,null}
	 * String textToWrite : {len > 0 ; len = 0 ; null}
	 * */
	@Test
	@Parameters({
		"test.txt,prova", // path = valid ; textToWrite > 0
		"./newFolder/test.txt,prova", //path = notExisting ; textToWrite > 0
		"test.txt,", // path = valid ; textToWrite = 0
		"0,prova", // path = null ; textToWrite > 0
		"test.txt,0" // path = valid ; textToWrite = null
	})
	public void writeAndReadFromStream(String path,String textToWrite) throws IOException {
		boolean t = false;
		if(path.equals("0")) {
			path = null;
		}
		if(textToWrite.equals("0")) {
			textToWrite = null;
		}
		try {
			File f = new File(path);
			OutputStream os = new FileOutputStream(f);
			FileUtil.writeTextToStream(textToWrite, os);
			InputStream is = new FileInputStream(f);
			String output = FileUtil.readTextFromStream(is);
			assertEquals(textToWrite,output);
		}catch (FileNotFoundException e) {
			t = true;
			assertTrue(t);
		} catch(NullPointerException e) {
			t = true;
			assertTrue(t);
		}
		
		
	}
	
	@After
	public void clearFile() throws IOException {
		try {
			FileUtils.forceDelete(new File("test.txt"));
		} catch(Exception e) {}
		try {
			FileUtils.deleteDirectory(new File("./newFolder"));
		} catch (Exception e) {}
		
	}

}
