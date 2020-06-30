package mytests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.tajo.util.FileUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/*
 * TestCase creata per uccidere la MUTATION 48.
 * */
@RunWith(PowerMockRunner.class)
@PrepareForTest(IOUtils.class)
public class FileUtilMutationKillTest {
	
	/*
	 * Concede i permessi a PowerMockito per accedere a IOUtils di hadoop
	 * */
	@BeforeClass
	public static void config() {
		UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("vyxx"));
	}

	/*
	 * Uccide la MUTATION 48 controllando che venga chiamata
	 * IOUtils.cleanup()
	 * */
	@Test
	public void test() throws IOException {
		boolean t = false;
		File f = new File("test.txt");
		PowerMockito.spy(IOUtils.class);
		
					
			Path filepath  = new Path("test.txt");

			FileUtil.writeTextToFile("prova", filepath);
			
			String read = FileUtil.readTextFile(f);
			assertEquals("prova",read);

			PowerMockito.verifyStatic();
			IOUtils.cleanup(Mockito.isNull(Log.class), Mockito.any(BufferedReader.class));
			FileUtilTest.clearFile();
	}

}
