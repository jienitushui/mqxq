-- 为 course_view 表添加唯一索引，确保一个用户对一个课程只有一条浏览记录

-- 首先清理重复数据（保留最新的浏览记录）
DELETE cv1 FROM course_view cv1
INNER JOIN course_view cv2 
WHERE cv1.user_id = cv2.user_id 
  AND cv1.course_id = cv2.course_id 
  AND cv1.view_time < cv2.view_time;

-- 添加唯一索引
ALTER TABLE course_view 
ADD UNIQUE INDEX `uk_user_course` (`user_id`, `course_id`) USING BTREE;