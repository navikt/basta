ALTER TABLE NODE DROP COLUMN FASITUPDATED;

ALTER TABLE NODE ADD (
	FASITURL			VARCHAR(255),
	DECOMMISSIONORDERID NUMBER(19,0)	
);

ALTER TABLE NODE ADD CONSTRAINT FK_NODE_ORDER_FK FOREIGN KEY (ORDERID)
REFERENCES ORDERTABLE (ID) ENABLE;

ALTER TABLE NODE ADD CONSTRAINT FK_NODE_DECOMMISSION_ORDER_FK FOREIGN KEY (DECOMMISSIONORDERID)
REFERENCES ORDERTABLE (ID) ENABLE;