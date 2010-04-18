package dapeng;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.tools.example.trace.Trace;

class MethodsRecord implements Comparable {
	String className;
	String methodName;
	
	public int compareTo ( Object arg0 ) {
		if ( ! ( arg0 instanceof MethodsRecord ) )
			throw new ClassCastException();
		MethodsRecord mr = ( MethodsRecord ) arg0;
		if ( className.equals( mr.className ) )
			return methodName.compareTo( mr.methodName );
		else
			return className.compareTo( mr.className );
	}
	
	public String toString () {
		return className + "\t" + methodName;
	}
	
	public boolean equals ( Object obj ) {
		if ( ! ( obj instanceof MethodsRecord ) )
			return false;
		MethodsRecord mr = ( MethodsRecord ) obj;
		if ( className.equals(mr.className) && methodName.equals(mr.methodName) )
			return true;
		return false;
	}
}

public class DotCreator {

	private BufferedWriter	writer				= null;
	private BufferedWriter	methodsWriter		= null;

	private Stack<String>	methodStack			= new Stack<String> ( );

	private HashSet<String>	existingLines		= new HashSet<String> ( );

	//control whether record on method-level granularity
	//two granularities produce 2.5 times difference in sizes for slice.thread.id.dot files
	private boolean			ifMehtodDetail		= false;

	//only record the first event slice
	private boolean			ifStopAfterFirst	= false;

	//whether record package name, or class name only
	private boolean			ifHasPackageName	= false;
	
	public String dotdir = ""; // Directory to save dot files to
	
	//for dynamic call graph
	private Hashtable<String, HashSet<String>>	cgClassDetails = new Hashtable<String, HashSet<String>>();
	private HashSet<String> cgExistingEdges = new HashSet<String>();
	//in dynamic call graph, the format is "class":method 
	private Stack<String>	cgClassMethodStack			= new Stack<String> ( );
	private Vector<MethodsRecord> allMethods = new Vector<MethodsRecord> (); 
	private boolean ifNoSelfCycle = false;
	private String threadLogName = null;

	public DotCreator() {
		//if keep tracing (disable output) from the begining, we don't need the following two statements
		//without '___', the tracing can sotp after the slice with exception
		//methodStack.push( "___" );
		try {
			writer = new BufferedWriter ( new FileWriter ( new File ( "slice.dot" ) ) );

			writer.write ( "digraph C {" );
			writer.newLine ( );
		} catch ( IOException e ) {
			e.printStackTrace ( );
			writer = null;
		}
	}

	public DotCreator( String threadLogName, String dotdir ) {
		try {
			this.dotdir = dotdir;
			this.threadLogName = threadLogName;
			writer = new BufferedWriter ( new FileWriter ( new File ( "slice."+ threadLogName+".dot" ) ) );
			writer.write ( "digraph C {" );
			writer.newLine ( );
			
			methodsWriter = new BufferedWriter ( new FileWriter ( new File ( "allMethods."+ threadLogName+".txt" ) ) );
		} catch ( IOException e ) {
			e.printStackTrace ( );
			writer = null;
		}
	}

	public void methodEntry ( MethodEntryEvent event ) {
		
		//allMethods.add( event.method().declaringType().name() + "\t" + event.method().name() );
		MethodsRecord mr = new MethodsRecord ();
		mr.className = event.method().declaringType().name();
		mr.methodName = event.method().name();
		if ( ! allMethods.contains( mr ))
			allMethods.add( mr );
		
		//if not tracing, don't care........this is more reasonable  
		if ( ! Trace.isTracing ) return;

		cgMethodEntry ( event );
		
		String callee;

		callee = event.method ( ).declaringType ( ).name ( );

		//remove package from the full class name
		if ( !ifHasPackageName && callee.lastIndexOf ( '.' ) >= 0 )
			callee = callee.substring ( callee.lastIndexOf ( '.' ) + 1 );

		// append method if wanted
		if ( ifMehtodDetail )
			callee = callee + "." + event.method ( ).name ( );

		//callee = polishToken ( callee );
		callee = "\"" + callee + "\"";
		if ( methodStack.empty ( ) ) {
			methodStack.push ( callee );
			return;
		}
		
		//if no tracing, don't keep track; just keep a runtime stack
		//this was here because Eclipse may have methodExit right after Trace.isTracing...
		//due to multi-threading...this is not my bug.
		//if ( ! Trace.isTracing ) return;
		
		String outLine = methodStack.peek ( ) + " -> " + callee;
		methodStack.push ( callee );
		if ( !existingLines.contains ( outLine ) ) {
			writeln ( outLine );
			existingLines.add ( outLine );
		}
	}

	public void methodExit ( MethodExitEvent event ) {
		
		if ( ! Trace.isTracing ) return;
		
		if( !cgClassMethodStack.empty() )
		{
			String className = event.method ( ).declaringType ( ).name ( );
			//remove package from the full class name
			if ( !ifHasPackageName && className.lastIndexOf ( '.' ) >= 0 )
				className = className.substring ( className.lastIndexOf ( '.' ) + 1 );
			String methodName = event.method( ).name( );
			className = polishToken ( className );
			methodName = polishToken ( methodName );
			String existedMethod = className + ':' + methodName;
			
			String cgTop = cgClassMethodStack.pop();
			
			if ( ! cgTop.equals(existedMethod) )
				System.out.println( "cgClassMethodStack:" + cgClassMethodStack.size() + " stack<" + cgTop + "> existed<" + existedMethod + ">" );
		} else {
			System.out.println( "-1 cgClassMethodStack" );
		}
		
		if ( ! methodStack.empty() ) {
			
			String existedMethod;
			existedMethod = event.method ( ).declaringType ( ).name ( );
			//remove package from the full class name
			if ( !ifHasPackageName && existedMethod.lastIndexOf ( '.' ) >= 0 )
				existedMethod = existedMethod.substring ( existedMethod.lastIndexOf ( '.' ) + 1 );
			// append method if wanted
			if ( ifMehtodDetail )
				existedMethod = existedMethod + "." + event.method ( ).name ( );
			existedMethod = "\"" + existedMethod + "\"";

			String msTop = methodStack.pop ( );
			
			if ( ! msTop.equals( existedMethod ) )
				System.out.println( "methodStack:" + methodStack.size() + " stack<" + msTop + "> existed<" + existedMethod + ">" );
			
			// only try to stop tracing while doing it......otherwise, this section may be invoked too many times
			if ( ifStopAfterFirst && Trace.isTracing && methodStack.empty ( ) ) {
				Trace.isTracing = false;
				Trace.jbIsTracing.setText ( "start tracing" );
				Trace.jbIsTracing.repaint ( );
			}
		} else {
			System.out.println( "-1 methodStack" );
		}
	}

	//this method is dedicated to collecting dynamic call information 
	public void cgMethodEntry ( MethodEntryEvent event ) {
		//
		String className = event.method ( ).declaringType ( ).name ( );
		//remove package from the full class name
		if ( !ifHasPackageName && className.lastIndexOf ( '.' ) >= 0 )
			className = className.substring ( className.lastIndexOf ( '.' ) + 1 );
		String methodName = event.method( ).name( );
		
		//className = '\"' + polishToken ( className ) + '\"';
		//methodName = '\"' + polishToken ( methodName ) + '\"';
		className = polishToken ( className );
		methodName = polishToken ( methodName );
		
		//fill cgClassDetails
		HashSet<String> methods = cgClassDetails.get( className );
		if ( methods == null ) 
			methods = new HashSet<String>();
		methods.add ( methodName );
		cgClassDetails.put ( className, methods );
		
		//fill cgExistingEdges
		String cgCallee = className + ':' + methodName;
		if ( cgClassMethodStack.empty() ) {
			cgClassMethodStack.push( cgCallee );
			return;
		}
		String cgCaller = cgClassMethodStack.peek();
		cgClassMethodStack.push ( cgCallee );
		if ( ifNoSelfCycle && cgCaller.substring(0,cgCaller.indexOf(':')).equals( cgCallee.substring(0,cgCallee.indexOf(':')) ) )
			return;
		String edge = cgCaller + " -> " + cgCallee;
		cgExistingEdges.add ( edge );
	}
	
	public void cgFlushOut ( ) {
		BufferedWriter writer;
		try {
			//BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File (  (threadLogName==null) ? "dcg.dot" : ("dcg."+threadLogName+".dot" ) ) ) );
			// FLATTT: Let's write these files to our plugin data directory... 
			if ( threadLogName==null )
				writer = new BufferedWriter ( new FileWriter ( new File (  dotdir+ "dcg.dot" ) ) );
			else {
				writer = new BufferedWriter ( new FileWriter ( new File (  dotdir+"dcg."+threadLogName+".dot" ) ) );
				
			}
			//header
			writer.write ( "digraph g {" );
			writer.newLine ( );
			writer.write ( "graph [ rankdir = \"LR\" ];" );
			writer.newLine ( );
			writer.write ( "node [ fontsize = \"16\" shape = \"record\" ];" );
			writer.newLine ( );
			//class nodes
			for ( Enumeration<String> enu=cgClassDetails.keys(); enu.hasMoreElements(); )
				cgOutputNodes ( writer, enu.nextElement() );
			//call edges
			for ( Iterator<String> ite=cgExistingEdges.iterator(); ite.hasNext(); ) {
				writer.write ( ite.next() );
				writer.newLine();
			}
			//footer
			writer.write ( "}" );
			writer.newLine();
			writer.flush();
			writer.close();

		} catch ( IOException e ) {
			e.printStackTrace ( );
			writer = null;
		}

	}
	
	private void cgOutputNodes ( BufferedWriter writer, String className ) throws IOException {
		writer.write ( className + " [" );
		writer.newLine();
		writer.write ( "label = \"");
		HashSet<String> methods = cgClassDetails.get ( className );
		//className = className.substring( 1, className.length()-1 );
		writer.write ( "<" + className + "> " + className );
		for ( Iterator<String> ite = methods.iterator(); ite.hasNext(); ) {
			String method = ite.next();
			writer.write ( "|<" + method + "> " + method );
		}
		writer.write ( "\"");
		writer.newLine();
		writer.write ( "];" );
		writer.newLine();
	}
	
	public void write ( String token ) {
		if ( !Trace.isTracing )
			return;
		try {
			writer.write ( token );
		} catch ( IOException e ) {
			e.printStackTrace ( );
		}
	}

	//this is called only when Trace.isTracing
	public void writeln ( String token ) {
		try {
			writer.write ( token );
			writer.newLine ( );
		} catch ( IOException e ) {
			e.printStackTrace ( );
		}
	}

	/**
	 * for dcg.dot, which is used to create UML diagrams,
	 * we have to remove special symbols from the tokens
	 * 
	 * for simple call graph, there is no need to do this
	 * but method names are added '\"' at two ends. 
	 * 
	 */
	private String polishToken ( String token ) {
		//how about we keep all tokens unchanged?
		//for internal class, induce it into its parent class
		
		//for slice.dot, we should comment this block
		{
		if ( token.indexOf ( '$' ) >= 0 )
			token = token.substring ( 0, token.indexOf ( '$' ) );
		token = token.replace ( '.', '_' );
		token = token.replace ( '$', '_' );
		token = token.replace ( '<', '_' );
		token = token.replace ( '>', '_' );
		if ( token.compareToIgnoreCase("graph")==0 )
			token = "_" + token;
		return token;
		}
	}

	public void close ( ) {
		if ( writer != null ) {
			try {
				writer.write ( "}" );
				writer.flush ( );
				writer.close ( );
			} catch ( IOException e ) {
				writer = null;
			}
		}
		cgFlushOut ( );
		outputAllMethods();
	}
	
	private void outputAllMethods () {
		Object[] methods = allMethods.toArray();
		Arrays.sort ( methods );
		if ( methodsWriter != null ) {
			try {
				for ( int i=0; i<methods.length; i++ ) {
					methodsWriter.write( ( (MethodsRecord) methods[i]).toString() );
					methodsWriter.newLine();
				}
				methodsWriter.flush();
				methodsWriter.close();
			} catch ( IOException e ) {
				methodsWriter = null;
			}
		}
	}

}


