package edu.wm.flat3.repository;

import java.util.ArrayList;
import java.util.List;

public class ComponentDomain
{
	private Integer id;
	private String name;
	private String sourceLanguage;

	public ComponentDomain(String name, String sourceLanguage)
	{
		this.name = name;
		this.sourceLanguage = sourceLanguage;
	}
	
	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getSource_language()
	{
		return sourceLanguage;
	}

	public void setSource_language(String source_language)
	{
		this.sourceLanguage = source_language;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<Object> getValuesAsList()
	{
		List<Object> list = new ArrayList<Object>();
		list.add(id);
		list.add(sourceLanguage);
		list.add(name);
		return list;
	}
}
