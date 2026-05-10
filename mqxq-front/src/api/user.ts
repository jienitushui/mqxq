import request from '@/utils/request'
import type { PageParams, PageResult } from './types/common'
import type { 
  Course, 
  CourseComment, 
  MyCourse, 
  CourseView, 
  CourseViewBatchDeleteDTO,
  CourseSearchParams,
  Chapter,
  Section
} from './types/course'
import type { 
  Homework, 
  HomeworkSubmission, 
  HomeworkDetailResponse,
  HomeworkSearchParams, 
  HomeworkSubmissionSearchParams 
} from './types/homework'
import type { Order, OrderSearchParams } from './types/order'

// 用户相关API（需要登录）
export const userApi = {
  // ========== 课程管理 ==========

  // 获取课程详情（用户端，包含个人化信息）
  getCourseDetail(id: number): Promise<Course> {
    return request.get(`/api/user/course/${id}`)
  },

  // 分页查询课程
  getCourseList(params: CourseSearchParams): Promise<PageResult<Course>> {
    return request.get('/api/user/course/page', { params })
  },

  // 获取浏览记录
  getViewHistory(params: PageParams): Promise<PageResult<CourseView>> {
    return request.get('/api/user/course/view-history', { params })
  },

  // 检查学习权限
  checkCanStudy(courseId: number): Promise<boolean> {
    return request.get(`/api/user/course/can-study/${courseId}`)
  },

  // 检查课程购买状态
  checkPurchaseStatus(courseId: number): Promise<boolean> {
    return request.get(`/api/user/course/purchase-status/${courseId}`)
  },

  // ========== 我的课程管理 ==========
  // 我的课程列表
  getMyCourseList(params: PageParams & { status?: number }): Promise<PageResult<MyCourse>> {
    return request.get('/api/user/my-course/list', { params })
  },

  // 检查课程加入状态
  checkCourseJoinStatus(courseId: number): Promise<boolean> {
    return request.get(`/api/user/my-course/check/${courseId}`)
  },

  // 加入课程
  joinCourse(courseId: number): Promise<void> {
    return request.post('/api/user/my-course/join', { courseId })
  },

  // 退出课程
  quitCourse(courseId: number): Promise<void> {
    return request.delete(`/api/user/my-course/quit/${courseId}`)
  },

  // 课程学习详情
  getMyCourseDetail(courseId: number): Promise<MyCourse> {
    return request.get(`/api/user/my-course/detail/${courseId}`)
  },

  // 更新学习进度
  updateLearningProgress(data: MyCourse): Promise<void> {
    return request.put('/api/user/my-course/progress', data)
  },

  // 更新学习时长
  updateStudyDuration(courseId: number, duration: number, lastSectionId?: number): Promise<void> {
    return request.put('/api/user/my-course/duration', null, {
      params: { courseId, duration, lastSectionId }
    })
  },

  // 学习统计
  getStudyStatistics(): Promise<any> {
    return request.get('/api/user/my-course/statistics')
  },

  // ========== 课程评论管理 ==========
  // 发表课程评论
  createComment(data: CourseComment): Promise<void> {
    return request.post('/api/user/comment', data)
  },

  // 修改评论
  updateComment(id: number, data: CourseComment): Promise<void> {
    return request.put(`/api/user/comment/${id}`, data)
  },

  // 删除评论
  deleteComment(id: number): Promise<void> {
    return request.delete(`/api/user/comment/${id}`)
  },

  // 检查用户是否已评论
  checkUserComment(courseId: number): Promise<{ hasCommented: boolean; comment: CourseComment | null }> {
    return request.get(`/api/user/comment/check/${courseId}`)
  },

  // 课程评论列表
  getCourseComments(courseId: number, params: PageParams & { orderBy?: string }): Promise<PageResult<CourseComment>> {
    return request.get(`/api/user/comment/course/${courseId}`, { params })
  },

  // 我的评论列表
  getMyComments(params: PageParams): Promise<PageResult<CourseComment>> {
    return request.get('/api/user/comment/my-list', { params })
  },

  // 课程评分统计
  getCourseCommentStatistics(courseId: number): Promise<any> {
    return request.get(`/api/user/comment/statistics/${courseId}`)
  },

  // ========== 章节管理 ==========
  // 查询章节详情
  getChapterDetail(id: number): Promise<Chapter> {
    return request.get(`/user/chapter/${id}`)
  },

  // 分页查询课程章节
  getCourseChapters(courseId: number, params: PageParams): Promise<PageResult<Chapter>> {
    return request.get(`/user/chapter/course/${courseId}`, { params })
  },

  // 获取课程所有章节
  getAllCourseChapters(courseId: number): Promise<Chapter[]> {
    return request.get(`/user/chapter/course/${courseId}/all`)
  },

  // 获取章节及小节数量
  getChapterWithSectionCount(courseId: number): Promise<any> {
    return request.get(`/user/chapter/course/${courseId}/with-section-count`)
  },

  // ========== 小节管理 ==========
  // 查询小节详情
  getSectionDetail(id: number): Promise<Section> {
    return request.get(`/user/section/${id}`)
  },

  // 分页查询章节小节
  getChapterSections(chapterId: number, params: PageParams): Promise<PageResult<Section>> {
    return request.get(`/user/section/chapter/${chapterId}`, { params })
  },

  // 获取章节已发布小节
  getAllChapterSections(chapterId: number): Promise<Section[]> {
    return request.get(`/user/section/chapter/${chapterId}/all`)
  },

  // 分页查询课程小节
  getCourseSections(courseId: number, params: PageParams): Promise<PageResult<Section>> {
    return request.get(`/user/section/course/${courseId}`, { params })
  },

  // 获取课程已发布小节
  getAllCourseSections(courseId: number): Promise<Section[]> {
    return request.get(`/user/section/course/${courseId}/all`)
  },

  // 课程小节统计
  getCourseSectionStatistics(courseId: number): Promise<any> {
    return request.get(`/user/section/course/${courseId}/statistics`)
  },

  // ========== 作业管理 ==========
  // 作业详情
  getHomeworkDetail(id: number): Promise<Homework> {
    return request.get(`/api/user/homework/${id}`)
  },

  // 检查作业状态
  checkHomeworkStatus(id: number): Promise<any> {
    return request.get(`/api/user/homework/${id}/status`)
  },

  // 课程作业列表
  getCourseHomeworks(courseId: number, params: PageParams): Promise<PageResult<Homework>> {
    return request.get(`/api/user/homework/course/${courseId}`, { params })
  },

  // 我的作业列表
  getMyHomeworks(params: HomeworkSearchParams): Promise<PageResult<Homework>> {
    return request.get('/api/user/homework/my-list', { params })
  },

  // ========== 作业提交管理 ==========
  // 提交详情
  getHomeworkSubmissionDetail(id: number): Promise<HomeworkSubmission> {
    return request.get(`/api/user/homework-submission/${id}`)
  },

  // 更新提交
  updateHomeworkSubmission(id: number, data: HomeworkSubmission): Promise<void> {
    return request.put(`/api/user/homework-submission/${id}`, data)
  },

  // 撤回提交
  deleteHomeworkSubmission(id: number): Promise<void> {
    return request.delete(`/api/user/homework-submission/${id}`)
  },

  // 检查提交状态
  checkSubmissionStatus(homeworkId: number): Promise<any> {
    return request.get(`/api/user/homework-submission/check/${homeworkId}`)
  },

  // 作业提交详情（根据作业ID获取当前用户的提交记录）
  getHomeworkSubmissionByHomework(homeworkId: number): Promise<HomeworkSubmission | null> {
    return request.get(`/api/user/homework-submission/homework/${homeworkId}`)
  },

  // 我的提交列表
  getMySubmissions(params: HomeworkSubmissionSearchParams): Promise<PageResult<HomeworkSubmission>> {
    return request.get('/api/user/homework-submission/my-submissions', { params })
  },

  // 提交作业
  submitHomework(data: HomeworkSubmission): Promise<void> {
    return request.post('/api/user/homework-submission/submit', data)
  },

  // 上传附件
  uploadHomeworkAttachment(homeworkId: number, file: File): Promise<any> {
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`/api/user/homework-submission/upload/${homeworkId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // ========== 订单管理 ==========
  // 创建课程订单
  createCourseOrder(courseId: number): Promise<Order> {
    return request.post(`/api/user/orders/create-course-order/${courseId}`)
  },

  // 订单详情
  getOrderDetail(orderId: number): Promise<Order> {
    return request.get(`/api/user/orders/detail/${orderId}`)
  },

  // 我的订单列表
  getMyOrders(params: OrderSearchParams): Promise<PageResult<Order>> {
    return request.get('/api/user/orders/my-orders', { params })
  },

  // 根据订单号查询
  getOrderByOrderNo(orderNo: string): Promise<Order> {
    return request.get(`/api/user/orders/by-order-no/${orderNo}`)
  },

  // 取消订单
  cancelOrder(orderNo: string): Promise<void> {
    return request.put(`/api/user/orders/cancel/${orderNo}`)
  },

  // 支付订单
  payOrder(): Promise<any> {
    return request.post('/api/user/orders/pay')
  },

  // ========== 课程浏览管理 ==========
  // 分页查询浏览记录
  getCourseViewPage(params: PageParams & { courseId?: number }): Promise<PageResult<CourseView>> {
    return request.get('/api/user/course-view/page', { params })
  },

  // 获取浏览记录详情
  getCourseViewDetail(id: number): Promise<CourseView> {
    return request.get(`/api/user/course-view/${id}`)
  },

  // 删除浏览记录
  deleteCourseView(id: number): Promise<void> {
    return request.delete(`/api/user/course-view/${id}`)
  },

  // 批量删除浏览记录
  batchDeleteCourseViews(ids: number[]): Promise<void> {
    const deleteDTO: CourseViewBatchDeleteDTO = { ids }
    return request.delete('/api/user/course-view/batch', { data: deleteDTO })
  },

  // 清空浏览记录
  clearCourseViews(): Promise<void> {
    return request.delete('/api/user/course-view/clear')
  },

  // 查询用户浏览记录
  getCourseViewList(params: PageParams & { courseId?: number }): Promise<PageResult<CourseView>> {
    return request.get('/api/user/course-view/page', { params })
  }
}