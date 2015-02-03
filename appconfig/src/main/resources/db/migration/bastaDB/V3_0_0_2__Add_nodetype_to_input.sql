
INSERT INTO input_properties
  select o.id, 'nodetype', n.nodetype
  from ordertable o, order_node onn, node n
  where o.id = onn.order_id
  and n.id = onn.node_id