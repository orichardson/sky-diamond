
# --- !Ups

CREATE TABLE Workspaces (
  wid serial PRIMARY KEY,
  name text,
  descr text,
  thumburl text,
  modified timestamp
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
  super varchar(20) []
);




# --- !Downs

DROP TABLE Cells;

DROP TABLE Workspaces