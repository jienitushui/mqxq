/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/05 21:25:59
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
  }),
  getters: {
    userId() {
      return this.userInfo?.id
    },
    username() {
      return this.userInfo?.username
    },
    nickName() {
      return this.userInfo?.nickname
    },
    avatar() {
      return this.userInfo?.avatar
    },
    currentRole() {
      return this.userInfo?.currentRole || {}
    },
    roles() {
      return this.userInfo?.roles || []
    },
  },
  actions: {
    setUser(user) {
      if (user) {
        const newUserInfo = { ...user }
        if (newUserInfo.avatar) {
          newUserInfo.avatar = `${newUserInfo.avatar}?t=${new Date().getTime()}`
        }
        if (Array.isArray(newUserInfo.roles) && newUserInfo.roles.length > 0 && !newUserInfo.currentRole) {
          newUserInfo.currentRole = { name: newUserInfo.roles[0] }
        }
        this.userInfo = newUserInfo
      } else {
        this.userInfo = null
      }
    },
    resetUser() {
      this.$reset()
    },
  },
  persist: true,
})
