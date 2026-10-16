-- hvac 公共场所集中空调通风系统清洗消毒作业登记与到期复查管理 -- schema (chen-046)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_046

CREATE TABLE IF NOT EXISTS t_hvac_ac_arch (
  id bigint NOT NULL COMMENT '主键',
  ac_no varchar(64) DEFAULT NULL COMMENT '集中空调系统记号（本局统一排）',
  ac_name varchar(128) DEFAULT NULL COMMENT '集中空调系统名目',
  place_txt varchar(128) DEFAULT NULL COMMENT '安放位置（楼宇—楼层—机房）',
  build_year int DEFAULT NULL COMMENT '投用年份',
  status int DEFAULT NULL COMMENT '系统情形 0在用 1已停用',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='集中空调通风系统档案';

CREATE TABLE IF NOT EXISTS t_hvac_clean_log (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(32) DEFAULT NULL COMMENT '作业单件次（系统排，不留手填口）',
  ac_id bigint DEFAULT NULL COMMENT '挂在哪一套集中空调系统名下（档案主键）',
  ac_no varchar(64) DEFAULT NULL COMMENT '空调系统记号（冗余自系统档案）',
  ac_name varchar(128) DEFAULT NULL COMMENT '空调系统名目（冗余自系统档案）',
  clean_by varchar(128) DEFAULT NULL COMMENT '来做的单位与到场的人',
  clean_date datetime DEFAULT NULL COMMENT '作业当日',
  due_date datetime DEFAULT NULL COMMENT '下次该复查的日头',
  stage int DEFAULT NULL COMMENT '当前环节 0待查验 1清洗中 2待复核 3已归档',
  status int DEFAULT NULL COMMENT '单子走到哪一步 0待发起 1在办 2已办结',
  content varchar(512) DEFAULT NULL COMMENT '本环节处置说明',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次流转动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='集中空调清洗消毒作业单';

-- 初始档案数据（id=1 启用 / id=2 停用）
INSERT IGNORE INTO t_hvac_ac_arch (id, ac_no, ac_name, place_txt, build_year, status, del_flag, create_by, create_time)
VALUES (1, 'AC0001', '榛原市桦川区政务东街办公楼主楼一层空调机房（在用）', '榛原市—桦川区—政务东街办公楼—一层—空调机房', 2015, 0, 0, 'seed', NOW()),
       (2, 'AC0002', '榛原市桦川区政务东街办公楼副楼三层空调机房（已停用）', '榛原市—桦川区—政务东街办公楼—三层—空调机房', 2012, 1, 0, 'seed', NOW());

