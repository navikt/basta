/*Create the manytomany mapping table*/
CREATE TABLE ORDER_NODE(
   ORDER_ID NUMBER(19,0) NOT NULL,
   NODE_ID NUMBER(19,0) NOT NULL
 );

/*Populate data*/
INSERT INTO ORDER_NODE
  SELECT ORDERID,ID
  FROM NODE;

INSERT INTO ORDER_NODE
  SELECT DECOMMISSIONORDERID,ID
  FROM NODE
  WHERE DECOMMISSIONORDERID IS NOT NULL;

/*Create foreign keys*/
ALTER TABLE ORDER_NODE
  ADD CONSTRAINT FK_ORDERNODE_ORDER_ID
  FOREIGN KEY (ORDER_ID)
  REFERENCES ORDERTABLE (ID) ENABLE;

ALTER TABLE ORDER_NODE
   ADD CONSTRAINT FK_ORDERNODE_NODE_ID
   FOREIGN KEY (NODE_ID)
	 REFERENCES NODE (ID) ENABLE;

/*Create indexes*/
CREATE INDEX IX_ORDERNODE_ORDER_ID
  ON ORDER_NODE (ORDER_ID);
CREATE INDEX IX_ORDERNODE_NODE_ID
  ON ORDER_NODE (NODE_ID);

/** Add node status and type to node entity*/
ALTER TABLE NODE ADD (
  NODESTATUS VARCHAR2(255),
  NODETYPE VARCHAR2(255)
  );

UPDATE NODE
  SET NODESTATUS = 'DECOMMISSIONED'
  WHERE DECOMMISSIONORDERID IS NOT NULL;

UPDATE NODE
  SET NODESTATUS = 'ACTIVE'
  WHERE NODESTATUS IS NULL;

UPDATE NODE
SET NODETYPE = (SELECT NODETYPE FROM ORDERTABLE WHERE ORDERTABLE.ID = NODE.ORDERID);

/** Add order type to ordertable entity*/
ALTER TABLE ORDERTABLE ADD (
  ORDERTYPE VARCHAR2(255)
  );

UPDATE ORDERTABLE
  SET ORDERTYPE = 'DECOMMISSION'
  WHERE NODETYPE = 'DECOMMISSIONING';

UPDATE ORDERTABLE
  SET ORDERTYPE = 'PROVISION'
  WHERE ORDERTYPE IS NULL;

-- Fjerne decommision enum
ALTER TABLE ORDERTABLE MODIFY (
 NODETYPE NULL
 );

UPDATE ORDERTABLE
  SET NODETYPE = NULL
  WHERE NODETYPE = 'DECOMMISSIONING';


/*Drop the now obsolete columns*/
ALTER TABLE NODE DROP COLUMN ORDERID;
ALTER TABLE NODE DROP COLUMN DECOMMISSIONORDERID;