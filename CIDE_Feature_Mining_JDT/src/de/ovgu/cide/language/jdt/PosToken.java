package de.ovgu.cide.language.jdt;

import cide.gast.IToken;

public class PosToken implements IToken {

	private final int pos, line;

	PosToken(int pos, int line) {
		this.pos = pos;
		this.line = line;
	}

	public int getLength() {
		return 0;
	}

	public int getOffset() {
		return pos;
	}

	public int getLine() {
		return line;
	}

}
