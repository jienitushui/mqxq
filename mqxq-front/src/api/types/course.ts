// 基础课程信息
export interface Course {
  id: number
  teacherId: number
  subjectId: number
  title: string
  description: string
  price: number
  lessonNum: number
  durationSum: number
  cover: string
  buyCount: number
  viewCount: number
  status: number
  publishTime: string
  createTime: string
  createUser: number
  updateTime: string
  updateUser: number
}

// 我的课程
export interface MyCourse {
  id: number
  userId: number
  courseId: number
  orderId: number
  status: number
  createTime: string
  updateTime: string
  // 关联的课程信息
  course?: Course
}

// 课程评论
export interface CourseComment {
  id?: number
  courseId: number
  userId?: number
  userName?: string
  content: string
  rating: number
  createTime?: string
  updateTime?: string
}

// 评论统计
export interface CommentStatistics {
  totalComments: number
  averageRating: number
  ratingDistribution: number[] // 5个元素的数组，分别对应1-5星的评论数量
}

// 课程分类
export interface CourseSubject {
  id: number
  name: string
  parentId: number
  sort?: number
  createTime?: string
  updateTime?: string
  children?: CourseSubject[]
}

// 课程浏览记录
export interface CourseView {
  id: number
  userId: number
  courseId: number
  viewTime: string
  ipAddress?: string
  userAgent?: string
  createTime?: string
  viewCount?: number
  // 关联的课程信息
  course?: Course
  // 课程基本信息（扁平化）
  courseTitle?: string
  courseDescription?: string
  courseCover?: string
  coursePrice?: number
  courseViewCount?: number
}

// 批量删除浏览记录请求参数
export interface CourseViewBatchDeleteDTO {
  ids: number[]
}

// 课程搜索参数
export interface CourseSearchParams {
  pageNum?: number
  pageSize?: number
  page?: number
  size?: number
  title?: string
  categoryId?: number
  teacherId?: number
  level?: number
  isFree?: boolean
  minPrice?: number
  maxPrice?: number
  orderBy?: string
}

// 章节
export interface Chapter {
  id: number
  courseId: number
  title: string
  description?: string
  orderNum: number
  status: number
  createTime: string
  updateTime: string
  sections?: Section[]
}

// 小节
export interface Section {
  id: number
  chapterId: number
  courseId: number
  title: string
  content?: string
  videoUrl?: string
  duration?: number
  orderNum: number
  status: number
  createTime: string
  updateTime: string
}

// 课程状态常量
export const CourseStatus = {
  JOINED: 0,    // 已加入
  LEARNING: 1,  // 学习中
  COMPLETED: 2  // 已完成
} as const

// 课程状态标签映射
export const CourseStatusLabels: Record<number, string> = {
  0: '已加入',
  1: '学习中',
  2: '已完成'
}

// 课程状态颜色映射
export const CourseStatusColors: Record<number, string> = {
  0: 'info',      // 已加入 - 蓝色
  1: 'success',   // 学习中 - 绿色
  2: 'warning'    // 已完成 - 橙色
}