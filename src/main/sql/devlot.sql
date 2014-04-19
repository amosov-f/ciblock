CREATE DATABASE devlot DEFAULT CHARACTER SET utf8;

USE devlot;

CREATE TABLE factor (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name TEXT,
  dimension TEXT
);

INSERT INTO factor (name, dimension) VALUES ('Площадь участка', 'м2');
INSERT INTO factor (name, dimension) VALUES ('Максимальная высота сооружений', 'м');

CREATE TABLE vector (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name TEXT
);

CREATE TABLE project_feature (
  project_id INT,
  feature_id INT,
  value DOUBLE
);




