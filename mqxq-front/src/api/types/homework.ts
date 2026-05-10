// 作业信息
export interface Homework {
  id: number
  courseId: number
  title: string
  description: string
  attachmentUrl?: string
  deadline: string
  maxScore: number
  status: number
  createTime: string
  updateTime: string
  createUser: number
  updateUser: number
  content?: string
  endTime?: string
  score?: number
  answer?: string  // 参考答案
}

// 作业提交信息
export interface HomeworkSubmission {
  id: number
  homeworkId: number
  studentId: number
  content: string
  attachmentUrl?: string
  submitTime: string
  score?: number
  maxScore: number
  teacherComment?: string
  gradeTime?: string
  gradeUser?: number
  status: number
  createTime: string
  createUser: number
  updateTime: string
  updateUser: number
  studentName?: string
  homeworkTitle?: string
}

// 作业详情响应（包含作业和提交信息）
export interface HomeworkDetailResponse {
  homework: Homework
  submission?: HomeworkSubmission
  hasSubmitted: boolean
  canSubmit: boolean
}

// 作业搜索参数
export interface HomeworkSearchParams {
  page?: number
  size?: number
  status?: number
  courseId?: number
}

// 作业提交搜索参数
export interface HomeworkSubmissionSearchParams {
  page?: number
  size?: number
  courseId?: number
  status?: number
}