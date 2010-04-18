package edu.wm.flat3.repository;

public interface DBConstants
{
	// EDGE_KIND TABLE
	
	static final String EDGE_KIND_TABLE 			= "EDGE_KIND";
	static final String EDGE_KIND_SQL 				= "insert into edge_kind values(?,?)";
	
	// COMPONENT TABLE

	static final String COMPONENT_TABLE 			= "COMPONENT";
	
	static final String COMPONENT_INSERT_SQL 		= "insert into component values(?,?,?,?,?,?,?,?,?)";

	static final String GET_COMPONENT_BY_ID 		= "select * from component where component_id_seq=?";
	static final String GET_COMPONENT_BY_HANDLE 	= "select * from component where handle=?";
	static final String GET_COMPONENT_BY_NAME 		= "select * from component where name=?";
	static final String GET_COMPONENT_CHILDREN 		= "select * from component a, component_edge b where a.component_id_seq = b.to_id and b.from_id=? and b.edge_kind_id=?";
	static final String GET_COMPONENT_CHILDREN_ORDERED = GET_COMPONENT_CHILDREN + " order by a.begin_line, a.begin_col";
	static final String GET_COMPONENT_PARENT 		= "select * from component a, component_edge b where a.component_id_seq = b.from_id and b.to_id=? and b.edge_kind_id=?";
	static final String GET_COMPONENTS_OF_KIND 		= "select * from component where kind_id=?";
	static final String GET_COMPONENTS 				= "select * from component";

	static final String UPDATE_COMPONENT_NAME 		= "update component set name=? where component_id_seq=?";
	static final String UPDATE_COMPONENT_SOURCE_RANGE = "update component set begin_line=?, begin_col=?, end_line=?, end_col=?, num_lines=? where component_id_seq=?";

	static final String REMOVE_COMPONENT 			= "delete from component where component_id_seq = ?";
	
	static final String GET_COMPONENTS_FOR_CONCERN 	= "select * from component a, concern_component_edge b where a.component_id_seq = b.to_id and b.from_id=? and b.edge_kind_id=?";
	
	// COMPONENT_EDGE TABLE

	static final String COMPONENT_EDGE_TABLE 		= "COMPONENT_EDGE";

	static final String COMPONENT_EDGE_SQL 			= "insert into component_edge values(?,?,?)";

	static final String REMOVE_COMPONENT_EDGE 		= "delete from component_edge where from_id = ? or to_id = ?";
	
	static final String COMPONENT_EDGE_SELECT_SQL 	= "select * from component_edge where from_id=?";
	static final String CHECK_COMPONENT_EDGE_SQL 	= "select * from component_edge where from_id=? and to_id=?";
	
	// COMPONENT_KIND TABLE
	
	static final String COMPONENT_KIND_TABLE 		= "COMPONENT_KIND";

	static final String COMPONENT_KIND_SQL 			= "insert into component_kind values(?,?)";
	
	// COMPONENT_DOMAIN TABLE

	static final String COMPONENT_DOMAIN_TABLE 		= "COMPONENT_DOMAIN";
	static final String COMPONENT_DOMAIN_INSERT 	= "insert into component_domain values(?,?,?)";;
	static final String COMPONENT_DOMAIN_SELECTALL_SQL = "select * from component_domain";
	
	// CONCERN_DOMAIN TABLE

	static final String CONCERN_DOMAIN_SQL 			= "insert into concern_domain values (?,?,?,?,?)";
	static final String UPDATE_CONCERN_DOMAIN_NAME 	= "update concern_domain set name=? where name = ?";
	
	static final String GET_CONCERN_DOMAINS 		= "select * from concern_domain";
	static final String GET_CONCERN_DOMAIN_BY_NAME 	= "select * from concern_domain where name=?";
	
	static final String REMOVE_CONCERN_DOMAIN 		= "delete from concern_domain where id = ?";
	static final String REMOVE_ALL_CONCERN_DOMAINS 	= "delete from concern_domain";
	
	// CONCERN TABLE

	static final String CONCERN_TABLE 				= "CONCERN";

	static final String CONCERN_SQL 				= "insert into concern values (?,?,?,?,?)";
	static final String UPDATE_CONCERN_NAME 		= "update concern set name=? where concern_id_seq=?";
	
	static final String GET_CONCERNS 				= "select * from concern";
	static final String GET_CONCERN_FROM_ID 		= "select * from concern where concern_id_seq=?";
	static final String GET_CONCERN_FROM_NAME 		= "select * from concern where name=?";
	static final String GET_CHILD_CONCERNS 			= "select * from concern a, concern_edge b where a.concern_id_seq=b.to_id and b.from_id=? and b.edge_kind_id=?";
	static final String GET_PARENT_CONCERN 			= "select * from concern a, concern_edge b where a.concern_id_seq=b.from_id and b.to_id=? and b.edge_kind_id=?";
	static final String GET_CONCERNS_FOR_COMPONENT 	= "select * from concern a, concern_component_edge b where a.concern_id_seq=b.from_id and b.to_id=? and b.edge_kind_id=?";
	
	static final String REMOVE_CONCERN 				= "delete from concern where concern_id_seq = ?";
	static final String REMOVE_ALL_CONCERNS 		= "delete from concern";

	// CONCERN_EDGE TABLE

	static final String CONCERN_EDGE 				= "CONCERN_EDGE";

	static final String CONCERN_EDGE_SQL 			= "insert into concern_edge values (?,?,?)";
	
	static final String REMOVE_CONCERN_EDGE 		      = "delete from concern_edge where from_id = ? or to_id = ?";
	static final String REMOVE_CONCERN_EDGE_FOR_EDGE_KIND = "delete from concern_edge where to_id=? and edge_kind_id=?";

	// CONCERN_COMPONENT_EDGE TABLE

	static final String CONCERN_COMPONENT_EDGE 		= "CONCERN_COMPONENT_EDGE";

	static final String CONCERN_COMPONENT_EDGE_SQL 	= "insert into concern_component_edge values (?,?,?)";
	
	static final String CHECK_CONCERN_COMPONENT_SQL = "select * from concern_component_edge where from_id=? and to_id=? and edge_kind_id=?";

	static final String REMOVE_CONCERN_COMPONENT_EDGE 					 = "delete from concern_component_edge where from_id=? and to_id=(select component_id_seq from component where handle=?) and edge_kind_id=?";
	static final String REMOVE_ALL_CONCERN_COMPONENT_EDGES_FOR_EDGE_KIND = "delete from concern_component_edge where edge_kind_id=?";
	static final String REMOVE_ALL_CONCERN_COMPONENT_EDGES_FOR_CONCERN 	 = "delete from concern_component_edge where from_id=?";
	static final String REMOVE_ALL_CONCERN_COMPONENT_EDGES_FOR_COMPONENT = "delete from concern_component_edge where to_id=?";

	// MISCELLANEOUS

	static final String SEQUENCE_SQL = "select next value for ";
	
	static final String DEFAULT_ROOT_CONCERN_NAME = "<ROOT>";
	static final String DEFAULT_CONCERN_DOMAIN_NAME = "<default>";
	static final String DEFAULT_CONCERN_DOMAIN_KIND = "<unknown>";
}
