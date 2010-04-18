package edu.wm.flat3.analysis.mutt;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 
 */

/**
 * @author Dapeng Liu
 *
 */
public class CallNode implements Comparable
	{
		public Hashtable<String, CallNode> fromNodes = new Hashtable<String, CallNode> ();
		public Hashtable<String, CallNode> toNodes = new Hashtable<String, CallNode> ();
		public Hashtable<String, Integer> fromNodesCallingNumber = new Hashtable<String, Integer> ();
		public Hashtable<String, Integer> toNodesCallingNumber = new Hashtable<String, Integer> ();
		
		public boolean visited = false;
		
		String nodeName;

		public CallNode ( String name )
		{
			nodeName = name;
		}

		@Override
		public String toString ( )
		{
			return nodeName;
		}

		public int compareTo ( Object arg0 )
		{
			return nodeName.compareTo( ((CallNode) arg0 ).nodeName );
		}
		
		/**
		 * suppose all query tokens are UPPERCASE.
		 * @param queryTokens
		 * @return
		 */
		public boolean ifPassFilter ( String[] queryTokens ) 
		{
			for ( int i=0; i<queryTokens.length; i++ )
				if ( ! nodeName.toUpperCase().contains(queryTokens[i]) )
					return false;
			return true;
		}
		
		/**
		 * call addOneNode to really setup internal data
		 */
		public void addOneFromNode ( String strNode, CallNode callNode )
		{
			addOneNode ( strNode, callNode, fromNodes, fromNodesCallingNumber );
		}
		
		/**
		 * call addOneNode to really setup internal data
		 */
		public void addOneToNode ( String strNode, CallNode callNode )
		{
			addOneNode ( strNode, callNode, toNodes, toNodesCallingNumber );
		}

		/**
		 * modify both the node and the number hashtable 
		 */
		public void addOneNode ( String strNode, CallNode callNode, Hashtable<String, CallNode> nodeHash, Hashtable<String, Integer> numberHash )
		{
			CallNode node = nodeHash.get( strNode );
			if ( node == null )
			{
				numberHash.put( strNode, new Integer(1) );
				nodeHash.put( strNode, callNode );
			} else
			{
				Integer callingNumer = numberHash.get( strNode );
				callingNumer = new Integer ( callingNumer.intValue() + 1 );
				numberHash.put( strNode, callingNumer );
			}
		}
		
		public int fromCallingNumberTotal ()
		{
			return calculateTotal ( fromNodesCallingNumber );
		}

		public int toCallingNumberTotal ()
		{
			return calculateTotal ( toNodesCallingNumber );
		}

		private int calculateTotal ( Hashtable<String, Integer> hashtable)
		{
			Enumeration<Integer> enm = hashtable.elements();
			int total = 0;
			while ( enm.hasMoreElements() )
			{
				total += enm.nextElement().intValue();
			}
			return total;
		}
	}
