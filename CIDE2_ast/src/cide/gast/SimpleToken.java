package cide.gast;

public class SimpleToken implements IToken {

	private final int offset;
	private final int length;
	private final int line;

	public SimpleToken(int offset, int length, int line) {
		this.offset = offset;
		this.length = length;
		this.line = line;
	}

	public int getLength() {
		return length;
	}

	public int getOffset() {
		return offset;
	}

	public int getLine() {
		return line;
	}
}
