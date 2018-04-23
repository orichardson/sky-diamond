
# --- !Ups

CREATE TABLE Workspaces (
  wid serial PRIMARY KEY,
  name text,
  descr text
);

CREATE TABLE Cells (
  cid serial PRIMARY KEY,
  workspace integer,
  name varchar(20),
  dim int,
  pos real [],
  blade real [],
  mag text,
  sub varchar(20) [],
  super varchar(20) []
);

/*ALTER TABLE Cells
  ADD FOREIGN KEY (workspace)
    REFERENCES Workspaces(wid)*/




# --- !Downs

DROP TABLE Cells;

DROP TABLE Workspaces