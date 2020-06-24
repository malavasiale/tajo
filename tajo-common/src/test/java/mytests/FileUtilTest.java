package mytests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.hadoop.fs.Path;
import org.apache.tajo.util.FileUtil;
import org.junit.Test;

public class FileUtilTest {

	@Test
	public void writeAndReadTest() throws IOException {
		Path filepath  = new Path("test.txt");
		FileUtil.writeTextToFile("prova", filepath);
		String read = FileUtil.readTextFile(new File("test.txt"));
		assertEquals("prova",read);
	}

}
