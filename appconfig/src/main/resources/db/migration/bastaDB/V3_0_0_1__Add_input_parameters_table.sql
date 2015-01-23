
/**Rename orchestratorOrderId to externalId*/
ALTER TABLE ORDERTABLE ADD (
EXTERNALID  VARCHAR2(255 CHAR),
INPUT_ID NUMBER(19,0)
);

UPDATE ORDERTABLE SET EXTERNALID = ORCHESTRATORORDERID;
UPDATE ORDERTABLE SET INPUT_ID = SETTINGS_ID;
ALTER TABLE ORDERTABLE DROP COLUMN ORCHESTRATORORDERID;
ALTER TABLE ORDERTABLE DROP COLUMN SETTINGS_ID;


CREATE TABLE INPUT (
   ID NUMBER(19,0) NOT NULL,
   CREATED TIMESTAMP (6),
   CREATEDBY VARCHAR2(255 CHAR),
   UPDATED TIMESTAMP (6),
   UPDATEDBY VARCHAR2(255 CHAR),

   CREATEDBYDISPLAYNAME VARCHAR2(255 CHAR),
   UPDATEDBYDISPLAYNAME VARCHAR2(255 CHAR)
);

/* Input properties generation*/
CREATE TABLE INPUT_PROPERTIES (
   INPUT_ID NUMBER(19,0) NOT NULL,
   INPUT_KEY VARCHAR2(255 CHAR) NOT NULL,
   INPUT_VALUE VARCHAR2(4000 CHAR)
);

CREATE INDEX IX_INPUT_PROPERTIES
ON INPUT_PROPERTIES (INPUT_ID);


INSERT INTO input
   SELECT s.id, s.created, s.createdby, s.updated, s.updatedby, s.createdbydisplayname, s.updatedbydisplayname
   FROM settings s;

INSERT INTO input_properties
   SELECT p.settings_id,p.property_key, p.property_value
   FROM  settings_properties p;


UPDATE input_properties
SET input_key = 'hostnames'
where input_key = 'decommissionHosts';

INSERT INTO input_properties
   SELECT s.id, 'applicationMappingName', s.applicationmappingname
   FROM  settings s ;

INSERT INTO input_properties
   SELECT s.id, 'middleWareType', s.middleWareType
   FROM  settings s ;

INSERT INTO input_properties
   SELECT s.id, 'environmentClass', s.environmentClass
   FROM  settings s ;

INSERT INTO input_properties
   SELECT s.id, 'environmentName', s.environmentName
   FROM  settings s ;

INSERT INTO input_properties
   SELECT s.id, 'serverCount', s.serverCount
   FROM  settings s ;

INSERT INTO input_properties
   SELECT s.id, 'serverSize', s.serverSize
   FROM  settings s ;

INSERT INTO input_properties
   SELECT s.id, 'zone', s.zone
   FROM  settings s ;

INSERT INTO input_properties
   SELECT s.id, 'disks', s.disks
   FROM  settings s ;

INSERT INTO input_properties
   SELECT s.id, 'xmlCustomized', s.xmlCustomized
   FROM  settings s ;