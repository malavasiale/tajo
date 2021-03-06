package mytests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.tajo.conf.TajoConf;
import org.apache.tajo.util.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class FileUtilTest {

	/*
	 * Test per lettura e scrittura su file
	 * Category Partition
	 * String path {valid, notExsisting, null}
	 * String textToWrite {len > 0 ; len = 0 ; null}
	 * 
	 * MUTATION 62 : Non può essere uccisa in quanto equivalente al SUT. Infatii la chiamata !fs.exists(path.getParent())
	 * 				 venendo negata non genera alcun problema in quanto non viene creata immediatamente la cartella con il relativo
	 * 				 path in cui scrivere ma viene creata dopo dalla chiamata FSDataOutputStream out = fs.create(path);
	 * 				 Producono quindi lo stesso output ma seguendo flussi diversi : STRONG MUTATION
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
			File f = new File(path);
			FileUtil.writeTextToFile(textToWrite, filepath);
			String read = FileUtil.readTextFile(f);
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
	 * Test per lettura e scrittura su stream
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
		"test.txt,0", // stream = valid ; textToWrite = null
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
	 * Test per verificare la chiusura di Closable. Ignora i fallimenti
	 * Category partition :
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
	
	/*
	 * Test per verificare la chiusura di Closable. Notifica i fallimenti
	 * Category partition :
	 * Closable c { valid ; null}
	 * */
	@Test
	@Parameters({
		"true", // c = valid
		"false" // c = null
	})
	public void cleanupAndThrowTest(boolean validClosable) throws IOException {
		boolean t = false;
		OutputStream os = null;
		try {
			if(validClosable) {
				os = new FileOutputStream("test.txt");
				os = Mockito.spy(os);
				FileUtil.cleanupAndthrowIfFailed(os);
				Mockito.verify(os).close();
			} else if (!validClosable) {
				FileUtil.cleanupAndthrowIfFailed(os);
			}
		}catch(NullPointerException e) {
			t = true;
			assertTrue(t);
		} catch(IOException e) {
			t = true;
			assertTrue(t);
		}
		finally {
			clearFile();
		}
	}
	
	/*
	 * Test per verificare la chiusura di Closable che lancia IOException. Notifica fallimenti.
	 * */
	@Test
	public void cleanupAndThrowExceptionTest() throws IOException {
		boolean t = false;
		OutputStream os = new FileOutputStream("test.txt");
		os = Mockito.spy(os);
		Mockito.doThrow(IOException.class).when(os).close();
		
		//Try close with IOexception throwing stream FOR COVERAGE
		try {
			FileUtil.cleanupAndthrowIfFailed(os);
		}catch(IOException e) {
			Mockito.verify(os).close();
			t = true;
		}
		assertTrue(t);
	}
	
	/*
	 * Test per verificare la chiusura di Closable che lancia IOException. Ignora fallimenti.
	 * */
	@Test
	public void cleanupExceptionCloseTest() throws IOException {
		OutputStream os = new FileOutputStream("test.txt");
		os = Mockito.spy(os);
		Mockito.doThrow(IOException.class).when(os).close();
		
		//Try close with IOexception throwing stream with null log FOR COVERAGE
		FileUtil.cleanup(null, os);
		Mockito.verify(os).close();
		
		//Try close with IOexception throwing stream with log without debug FOR COVERAGE
		Log l = LogFactory.getLog(FileUtilTest.class);
		FileUtil.cleanup(l,os);
		Mockito.verify(os,Mockito.times(2)).close();
		
		
		//Try close with IOexception throwing stream with log with mocked debug FOR COVERAGE
		l = Mockito.spy(l);
		Mockito.when(l.isDebugEnabled()).thenReturn(true);
		FileUtil.cleanup(l,os);
		Mockito.verify(os,Mockito.times(3)).close();
	}
	
	/*
	 * Test per verificare la corretta scrittura in linguaggio umano dei bytes
	 * Category partition
	 * long bytes : {< 0; = 0 ; 0 < bytes < 1000 ; >= 1000}
	 * boolean si : {true ; false}
	 * */
	@Test
	@Parameters({
		"-1,true,-1 B", // bytes < 0 ; si = true
		"0,true,0 B", // bytes = 0 ; si = true
		"1,false,1 B", // 0 < bytes < 1000 ; si = false
		"1000,true,1.0 kB", //bytes >=1000 ; si = true
		"1000,false,1000 B", //bytes >=1000 ; si = false  SE "si" È FALSE RIMANE SCRITTO IN BYTES FOR COVERAGE
		"1,true,1 B" // bytes > 0 ;si = true FOR COVERAGE
	})
	public void humanReadableBytesTest(long bytes, boolean si, String expected) {
		String result  = FileUtil.humanReadableByteCount(bytes, si);
		assertEquals(expected,result.replace(",", "."));
		
	}
	
	/*
	 * Utility in alcuni test per eliminazione dei file temporanei creati
	 * */
	public static void clearFile() throws IOException {
		try {
			FileUtils.forceDelete(new File("test.txt"));
		} catch(Exception e) {}
		try {
			FileUtils.deleteDirectory(new File("./newFolder"));
		} catch (Exception e) {}
		
	}

}
