CREATE TYPE tag (
  type text,
  value text,
);

CREATE TABLE structure (
	id bigint,
	source_key varchar,
	name varchar,
	version bigint,
	attributes map<varchar, varchar>,
	rich_reference varchar,
	rich_parameters map<varchar, varchar>,
	rich_tags map<varchar, frozen<tag>> ,

	PRIMARY KEY((id, source_key), version)
);

--simple structure with version
INSERT INTO structure(id, source_key, name, version) values (1,'src-1','simple_strucuture', 1);

-- structure with attributes for a version
INSERT INTO structure(id, source_key, name, version, attributes) values 
(2,'src-2', 'structure with version', 2, {'attrib-1':'string', 'attrib-2':'integer'});

-- structure with attributes for a version
INSERT INTO structure(id, source_key, name, version, attributes) values 
(2,'src-2', 'structure with version', 20, {'attrib-1':'string', 'attrib-2':'integer', 'attrib-3':'long'});


-- rich structure with attributes for a version
INSERT INTO structure(id, source_key, name, version, attributes, rich_reference) values 
(3,'src-3', 'rich structure with version', 3, {'attrib-1':'string', 'attrib-2':'integer'}, 'rich$$$');

-- rich structure with attributes for a version and parameters
INSERT INTO structure(id, source_key, name, version, attributes, rich_reference, rich_parameters) values 
(4,'src-4', 'rich structure with version and rich parameters', 4, {'attrib-1':'string', 'attrib-2':'integer'}, 'rich$$$', {'rich-param-1':'$$$', 'rich-param-2':'eee'});

-- rich structure with attributes for a version, parameters and tags
INSERT INTO structure(id, source_key, name, version, attributes, rich_reference, rich_parameters, rich_tags) values 
(5,'src-5', 'rich structure with version, rich parameters and rich tags', 5, {'attrib-1':'string', 'attrib-2':'integer'}, 'rich$$$', {'rich-param-1':'$$$', 'rich-param-2':'eee'}, {'tag':{type:'string', value:'rich-tag'}});

-- get all unique structures
select DISTINCT id, source_key from structure;

--  id | source_key
-- ----+------------
--   4 |      src-4
--   1 |      src-1
--   3 |      src-3
--   2 |      src-2
--   5 |      src-5


-- get all versions for a given PK (id, source_key)
SELECT id, source_key, name, version from structure WHERE id=2 and source_key='src-2';

--  id | source_key | name                   | version
-- ----+------------+------------------------+---------
--   2 |      src-2 | structure with version |       2
--   2 |      src-2 | structure with version |      20

