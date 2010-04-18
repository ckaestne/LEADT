package edu.wm.flat3.analysis.visualization;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;

import edu.wm.flat3.analysis.FLATTTMember;

public class FLATTTSourceFile {
	ArrayList<FLATTTMember> components = new ArrayList<FLATTTMember>();
    ICompilationUnit fileUnit = null;
    
    public FLATTTSourceFile(ICompilationUnit compilationUnit) {
		fileUnit = compilationUnit;
	}
    
	public void add(FLATTTMember member) {
		components.add(member);
	}
    
}
