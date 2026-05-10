import { userApi } from '@/api/user'
import { publicApi } from '@/api/public'
import { useAuthStore } from '@/store'
import type { Course } from '@/api/types/course'

/**
 * 智能获取课程详情
 * 登录用户使用 userApi.getCourseDetail (包含个人化信息)
 * 未登录用户使用 publicApi.getCourseDetail (公共信息)
 */
export const getCourseDetail = async (courseId: number): Promise<Course> => {
  const authStore = useAuthStore()
  
  try {
    if (authStore.isAuthenticated) {
      // 登录用户使用用户接口，可以获取个人化信息
      return await userApi.getCourseDetail(courseId)
    } else {
      // 未登录用户使用公共接口
      return await publicApi.getCourseDetail(courseId)
    }
  } catch (error) {
    console.error('获取课程详情失败:', error)
    // 如果用户接口失败，降级到公共接口
    if (authStore.isAuthenticated) {
      console.warn('用户接口失败，降级使用公共接口')
      return await publicApi.getCourseDetail(courseId)
    }
    throw error
  }
}

/**
 * 记录课程浏览历史（仅登录用户）
 */
export const recordCourseView = async (courseId: number): Promise<void> => {
  const authStore = useAuthStore()
  
  if (authStore.isAuthenticated) {
    try {
      // 调用记录浏览历史的接口
      // 注意：这个接口可能需要后端提供，暂时使用 console.log 占位
      console.log(`记录课程 ${courseId} 浏览历史`)
      
      // 如果后端提供了记录浏览历史的接口，可以这样调用：
      // await userApi.recordCourseView(courseId)
    } catch (error) {
      console.warn('记录浏览历史失败:', error)
    }
  }
}