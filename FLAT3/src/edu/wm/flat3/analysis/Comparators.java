package edu.wm.flat3.analysis;

import java.util.Comparator;
//import org.severe.jripples.eig.JRipplesEIGNode;


public class Comparators {
	private Comparator nameComparator; 
	private Comparator markComparator;
	private Comparator probabilityComparator;
	private Comparator fullyQualifiedNameComparator;
	private Comparator memberComparator;
	
	public Comparators() {
		initCopmarators();
	}

	
	public Comparator getNameComparator() { 
		return nameComparator;
	}
	
	public Comparator getMarkComparator() {
		return markComparator;
	}
	
	public Comparator getProbabilityComparator() {
		return probabilityComparator;
	}
	public Comparator getFullyQualifiedNameComparator() {
		return fullyQualifiedNameComparator;
	}
	public Comparator getMemberComparator() {
		return memberComparator;
	}
	private void initCopmarators() {
		nameComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if ((((FLATTTMember) o1).getShortName()==null) || (((FLATTTMember) o2).getShortName() ==null)) return 0;
				return ((FLATTTMember) o1).getShortName().compareTo(

						((FLATTTMember) o2).getShortName());
			}

		};

		markComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if ((((FLATTTMember) o1).getMark()==null) || (((FLATTTMember) o2).getMark() ==null)) return 0;
				
				return ((FLATTTMember) o1).getMark().compareTo(
						((FLATTTMember) o2).getMark());
			}

		};

		probabilityComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				
				if ((((FLATTTMember) o1).getProbability()==null) && (((FLATTTMember) o2).getProbability()==null)) return 0;
				if ((((FLATTTMember) o1).getProbability()==null) && (((FLATTTMember) o2).getProbability()!=null)) return -1;
				if ((((FLATTTMember) o1).getProbability()!=null) && (((FLATTTMember) o2).getProbability()==null)) return 1;
				if (((((FLATTTMember) o1).getProbability()+"").compareTo("")==0) && ((((FLATTTMember) o2).getProbability()+"").compareTo("")==0)) return 0;
				if (((((FLATTTMember) o1).getProbability()+"").compareTo("")==0) && ((((FLATTTMember) o2).getProbability()+"").compareTo("")!=0)) return -1;
				if (((((FLATTTMember) o1).getProbability()+"").compareTo("")!=0) && ((((FLATTTMember) o2).getProbability()+"").compareTo("")==0)) return 1;
				
				return ((Float.valueOf(((FLATTTMember) o1)
						.getProbability()+"")).compareTo((Float
						.valueOf(((FLATTTMember) o2).getProbability()+""))));
			}
		};

		fullyQualifiedNameComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if ((((FLATTTMember) o1).getFullName()==null) || (((FLATTTMember) o2).getFullName() ==null)) return 0;
				return ((FLATTTMember) o1).getFullName().compareTo(
						((FLATTTMember) o2).getFullName());
			}

		};
		
		memberComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if (((FLATTTMember) o1).getNodeIMember().getElementType()!=((FLATTTMember) o2).getNodeIMember().getElementType()) return Integer.valueOf(((FLATTTMember) o1).getNodeIMember().getElementType()).compareTo(Integer.valueOf(((FLATTTMember) o2).getNodeIMember().getElementType()));
					else return ((FLATTTMember) o1).getNodeIMember().getElementName().compareTo(((FLATTTMember) o2).getNodeIMember().getElementName());
				
			}

		};
		
	}
}
