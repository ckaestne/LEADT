package cide.languages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import cide.gast.IASTNode;

/**
 * Erweiterung einer LanguageExtension um die Funktionalit�t, auch einzelne Codefragmente parsen zu k�nnen
 * 
 * @author Malte Rosenthal
 *
 * @param <ParserType> Typ des interner Parsers (z.B. FJParser)
 */
public abstract class ExtendedLanguageExtension<ParserType> implements ILanguageExtension {
	
	/**
	 * Parsed das gegebene Codefragment, das an Stelle des gegebenen AST-Knotens stehen kann.
	 * D.h. z.B. auch, dass die ID des gegebenen Knotens �bernommen wird.
	 * @param node
	 * @param code
	 * @return
	 */
	public IASTNode parseCodeFragment(IASTNode node, String code) {
		IASTNode result = this.parseCodeFragment(node.getClass().getSimpleName(), code);
		if (result != null) {
			result.setId(node.getId());
			// Habe ich mal rausgenommen, weil es denke ich nicht ben�tigt wird
			//result.setParent(node.getParent(), node.getLocationInParent());
		}
		
		return result;
	}

	private IASTNode parseCodeFragment(String nonTerminalName, String code) {
		ParserType internalParser = getInternalParser(code);
		if (internalParser == null)
			return null;
		
		Method method = null;
		try {
			method = internalParser.getClass().getMethod(nonTerminalName, (Class<?>[]) null);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		if (method == null)
			return null;
		
		Object result = null;
		try {
			result = method.invoke(internalParser, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		if ((result == null) || !(result instanceof IASTNode))
			return null;
		
		return (IASTNode) result;
	}
	
	/**
	 * Gibt zur�ck, ob zur gegebenen Liste von AST-Knoten Alternativen angegeben werden k�nnen. Standardwert ist true.
	 * Subklassen k�nnen diese Methode �berschreiben, um z.B. Alternativen nur f�r eine bestimmte Menge von AST-Knoten
	 * zu erm�glichen.
	 */
	public boolean canCreateAlternatives(List<IASTNode> nodes) { return true; }
	
	protected abstract ParserType getInternalParser(String code);
}
