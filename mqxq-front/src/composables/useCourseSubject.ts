import { ref, computed } from 'vue'
import { publicApi } from '@/api'
import type { CourseSubject } from '@/api/types/course'
import { buildSubjectTree, getTopLevelSubjects, getChildSubjects, findSubjectById, getSubjectPath } from '@/utils/courseSubject'

export function useCourseSubject() {
  const subjects = ref<CourseSubject[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 树形结构的分类
  const subjectTree = computed(() => buildSubjectTree(subjects.value))

  // 顶级分类
  const topLevelSubjects = computed(() => getTopLevelSubjects(subjects.value))

  /**
   * 获取所有课程分类
   */
  const fetchAllSubjects = async () => {
    loading.value = true
    error.value = null
    
    try {
      const data = await publicApi.getSubjectTree()
      subjects.value = data
      return data
    } catch (err: any) {
      error.value = err.message || '获取课程分类失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 根据父ID获取子分类
   */
  const getChildren = (parentId: number): CourseSubject[] => {
    return getChildSubjects(subjects.value, parentId)
  }

  /**
   * 根据ID查找分类
   */
  const findById = (id: number): CourseSubject | undefined => {
    return findSubjectById(subjects.value, id)
  }

  /**
   * 获取分类路径（面包屑）
   */
  const getPath = (id: number): CourseSubject[] => {
    return getSubjectPath(subjects.value, id)
  }

  /**
   * 获取分类名称
   */
  const getSubjectName = (id: number): string => {
    const subject = findById(id)
    return subject?.name || '未知分类'
  }

  return {
    subjects,
    subjectTree,
    topLevelSubjects,
    loading,
    error,
    fetchAllSubjects,
    getChildren,
    findById,
    getPath,
    getSubjectName
  }
}
