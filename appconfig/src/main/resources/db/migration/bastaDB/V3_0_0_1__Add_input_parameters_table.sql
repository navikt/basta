
/**Rename orchestratorOrderId to externalId*/
ALTER TABLE ORDERTABLE ADD (
EXTERNALID  VARCHAR2(255 CHAR)
);

UPDATE ORDERTABLE SET EXTERNALID = orchestratorOrderId;
ALTER TABLE ORDERTABLE DROP COLUMN orchestratorOrderId;


/* Input properties generation*/
CREATE TABLE INPUT_PROPERTIES (
   ORDER_ID NUMBER(19,0) NOT NULL,
   INPUT_KEY VARCHAR2(255 CHAR) NOT NULL,
   INPUT_VALUE VARCHAR2(4000 CHAR)
);

CREATE INDEX IX_INPUT_PROPERTIES
ON INPUT_PROPERTIES (ORDER_ID);

INSERT INTO input_properties
   SELECT o.id,p.property_key, p.property_value
   FROM ordertable o, settings_properties p
   WHERE o.settings_id = p.settings_id;

UPDATE input_properties
SET input_key = 'hostnames'
where input_key = 'decommissionHosts';

INSERT INTO input_properties
   SELECT o.id, 'applicationMappingName', s.applicationmappingname
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;

INSERT INTO input_properties
   SELECT o.id, 'middleWareType', s.middleWareType
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;

INSERT INTO input_properties
   SELECT o.id, 'environmentClass', s.environmentClass
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;

INSERT INTO input_properties
   SELECT o.id, 'environmentName', s.environmentName
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;

INSERT INTO input_properties
   SELECT o.id, 'serverCount', s.serverCount
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;

INSERT INTO input_properties
   SELECT o.id, 'serverSize', s.serverSize
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;

INSERT INTO input_properties
   SELECT o.id, 'zone', s.zone
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;

INSERT INTO input_properties
   SELECT o.id, 'disks', s.disks
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;

INSERT INTO input_properties
   SELECT o.id, 'xmlCustomized', s.xmlCustomized
   FROM ordertable o, settings s
   WHERE o.settings_id = s.id;