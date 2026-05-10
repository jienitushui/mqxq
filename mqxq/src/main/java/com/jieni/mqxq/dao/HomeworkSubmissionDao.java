package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.HomeworkSubmission;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 作业提交数据访问层接口
 * 
 * 提供作业提交系统的数据操作方法，包括提交记录的增删改查、批量操作等
 * 支持提交状态管理、成绩统计、学生信息关联查询以及批改管理
 * 实现提交数据的高效管理和复杂查询支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface HomeworkSubmissionDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    HomeworkSubmission queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param homeworkSubmission 查询条件
     * @return 对象列表
     */
    List<HomeworkSubmission> queryAllByLimit(@Param("homeworkSubmission") HomeworkSubmission homeworkSubmission);

    /**
     * 统计总行数
     *
     * @param homeworkSubmission 查询条件
     * @return 总行数
     */
    long count(HomeworkSubmission homeworkSubmission);

    /**
     * 新增数据
     *
     * @param homeworkSubmission 实例对象
     * @return 影响行数
     */
    int insert(HomeworkSubmission homeworkSubmission);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<HomeworkSubmission> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<HomeworkSubmission> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<HomeworkSubmission> 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<HomeworkSubmission> entities);

    /**
     * 修改数据
     *
     * @param homeworkSubmission 实例对象
     * @return 影响行数
     */
    int update(HomeworkSubmission homeworkSubmission);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 根据作业ID和学生ID查询提交记录
     *
     * @param homeworkId 作业ID
     * @param studentId  学生ID
     * @return 提交记录
     */
    HomeworkSubmission queryByHomeworkAndStudent(@Param("homeworkId") Integer homeworkId, @Param("studentId") Integer studentId);

    /**
     * 查询提交记录（包含学生信息）
     *
     * @param params 查询参数
     * @return 提交记录列表
     */
    List<HomeworkSubmission> querySubmissionsWithStudentInfo(@Param("params") Map<String, Object> params);

    /**
     * 获取作业提交统计
     *
     * @param homeworkId 作业ID
     * @return 统计信息
     */
    Map<String, Object> getSubmissionStats(@Param("homeworkId") Integer homeworkId);

    /**
     * 根据学生ID查询提交记录
     *
     * @param studentId 学生ID
     * @return 提交记录列表
     */
    List<HomeworkSubmission> queryByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据作业ID查询所有提交记录
     *
     * @param homeworkId 作业ID
     * @return 提交记录列表
     */
    List<HomeworkSubmission> queryByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 批量删除
     *
     * @param ids ID列表
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据条件查询
     *
     * @param params 查询条件
     * @return 提交记录列表
     */
    List<HomeworkSubmission> queryByParams(@Param("params") Map<String, Object> params);

    /**
     * 获取最近的提交记录
     *
     * @param limit 限制数量
     * @return 提交记录列表
     */
    List<HomeworkSubmission> getRecentSubmissions(@Param("limit") int limit);

    /**
     * 获取待批改的提交记录
     *
     * @param teacherId 教师ID (可选)
     * @return 提交记录列表
     */
    List<HomeworkSubmission> getPendingGradeSubmissions(@Param("teacherId") Integer teacherId);
}