USE fastats;

DELIMITER //

CREATE PROCEDURE insert_random_data(IN count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_sector_id INT;
    DECLARE random_org_id INT;
    DECLARE random_survey_id INT;
    DECLARE random_stat_table_id BIGINT;

    -- sector 테이블에 랜덤 데이터 삽입
    WHILE i < count
        DO
            INSERT INTO sector (code, description)
            VALUES (CONCAT('A', LPAD(FLOOR(RAND() * 1000), 3, '0')),
                    CONCAT('Sector ', CHAR(FLOOR(65 + RAND() * 26))));
            SET i = i + 1;
        END WHILE;

    SET i = 0;

    -- stat_org 테이블에 랜덤 데이터 삽입
    WHILE i < count
        DO
            INSERT INTO stat_org (code, name)
            VALUES (FLOOR(RAND() * 1000),
                    CONCAT('Organization ', CHAR(FLOOR(65 + RAND() * 26))));
            SET i = i + 1;
        END WHILE;

    SET i = 0;

    -- stat_survey 테이블에 랜덤 데이터 삽입
    WHILE i < count
        DO
            SET random_sector_id = (SELECT id FROM sector ORDER BY RAND() LIMIT 1);
            SET random_org_id = (SELECT id FROM stat_org ORDER BY RAND() LIMIT 1);

            INSERT INTO stat_survey (sector_id, org_id, name)
            VALUES (random_sector_id, random_org_id,
                    CONCAT('Survey ', CHAR(FLOOR(65 + RAND() * 26)), FLOOR(RAND() * 100)));
            SET i = i + 1;
        END WHILE;

    SET i = 0;

    -- stat_table 테이블에 랜덤 데이터 삽입
    WHILE i < count
        DO
            SET random_survey_id = (SELECT id FROM stat_survey ORDER BY RAND() LIMIT 1);

            INSERT INTO stat_table (survey_id, name, content, comment, kosis_tb_id, kosis_view_link)
            VALUES (random_survey_id,
                    CONCAT('Table ', CHAR(FLOOR(65 + RAND() * 26))),
                    'Random Text for Content',
                    'Random Text for Comment',
                    CONCAT('TB', LPAD(FLOOR(RAND() * 1000), 3, '0')),
                    CONCAT('httpexamplecomtable', FLOOR(RAND() * 100)));
            SET i = i + 1;
        END WHILE;

    SET i = 0;

    -- coll_info 테이블에 랜덤 데이터 삽입
    WHILE i < count
        DO
            SET random_stat_table_id = (SELECT id FROM stat_table ORDER BY RAND() LIMIT 1);
            INSERT INTO coll_info (stat_table_id, start_date, end_date, period)
            VALUES (random_stat_table_id,
                    "20220101", "20241231",
                    CASE FLOOR(RAND() * 3)
                        WHEN 0 THEN '1Y'
                        WHEN 1 THEN '1Q'
                        ELSE '1M'
                        END);
            SET i = i + 1;
        END WHILE;
END //

DELIMITER ;
