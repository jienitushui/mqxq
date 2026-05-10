<template>
  <nav class="navbar">
    <div class="navbar-container">
      <!-- Logo -->
      <div class="navbar-brand">
        <router-link to="/" class="brand-link">
          <img src="@/assets/logo.jpeg" alt="码趣星球Logo" class="brand-logo"/>
          <span class="brand-text">码趣星球</span>
        </router-link>
      </div>

      <!-- 主导航菜单 -->
      <div class="navbar-menu" :class="{ 'is-active': isMenuOpen }">
        <div class="navbar-nav">
          <!-- 首页 -->
          <router-link to="/" class="nav-item" @click="closeMenu">
            <span class="nav-text">首页</span>
          </router-link>

          <!-- 课程 -->
          <router-link to="/courses" class="nav-item" @click="closeMenu">
            <span class="nav-text">课程</span>
          </router-link>

          <!-- 公告 -->
          <router-link to="/announcements" class="nav-item" @click="closeMenu">
            <span class="nav-text">公告</span>
          </router-link>

          <!-- AI 助手
          <router-link v-if="isAuthenticated" to="/chat" class="nav-item nav-item-highlight" @click="closeMenu">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
            </svg>
            <span class="nav-text">AI 助手</span>
          </router-link> -->

          <!-- 需要登录的菜单项 -->
          <template v-if="isAuthenticated">
            <!-- 我的学习 -->
            <div class="nav-dropdown">
              <span class="nav-item dropdown-trigger">
                <span class="nav-text">我的学习</span>
                <span class="dropdown-arrow">▼</span>
              </span>
              <div class="dropdown-menu">
                <router-link to="/learning/courses" class="dropdown-item" @click="closeMenu">
                  我的课程
                </router-link>
                <router-link to="/learning/history" class="dropdown-item" @click="closeMenu">
                  浏览记录
                </router-link>
              </div>
            </div>

            <!-- 作业 -->
            <div class="nav-dropdown">
              <span class="nav-item dropdown-trigger">
                <span class="nav-text">作业</span>
                <span class="dropdown-arrow">▼</span>
              </span>
              <div class="dropdown-menu">
                <router-link to="/homework" class="dropdown-item" @click="closeMenu">
                  我的作业
                </router-link>
                <router-link to="/homework/submissions" class="dropdown-item" @click="closeMenu">
                  提交记录
                </router-link>
              </div>
            </div>

            <!-- 订单 -->
            <router-link to="/orders" class="nav-item" @click="closeMenu">
              <span class="nav-text">我的订单</span>
            </router-link>
          </template>
        </div>

        <!-- 用户操作区域 -->
        <div class="navbar-actions">
          <template v-if="!isAuthenticated">
            <router-link to="/auth/login" class="btn btn-outline" @click="closeMenu">
              登录
            </router-link>
            <router-link to="/auth/register" class="btn btn-primary" @click="closeMenu">
              注册
            </router-link>
          </template>
          <template v-else>
            <!-- 用户头像下拉菜单 -->
            <div class="user-dropdown">
              <div class="user-avatar dropdown-trigger">
                <span class="avatar-text">{{ userInitial }}</span>
                <span class="dropdown-arrow">▼</span>
              </div>
              <div class="dropdown-menu">
                <router-link to="/profile" class="dropdown-item" @click="closeMenu">
                  个人中心
                </router-link>
                <router-link to="/profile/settings" class="dropdown-item" @click="closeMenu">
                  个人设置
                </router-link>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item logout-btn" @click="handleLogout">
                  退出登录
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- 移动端菜单按钮 -->
      <div class="navbar-burger" @click="toggleMenu">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store'

const router = useRouter()
const authStore = useAuthStore()

// 移动端菜单状态
const isMenuOpen = ref(false)

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)
const userInitial = computed(() => {
  // 假设用户信息中有用户名，取首字母
  const username = authStore.userInfo?.username || authStore.userInfo?.email || 'U'
  return username.charAt(0).toUpperCase()
})

// 方法
const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}

const handleLogout = async () => {
  try {
    await authStore.logout()
    closeMenu()
  } catch (error) {
    console.error('退出登录失败:', error)
    closeMenu()
  }
}
</script>

<style scoped>
.navbar {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 64px;
}

/* Logo */
.navbar-brand {
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #3498db;
  font-size: 24px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.brand-link:hover {
  transform: translateY(-1px);
}

.brand-logo {
  width: 55px;
  height: 30px;
  object-fit: contain;
  /* margin-right: 6px; */
  border-radius: 4px;
  transition: all 0.3s ease;
}

.brand-logo:hover {
  transform: scale(1.05);
}

.brand-text {
  background: linear-gradient(45deg, #3498db, #2980b9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* 导航菜单 */
.navbar-menu {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: space-between;
  margin-left: 40px;
}

.navbar-nav {
  display: flex;
  align-items: center;
  gap: 30px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  text-decoration: none;
  color: #333;
  border-radius: 6px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.nav-item:hover {
  background: #f8f9fa;
  color: #3498db;
}

.nav-item.router-link-active {
  color: #3498db;
  background: #e3f2fd;
}

.nav-item-highlight {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white !important;
  
  &:hover {
    background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
    color: white !important;
  }
  
  &.router-link-active {
    background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
    color: white !important;
  }
}

.nav-text {
  font-weight: 500;
}

/* 下拉菜单 */
.nav-dropdown,
.user-dropdown {
  position: relative;
}

.dropdown-trigger {
  gap: 8px;
}

.dropdown-arrow {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.nav-dropdown:hover .dropdown-arrow,
.user-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  min-width: 160px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  z-index: 1001;
}

.user-dropdown .dropdown-menu {
  right: 0;
  left: auto;
}

.nav-dropdown:hover .dropdown-menu,
.user-dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  display: block;
  padding: 10px 16px;
  text-decoration: none;
  color: #333;
  transition: background 0.2s ease;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.dropdown-item:hover {
  background: #f8f9fa;
  color: #3498db;
}

.dropdown-divider {
  height: 1px;
  background: #e9ecef;
  margin: 8px 0;
}

.logout-btn {
  color: #e74c3c;
}

.logout-btn:hover {
  background: #ffeaea;
  color: #c0392b;
}

/* 用户头像 */
.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 20px;
  background: #3498db;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-avatar:hover {
  background: #2980b9;
}

.avatar-text {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  font-size: 14px;
  font-weight: bold;
}

/* 按钮样式 */
.navbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.btn-outline {
  color: #3498db;
  border-color: #3498db;
}

.btn-outline:hover {
  background: #3498db;
  color: white;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

/* 移动端菜单按钮 */
.navbar-burger {
  display: none;
  flex-direction: column;
  cursor: pointer;
  padding: 8px;
}

.navbar-burger span {
  width: 24px;
  height: 3px;
  background: #333;
  margin: 3px 0;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-burger {
    display: flex;
  }

  .navbar-menu {
    position: fixed;
    top: 64px;
    left: 0;
    right: 0;
    background: #fff;
    flex-direction: column;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-100%);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
    margin-left: 0;
  }

  .navbar-menu.is-active {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }

  .navbar-nav {
    flex-direction: column;
    width: 100%;
    gap: 0;
  }

  .nav-item {
    width: 100%;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
  }

  .navbar-actions {
    width: 100%;
    justify-content: center;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
  }

  .dropdown-menu {
    position: static;
    opacity: 1;
    visibility: visible;
    transform: none;
    box-shadow: none;
    background: #f8f9fa;
    margin-left: 20px;
  }

  .nav-dropdown:hover .dropdown-arrow {
    transform: none;
  }
}

/* 菜单激活状态的汉堡按钮动画 */
.navbar-burger.is-active span:nth-child(1) {
  transform: rotate(45deg) translate(6px, 6px);
}

.navbar-burger.is-active span:nth-child(2) {
  opacity: 0;
}

.navbar-burger.is-active span:nth-child(3) {
  transform: rotate(-45deg) translate(6px, -6px);
}
</style>