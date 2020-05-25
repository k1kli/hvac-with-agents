create database hvac_db; -- Creates the new database
create user 'hvac_user'@'%' identified by 'ThePassword'; -- Creates the user
grant all on hvac_db.* to 'hvac_user'@'%'; -- Gives all privileges to the new user on the newly created database