import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store'

// 路由配置
const routes: RouteRecordRaw[] = [
  // ========== 首页与导�?==========
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/Home.vue'),
    meta: {
      title: '首页',
      requiresAuth: false
    }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/pages/Search.vue'),
    meta: {
      title: '搜索结果',
      requiresAuth: false
    }
  },

  // ========== 用户认证模块 ==========
  {
    path: '/auth',
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/pages/auth/Login.vue'),
        meta: {
          title: '登录',
          requiresGuest: true
        }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/pages/auth/Register.vue'),
        meta: {
          title: '注册',
          requiresGuest: true
        }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/pages/auth/ForgotPassword.vue'),
        meta: {
          title: '忘记密码',
          requiresGuest: true
        }
      },
      {
        path: 'verify-email',
        name: 'VerifyEmail',
        component: () => import('@/pages/auth/VerifyEmail.vue'),
        meta: {
          title: '邮箱验证',
          requiresAuth: false
        }
      }
    ]
  },

  // ========== 课程管理系统 ==========
  {
    path: '/courses',
    children: [
      {
        path: '',
        name: 'CourseList',
        component: () => import('@/pages/courses/CourseList.vue'),
        meta: {
          title: '课程列表',
          requiresAuth: false
        }
      },
      {
        path: ':id',
        name: 'CourseDetail',
        component: () => import('@/pages/courses/CourseDetail.vue'),
        meta: {
          title: '课程详情',
          requiresAuth: false
        }
      },
      {
        path: ':id/learn',
        name: 'CourseLearn',
        component: () => import('@/pages/courses/CourseLearn.vue'),
        meta: {
          title: '课程学习',
          requiresAuth: true
        }
      },
      {
        path: ':id/chapters',
        name: 'CourseChapters',
        component: () => import('@/pages/courses/CourseChapters.vue'),
        meta: {
          title: '课程目录',
          requiresAuth: false
        }
      }
    ]
  },

  // ========== 我的学习中心 ==========
  {
    path: '/learning',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'courses',
        name: 'MyCourses',
        component: () => import('@/pages/learning/MyCourses.vue'),
        meta: {
          title: '我的课程',
          requiresAuth: true
        }
      },
      {
        path: 'history',
        name: 'ViewHistory',
        component: () => import('@/pages/learning/ViewHistory.vue'),
        meta: {
          title: '浏览记录',
          requiresAuth: true
        }
      },
      {
        path: 'history/:id',
        name: 'ViewHistoryDetail',
        component: () => import('@/pages/learning/ViewHistoryDetail.vue'),
        meta: {
          title: '浏览记录详情',
          requiresAuth: true
        }
      },
      {
        path: 'test-batch-delete',
        name: 'BatchDeleteTest',
        component: () => import('@/components/BatchDeleteTest.vue'),
        meta: {
          title: '批量删除测试',
          requiresAuth: true
        }
      }
    ]
  },

  // ========== 作业系统 ==========
  {
    path: '/homework',
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'HomeworkList',
        component: () => import('@/pages/homework/HomeworkList.vue'),
        meta: {
          title: '我的作业',
          requiresAuth: true
        }
      },
      {
        path: ':id',
        name: 'HomeworkDetail',
        component: () => import('@/pages/homework/HomeworkDetail.vue'),
        meta: {
          title: '作业详情',
          requiresAuth: true
        }
      },
      {
        path: ':id/submit',
        name: 'HomeworkSubmit',
        component: () => import('@/pages/homework/HomeworkSubmit.vue'),
        meta: {
          title: '作业提交',
          requiresAuth: true
        }
      },
      {
        path: 'submissions',
        name: 'HomeworkSubmissions',
        component: () => import('@/pages/homework/HomeworkSubmissions.vue'),
        meta: {
          title: '提交记录',
          requiresAuth: true
        }
      }
    ]
  },

  // ========== 订单与支付系�?==========
  {
    path: '/orders',
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'OrderList',
        component: () => import('@/pages/orders/OrderList.vue'),
        meta: {
          title: '我的订单',
          requiresAuth: true
        }
      },
      {
        path: ':id',
        name: 'OrderDetail',
        component: () => import('@/pages/orders/OrderDetail.vue'),
        meta: {
          title: '订单详情',
          requiresAuth: true
        }
      },
      {
        path: 'checkout/:courseId',
        name: 'Checkout',
        component: () => import('@/pages/orders/Checkout.vue'),
        meta: {
          title: '订单结算',
          requiresAuth: true
        }
      }
    ]
  },

  // ========== 支付页面 ==========
  {
    path: '/payment',
    meta: { requiresAuth: true },
    children: [
      {
        path: ':orderNo',
        name: 'Payment',
        component: () => import('@/pages/payment/Payment.vue'),
        meta: {
          title: '支付',
          requiresAuth: true
        }
      }
    ]
  },

  // ========== 个人中心 ==========
  {
    path: '/profile',
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/pages/profile/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: 'settings',
        name: 'ProfileSettings',
        component: () => import('@/pages/profile/ProfileSettings.vue'),
        meta: {
          title: '个人设置',
          requiresAuth: true
        }
      }
    ]
  },

  // ========== 公告系统 ==========
  {
    path: '/announcements',
    children: [
      {
        path: '',
        name: 'AnnouncementList',
        component: () => import('@/pages/announcements/AnnouncementList.vue'),
        meta: {
          title: '系统公告',
          requiresAuth: false
        }
      },
      {
        path: ':id',
        name: 'AnnouncementDetail',
        component: () => import('@/pages/announcements/AnnouncementDetail.vue'),
        meta: {
          title: '公告详情',
          requiresAuth: false
        }
      }
    ]
  },

  // ========== AI 聊天助手 ==========
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/pages/ChatPage.vue'),
    meta: {
      title: 'AI 学习助手',
      requiresAuth: true
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 码趣星球在线学习平台`
  }
  
  // 检查是否需要认�?
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  
  // 检查是否需要游客状态（如登录、注册页面）
  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

export default router