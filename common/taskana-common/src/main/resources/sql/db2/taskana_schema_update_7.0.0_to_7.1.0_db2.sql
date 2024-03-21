-- this script updates the TASKANA database schema from version 7.0.0. to version 7.1.0.
SET SCHEMA %schemaName%;

INSERT INTO TASKANA_SCHEMA_VERSION (ID, VERSION, CREATED)
VALUES (TASKANA_SCHEMA_VERSION_ID_SEQ.NEXTVAL, '7.1.0', CURRENT_TIMESTAMP);

ALTER TABLE TASK
    ADD COLUMN NUMBER_OF_COMMENTS INT DEFAULT 0;

UPDATE TASK t
SET NUMBER_OF_COMMENTS = subquery.COMMENT_COUNT
FROM (
  SELECT t.ID, COUNT(tc.ID) AS COMMENT_COUNT
  FROM TASK t
  RIGHT OUTER JOIN TASK_COMMENT tc
  ON t.ID = tc.TASK_ID
  GROUP BY t.ID
) AS subquery
WHERE t.ID = subquery.ID;
