package edu.wm.flat3.analysis;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
/**
 * A set of useful utilities for different evaluations of <code>org.eclipse.jdt.core.IMember</code> objects.   
 * @see org.eclipse.jdt.core.IMember
 * @author Maksym Petrenko
 * 
 */
public class FLATTTIMemberServices {

	/**
	 * Compares two IMember objects to find out if they are similar (i.e. have the same names, signatures etc.).
	 * @param mem1
	 * First IMember object to compare
	 * @param mem2
	 * Second IMember object to compare
	 * @return
	 * <code>true</code> if objects are similar,<br> <code>false</code> otherwise
	 */

	public static boolean areSimilar(IMember mem1, IMember mem2) {
		if ((IType.class.isInstance(mem1)) && (IType.class.isInstance(mem2))) {
			if ( ((IType)mem1).getFullyQualifiedName().compareTo( ((IType)mem2).getFullyQualifiedName() )==0) return true;
			return false;
		}
		
		else if ((IMethod.class.isInstance(mem1)) && (IMethod.class.isInstance(mem2))) {
			if (areSimilar(mem1.getDeclaringType(),mem2.getDeclaringType())) return ((IMethod) mem1).isSimilar((IMethod) mem2);
			return false;
		}
	
		else if ((IField.class.isInstance(mem1)) && (IField.class.isInstance(mem2))) {
			if (areSimilar(mem1.getDeclaringType(),mem2.getDeclaringType())) 
				if (((IField) mem1).getElementName().compareTo(((IField) mem2).getElementName())==0) return true;
			return false;
		}
		else if ((IInitializer.class.isInstance(mem1)) && (IInitializer.class.isInstance(mem2))) {
			if (areSimilar(mem1.getDeclaringType(),mem2.getDeclaringType())) 
				if (((IInitializer) mem1).getElementName().compareTo(((IInitializer) mem2).getElementName())==0) 
					if  (((IInitializer) mem1).getOccurrenceCount()==((IInitializer) mem1).getOccurrenceCount())
						return true;
			return false;
		}
		return false;
	}
	
	/**
	 * Returns top declaring type of the IMember object by recursivly walking through the types that declare the object.
	 * @param member
	 * 	IMember object to evaluate
	 * @return
	 * 	Top declaring type of this object, or object itself if is top in the nesting hierarchy 
	 */
	public static IType getTopDeclaringType(IMember member) {
		if (member==null) return null;
		if ((member.getDeclaringType()==null) && (member instanceof IType)) return (IType) member;
		
		IType type=member.getDeclaringType();
				
		while (type.getDeclaringType()!=null) {
			type=type.getDeclaringType();
		}
		
		return type;
	}

	
	
	/**
	 * Evaluates degree of nesting of a given IMember object. Done thorugh recursive calls of getParent() function of IJavaElement that this object represents.
	 * @param member
	 * 	IMember object to evaluate
	 * @return
	 * 	Degree of nesting of the object relatively to it's compilation unit
	 */
	public static int getMemberNestingLevel(IMember member) {
		int level=0;
		if (member instanceof ICompilationUnit) return level;
		if (member.getParent()==null) return level;
		
		IJavaElement element=member;
		while (element.getParent()!=null) {
			level++;
			element=element.getParent();
			if (member instanceof ICompilationUnit) return level;
			if (member.getParent()==null) return level;			
		}
		return level;
	}

	
}
