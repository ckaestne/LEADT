package edu.wm.flat3.analysis.mutt;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Internally, methods are saved in the format of class.method. 
 * The symbol : is replaced with . is parsing DOT files. To work out with GraphViz.
 * @author Dapeng Liu
 *
 */
public class TraceGraph
	{
		public Hashtable<String, CallNode> callNodes = new Hashtable<String, CallNode> ();
		public ArrayList<CallNode> visitedNodes = new ArrayList<CallNode> ();
//		private HashSet<String> visitedEdges = new HashSet<String> ();
		private ArrayList<String> visitedEdges = new ArrayList<String> ();
		public CallNode focusNode = null;
		
		/**
		 * it is used to create the trace graph
		 * @param strNodeFrom
		 * @param strNodeTo
		 */
		public void addOneCall ( String strNodeFrom, String strNodeTo )
		{
			CallNode callNodeFrom = getNodeFromGraph ( strNodeFrom );
			CallNode callNodeTo = getNodeFromGraph ( strNodeTo );
			
			callNodeFrom.addOneToNode( strNodeTo, callNodeTo );
			callNodeTo.addOneFromNode( strNodeFrom, callNodeFrom );
		}

		/**
		 * If the node is already explored, get it from the Hashtable callNodes;
		 * otherwise, create a new one.
		 * @param nodeString
		 * @return
		 */
		private CallNode getNodeFromGraph ( String nodeString )
		{
			CallNode callNode = callNodes.get( nodeString );
			if ( callNode == null )
			{
				callNode = new CallNode ( nodeString );
				callNodes.put( nodeString, callNode );
			}
			return callNode;
		}
		
		/**
		 * Read a DOT file, skip the first part, read all strNodeFrom -> strNodeTo,
		 * and covert the content into a TraceGraph
		 * @param fileName
		 */
		public boolean parseDOT ( String fileName )
		{
			try
			{
				BufferedReader br = new BufferedReader ( new FileReader( fileName ) );
				String line;
				for ( ; ; )
				{
					line = br.readLine();
					if ( line!=null && line.indexOf(" -> ")>-1 )
						break;
					// handle the case where it has no "->" lines
					if ( line == null) return false;
				}

				callNodes.clear();
				visitedNodes.clear();
				visitedEdges.clear();
				focusNode = null;
				
				while ( line!=null && line.indexOf(" -> ")>-1 )
				{
					String strNodeFrom = line.substring ( 0, line.indexOf(" -> ") ).replace( ':', '.' );
					String strNodeTo = line.substring( line.indexOf(" -> ") + 4 ).replace( ':', '.' );
					addOneCall( strNodeFrom, strNodeTo );
					line = br.readLine();
				}
				br.close();
			} catch ( Exception e )
			{
				System.out.println( fileName );
				e.printStackTrace();
			}
			printStats();
			
			return true;
		}
		
		public static final int CHANGE_SOURCE_NEWSTART = 1;
		public static final int CHANGE_SOURCE_FROM = 2;
		public static final int CHANGE_SOURCE_TO = 3;
		
		/**
		 * maintain visited methods and edges
		 * @param newFocusNode
		 * @param source from/caller or to/callee
		 * @return
		 */
		public boolean focusNodeChange ( CallNode newFocusNode, int source )
		{
			if ( newFocusNode.equals( focusNode) )
				return false;
			
			if ( ! visitedNodes.contains( newFocusNode ) )
			{
				visitedNodes.add( 0, newFocusNode );
				newFocusNode.visited = true;
			}
			switch ( source ) 
			{
			case CHANGE_SOURCE_FROM:
				addOneVisitedEdge( newFocusNode, focusNode );
				break;
			case CHANGE_SOURCE_TO:
				addOneVisitedEdge( focusNode, newFocusNode );
			}
			focusNode = newFocusNode;
			return true;
		}
		
		/**
		 * do not add duplicated edges
		 * @param fromNode
		 * @param toNode
		 */
		private void addOneVisitedEdge ( CallNode fromNode, CallNode toNode )
		{
			String edge = "\"" + fromNode.toString().replace(':','.') + "\" -> \"" + toNode.toString().replace(':','.') + "\"";
			for ( int i=0; i<visitedEdges.size(); i++ )
				if ( visitedEdges.get(i).equals( edge ) )
					return;
			visitedEdges.add( edge );
		}
		
		/**
		 * clear all visit history
		 *
		 */
		public void clearVisitHisotry ()
		{
			while ( visitedNodes.size() > 0 )
			{
				visitedNodes.remove( 0 ).visited = false;
			}
			//visitedNodes.clear();
			visitedEdges.clear();
			focusNode = null;
		}
		
		/**
		 * write the visited call graph to a DOT file.
		 * @param filename
		 */
		public void writeDOTFile ( String filename )
		{
			try
			{
				BufferedWriter bw = new BufferedWriter ( new FileWriter ( filename ) );
				bw.write( "digraph C {" );
				bw.newLine();
				Iterator<String> ite = visitedEdges.iterator();
				while ( ite.hasNext() )
				{
					bw.write( ite.next() );
					bw.newLine();
				}
				bw.write( "}" );
				bw.close();
			} catch ( IOException e )
			{
				e.printStackTrace();
			}
			writeFaninFanoutInfo ( filename + ".xls" );
		}
		
		/**
		 * write fanin and fanout info for 1. the whole graph; and 2. the visited nodes
		 * @param filename
		 */
		public void writeFaninFanoutInfo ( String filename )
		{
			try
			{
				BufferedWriter bw = new BufferedWriter ( new FileWriter ( filename ) );
				
				//write fanin and fanout info of all nodes
				Enumeration<CallNode> enm = callNodes.elements();
				while ( enm.hasMoreElements() )
				{
					CallNode cnode = enm.nextElement();
					bw.write( cnode + "\t" + cnode.fromNodes.size() + "\t" + /*cnode.fromCallingNumberTotal() + "\t" +*/ 
							cnode.toNodes.size() /*+ "\t" + cnode.toCallingNumberTotal()*/ );
					bw.newLine();
				}
				
				bw.newLine();

				//write fanin and fanout info of visited nodes
				for ( int i=0; i<visitedNodes.size();i ++ )
				{
					CallNode cnode = visitedNodes.get(i);
					bw.write( cnode + "\t" + cnode.fromNodes.size() + "\t" + /*cnode.fromCallingNumberTotal() + "\t" + */
							cnode.toNodes.size() /*+ "\t" + cnode.toCallingNumberTotal()*/ );
					bw.newLine();
				}
				bw.close();
			} catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
		/**
		 * Print out the TraceGraph in DOT format.
		 * If the fileName is null, or the file cannot be created, print to System.out.
		 * @param fileName
		 */
		public void printOut ( String fileName )
		{
			//
			BufferedWriter bw;
			try
			{
				if ( fileName != null )
					bw = new BufferedWriter ( new FileWriter ( fileName ) );
				else
					bw = new BufferedWriter ( new PrintWriter ( System.out ) );
			} catch ( IOException e )
			{
				e.printStackTrace();
				//System.exit(0);
				bw = new BufferedWriter ( new PrintWriter ( System.out ) );
			}
			try
			{
				Enumeration<CallNode> enm = callNodes.elements();
				while ( enm.hasMoreElements() )
				{
					CallNode callNode = enm.nextElement();
					Enumeration<CallNode> enmFrom = callNode.fromNodes.elements();
					while ( enmFrom.hasMoreElements() )
					{
						CallNode fromCallNode = enmFrom.nextElement();
						bw.write ( fromCallNode + " -> " + callNode);
						bw.newLine();
					}
				}
					bw.close();
			} catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
		
		/**
		 * Print info about [min, max] and average calling and called methods
		 * @param args
		 */
		public void printStats ()
		{
			int noCallNodes = callNodes.size();
			int noCallEdges = 0;
			int iFMin = 10000, iFMax=0, iTMin=1000, iTMax=0, tmp;
			Enumeration<CallNode> enm = callNodes.elements();
			while ( enm.hasMoreElements() )
			{
				CallNode callNode = enm.nextElement();
				noCallEdges += callNode.toNodes.size();
				tmp = callNode.fromNodes.size();
				iFMin = ( iFMin < tmp ) ? iFMin : tmp;
				iFMax = ( iFMax > tmp ) ? iFMax : tmp;
				tmp = callNode.toNodes.size();
				iTMin = ( iTMin < tmp ) ? iTMin : tmp;
				iTMax = ( iTMax > tmp ) ? iTMax : tmp;
			}
			//for edges of a complete graph, #from==#to
			System.out.println( "There are " + noCallNodes + " methods and " + noCallEdges + " call relationships and [min, max] (from, to) [" +
					iFMin+", "+iFMax+"] ["+iTMin+", "+iTMax+"] and average call/method is " + ((float)noCallEdges)/noCallNodes );
		}
		
		/**
		 * For visited methods, print info about [min, max] and average calling and called methods
		 * @param args
		 */
		public void printStatsVisited ()
		{
			int noCallNodes = visitedNodes.size();
			int noCallEdgesFrom = 0;
			int noCallEdgesTo = 0;
			int iFMin = 10000, iFMax=0, iTMin=1000, iTMax=0, tmp;
			for ( int i=0; i<noCallNodes; i++ )
			{
				CallNode callNode = visitedNodes.get( i );
				tmp = callNode.fromNodes.size();
				noCallEdgesFrom += tmp; 
				iFMin = ( iFMin < tmp ) ? iFMin : tmp;
				iFMax = ( iFMax > tmp ) ? iFMax : tmp;
				tmp = callNode.toNodes.size();
				noCallEdgesTo += tmp;
				iTMin = ( iTMin < tmp ) ? iTMin : tmp;
				iTMax = ( iTMax > tmp ) ? iTMax : tmp;
			}
			System.out.println( "There are " + noCallNodes + " methods and [min, max] (from, to) [" +
					iFMin+", "+iFMax+"] ["+iTMin+", "+iTMax+"] and average call/method is from:" + 
					((float)noCallEdgesFrom)/noCallNodes + " and to:" + ((float)noCallEdgesTo)/noCallNodes);
		}
		
		public static void main ( String[] args )
		{
			TraceGraph traceGraph = new TraceGraph ();
			traceGraph.parseDOT( "M:\\Key Assitant\\dcg.main.0.dot" );
			traceGraph.printOut( "M:\\Key Assitant\\1.txt" );
		}

	}
