
# --- !Ups

CREATE TABLE Workspaces (
  wid serial PRIMARY KEY,
  name text,
  descr text,
  svgthumb text,
  modified timestamp,
  geometry text /*REFERENCES Geoms */
);

CREATE TABLE Cells (
  cid serial PRIMARY KEY,
  workspace integer REFERENCES Workspaces,
  name varchar(20),
  dim int,
  pos real [],
  blade real [],
  mag text,
  sub varchar(20) [],
  sup varchar(20) [],
  flipped varchar(20),
  extrajson text
);

CREATE TABLE Geoms (
  name text PRIMARY KEY,
  metric real [],
  scalar_type text,
  dimension int
);

CREATE TABLE Shapes (
  sid serial PRIMARY KEY,
  name text,
  n_params int,
  geom text REFERENCES Geoms,
  code text /* scary. */
);


/* ******* fun, auto-update last modified ******* */

CREATE FUNCTION sync_lastmod() RETURNS trigger AS $$
BEGIN
  NEW.modified := NOW();;
  RETURN NEW;;
END;;
$$ LANGUAGE plpgsql;

CREATE TRIGGER
  sync_lastmod
BEFORE UPDATE ON
  Workspaces
FOR EACH ROW EXECUTE PROCEDURE
  sync_lastmod();


# --- !Downs


/******** unsure if this down is correct. ********/

DROP FUNCTION sync_lastmod();

DROP TABLE Geoms;

DROP TABLE Shapes;

DROP TABLE Cells;

DROP TABLE Workspaces;
