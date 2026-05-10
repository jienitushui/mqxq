import request from '@/utils/request'
import type { PageParams, PageResult } from './types/common'
import type { Course, CourseSubject, CourseSearchParams, CommentStatistics, Chapter } from './types/course'
import type { Announcement, AnnouncementCategory, AnnouncementSearchParams } from './types/announcement'
import type { Carousel } from './types/carousel'

// 公共API（无需登录）
export const publicApi = {
  // ========== 课程相关 ==========
  // 获取课程详情
  getCourseDetail(id: number): Promise<Course> {
    return request.get(`/api/public/course/${id}`)
  },

  // 分页查询课程
  getCourseList(params: CourseSearchParams): Promise<PageResult<Course>> {
    return request.get('/api/public/course/page', { params })
  },

  // 搜索课程
  searchCourses(params: CourseSearchParams & { title: string }): Promise<PageResult<Course>> {
    return request.get('/api/public/course/search', { params })
  },

  // 获取免费课程
  getFreeCourses(params: PageParams): Promise<PageResult<Course>> {
    return request.get('/api/public/course/free', { params })
  },

  // 获取热门课程
  getHotCourses(limit?: number): Promise<Course[]> {
    return request.get('/api/public/course/hot', { params: { limit } })
  },

  // ========== 课程分类相关 ==========
  // 获取所有课程分类
  getAllSubjects(): Promise<CourseSubject[]> {
    return request.get('/api/public/course-subject/all')
  },

  // 获取课程分类详情
  getSubjectDetail(id: number): Promise<CourseSubject> {
    return request.get(`/api/public/course-subject/${id}`)
  },

  // 获取子分类
  getSubjectChildren(parentId: number): Promise<CourseSubject[]> {
    return request.get(`/api/public/course-subject/children/${parentId}`)
  },

  // 获取树形分类
  getSubjectTree(): Promise<CourseSubject[]> {
    return request.get('/api/public/course-subject/tree')
  },
  // ========== 章节小节相关 ==========
  // 获取课程章节结构
  getCourseStructure(courseId: number): Promise<Chapter[]> {
    return request.get(`/api/public/chapter-section/course/${courseId}/structure`)
  },

  // 获取课程小节统计
  getCourseSectionStatistics(courseId: number): Promise<any> {
    return request.get(`/api/public/chapter-section/course/${courseId}/statistics`)
  },

  // ========== 课程评论相关 ==========
  // 获取课程评论列表
  getCourseComments(courseId: number, params: PageParams & { orderBy?: string }): Promise<PageResult<any>> {
    return request.get(`/api/public/comment/course/${courseId}`, { params })
  },

  // 获取最新评论
  getLatestComments(courseId: number, limit?: number): Promise<any[]> {
    return request.get(`/api/public/comment/latest/${courseId}`, { params: { limit } })
  },

  // 获取课程评分统计
  getCourseCommentStatistics(courseId: number): Promise<CommentStatistics> {
    return request.get(`/api/public/comment/statistics/${courseId}`)
  },

  // ========== 公告相关 ==========
  // 分页查询公告
  getAnnouncementList(params: AnnouncementSearchParams): Promise<PageResult<Announcement>> {
    return request.get('/api/public/announcements/page', { params })
  },

  // 获取公告详情
  getAnnouncementDetail(id: number): Promise<Announcement> {
    return request.get(`/api/public/announcements/${id}`)
  },

  // 搜索公告
  searchAnnouncements(params: AnnouncementSearchParams & { keyword: string }): Promise<PageResult<Announcement>> {
    return request.get('/api/public/announcements/search', { params })
  },

  // 获取最新公告
  getLatestAnnouncements(count?: number): Promise<Announcement[]> {
    return request.get('/api/public/announcements/latest', { params: { count } })
  },

  // 按分类获取公告
  getAnnouncementsByCategory(categoryId: number): Promise<Announcement[]> {
    return request.get(`/api/public/announcements/category/${categoryId}`)
  },

  // ========== 公告分类相关 ==========
  // 获取所有公告分类
  getAllAnnouncementCategories(): Promise<AnnouncementCategory[]> {
    return request.get('/api/public/announcement-categories/all')
  },

  // 获取公告分类详情
  getAnnouncementCategoryDetail(id: number): Promise<AnnouncementCategory> {
    return request.get(`/api/public/announcement-categories/${id}`)
  },

  // ========== 轮播图相关 ==========
  // 获取启用的轮播图列表
  getCarouselList(): Promise<Carousel[]> {
    return request.get('/api/public/carousel/list')
  },

  // 获取首页轮播图
  getHomepageCarousel(limit?: number): Promise<Carousel[]> {
    return request.get('/api/public/carousel/homepage', { params: { limit } })
  },

  // 获取轮播图详情
  getCarouselDetail(id: number): Promise<Carousel> {
    return request.get(`/api/public/carousel/${id}`)
  }
}