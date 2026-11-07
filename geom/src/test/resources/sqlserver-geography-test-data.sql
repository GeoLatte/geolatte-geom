-- Generates the <TestCase> entries of sqlserver-geography-test-data.xml from the serialization
-- SQL Server produces for GEOGRAPHY values.
-- Requires SQL Server 2012 or later. No database objects are created.
--
-- WKT coordinates are (longitude latitude). Exterior rings are counter-clockwise and interior
-- rings clockwise, as GEOGRAPHY requires.
SET NOCOUNT ON;

DECLARE @cases TABLE (
    id   int          PRIMARY KEY,
    ewkt varchar(400) NOT NULL,  -- fixture notation: PostGIS 1.5 EWKT, without the SRID
    wkt  varchar(400) NOT NULL,  -- SQL Server WKT: an M-only coordinate is written as "x y NULL m"
    srid int          NOT NULL
);

INSERT INTO @cases (id, ewkt, wkt, srid) VALUES
(1,  'POINT(4.35 50.85)',
     'POINT(4.35 50.85)', 4326),
(2,  'POINT(4.35 50.85 100)',
     'POINT(4.35 50.85 100)', 4326),
(3,  'POINTM(4.35 50.85 7)',
     'POINT(4.35 50.85 NULL 7)', 4326),
(4,  'LINESTRING(4.35 50.85, 5.57 50.63)',
     'LINESTRING(4.35 50.85, 5.57 50.63)', 4326),
(5,  'LINESTRING(4.35 50.85, 5.57 50.63, 3.72 51.05)',
     'LINESTRING(4.35 50.85, 5.57 50.63, 3.72 51.05)', 4326),
(6,  'LINESTRING(4.35 50.85 10 1, 5.57 50.63 20 2, 3.72 51.05 30 3)',
     'LINESTRING(4.35 50.85 10 1, 5.57 50.63 20 2, 3.72 51.05 30 3)', 4326),
(7,  'POLYGON((4 50, 6 50, 6 52, 4 52, 4 50), (4.5 50.5, 4.5 51, 5 51, 5 50.5, 4.5 50.5))',
     'POLYGON((4 50, 6 50, 6 52, 4 52, 4 50), (4.5 50.5, 4.5 51, 5 51, 5 50.5, 4.5 50.5))', 4326),
(8,  'MULTIPOINT(4.35 50.85, 5.57 50.63)',
     'MULTIPOINT((4.35 50.85), (5.57 50.63))', 4326),
(9,  'MULTILINESTRING((4.35 50.85, 5.57 50.63), (3.72 51.05, 4.4 51.22))',
     'MULTILINESTRING((4.35 50.85, 5.57 50.63), (3.72 51.05, 4.4 51.22))', 4326),
(10, 'MULTIPOLYGON(((4 50, 5 50, 5 51, 4 51, 4 50)), ((6 50, 7 50, 7 51, 6 51, 6 50)))',
     'MULTIPOLYGON(((4 50, 5 50, 5 51, 4 51, 4 50)), ((6 50, 7 50, 7 51, 6 51, 6 50)))', 4326),
-- [MS-SSCLRT] 3.1.4
(11, 'GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3), POLYGON((0 0, 3 0, 3 3, 0 3, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1)))',
     'GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3), POLYGON((0 0, 3 0, 3 3, 0 3, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1)))', 4326),
(12, 'POINT EMPTY',
     'POINT EMPTY', 4326),
(13, 'GEOMETRYCOLLECTION EMPTY',
     'GEOMETRYCOLLECTION EMPTY', 4326),
-- an SRID other than the default
(14, 'POINT(4.35 50.85)',
     'POINT(4.35 50.85)', 4258);

-- Check that version = 01 for every row, then copy the fixture_xml column into
-- sqlserver-geography-test-data.xml.
SELECT
    c.id,
    CONVERT(varchar(2), SUBSTRING(b.bytes, 5, 1), 2) AS version,
    CONVERT(varchar(2), SUBSTRING(b.bytes, 6, 1), 2) AS props,
    g.geog.STIsValid()                               AS is_valid,
    g.geog.AsTextZM()                                AS server_wkt,
    CONCAT('<TestCase><id>', c.id, '</id><ewkt>', c.ewkt, '</ewkt><srid>', c.srid,
           '</srid><db_representation>', CONVERT(varchar(max), b.bytes, 2),
           '</db_representation></TestCase>')        AS fixture_xml
FROM @cases c
CROSS APPLY (SELECT geography::STGeomFromText(c.wkt, c.srid) AS geog) g
CROSS APPLY (SELECT CAST(g.geog AS varbinary(max)) AS bytes) b
ORDER BY c.id;
