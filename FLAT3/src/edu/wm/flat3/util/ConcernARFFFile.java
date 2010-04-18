/**
 * 
 */
package edu.wm.flat3.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;

import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.InvalidConcernNameException;

/**
 * Parses AARF files that contain a list of concerns.
 * <P>
 * We expect one &#064;ATTRIBUTE declarations, followed by a &#064;DATA
 * declaration, followed by the instance data.
 * <P>
 * Here's a minimal example:
 * <P>
 * &#064;ATTRIBUTE concern-name string<BR>
 * &#064;DATA<BR>
 * Logging<BR>
 * <P>
 * Other than these requirements, we try to be as permissive as possible and
 * ignore stuff we don't understand.
 * 
 * @author eaddy
 */
public class ConcernARFFFile extends ARFFFile
{
	private int concernNameCol = -1;
	private static final String CONCERN_NAME_ATTR_NAME = "concern-name";

	private int concernShortNameCol = -1;
	private static final String CONCERN_SHORT_NAME_ATTR_NAME = "concern-short-name";
	
	public ConcernARFFFile(String path, 
	                       IConcernModelProvider provider, 
	                       IProgressMonitor progressMonitor, 
	                       IStatusLineManager statusLineManager)
	{
		super(path, provider, progressMonitor, statusLineManager);
	}

	@Override
	public Boolean onAttribute(List<String> fields)
	{
		if (fields.get(1).equalsIgnoreCase(CONCERN_NAME_ATTR_NAME))
		{
			if (concernNameCol != -1)
				return true; // Already assigned, ignore
			else if (!verifyAttributeDataType(fields, "string"))
				return false;
			else
				concernNameCol = currentFieldIndex;
		}
		else if (fields.get(1).equalsIgnoreCase(CONCERN_SHORT_NAME_ATTR_NAME))
		{
			if (concernShortNameCol != -1)
				return true; // Already assigned, ignore
			if (!verifyAttributeDataType(fields, "string"))
				return false;
			else
				concernShortNameCol = currentFieldIndex;
		}

		return true;
	}
	
	@Override
	public Boolean onDataInstance(List<String> cols, String raw_line)
	{
		if (concernNameCol < 0)
		{
			ProblemManager.reportError("Invalid ARFF File", 
					"Expected attribute '" + CONCERN_NAME_ATTR_NAME + "'.", 
					"File: " + path + ", Line: " + currentLine, 
					true);
			return false; // Halt further processing
		}

		assert currentFieldIndex >= 1;

		// Make sure there are enough columns

		int maxCol = Math.max(concernNameCol, concernShortNameCol);

		if (maxCol >= cols.size())
		{
			ProblemManager.reportError("Invalid ARFF Data Instance",
					"Not enough columns for data instance '" + raw_line +
					"'. Got " + cols.size() +
					", expected " + (maxCol + 1) + ". Ignoring.",
					"File: " + path + ", Line: " + currentLine, 
					true);
			return true; // Continue processing
		}
		
		// Parse Concern Name

		String concernPath = cols.get(concernNameCol);
		if (concernPath == null)
		{
			ProblemManager.reportError("Invalid ARFF Data Instance", 
					"Data instance '" + raw_line +
						"' has an empty concern-name. Ignoring.",
					"File: " + path + ", Line: " + currentLine, 
					true);
			return true; // Continue processing
		}

		String concernShortName = "";
		
		if (concernShortNameCol >= 0)
		{
			concernShortName = cols.get(concernShortNameCol);
			if (concernPath == null)
			{
				ProblemManager.reportError("Invalid ARFF Data Instance", 
						"Data instance '" + raw_line +
							"' has an empty concern-short-name. Ignoring.",
						"File: " + path + ", Line: " + currentLine, 
						true);
			}
		}

		// If concern path is a hierarchy, this will create multiple
		// concerns
		
		try
		{
			Concern concern = provider.getModel().createConcernPath(concernPath, concernShortName);
			if (concern == null)
			{
				ProblemManager.reportError("Failed to Create Concern", 
				   		"Failed to create concern '" + concernPath + "', ignoring.",
				   		"Data instance: " + raw_line + 
				   		", File: " + path + ", Line: " + currentLine + ".",
				   		true);
			}
			else
			{
				++validInstances;
				progressMonitor.subTask(concern.getDisplayName());
			}
		}
		catch (InvalidConcernNameException e)
		{
			ProblemManager.reportError("Invalid Concern Name", 
					"Concern path '" + concernPath + "' is invalid, ignoring.",
					e + "\n" +
						"Data instance: " + raw_line + "\n" + 
						"File: " + path + ", Line: " + currentLine,
					true);
		}

		return true;
	}
	
	public void save()
	{
		FileOutputStream stream;
		try
		{
			stream = new FileOutputStream(path);
		}
		catch (FileNotFoundException e)
		{
			ProblemManager.reportException(e);
			return;
		}
		
		PrintStream out = new PrintStream(stream);
		
		ConcernModel concernModel = provider.getModel();

		out.print("@RELATION \"");
		
		if (concernModel.getConcernDomain().isDefault())
		{
			out.print("Concerns");
		}
		else
		{
			out.print(concernModel.getConcernDomain().getSingularName() + " Concerns");
		}
		
		out.println("\"");
		
		out.println();
		out.println("@ATTRIBUTE concern-name string");
		out.println("@ATTRIBUTE concern-short-name string");
		out.println();
		
		out.println("@DATA");
		
		Collection<Concern> allConcernsInThisConcernDomain = 
			concernModel.getConcernDomain().getRoot().getSelfAndDescendants();
		
		if (progressMonitor != null)
			progressMonitor.beginTask("Exporting", allConcernsInThisConcernDomain.size());
		
		for(Concern concern : allConcernsInThisConcernDomain)
		{
			if (concern.isRoot())
				continue;
			
			if (progressMonitor != null)
			{
				if (progressMonitor.isCanceled())
						return;
				else
					progressMonitor.subTask(concern.getName());
			}
			
			out.println(	"\"" + concern.getQualifiedName() + "\"," +
							"\"" + concern.getShortName() + "\"");

			++this.validInstances;
		}

		out.close();
		out = null;
		
		onSaveEnd();
	}
	
	public void saveWithIndention()
	{
		FileOutputStream stream;
		try
		{
			stream = new FileOutputStream(path);
		}
		catch (FileNotFoundException e)
		{
			ProblemManager.reportException(e);
			return;
		}
		
		PrintStream out = new PrintStream(stream);
		
		outputRecursive(provider.getModel().getRoot(), out, -1);
		
		out.close();
		out = null;
		
		onSaveEnd();
	}
	
	public void outputRecursive(Concern concern, PrintStream out, int indent)
	{
		if (!concern.isRoot())
		{
			for(int i = 0; i < indent; ++i)
				out.print('\t');
			
			out.println(concern.getDisplayName());
		}
		
		for(Concern child : concern.getChildren())
		{
			outputRecursive(child, out, indent+1);
		}
	}
}
