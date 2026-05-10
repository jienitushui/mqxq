// 轮播图信息
export interface Carousel {
  id: number
  carouselUrl: string
  linkUrl?: string
  sort: number
  isDeleted: number
  createTime: string
  createUser: number
  updateTime: string
  updateUser: number
  title?: string
  description?: string
  buttonText?: string
}