package mytests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	 * String path {valid, notExsisting, invalid, null}
	 * String textToWrite {len > 0 ; len = 0 ; null}
	 * */
	@Test
	@Parameters({
		"test.txt,prova", // path = valid ; textToWrite > 0
		"test.txt,", // path = valid ; textToWrite = 0
		"./newFolder/test.txt,prova" // path = noExisting ; textToWrite > 0
	})
	public void writeAndReadTest(String path,String textToWrite) throws IOException {
		Path filepath  = new Path(path);
		FileUtil.writeTextToFile(textToWrite, filepath);
		String read = FileUtil.readTextFile(new File(path));
		assertEquals(textToWrite,read);
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
