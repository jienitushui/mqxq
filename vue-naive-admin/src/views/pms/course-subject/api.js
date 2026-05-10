import { request } from '@/utils'

const BASE_URL = '/api/admin/course-subject'

export default {
  // 获取所有课程分类
  getAllSubjects() {
    return request.get(`${BASE_URL}/all`)
  },

  // 分页查询课程分类
  getSubjectPage(params) {
    return request.get(`${BASE_URL}/page`, { params })
  },

  // 获取课程分类详情
  getSubjectById(id) {
    return request.get(`${BASE_URL}/${id}`)
  },

  // 创建课程分类
  createSubject(data) {
    return request.post(BASE_URL, data)
  },

  // 更新课程分类
  updateSubject(id, data) {
    return request.put(`${BASE_URL}/${id}`, data)
  },

  // 删除课程分类
  deleteSubject(id) {
    return request.delete(`${BASE_URL}/${id}`)
  },

  // 获取子分类
  getChildrenSubjects(parentId) {
    return request.get(`${BASE_URL}/children/${parentId}`)
  },

  // 获取树形分类
  getSubjectTree() {
    return request.get(`${BASE_URL}/tree`)
  },

  // 搜索课程分类
  searchSubjects(keyword) {
    return request.get(`${BASE_URL}/search`, { params: { keyword } })
  },

  // 批量删除课程分类
  batchDeleteSubjects(ids) {
    return request.delete(`${BASE_URL}/batch`, { data: { ids } })
  }
}