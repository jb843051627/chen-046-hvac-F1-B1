-- hvac 集中空调清洗消毒作业单流转加固（chen-046）
-- 需先执行 doc/schema/hvac.sql 建出两张基础表，再执行本脚本。
-- 科里口径（这些就是"以什么为准"）：
--   1) 档案为准：作业单上的 ac_no/ac_name 一律以系统档案为唯一出处，两边对不上时按档案改单。
--   2) 环节为准：单子 status 一律由 stage 推导（0待发起/1在办/2已办结），不许手填；
--      历史存量数据按环节修正，并留修正标记供对账。
--   3) 件次统一排：作业单件次由本局排号器发号，没有敲键的地方；件次为空、跟前头撞号的，
--      一律不许往下走（纸本存量老单也一样）——数据库 NOT NULL + UNIQUE 兜底。
--   4) 归档即定死：已归档不得推进、回退、改处置说明、删除——服务层与数据库触发器双重关门。

-- 1) 作业单：乐观锁版本号、件次非空且唯一、环节/状态非空、送检冷却水份数
ALTER TABLE t_hvac_clean_log
  ADD COLUMN version bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（改处置说明用）',
  ADD COLUMN sample_count int NOT NULL DEFAULT 0 COMMENT '这一趟抽了几份冷却水送检',
  MODIFY biz_no varchar(32) NOT NULL COMMENT '作业单件次（系统排，不留手填口，唯一）',
  MODIFY stage int NOT NULL DEFAULT 0 COMMENT '当前环节 0待查验 1清洗中 2待复核 3已归档',
  MODIFY status int NOT NULL DEFAULT 0 COMMENT '单子走到哪一步 0待发起 1在办 2已办结（由环节推导）',
  ADD UNIQUE KEY uk_hvac_biz_no (biz_no),
  ADD KEY idx_hvac_log_ac_id (ac_id),
  ADD KEY idx_hvac_log_stage (stage),
  ADD KEY idx_hvac_log_status (status);

-- 2) 件次排号器（按年排，本局统一发号）
CREATE TABLE IF NOT EXISTS t_hvac_biz_seq (
  seq_year int NOT NULL COMMENT '年份（件次按年排）',
  seq_value bigint NOT NULL DEFAULT 0 COMMENT '本年已排到的号',
  PRIMARY KEY (seq_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='作业单件次排号器';

-- 3) 流转台账（只追加，不改不删；事后倒着核的底账）
CREATE TABLE IF NOT EXISTS t_hvac_clean_trace (
  id bigint NOT NULL COMMENT '主键',
  log_id bigint NOT NULL COMMENT '作业单主键',
  biz_no varchar(32) DEFAULT NULL COMMENT '作业单件次（冗余，便于按件追查）',
  action varchar(16) NOT NULL COMMENT '动作 CREATE/ADVANCE/ROLLBACK/UPDATE/REMOVE/IMPORT',
  from_stage int DEFAULT NULL COMMENT '离开的环节',
  to_stage int DEFAULT NULL COMMENT '到达的环节',
  content varchar(512) DEFAULT NULL COMMENT '本环节处置说明快照（后一次不得覆盖前一次）',
  action_by varchar(64) DEFAULT NULL COMMENT '谁动的手',
  action_time datetime DEFAULT NULL COMMENT '什么时候动的',
  action_remark varchar(500) DEFAULT NULL COMMENT '为什么动（回退原因/处置说明）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_trace_log_id (log_id),
  KEY idx_trace_biz_no (biz_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='集中空调清洗消毒作业单流转台账';

-- 4) 作业单校验：环节合法、状态必须由环节推导（不许手填）、件次不许为空
DELIMITER $$
DROP TRIGGER IF EXISTS tri_hvac_log_insert_validate$$
CREATE TRIGGER tri_hvac_log_insert_validate
BEFORE INSERT ON t_hvac_clean_log
FOR EACH ROW
BEGIN
  IF NEW.biz_no IS NULL OR NEW.biz_no = '' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '件次为空，不许入册';
  END IF;
  IF NEW.stage < 0 OR NEW.stage > 3 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '环节不合法';
  END IF;
  IF NEW.status <> (CASE WHEN NEW.stage = 0 THEN 0 WHEN NEW.stage < 3 THEN 1 ELSE 2 END) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '状态必须由环节推导，不许手填';
  END IF;
END$$

-- 5) 归档锁 + 更新校验：已归档不得改；环节/状态合法性同样校验
DROP TRIGGER IF EXISTS tri_hvac_log_archived_immutable$$
CREATE TRIGGER tri_hvac_log_archived_immutable
BEFORE UPDATE ON t_hvac_clean_log
FOR EACH ROW
BEGIN
  IF OLD.stage = 3 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '已归档作业单不可再改动';
  END IF;
  IF NEW.stage < 0 OR NEW.stage > 3 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '环节不合法';
  END IF;
  IF NEW.status <> (CASE WHEN NEW.stage = 0 THEN 0 WHEN NEW.stage < 3 THEN 1 ELSE 2 END) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '状态必须由环节推导，不许手填';
  END IF;
END$$

DROP TRIGGER IF EXISTS tri_hvac_log_archived_no_delete$$
CREATE TRIGGER tri_hvac_log_archived_no_delete
BEFORE DELETE ON t_hvac_clean_log
FOR EACH ROW
BEGIN
  IF OLD.stage = 3 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '已归档作业单不可删除';
  END IF;
END$$

-- 6) 台账只追加：不许改、不许删，谁都不能抹账
DROP TRIGGER IF EXISTS tri_hvac_trace_append_only_update$$
CREATE TRIGGER tri_hvac_trace_append_only_update
BEFORE UPDATE ON t_hvac_clean_trace
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '流转台账只追加，不可修改';
END$$

DROP TRIGGER IF EXISTS tri_hvac_trace_append_only_delete$$
CREATE TRIGGER tri_hvac_trace_append_only_delete
BEFORE DELETE ON t_hvac_clean_trace
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '流转台账只追加，不可删除';
END$$
DELIMITER ;

-- 7) 本年件次排号起点
INSERT IGNORE INTO t_hvac_biz_seq (seq_year, seq_value) VALUES (YEAR(NOW()), 0);

-- 8) 纸本存量老单走"补录"入口（/hvacCleanLog/import）进系统，不要直接 INSERT 到这里。
--    示例（仅作补录格式参考，勿直接执行）：
--    INSERT INTO t_hvac_clean_log (id, biz_no, ac_id, ac_no, ac_name, clean_by, clean_date, due_date,
--                                  sample_count, stage, status, content, last_action, del_flag, create_by, create_time)
--    VALUES (900200000000000001, '2019-008', 1, 'AC0001', '政务东街办公楼主楼一层空调机房（在用）',
--            '某某清洗公司-张三', '2019-05-10 00:00:00', '2020-05-10 00:00:00',
--            2, 2, 1, '送检冷却水2份，待复核', 'IMPORT', 0, 'admin', NOW());
--    补录时若原状态写着"已归档"而环节还停在"待复核"，一律以环节为准修正状态并留标记。
