// 公告信息
export interface Announcement {
  id: number
  categoryId: number
  title: string
  content: string
  publishDate: string
  createTime: string
  createUser: number
  updateTime: string
  updateUser: number
  categoryName?: string
}

// 公告分类
export interface AnnouncementCategory {
  id: number
  name: string
  description?: string
  sort: number
  status: number
  createTime: string
}

// 公告搜索参数
export interface AnnouncementSearchParams {
  page?: number
  size?: number
  title?: string
  categoryId?: number
  keyword?: string
}