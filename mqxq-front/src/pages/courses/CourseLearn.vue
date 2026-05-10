<template>
  <div class="course-learn">
    <!-- 导航栏 -->
    <Navbar />
    <!-- 学习页面头部 -->
    <div class="learn-header">
      <div class="header-left">
        <button @click="goBack" class="btn-back">← 退出学习</button>
        <div class="course-info">
          <h1 class="course-title">课程名称：{{ course.title }}</h1>
        </div>
      </div>

      <div class="header-right">
        <button @click="toggleSidebar" class="btn-toggle">
          {{ showSidebar ? "隐藏目录" : "显示目录" }}
        </button>
      </div>
    </div>

    <!-- 学习内容区域 -->
    <div class="learn-content" :class="{ 'sidebar-hidden': !showSidebar }">
      <!-- 视频播放区域 -->
      <div class="video-section">
        <div v-if="currentSection.id" class="video-container">
          <!-- 视频播放器 - 只有当videoUrl存在时才显示 -->
          <div v-if="currentSection.videoUrl" class="video-player">
            <video
              ref="videoPlayer"
              :src="currentSection.videoUrl"
              controls
              @timeupdate="handleTimeUpdate"
              @ended="handleVideoEnded"
              @loadedmetadata="handleVideoLoaded"
            >
              您的浏览器不支持视频播放
            </video>
          </div>

          <!-- 控制按钮区域 - 位于视频和内容之间 -->
          <div class="controls-section">
            <!-- 视频控制栏 - 只有当有视频时才显示 -->
            <div v-if="currentSection.videoUrl" class="video-controls">
              <button
                @click="playPreviousSection"
                :disabled="!hasPreviousSection"
                class="btn-control"
              >
                ⏮️ 上一节
              </button>

              <button @click="togglePlayPause" class="btn-control primary">
                {{ isPlaying ? "⏸️ 暂停" : "▶️ 播放" }}
              </button>

              <button
                @click="playNextSection"
                :disabled="!hasNextSection"
                class="btn-control"
              >
                下一节 ⏭️
              </button>
            </div>

            <!-- 无视频时的导航控制栏 -->
            <div v-else class="navigation-controls">
              <button
                @click="playPreviousSection"
                :disabled="!hasPreviousSection"
                class="btn-control"
              >
                ⏮️ 上一节
              </button>

              <button
                @click="playNextSection"
                :disabled="!hasNextSection"
                class="btn-control"
              >
                下一节 ⏭️
              </button>
            </div>
          </div>

          <!-- 视频信息和内容 -->
          <div class="video-info">
            <h2 class="section-title">{{ currentSection.title }}</h2>
            <div class="section-meta">
              <!-- <span class="duration">
                <i class="icon">⏱️</i>
                {{ formatDuration(currentSection.duration) }}
              </span> -->
              <span class="chapter-info">
                第{{ currentChapterIndex + 1 }}章 第{{
                  currentSectionIndex + 1
                }}节
              </span>
            </div>
            
            <!-- 课程内容区域 -->
            <div v-if="currentSection.content" class="section-content">
              <h3 class="content-title">课程内容</h3>
              <div class="content-text markdown-content" v-html="renderedContent"></div>
            </div>
            
            <p v-if="currentSection.description" class="section-description">
              {{ currentSection.description }}
            </p>
          </div>
        </div>

        <!-- 无视频状态 -->
        <div v-else class="no-video">
          <div class="no-video-icon">📹</div>
          <h3>请选择要学习的章节</h3>
          <p>从右侧目录中选择一个小节开始学习</p>
        </div>
      </div>

      <!-- 课程目录侧边栏 -->
      <div v-show="showSidebar" class="sidebar">
        <div class="sidebar-header">
          <h3>课程目录</h3>
          <div class="course-stats">
            <span>{{ totalSections }}个小节</span>
          </div>
        </div>

        <div class="chapters-list">
          <div
            v-for="(chapter, chapterIndex) in chapters"
            :key="chapter.id"
            class="chapter-item"
          >
            <!-- 章节标题 -->
            <div class="chapter-header" @click="toggleChapter(chapter.id)">
              <div class="chapter-info">
                <span class="chapter-number">{{ chapterIndex + 1 }}</span>
                <span class="chapter-title">{{ chapter.title }}</span>
              </div>
              <i
                :class="[
                  'toggle-icon',
                  { expanded: expandedChapters.includes(chapter.id) },
                ]"
              >
                ▼
              </i>
            </div>

            <!-- 小节列表 -->
            <div
              v-show="expandedChapters.includes(chapter.id)"
              class="sections-list"
            >
              <div
                v-for="(section, sectionIndex) in chapter.sections"
                :key="section.id"
                :class="[
                  'section-item',
                  {
                    active: currentSection.id === section.id,
                  },
                ]"
                @click="selectSection(section, chapterIndex, sectionIndex)"
              >
                <div class="section-status">
                  <i
                    v-if="currentSection.id === section.id"
                    class="status-icon playing"
                    >▶️</i
                  >
                  <span v-else class="section-number">{{
                    sectionIndex + 1
                  }}</span>
                </div>

                <div class="section-info">
                  <span class="section-title">{{ section.title }}</span>
                  <span class="section-duration">{{
                    formatDuration(section.duration)
                  }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>



    <!-- 权限验证弹窗 -->
    <AccessModal
      :visible="accessModal.visible"
      :type="accessModal.type"
      :course-info="course"
      @close="closeAccessModal"
      @confirm="handleAccessConfirm"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/store";
import { publicApi, userApi } from "@/api";
import { getCourseDetail } from '@/utils/courseApi';
import AccessModal from "@/components/AccessModal.vue";
import Navbar from "@/components/Navbar.vue";
import { marked } from 'marked';
// import { checkCourseAccess, safeFetchMyCourse, validateSectionAccess, joinCourseAndUpdate } from '@/utils/courseAccess'

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 响应式数据
const loading = ref(false);
const course = ref({});
const chapters = ref([]);
const currentSection = ref({});
const currentChapterIndex = ref(0);
const currentSectionIndex = ref(0);
const expandedChapters = ref([]);
const showSidebar = ref(true);
const isPlaying = ref(false);

// 学习数据
const myCourse = ref({});

// 视频相关
const videoPlayer = ref(null);
const watchTime = ref(0);
const lastUpdateTime = ref(0);

// 权限验证弹窗
const accessModal = ref({
  visible: false,
  type: "login",
});

// 计算属性
const totalSections = computed(() => {
  return chapters.value.reduce((total, chapter) => {
    return total + (chapter.sections?.length || 0);
  }, 0);
});

const hasPreviousSection = computed(() => {
  return currentChapterIndex.value > 0 || currentSectionIndex.value > 0;
});

const hasNextSection = computed(() => {
  const currentChapter = chapters.value[currentChapterIndex.value];
  if (!currentChapter) return false;

  const isLastSectionInChapter =
    currentSectionIndex.value >= (currentChapter.sections?.length || 0) - 1;
  const isLastChapter = currentChapterIndex.value >= chapters.value.length - 1;

  return !(isLastSectionInChapter && isLastChapter);
});

// 简单的Markdown渲染函数
const renderMarkdown = (content) => {
  if (!content) return '';
  
  return content
    // 标题
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    // 粗体
    .replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.*)\*/gim, '<em>$1</em>')
    // 代码块
    .replace(/```([\s\S]*?)```/gim, '<pre><code>$1</code></pre>')
    // 行内代码
    .replace(/`([^`]*)`/gim, '<code>$1</code>')
    // 链接
    .replace(/\[([^\]]*)\]\(([^\)]*)\)/gim, '<a href="$2" target="_blank">$1</a>')
    // 无序列表
    .replace(/^\- (.*$)/gim, '<li>$1</li>')
    .replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
    // 有序列表
    .replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
    // 引用
    .replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>')
    // 换行
    .replace(/\n/gim, '<br>');
};

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (!currentSection.value.content) return '';
  
  try {
    // 首先尝试使用marked库
    const rendered = marked(currentSection.value.content, {
      breaks: true,
      gfm: true,
      headerIds: false,
      mangle: false
    });
    // console.log('原始内容:', currentSection.value.content);
    // console.log('Markdown渲染结果:', rendered);
    return rendered;
  } catch (error) {
    // console.error('Markdown渲染错误，使用简单渲染:', error);
    // 如果marked失败，使用简单的渲染函数
    return renderMarkdown(currentSection.value.content);
  }
});

// 获取课程信息
const fetchCourseInfo = async (courseId) => {
  try {
    const response = await getCourseDetail(courseId);
    course.value = response;
  } catch (error) {
    console.error("获取课程信息失败:", error);
  }
};

// 获取课程章节
const fetchChapters = async (courseId) => {
  try {
    const response = await publicApi.getCourseStructure(courseId);
    chapters.value = response || [];

    // 默认展开所有章节
    expandedChapters.value = chapters.value.map((chapter) => chapter.id);
  } catch (error) {
    console.error("获取课程章节失败:", error);
  }
};

// 获取我的课程信息
const fetchMyCourse = async (courseId) => {
  try {
    // 先检查是否已加入课程
    const joinStatusResponse = await userApi.checkCourseJoinStatus(courseId);
    console.log("检查加入状态:", joinStatusResponse);
    if (joinStatusResponse.data === false) {
      try {
        await userApi.joinCourse(courseId);
        console.log("加入课程成功");
      } catch (error) {
        console.error("加入课程失败:", error);
        // 显示加入课程提示
        showJoinCoursePrompt();
        return;
      }
    }
    // // 处理API返回的数据结构 {code: '200', msg: '请求成功', data: false}
    // const hasJoined = joinStatusResponse?.data
    // console.log('用户是否已加入课程:', hasJoined)
    // if (!hasJoined) {
    //   console.log('用户未加入课程，需要先加入学习')
    //   // 如果是免费课程，自动加入
    //     try {
    //       await userApi.joinCourse(courseId)
    //       console.log('自动加入免费课程成功')
    //     } catch (error) {
    //       console.error('自动加入课程失败:', error)
    //       // 显示加入课程提示
    //       showJoinCoursePrompt()
    //       return
    //     }
    // }

    // 获取我的课程信息
    const response = await userApi.getMyCourseDetail(courseId);

    if (response) {
      myCourse.value = response;
    } else {
      // 初始化默认值
      myCourse.value = {
        id: null,
        userId: null,
        courseId: courseId,
        status: 0,
      };
    }
  } catch (error) {
    console.error("获取我的课程信息失败:", error);
    // 初始化默认值
    myCourse.value = {
      id: null,
      userId: null,
      courseId: courseId,
      status: 0,
      progress: 0,
      lastSectionId: null,
    };
  }
};

// 更新课程加入状态
const updateCourseJoinStatus = async (courseId) => {
  try {
    const hasJoined = await userApi.checkCourseJoinStatus(courseId);
    if (hasJoined) {
      // 重新获取我的课程信息
      await fetchMyCourse(courseId);
    }
    return hasJoined;
  } catch (error) {
    console.error("更新加入状态失败:", error);
    return false;
  }
};

// 验证小节访问权限
const validateSectionAccessLocal = async (section) => {
  // 如果是免费小节，直接允许观看
  if (section.isFree) {
    return true;
  }

  // 检查用户是否登录
  if (!authStore.isAuthenticated) {
    showLoginPrompt();
    return false;
  }

  try {
    // 检查购买状态
    const hasPurchased = await userApi.checkPurchaseStatus(course.value.id);
    const hasJoined = await userApi.checkCourseJoinStatus(course.value.id);

    // 如果是免费课程，检查是否已加入
    if (course.value.price === 0) {
      if (!hasJoined) {
        const joined = await showJoinCoursePrompt();
        return joined;
      }
      return true;
    }

    // 如果是付费课程，检查是否已购买和加入
    if (!hasPurchased) {
      showPurchasePrompt();
      return false;
    }

    if (!hasJoined) {
      const joined = await showJoinCoursePrompt();
      return joined;
    }

    return true;
  } catch (error) {
    console.error("验证访问权限失败:", error);
    return false;
  }
};

// 显示登录提示
const showLoginPrompt = () => {
  accessModal.value = {
    visible: true,
    type: "login",
  };
};

// 显示购买提示
const showPurchasePrompt = () => {
  accessModal.value = {
    visible: true,
    type: "purchase",
  };
};

// 显示加入课程提示
const showJoinCoursePrompt = async () => {
  return new Promise((resolve) => {
    accessModal.value = {
      visible: true,
      type: "join",
    };

    // 保存resolve函数，在确认时调用
    accessModal.value.resolve = resolve;
  });
};

// 关闭权限验证弹窗
const closeAccessModal = () => {
  accessModal.value.visible = false;
  if (accessModal.value.resolve) {
    accessModal.value.resolve(false);
  }
};

// 处理权限验证确认
const handleAccessConfirm = async () => {
  const { type } = accessModal.value;

  try {
    if (type === "login") {
      router.push({ name: "Login", query: { redirect: route.fullPath } });
    } else if (type === "purchase") {
      router.push({ name: "Checkout", params: { courseId: course.value.id } });
    } else if (type === "join") {
      try {
        await userApi.joinCourse(course.value.id);
        console.log("加入课程成功");
        // 重新获取课程信息
        await fetchMyCourse(course.value.id);

        if (accessModal.value.resolve) {
          accessModal.value.resolve(true);
        }
      } catch (error) {
        console.error("加入课程失败:", error);
        if (accessModal.value.resolve) {
          accessModal.value.resolve(false);
        }
      }
    }
  } catch (error) {
    console.error("操作失败:", error);
    if (accessModal.value.resolve) {
      accessModal.value.resolve(false);
    }
  } finally {
    accessModal.value.visible = false;
  }
};

// 选择小节
const selectSection = async (section, chapterIndex, sectionIndex) => {
  // 验证是否可以观看该小节
  const canWatch = await validateSectionAccessLocal(section);
  if (!canWatch) {
    return;
  }

  currentSection.value = section;
  currentChapterIndex.value = chapterIndex;
  currentSectionIndex.value = sectionIndex;

  // 重置播放状态
  isPlaying.value = false;
  watchTime.value = 0;
};

// 播放上一节
const playPreviousSection = () => {
  if (!hasPreviousSection.value) return;

  if (currentSectionIndex.value > 0) {
    // 同章节的上一节
    const section =
      chapters.value[currentChapterIndex.value].sections[
        currentSectionIndex.value - 1
      ];
    selectSection(
      section,
      currentChapterIndex.value,
      currentSectionIndex.value - 1
    );
  } else {
    // 上一章节的最后一节
    const prevChapter = chapters.value[currentChapterIndex.value - 1];
    const lastSectionIndex = prevChapter.sections.length - 1;
    const section = prevChapter.sections[lastSectionIndex];
    selectSection(section, currentChapterIndex.value - 1, lastSectionIndex);
  }
};

// 播放下一节
const playNextSection = () => {
  if (!hasNextSection.value) return;

  const currentChapter = chapters.value[currentChapterIndex.value];

  if (currentSectionIndex.value < currentChapter.sections.length - 1) {
    // 同章节的下一节
    const section = currentChapter.sections[currentSectionIndex.value + 1];
    selectSection(
      section,
      currentChapterIndex.value,
      currentSectionIndex.value + 1
    );
  } else {
    // 下一章节的第一节
    const nextChapter = chapters.value[currentChapterIndex.value + 1];
    const section = nextChapter.sections[0];
    selectSection(section, currentChapterIndex.value + 1, 0);
  }
};

// 切换播放暂停
const togglePlayPause = () => {
  if (!videoPlayer.value) return;

  if (isPlaying.value) {
    videoPlayer.value.pause();
  } else {
    videoPlayer.value.play();
  }
};

// 切换章节展开状态
const toggleChapter = (chapterId) => {
  const index = expandedChapters.value.indexOf(chapterId);
  if (index > -1) {
    expandedChapters.value.splice(index, 1);
  } else {
    expandedChapters.value.push(chapterId);
  }
};

// 切换侧边栏
const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value;
};

// 视频事件处理
const handleVideoLoaded = () => {
  console.log("视频加载完成");
};

const handleTimeUpdate = () => {
  if (!videoPlayer.value) return;

  const currentTime = videoPlayer.value.currentTime;
  watchTime.value = currentTime;
};

const handleVideoEnded = () => {
  // 自动播放下一节
  if (hasNextSection.value) {
    setTimeout(() => {
      playNextSection();
    }, 2000);
  }
};

// 工具函数
const formatDuration = (seconds) => {
  if (!seconds || seconds === 0) return "0分钟";

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  if (hours > 0) {
    return `${hours}:${minutes.toString().padStart(2, "0")}:${secs
      .toString()
      .padStart(2, "0")}`;
  }
  return `${minutes}:${secs.toString().padStart(2, "0")}`;
};

// 页面操作
const goBack = () => {
  router.go(-1);
};

// 监听视频播放状态
watch(
  () => videoPlayer.value,
  (newPlayer) => {
    if (newPlayer) {
      newPlayer.addEventListener("play", () => {
        isPlaying.value = true;
      });

      newPlayer.addEventListener("pause", () => {
        isPlaying.value = false;
      });
    }
  }
);

// 页面离开时的清理工作
onUnmounted(() => {
  // 清理工作
});

// 页面初始化
onMounted(async () => {
  const courseId = route.params.id;
  if (courseId) {
    // 先获取课程信息和章节
    await Promise.all([fetchCourseInfo(courseId), fetchChapters(courseId)]);

    // 然后检查和处理课程加入状态
    await fetchMyCourse(courseId);

    // 如果有指定的小节ID，直接跳转
    const sectionId = route.query.sectionId;
    if (sectionId) {
      // 查找对应的小节
      for (
        let chapterIndex = 0;
        chapterIndex < chapters.value.length;
        chapterIndex++
      ) {
        const chapter = chapters.value[chapterIndex];
        const sectionIndex = chapter.sections?.findIndex(
          (s) => s.id == sectionId
        );
        if (sectionIndex !== -1) {
          selectSection(
            chapter.sections[sectionIndex],
            chapterIndex,
            sectionIndex
          );
          break;
        }
      }
    } else if (
      chapters.value.length > 0 &&
      chapters.value[0].sections?.length > 0
    ) {
      // 默认选择第一个小节
      selectSection(chapters.value[0].sections[0], 0, 0);
    }
  }
});
</script>

<style scoped>
.course-learn {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* 学习页面头部 */
.learn-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #ffffff;
  color: #333;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.btn-back {
  padding: 8px 16px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  color: #495057;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

.course-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}



.btn-toggle {
  padding: 8px 16px;
  background: #2196f3;
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-toggle:hover {
  background: #1976d2;
}

/* 学习内容区域 */
.learn-content {
  flex: 1;
  display: flex;
}

.learn-content.sidebar-hidden .video-section {
  width: 100%;
}

/* 视频区域 */
.video-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  padding: 20px;
}

.video-container {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin: 20px;
}

.video-player {
  height: 65vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  position: relative;
  overflow: hidden;
}

.video-player video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.video-info {
  padding: 25px;
  background: #ffffff;
  color: #333;
}

.section-title {
  font-size: 1.3rem;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.section-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.duration {
  display: flex;
  align-items: center;
  gap: 5px;
}

.section-description {
  line-height: 1.6;
  color: #666;
}

/* 课程内容样式 */
.section-content {
  margin: 20px 0 0 0;
  padding: 25px;
  background: #ffffff;
  border-radius: 8px;
  text-align: start;
  border-left: 4px solid #2196f3;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.content-title {
  color: #2196f3;
  font-size: 1.1rem;
  margin-bottom: 15px;
  font-weight: 600;
}

.content-text {
  color: #333;
  line-height: 1.8;
  font-size: 14px;
}

/* Markdown 内容样式 - 使用深度选择器确保样式生效 */
.content-text.markdown-content :deep(h1),
.content-text.markdown-content :deep(h2),
.content-text.markdown-content :deep(h3),
.content-text.markdown-content :deep(h4),
.content-text.markdown-content :deep(h5),
.content-text.markdown-content :deep(h6) {
  color: #333;
  margin: 20px 0 15px 0;
  font-weight: 600;
  line-height: 1.4;
}

.content-text.markdown-content :deep(h1) {
  font-size: 1.8rem;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 10px;
}

.content-text.markdown-content :deep(h2) {
  font-size: 1.5rem;
  border-bottom: 1px solid #ddd;
  padding-bottom: 8px;
}

.content-text.markdown-content :deep(h3) {
  font-size: 1.3rem;
}

.content-text.markdown-content :deep(h4) {
  font-size: 1.1rem;
}

.content-text.markdown-content :deep(p) {
  margin-bottom: 15px;
  line-height: 1.7;
  color: #333;
}

.content-text.markdown-content :deep(ul),
.content-text.markdown-content :deep(ol) {
  margin: 15px 0;
  padding-left: 25px;
}

.content-text.markdown-content :deep(li) {
  margin-bottom: 8px;
  line-height: 1.6;
  color: #333;
}

.content-text.markdown-content :deep(ul li) {
  list-style-type: disc;
}

.content-text.markdown-content :deep(ol li) {
  list-style-type: decimal;
}

.content-text.markdown-content :deep(code) {
  background: #f1f3f4;
  color: #d73a49;
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  border: 1px solid #e1e4e8;
}

.content-text.markdown-content :deep(pre) {
  background: #f6f8fa;
  color: #24292e;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 20px 0;
  border: 1px solid #e1e4e8;
}

.content-text.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  border: none;
  font-size: 14px;
  color: #24292e;
}

.content-text.markdown-content :deep(blockquote) {
  border-left: 4px solid #2196f3;
  padding: 15px 20px;
  margin: 20px 0;
  background: #f8f9fa;
  color: #666;
  font-style: italic;
  border-radius: 0 8px 8px 0;
}

.content-text.markdown-content :deep(blockquote p) {
  margin: 0;
}

.content-text.markdown-content :deep(a) {
  color: #2196f3;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s ease;
}

.content-text.markdown-content :deep(a:hover) {
  border-bottom-color: #2196f3;
}

.content-text.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e1e4e8;
}

.content-text.markdown-content :deep(th),
.content-text.markdown-content :deep(td) {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #e1e4e8;
  color: #333;
}

.content-text.markdown-content :deep(th) {
  background: #f6f8fa;
  color: #333;
  font-weight: 600;
}

.content-text.markdown-content :deep(tr:last-child td) {
  border-bottom: none;
}

.content-text.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 15px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.content-text.markdown-content :deep(hr) {
  border: none;
  height: 2px;
  background: linear-gradient(to right, transparent, #2196f3, transparent);
  margin: 30px 0;
}

.content-text.markdown-content :deep(strong) {
  color: #333;
  font-weight: 600;
}

.content-text.markdown-content :deep(em) {
  color: #666;
  font-style: italic;
}

/* 控制按钮区域 */
.controls-section {
  background: #ffffff;
  border-top: 1px solid #e9ecef;
  border-bottom: 1px solid #e9ecef;
}

.video-controls {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding: 20px 25px;
  background: #ffffff;
}

.navigation-controls {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding: 20px 25px;
  background: #ffffff;
}

.btn-control {
  padding: 12px 24px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background: #f8f9fa;
  color: #495057;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 120px;
  justify-content: center;
}

.btn-control:hover:not(:disabled) {
  background: #e9ecef;
  border-color: #adb5bd;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-control:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8f9fa;
}

.btn-control.primary {
  background: #2196f3;
  border-color: #2196f3;
  color: white;
  font-weight: 600;
  min-width: 140px;
}

.btn-control.primary:hover {
  background: #1976d2;
  border-color: #1976d2;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

/* 无视频状态 */
.no-video {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  text-align: center;
}

.no-video-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.no-video h3 {
  margin-bottom: 10px;
  color: #999;
}

/* 侧边栏 */
.sidebar {
  width: 350px;
  background: #f8f9fa;
  border-left: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  background: white;
}

.sidebar-header h3 {
  margin-bottom: 10px;
  color: #333;
}

.course-stats {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.chapters-list {
  flex: 1;
}

.chapter-item {
  border-bottom: 1px solid #e9ecef;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: white;
  cursor: pointer;
  transition: background 0.3s ease;
}

.chapter-header:hover {
  background: #f8f9fa;
}

.chapter-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chapter-number {
  background: #2196f3;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.chapter-title {
  font-weight: 500;
  color: #333;
}

.toggle-icon {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s ease;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

.sections-list {
  background: #f8f9fa;
}

.section-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px 12px 40px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid #e9ecef;
}

.section-item:hover {
  background: #e9ecef;
}

.section-item.active {
  background: #2196f3;
  color: white;
}



.section-status {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e9ecef;
  font-size: 12px;
}

.section-item.active .section-status {
  background: rgba(255, 255, 255, 0.2);
}



.status-icon.playing {
  font-size: 10px;
}

.section-number {
  font-size: 12px;
  font-weight: bold;
}

.section-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
}

.section-duration {
  font-size: 12px;
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .learn-header {
    flex-direction: column;
    gap: 10px;
    padding: 10px;
  }

  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .progress-bar {
    width: 150px;
  }

  .sidebar {
    width: 100%;
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    z-index: 100;
    transform: translateX(100%);
    transition: transform 0.3s ease;
  }

  .learn-content:not(.sidebar-hidden) .sidebar {
    transform: translateX(0);
  }

  .video-controls {
    flex-wrap: wrap;
    gap: 10px;
  }

  .btn-control {
    flex: 1;
    min-width: 120px;
  }

  .video-info {
    max-height: 50vh;
  }

  .section-content {
    margin: 15px 0;
    padding: 15px;
  }

  .content-title {
    font-size: 1rem;
  }

  .content-text {
    font-size: 13px;
  }
}
</style>