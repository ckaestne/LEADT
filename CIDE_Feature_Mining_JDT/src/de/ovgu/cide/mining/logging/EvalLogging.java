package de.ovgu.cide.mining.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;

import cide.gast.IASTNode;
import de.ovgu.cide.ASTColorChangedEvent;
import de.ovgu.cide.FileColorChangedEvent;
import de.ovgu.cide.mining.database.AbstractProgramDatabase;

public class EvalLogging {

	private EvalLogging() {
	}

	private static EvalLogging instance = new EvalLogging();

	public static EvalLogging getInstance() {
		return instance;
	}

	private BufferedWriter writer = null;

	public void init(IProject pProject, AbstractProgramDatabase aDB) {
		URI fileUri = pProject.getFile("leadt.log").getRawLocationURI();
		File logFile = new File(fileUri);
		try {
			writer = new BufferedWriter(new FileWriter(logFile, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		write("INIT;" + date() + ";" + pProject.getName());
	}

	private void write(String string) {
		try {
			writer.write(string + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String date() {
		return new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(new Date());
	}

	public void fileColorChanged(FileColorChangedEvent event) {
		for (IContainer folder : event.getAffectedFolders())
			write("FILE;" + date() + ";" + folder.getName());
	}

	public void astColorChanged(ASTColorChangedEvent event) {
		for (IASTNode node : event.getAffectedNodes())
			write("NODE;"
					+ date()
					+ ";"
					+ event.getColoredSourceFile().getResource().getName()
					+ ";"
					+ node.getId()
					+ ";"
					+ node.getStartPosition()
					+ ";"
					+ node.getLength()
					+ ";"
					+ event.getColoredSourceFile().getColorManager().getColors(
							node));

	}

	public void updateRecommendations(List<?> recommendations) {
		write("RECUP;" + date() + ";updated recommendations;"
				+ recommendations.size());
	}

	public void selectRecommendation(ICompilationUnit cu, int start, int len,
			double supportValue) {
		write("SELREC;" + date() + ";" + cu.getResource().getName() + ";"
				+ start + ";" + len + ";" + supportValue);
	}
}
