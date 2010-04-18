package edu.wm.flat3.model;

import java.util.HashMap;
import java.util.Map;

import edu.wm.flat3.repository.ConcernDomain;
import edu.wm.flat3.repository.ConcernRepository;
import edu.wm.flat3.repository.EdgeKind;

public class ConcernModelFactory
	extends ConcernListenerManager
	implements IConcernModelProvider
{
	Map<String, ConcernModel> concernModels = new HashMap<String, ConcernModel>();

	static ConcernModel defaultConcernModel = null;
	static EdgeKind defaultConcernComponentRelation = EdgeKind.RELATED_TO;

	static IConcernModelProvider activeConcernModelProvider = null;
	
	static ConcernModelFactory factorySingleton = new ConcernModelFactory();
	
	// Can't create directly
	private ConcernModelFactory()
	{ }
	
	public static ConcernModelFactory singleton()
	{
		return factorySingleton;
	}
	
	public ConcernModel getConcernModel(String concernDomain)
	{
		// True means create the database if it doesn't exist
		ConcernRepository repository = ConcernRepository.openDatabase();

		return getConcernModel(repository, concernDomain);
	}
	
	public ConcernModel getConcernModel(ConcernRepository repository, String concernDomain)
	{
		if (concernDomain != null &&
			concernDomain.equals(ConcernRepository.DEFAULT_CONCERN_DOMAIN_NAME))
		{
			concernDomain = null;
		}
		
		ConcernDomain defaultConcernDomain = null;
		if (defaultConcernModel != null)
		{
			defaultConcernDomain = defaultConcernModel.getConcernDomain();
		}
		
		if (defaultConcernDomain != null && 
			((concernDomain == null && defaultConcernDomain.isDefault()) ||
				defaultConcernDomain.getName().equals(concernDomain)))
		{
			return defaultConcernModel;
		}
		
		ConcernModel concernModel = concernModels.get(concernDomain);
		if (concernModel != null)
			return concernModel;
		
		concernModel = new ConcernModel(repository, concernDomain);
			concernModels.put(concernDomain, concernModel);

		// The first concern model created becomes the default
		if (defaultConcernModel == null)
		{
			defaultConcernModel = concernModel;
			defaultConcernComponentRelation = 
				defaultConcernModel.getDefaultLinkType();
		}
			
		return concernModel;
	}

	public ConcernModel getDefaultConcernModel()
	{
		return defaultConcernModel;
	}
	
	public void setActiveConcernModelProvider(IConcernModelProvider concernModelProvider)
	{
		assert concernModelProvider != null;
	
		if (activeConcernModelProvider == null || 
			!activeConcernModelProvider.equals(concernModelProvider))
		{
			activeConcernModelProvider = concernModelProvider;
			
			modelChanged(ConcernEvent.createActiveConcernModelChangedEvent());
		}
	}

	public void clearActiveConcernModelProvider(ConcernListenerManager concernModel)
	{
		if (activeConcernModelProvider != null && 
			activeConcernModelProvider.equals(concernModel))
		{
			activeConcernModelProvider = null;
		}
	}

	@Override
	public EdgeKind getLinkType()
	{
		if (activeConcernModelProvider != null)
			return activeConcernModelProvider.getLinkType();
		else
			return defaultConcernComponentRelation;
	}

	@Override
	public ConcernModel getModel()
	{
		if (activeConcernModelProvider != null)
			return activeConcernModelProvider.getModel();
		else
			return defaultConcernModel;
	}
}
