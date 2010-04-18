package edu.wm.flat3.repository;

public class InvalidConcernNameException
	extends Exception
{
	private static final long serialVersionUID = -4446263128669792892L;

	String name;
	String reason;

	public InvalidConcernNameException(String name, String reason)
	{
		this.name = name;
		this.reason = reason;
	}
	
	@Override
	public String getMessage()
	{
		return reason;
	}

	@Override
	public String toString()
	{
		return "Concern name '" + name + "' is invalid: " + reason;
	}
}
