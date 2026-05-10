import type { CourseSubject } from '@/api/types/course'

/**
 * 将扁平的分类数组转换为树形结构
 * @param subjects 扁平的分类数组
 * @returns 树形结构的分类数组
 */
export function buildSubjectTree(subjects: CourseSubject[]): CourseSubject[] {
  if (!subjects || subjects.length === 0) return []

  // 创建一个映射表，方便查找
  const subjectMap = new Map<number, CourseSubject>()
  
  // 初始化映射表，并为每个分类添加 children 数组
  subjects.forEach(subject => {
    subjectMap.set(subject.id, { ...subject, children: [] })
  })

  // 构建树形结构
  const tree: CourseSubject[] = []
  
  subjects.forEach(subject => {
    const node = subjectMap.get(subject.id)!
    
    if (subject.parentId === 0) {
      // 顶级分类
      tree.push(node)
    } else {
      // 子分类，添加到父分类的 children 中
      const parent = subjectMap.get(subject.parentId)
      if (parent) {
        parent.children!.push(node)
      }
    }
  })

  return tree
}

/**
 * 获取所有顶级分类
 * @param subjects 分类数组
 * @returns 顶级分类数组
 */
export function getTopLevelSubjects(subjects: CourseSubject[]): CourseSubject[] {
  return subjects.filter(subject => subject.parentId === 0)
}

/**
 * 根据父ID获取子分类
 * @param subjects 分类数组
 * @param parentId 父分类ID
 * @returns 子分类数组
 */
export function getChildSubjects(subjects: CourseSubject[], parentId: number): CourseSubject[] {
  return subjects.filter(subject => subject.parentId === parentId)
}

/**
 * 根据ID查找分类
 * @param subjects 分类数组
 * @param id 分类ID
 * @returns 分类对象或undefined
 */
export function findSubjectById(subjects: CourseSubject[], id: number): CourseSubject | undefined {
  return subjects.find(subject => subject.id === id)
}

/**
 * 获取分类的完整路径（面包屑）
 * @param subjects 分类数组
 * @param id 分类ID
 * @returns 分类路径数组
 */
export function getSubjectPath(subjects: CourseSubject[], id: number): CourseSubject[] {
  const path: CourseSubject[] = []
  let currentId = id

  while (currentId) {
    const subject = findSubjectById(subjects, currentId)
    if (!subject) break
    
    path.unshift(subject)
    currentId = subject.parentId
    
    // 防止无限循环
    if (currentId === 0) break
  }

  return path
}

/**
 * 扁平化树形结构
 * @param tree 树形结构的分类数组
 * @returns 扁平的分类数组
 */
export function flattenSubjectTree(tree: CourseSubject[]): CourseSubject[] {
  const result: CourseSubject[] = []

  function traverse(nodes: CourseSubject[]) {
    nodes.forEach(node => {
      const { children, ...rest } = node
      result.push(rest as CourseSubject)
      if (children && children.length > 0) {
        traverse(children)
      }
    })
  }

  traverse(tree)
  return result
}
