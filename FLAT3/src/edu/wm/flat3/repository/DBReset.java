package edu.wm.flat3.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.hsqldb.util.SqlFile;

import java.sql.Connection;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.util.ProblemManager;

/**
 * Class to recreate the database.
 * 
 * @author vgarg
 * 
 */
public class DBReset
{

	/**
	 * This method uses the SQLFile class provided by HSQLDB to recrate the
	 * database. Since we include the concerntagger.sql file in the plugin, we
	 * simply copy the file from the plugin folder to temporary plugin workspace
	 * and execute the reset.
	 * 
	 * @param hsqldb
	 */
	public static void resetDatabase(ConcernRepository hsqldb)
	{
		try
		{
			// We have to copy the SQL file since SqlFile() requires a File
			
			String metadataPath = FLATTT.singleton().getStateLocation()
					.toOSString();
			File tempFile = copySQLFile(metadataPath);
			SqlFile sqlFile = new SqlFile(tempFile, false, null);
			Connection con = hsqldb.getConnection();
			sqlFile.execute(con, false);
			con.commit();

			// close the connection so that a new connection is requested
			// the next time a database call is made.
			con.close();
		}
		catch (Exception e)
		{
			ProblemManager.reportException(e);
		}

	}

	/**
	 * Gets the sql file from the plugin path. Creates a new one by copying from
	 * the plugin jar file if there isn't already one.
	 * 
	 * @param metadataPath
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static File copySQLFile(String metadataPath) throws IOException,
			FileNotFoundException
	{
		File tempFile = new File(metadataPath + File.separator
				+ "flattt.sql");

		InputStream istream = FileLocator.openStream(FLATTT
				.singleton().getBundle(),
				new Path("sql/flattt.sql"), false);

		FileOutputStream ostream = new FileOutputStream(tempFile);
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = istream.read(b, 0, b.length)) != -1)
		{
			ostream.write(b, 0, len);
		}
		
		ostream.flush();
		istream.close();
		ostream.close();

		return tempFile;
	}
}
