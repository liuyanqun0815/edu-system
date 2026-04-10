import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import LoginView from '@/views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { public: true }
    },
    {
      path: '/forgot',
      name: 'forgot',
      component: () => import('@/views/ForgotView.vue'),
      meta: { public: true }
    },
    {
      path: '/change-password',
      name: 'change-password',
      component: () => import('@/views/ChangePasswordView.vue'),
      meta: { title: '修改密码' }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '工作台' }
        },
        {
          path: 'course',
          name: 'course',
          component: () => import('@/views/CourseView.vue'),
          meta: { title: '课程管理' }
        },
        {
          path: 'exam',
          name: 'exam',
          component: () => import('@/views/ExamView.vue'),
          meta: { title: '考试管理' }
        },
        {
          path: 'question',
          name: 'question',
          component: () => import('@/views/QuestionView.vue'),
          meta: { title: '题库管理' }
        },
        {
          path: 'subject',
          name: 'subject',
          component: () => import('@/views/SubjectView.vue'),
          meta: { title: '学科管理' }
        },
        {
          path: 'word',
          name: 'word',
          component: () => import('@/views/WordView.vue'),
          meta: { title: '单词训练' }
        },
        {
          path: 'word-training',
          name: 'word-training',
          component: () => import('@/views/WordTrainingView.vue'),
          meta: { title: '单词AI训练' }
        },
        {
          path: 'word-training-session/:sessionId',
          name: 'word-training-session',
          component: () => import('@/views/WordTrainingSession.vue'),
          meta: { title: '训练中' }
        },
        {
          path: 'word-training-result/:sessionId',
          name: 'word-training-result',
          component: () => import('@/views/WordTrainingResult.vue'),
          meta: { title: '训练结果' }
        },
        {
          path: 'word-wrongbook',
          name: 'word-wrongbook',
          component: () => import('@/views/WordWrongBookView.vue'),
          meta: { title: '我的错题本' }
        },
        {
          path: 'user',
          name: 'user',
          component: () => import('@/views/UserView.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'role',
          name: 'role',
          component: () => import('@/views/RoleView.vue'),
          meta: { title: '角色管理' }
        },
        {
          path: 'menu',
          name: 'menu',
          component: () => import('@/views/MenuView.vue'),
          meta: { title: '菜单管理' }
        },
        {
          path: 'activity',
          name: 'activity',
          component: () => import('@/views/ActivityView.vue'),
          meta: { title: '活动管理' }
        },
        {
          path: 'evaluation',
          name: 'evaluation',
          component: () => import('@/views/EvaluationView.vue'),
          meta: { title: '评价管理' }
        },
        {
          path: 'stats',
          name: 'stats',
          component: () => import('@/views/StatsView.vue'),
          meta: { title: '数据统计' }
        },
        {
          path: 'setting',
          name: 'setting',
          component: () => import('@/views/SettingView.vue'),
          meta: { title: '系统设置' }
        },
        {
          path: 'config',
          name: 'config',
          component: () => import('@/views/ConfigView.vue'),
          meta: { title: '配置管理' }
        },
        {
          path: 'login-log',
          name: 'login-log',
          component: () => import('@/views/LoginLogView.vue'),
          meta: { title: '登录日志' }
        },
        {
          path: 'paper-upload',
          name: 'paper-upload',
          component: () => import('@/views/PaperUploadView.vue'),
          meta: { title: '试卷上传' }
        },
        {
          path: 'paper-import',
          name: 'paper-import',
          component: () => import('@/views/PaperImportView.vue'),
          meta: { title: '试卷导入' }
        },
        {
          path: 'online-exam',
          name: 'online-exam',
          component: () => import('@/views/OnlineExamView.vue'),
          meta: { title: '在线考试' }
        },
        {
          path: 'exam-result/:recordId',
          name: 'exam-result',
          component: () => import('@/views/ExamResultView.vue'),
          meta: { title: '成绩详情' }
        },
        {
          path: 'email-template',
          name: 'email-template',
          component: () => import('@/views/EmailTemplateView.vue'),
          meta: { title: '邮件模板' }
        },
        {
          path: 'email-log',
          name: 'email-log',
          component: () => import('@/views/EmailLogView.vue'),
          meta: { title: '邮件日志' }
        },
        {
          path: 'lesson',
          name: 'lesson',
          component: () => import('@/views/LessonView.vue'),
          meta: { title: '课程排课' }
        },
        {
          path: 'ai-import',
          name: 'ai-import',
          component: () => import('@/views/AIImportView.vue'),
          meta: { title: 'AI智能导入' }
        },
        {
          path: 'ai-resource',
          name: 'ai-resource',
          component: () => import('@/views/AiResourceView.vue'),
          meta: { title: 'AI资源管理' }
        },
        {
          path: 'parse-template',
          name: 'parse-template',
          component: () => import('@/views/ParseTemplateManage.vue'),
          meta: { title: '解析模板管理' }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.title) {
    document.title = to.meta.title + ' - 智慧教培管理系统'
  }
  
  if (!to.meta.public && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
