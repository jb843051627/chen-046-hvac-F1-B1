-- hvac 菜单与权限（chen-046）
-- 需先执行 springbootv2.sql（建出 t_sys_permission / t_sys_permission_role）。
-- 建菜单与按钮，并授权给"管理员"角色（id 488243256161730560）。

INSERT INTO `t_sys_permission`
(`id`, `name`, `descripion`, `url`, `is_blank`, `pid`, `perms`, `type`, `icon`, `order_num`, `visible`,
 `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
-- 目录
(900100000000000001, '集中空调通风监督', '集中空调通风监督', NULL, 0, 0, NULL, 0, 'layui-icon layui-icon-app', 90, 0, 'admin', NOW(), NULL, NULL, NULL),
-- 菜单：空调系统档案
(900100000000000010, '空调系统档案', '空调系统档案', '/hvacArch/view', 0, 900100000000000001, 'hvacArch:view', 1, 'layui-icon layui-icon-list', 1, 0, 'admin', NOW(), NULL, NULL, NULL),
-- 菜单：清洗消毒作业单台账
(900100000000000020, '清洗消毒作业单', '清洗消毒作业单流转台账', '/hvacCleanLog/view', 0, 900100000000000001, 'hvacCleanLog:view', 1, 'layui-icon layui-icon-list', 2, 0, 'admin', NOW(), NULL, NULL, NULL),
-- 按钮：档案
(900100000000000011, '档案列表', '档案列表', '/hvacArch/list', 0, 900100000000000010, 'hvacArch:list', 2, NULL, 1, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000012, '建档', '建档', '/hvacArch/create', 0, 900100000000000010, 'hvacArch:create', 2, NULL, 2, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000013, '改档案', '改档案', '/hvacArch/edit', 0, 900100000000000010, 'hvacArch:edit', 2, NULL, 3, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000014, '标停用', '标一笔停用，不再往下挂单', '/hvacArch/stop', 0, 900100000000000010, 'hvacArch:stop', 2, NULL, 4, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000015, '删档案', '删档案', '/hvacArch/remove', 0, 900100000000000010, 'hvacArch:remove', 2, NULL, 5, 0, 'admin', NOW(), NULL, NULL, NULL),
-- 按钮：作业单
(900100000000000021, '台账列表', '台账列表', '/hvacCleanLog/list', 0, 900100000000000020, 'hvacCleanLog:list', 2, NULL, 1, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000022, '新建作业单', '新建作业单（件次系统排）', '/hvacCleanLog/create', 0, 900100000000000020, 'hvacCleanLog:create', 2, NULL, 2, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000023, '推进一档', '推进一档', '/hvacCleanLog/advance', 0, 900100000000000020, 'hvacCleanLog:advance', 2, NULL, 3, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000024, '回退一档', '回退一档（撤销刚推那一下，留痕）', '/hvacCleanLog/rollback', 0, 900100000000000020, 'hvacCleanLog:rollback', 2, NULL, 4, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000025, '改处置说明', '改处置说明（已归档不得改）', '/hvacCleanLog/updateContent', 0, 900100000000000020, 'hvacCleanLog:updateContent', 2, NULL, 5, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000026, '抹单', '抹单（已归档不可抹）', '/hvacCleanLog/remove', 0, 900100000000000020, 'hvacCleanLog:remove', 2, NULL, 6, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000027, '补录老单', '纸本存量老单补录', '/hvacCleanLog/import', 0, 900100000000000020, 'hvacCleanLog:import', 2, NULL, 7, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000028, '册上对账', '册上对账', '/hvacCleanLog/reconcile', 0, 900100000000000020, 'hvacCleanLog:reconcile', 2, NULL, 8, 0, 'admin', NOW(), NULL, NULL, NULL),
(900100000000000029, '流转台账', '流转台账（事后倒着核）', '/hvacCleanLog/trace', 0, 900100000000000020, 'hvacCleanLog:trace', 2, NULL, 9, 0, 'admin', NOW(), NULL, NULL, NULL);

-- 授权给"管理员"角色
INSERT INTO `t_sys_permission_role`
(`id`, `role_id`, `permission_id`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(900200000000000001, 488243256161730560, 900100000000000010, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000002, 488243256161730560, 900100000000000020, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000003, 488243256161730560, 900100000000000011, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000004, 488243256161730560, 900100000000000012, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000005, 488243256161730560, 900100000000000013, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000006, 488243256161730560, 900100000000000014, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000007, 488243256161730560, 900100000000000015, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000008, 488243256161730560, 900100000000000021, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000009, 488243256161730560, 900100000000000022, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000010, 488243256161730560, 900100000000000023, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000011, 488243256161730560, 900100000000000024, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000012, 488243256161730560, 900100000000000025, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000013, 488243256161730560, 900100000000000026, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000014, 488243256161730560, 900100000000000027, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000015, 488243256161730560, 900100000000000028, 'admin', NOW(), NULL, NULL, NULL),
(900200000000000016, 488243256161730560, 900100000000000029, 'admin', NOW(), NULL, NULL, NULL);
