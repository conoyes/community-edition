--
-- Title:      Upgrade to V3.4 - Add alf_prop_unique_ctx.prop1_id index
-- Database:   MySQL
-- Since:      V3.4 schema 4105
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Optional if upgrading from before 3.2.x

CREATE INDEX fk_alf_propuctx_p1 ON alf_prop_unique_ctx(prop1_id); --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-property-unique-ctx-idx';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-property-unique-ctx-idx', 'Manually executed script upgrade V3.4',
     0, 4104, -1, 4105, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
