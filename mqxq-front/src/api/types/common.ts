// 通用分页参数
export interface PageParams {
  pageNum?: number
  pageSize?: number
  page?: number
  size?: number
}

// 通用响应结果
export interface Result<T = any> {
  code: string
  msg: string
  data: T
}

// 分页响应数据
export interface PageResult<T = any> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  size: number
  startRow: number
  endRow: number
  pages: number
  prePage: number
  nextPage: number
  isFirstPage: boolean
  isLastPage: boolean
  hasPreviousPage: boolean
  hasNextPage: boolean
  navigatePages: number
  navigatepageNums: number[]
  navigateFirstPage: number
  navigateLastPage: number
  records?: T[]  // Alternative field name
}

// 文件上传响应
export interface FileUploadResult {
  url: string
  fileName: string
  fileSize: number
}