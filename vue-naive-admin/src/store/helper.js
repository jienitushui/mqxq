import { cloneDeep } from 'lodash-es'
import api from '@/api'
import { adminPermissions, teacherPermissions } from '@/settings'
import { useUserStore } from '@/store'

export async function getUserInfo() {
  const userStore = useUserStore()
  const res = await api.getUser()
  if (res.data) {
    // console.log(res.data)
    const { name, ...rest } = res.data
    userStore.setUser({ nickname: name, ...rest })
  }
}

export async function getPermissions() {
  const userStore = useUserStore()
  const role = userStore.currentRole?.name || userStore.currentRole
  let permissions = []
  if (role === '管理员') {
    permissions = adminPermissions
  }
  else if (role === '教师') {
    permissions = teacherPermissions
  }
  return cloneDeep(permissions)
}
