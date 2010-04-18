package edu.wm.flat3.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hsqldb.util.SqlFile;
import org.hsqldb.util.SqlToolError;

public class DBLoader implements DBConstants
{
	private static final String EDGE_KIND_FILE = "edge_kind.txt";
	private static final String COMPONENT_KIND_FILE = "component_kind.txt";
	private static final String COMPONENT_FILE = "component.txt";
	private static final String COMPONENT_DOMAIN_FILE = "component_domain.txt";
	private static final String COMPONENT_EDGE_FILE = "component_edge.txt";
	private static final String CONCERN_FILE = "concern.txt";
	private static final String CONCERN_DOMAIN_FILE = "concern_domain.txt";
	private static final String CONCERN_EDGE_FILE = "concern_edge.txt";
	private static final String CONCERN_COMPONENT_EDGE_FILE = "concern_component_edge.txt";

	private static Connection con;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{

		Class.forName("org.hsqldb.jdbcDriver");
		System.out.print("Please enter directory for data files: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String directory = reader.readLine();
		System.out.print("Please enter full path to database: ");
		String database = reader.readLine();
		reader.close();
		if (directory.equals(""))
		{
			System.out.println("Invalid directory ");
		}
		else
		{
			File dir = new File(directory);
			if (!dir.isDirectory())
			{
				System.out.println("Invalid directory " + directory);
			}
			else
			{
				createDatabase(database);
				process(directory, database);
				shutdown(database);
			}
		}

	}

	private static void process(String dir, String database)
	{
		try
		{
			con = getConnection(database);
			con.setAutoCommit(false);
			processEdgeKindTable(dir);
			processComponentKindTable(dir);
			processComponentTable(dir);
			processComponentDomainTable(dir);
			processComponentEdgeTable(dir);
			processConcernTable(dir);
			processConcernRootTable(dir);
			processConcernEdgeTable(dir);
			processConcernComponentEdgeTable(dir);
			con.commit();

			System.out.println("Done...");

		}
		catch (SQLException e)
		{
			e.printStackTrace();

			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}
	}

	/**
	 * @throws IOException
	 * @throws SqlToolError
	 * @throws SQLException
	 */
	private static void createDatabase(String database) throws IOException,
			SqlToolError, SQLException
	{
		con = getConnection(database);
		SqlFile sqlFile = new SqlFile(new File("flattt.sql"), false,
				null);
		sqlFile.execute(con, false);
		con.commit();
		con.close();
	}

	private static void processEdgeKindTable(String dir) throws SQLException,
			IOException
	{
		List<List<String>> valueList = processFile(dir, EDGE_KIND_FILE);
		insert(valueList, EDGE_KIND_SQL);
	}

	private static void processComponentKindTable(String dir)
			throws SQLException, IOException
	{
		List<List<String>> valueList = processFile(dir, COMPONENT_KIND_FILE);
		insert(valueList, COMPONENT_KIND_SQL);
	}

	private static void processComponentTable(String dir) throws SQLException,
			IOException
	{
		List<List<String>> valueList = processFile(dir, COMPONENT_FILE);
		insert(valueList, COMPONENT_INSERT_SQL);
	}

	private static void processComponentDomainTable(String dir)
			throws SQLException, IOException
	{
		List<List<String>> valueList = processFile(dir, COMPONENT_DOMAIN_FILE);
		insert(valueList, COMPONENT_DOMAIN_INSERT);
	}

	private static void processComponentEdgeTable(String dir)
			throws SQLException, IOException
	{
		List<List<String>> valueList = processFile(dir, COMPONENT_EDGE_FILE);
		insert(valueList, COMPONENT_EDGE_SQL);
	}

	private static void processConcernTable(String dir) throws SQLException,
			IOException
	{
		List<List<String>> valueList = processFile(dir, CONCERN_FILE);
		insert(valueList, CONCERN_SQL);
	}

	private static void processConcernRootTable(String dir)
			throws SQLException, IOException
	{
		List<List<String>> valueList = processFile(dir, CONCERN_DOMAIN_FILE);
		insert(valueList, CONCERN_DOMAIN_SQL);
	}

	private static void processConcernEdgeTable(String dir)
			throws SQLException, IOException
	{
		List<List<String>> valueList = processFile(dir, CONCERN_EDGE_FILE);
		insert(valueList, CONCERN_EDGE_SQL);
	}

	private static void processConcernComponentEdgeTable(String dir)
			throws SQLException, IOException
	{
		List<List<String>> valueList = processFile(dir,
				CONCERN_COMPONENT_EDGE_FILE);
		insert(valueList, CONCERN_COMPONENT_EDGE_SQL);
	}

	private static List<List<String>> processFile(String dir, String fileName)
			throws SQLException, IOException
	{
		File file = new File(dir + File.separator + fileName);
		if (!file.exists())
		{
			System.out.println("File " + dir + File.separator + fileName
					+ " does not exist, skiping...");
			return null;
		}
		List<String> data = readFile(file);
		List<List<String>> valueList = new ArrayList<List<String>>();
		for (String rowData : data)
		{
			List<String> values = getData(rowData);
			valueList.add(values);
		}
		return valueList;
	}

	private static void insert(List<List<String>> valueList, String sql)
			throws SQLException
	{
		if (valueList == null)
		{
			return;
		}
		PreparedStatement preparedStatement = con.prepareStatement(sql);
		for (List<String> values : valueList)
		{
			int i = 1;
			for (String val : values)
			{
				preparedStatement.setString(i++, val);
			}
			preparedStatement.addBatch();
		}
		int[] numRows = preparedStatement.executeBatch();

		System.out.println("Updated " + numRows.length + " rows using sql "
				+ sql);
	}

	private static List<String> getData(String rowData)
	{
		StringTokenizer tokenizer = new StringTokenizer(rowData, "\t");
		List<String> values = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
		{
			values.add(tokenizer.nextToken());
		}
		return values;
	}

	private static List<String> readFile(File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		List<String> data = new ArrayList<String>();
		String line = reader.readLine();
		while (line != null)
		{
			data.add(line);
			line = reader.readLine();
		}
		return data;
	}

	public static void shutdown(String database)
	{
		Connection con = null;
		try
		{
			// String home = System.getProperty("user.home");
			// System.out.println(home);
			con = getConnection(database);
			Statement statement = con.createStatement();
			statement.execute("SHUTDOWN");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				con.close();
			}
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	private static Connection getConnection(String database)
			throws SQLException
	{
		return DriverManager.getConnection("jdbc:hsqldb:file:" + database);
	}

}
