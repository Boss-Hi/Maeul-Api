ALTER TABLE profiles ADD COLUMN name VARCHAR(32) DEFAULT NULL COMMENT '이름' AFTER nickname;
ALTER TABLE profiles ADD COLUMN description TEXT DEFAULT NULL COMMENT '설명' AFTER name;
ALTER TABLE profiles ADD COLUMN year tinyint unsigned DEFAULT NULL COMMENT '년도' AFTER description;
ALTER TABLE profiles ADD COLUMN month tinyint unsigned DEFAULT NULL COMMENT '월' AFTER description;
ALTER TABLE profiles ADD COLUMN day tinyint unsigned DEFAULT NULL COMMENT '일' AFTER description;
ALTER TABLE profiles MODIFY COLUMN nickname VARCHAR(32) DEFAULT NULL COMMENT '닉네임' AFTER description;
ALTER TABLE profiles DROP COLUMN bio;