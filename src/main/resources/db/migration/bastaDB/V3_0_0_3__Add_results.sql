ALTER TABLE ORDERTABLE
RENAME COLUMN REQUESTXML TO EXTERNALREQUEST;

/* Input properties generation*/
CREATE TABLE RESULT_PROPERTIES (
   ORDER_ID NUMBER(19,0) NOT NULL,
   RESULT_KEY VARCHAR2(255 CHAR) NOT NULL,
   RESULT_VALUE VARCHAR2(4000 CHAR)
);

CREATE INDEX IX_RESULT_PROPERTIES
ON RESULT_PROPERTIES (ORDER_ID);

INSERT INTO result_properties
      SELECT o.id, concat(substr(n.hostname, 1,instr(n.hostname, '.', 1,1)-1),'.hostname'), n.hostname
      FROM ordertable o, node n, order_node onn
      WHERE o.id = onn.order_id
      and n.id = onn.node_id
      order by o.id;
      
      
      
 INSERT INTO result_properties     
      SELECT o.id, concat(substr(n.hostname, 1,instr(n.hostname, '.', 1,1)-1),'.nodestatus'), n.nodestatus
      FROM ordertable o, node n, order_node onn
      WHERE o.id = onn.order_id
      and n.id = onn.node_id
      order by o.id;
      