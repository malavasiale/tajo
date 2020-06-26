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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.tajo.util.FileUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

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
		}finally {
			clearFile();
		}
	}
	
	/*
	 * Category Partition
	 * Streams os / is : {valid,notExisting,null}
	 * String textToWrite : {len > 0 ; len = 0 ; null}
	 * */
	@Test
	@Parameters({
		"test.txt,prova", // stream = valid ; textToWrite > 0
		"./newFolder/test.txt,prova", // stream = notExisting ; textToWrite > 0
		"test.txt,", // stream = valid ; textToWrite = 0
		"0,prova", // stream = null ; textToWrite > 0
		"test.txt,0" // stream = valid ; textToWrite = null
	})
	public void writeAndReadFromStream(String path,String textToWrite) throws IOException {
		boolean t = false;
		OutputStream os = null;
		InputStream is = null;
		File f = null;
		
		if(path.equals("0")) {
			path = null;
		}
		if(textToWrite.equals("0")) {
			textToWrite = null;
		}
		try {
			f = new File(path);
			os = new FileOutputStream(f);
			os = Mockito.spy(os);
			FileUtil.writeTextToStream(textToWrite, os);
			is = new FileInputStream(f);
			is = Mockito.spy(is);
			String output = FileUtil.readTextFromStream(is);
			assertEquals(textToWrite,output);
		}catch (FileNotFoundException e) {
			t = true;
			assertTrue(t);
		} catch(NullPointerException e) {
			t = true;
			assertTrue(t);
		}finally {
			if(os != null) {
				Mockito.verify(os).close();
			}
			if(is != null) {
				Mockito.verify(is).close();
			}
		}	
	}
	
	/*
	 * Logger log { valid ; null }
	 * Closable c { valid ; null}
	 * */
	@Test
	@Parameters({
		"true,true", // log = valid ; c = valid
		"true,false", // log = valid ; c = null
		"false,true", // log = null ; c = valid
		"false,false" // log = null ; c = null
	})
	public void cleanupTest(boolean validLog,boolean validClosable) throws IOException {
		boolean t = false;
		Log log = null;
		OutputStream os = null;
		try {
			if(validLog) {
				log = LogFactory.getLog(FileUtilTest.class);
			}
			if(validClosable) {
				os = new FileOutputStream("test.txt");
				os = Mockito.spy(os);
				FileUtil.cleanup(log, os);
				Mockito.verify(os).close();
			} else if (!validClosable) {
				FileUtil.cleanup(log, os);
			}
		}catch(NullPointerException e) {
			t = true;
			assertTrue(t);
		} finally {
			clearFile();
		}
	}
	
	@Test
	public void cleanupExceptionCloseTest() throws IOException {
		OutputStream os = new FileOutputStream("test.txt");
		os = Mockito.spy(os);
		Mockito.doThrow(IOException.class).when(os).close();
		
		//Try close with IOexception throwing stream FOR COVERAGE
		FileUtil.cleanup(null, os);
		Mockito.verify(os).close();
		
		FileUtil.cleanup(LogFactory.getLog(FileUtilTest.class),os);
		Mockito.verify(os,Mockito.times(2)).close();
	}
	
	
	public void clearFile() throws IOException {
		try {
			FileUtils.forceDelete(new File("test.txt"));
		} catch(Exception e) {}
		try {
			FileUtils.deleteDirectory(new File("./newFolder"));
		} catch (Exception e) {}
		
	}

}
