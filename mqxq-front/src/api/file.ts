import request from '@/utils/request'
import type { FileUploadResult } from './types/common'

// 文件管理API
export const fileApi = {
  // 智能文件上传
  uploadAuto(file: File): Promise<FileUploadResult> {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/public/files/auto', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 上传图片文件
  uploadImage(file: File): Promise<FileUploadResult> {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/public/files/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 上传视频文件
  uploadVideo(file: File): Promise<FileUploadResult> {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/public/files/video', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 通用文件上传
  upload(file: File): Promise<FileUploadResult> {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/public/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 更新用户头像
  uploadAvatar(file: File): Promise<FileUploadResult> {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/public/files/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 删除文件
  deleteFile(pathUrl: string): Promise<void> {
    return request.delete(`/api/public/files/${pathUrl}`)
    // return request.delete(`/api/public/files/${encodeURIComponent(pathUrl)}`)
  },

  // 删除文件（参数方式）
  deleteFileByParam(pathUrl: string): Promise<void> {
    return request.delete('/api/public/files/delete', { params: { pathUrl } })
  }
}