import { request } from '@/utils/http'

// 课程浏览管理API
export const courseBrowseApi = {
  // 分页查询浏览记录
  getBrowseList(params) {
    return request.get('/api/admin/course-view/page', { params })
  },
  
  // 获取浏览记录详情
  getBrowseDetail(id) {
    return request.get(`/api/admin/course-view/${id}`)
  },
  
  // 删除浏览记录
  deleteBrowse(id) {
    return request.delete(`/api/admin/course-view/${id}`)
  },
  
  // 批量删除浏览记录
  batchDeleteBrowse(ids) {
    return request.delete('/api/admin/course-view/batch', { data: ids })
  },
  
  // 获取浏览统计
  getBrowseStats() {
    return request.get('/api/admin/course-view/statistics')
  }
}

export default courseBrowseApi