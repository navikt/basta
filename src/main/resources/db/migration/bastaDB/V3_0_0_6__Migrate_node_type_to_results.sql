/* Ensure nodetype with each result*/
INSERT INTO result_properties
      SELECT o.id, concat(substr(n.hostname, 1,instr(n.hostname, '.', 1,1)-1),'.nodetype'), n.nodetype
      FROM ordertable o, node n, order_node onn
      WHERE o.id = onn.order_id
      and n.id = onn.node_id
      order by o.id;

/*Undoing step 3.0.0.2*/
DELETE FROM INPUT_PROPERTIES where input_key = 'nodeType';

/*Only set nodeType where order operation is CREATE*/
INSERT INTO input_properties
  select distinct o.id, 'nodeType', n.nodetype
  from ordertable o, order_node onn, node n
  where o.id = onn.order_id
  and n.id = onn.node_id and o.orderOperation='CREATE';


INSERT INTO input_properties
  select o.id, 'nodeType', o.nodetype
  from ordertable o
  where o.orderOperation='CREATE';

