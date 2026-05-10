<!--------------------------------
 - @Author: Ronnie Zhang
 - @LastEditor: Ronnie Zhang
 - @LastEditTime: 2023/12/05 21:30:11
 - @Email: zclzone@outlook.com
 - Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 --------------------------------->

<template>
  <AppPage show-footer>
    <n-card>
      <n-space align="center">
        <n-avatar round :size="100" :src="userStore.avatar" />
        <div class="ml-20">
          <div class="flex items-center text-16">
            <span>用户名:</span>
            <span class="ml-12 opacity-80">{{ userStore.username }}</span>
            <n-button class="ml-32" type="primary" text @click="pwdModalRef.open()">
              <i class="i-fe:edit mr-4" />
              修改密码
            </n-button>
          </div>
          <div class="mt-16 flex items-center">
            <n-button type="primary" ghost @click="avatarModalRef.open()">
              更改头像
            </n-button>
            <span class="ml-12 opacity-60">
              修改头像只支持在线链接，不提供上传图片功能，如有需要可自行对接！
            </span>
          </div>
        </div>
      </n-space>
    </n-card>

    <n-card class="mt-20" title="个人资料信息">
      <template #header-extra>
        <n-button type="primary" text @click="profileModalRef.open()">
          <i class="i-fe:edit mr-4" />
          修改资料
        </n-button>
      </template>

      <n-descriptions
        label-placement="left"
        :label-style="{ width: '200px', textAlign: 'center' }"
        :column="1"
        bordered
      >
        <n-descriptions-item label="昵称">
          {{ userStore.nickName || '未填写' }}
        </n-descriptions-item>
        <n-descriptions-item label="手机号">
          {{ userStore.userInfo?.phone || '未填写' }}
        </n-descriptions-item>
        <n-descriptions-item label="角色">
          {{ userStore.userInfo?.roles?.join(', ') || '无' }}
        </n-descriptions-item>
      </n-descriptions>
    </n-card>

    <MeModal ref="avatarModalRef" width="420px" title="更改头像" :show-footer="false">
      <n-upload
        class="mx-auto w-[75%] p-20 text-center"
        :custom-request="handleUpload"
        :show-file-list="false"
        accept=".png,.jpg,.jpeg"
        @before-upload="onBeforeUpload"
      >
        <n-upload-dragger>
          <div class="h-150 f-c-c flex-col">
            <i class="i-mdi:upload mb-12 text-68 color-primary" />
            <n-text class="text-14 color-gray">
              点击或者拖动文件到该区域来上传
            </n-text>
          </div>
        </n-upload-dragger>
      </n-upload>
    </MeModal>

    <MeModal ref="pwdModalRef" title="修改密码" width="420px" @ok="handlePwdSave()">
      <n-form
        ref="pwdFormRef"
        :model="pwdForm"
        label-placement="left"
        require-mark-placement="left"
      >
        <n-form-item label="原密码" path="oldPassword" :rule="required">
          <n-input v-model:value="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password-on="mousedown" />
        </n-form-item>
        <n-form-item label="新密码" path="newPassword" :rule="required">
          <n-input v-model:value="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password-on="mousedown" />
        </n-form-item>
      </n-form>
    </MeModal>

    <MeModal ref="profileModalRef" title="修改资料" width="420px" @ok="handleProfileSave()">
      <n-form ref="profileFormRef" :model="profileForm" label-placement="left">
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="profileForm.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="profileForm.phone" placeholder="请输入手机号" />
        </n-form-item>
      </n-form>
    </MeModal>
  </AppPage>
</template>

<script setup>
import { MeModal } from '@/components'
import { useForm, useModal } from '@/composables'
import { useUserStore } from '@/store'
import { getUserInfo } from '@/store/helper'
import api from './api'

const userStore = useUserStore()
const required = {
  required: true,
  message: '此为必填项',
  trigger: ['blur', 'change'],
}

const [pwdModalRef] = useModal()
const [pwdFormRef, pwdForm, pwdValidation] = useForm()

async function handlePwdSave() {
  await pwdValidation()
  await api.changePassword(pwdForm.value)
  $message.success('密码修改成功')
  await refreshUserInfo()
  pwdModalRef.value.close()
}

const [avatarModalRef] = useModal()

function onBeforeUpload({ file }) {
  if (!file.file?.type.startsWith('image/')) {
    $message.error('只能上传图片')
    return false
  }
  return true
}

async function handleUpload({ file, onFinish, onError }) {
  try {
    $message.loading('上传中...')
    const formData = new FormData()
    formData.append('file', file.file)
    const res = await api.upload(formData)
    await api.updateProfile({ avatar: res.data })
    $message.success('头像修改成功')
    await refreshUserInfo()
    avatarModalRef.value.close()
    onFinish()
  }
  catch (err) {
    console.error(err)
    $message.error('上传失败')
    onError()
  }
}

const [profileModalRef] = useModal()
const [profileFormRef, profileForm, profileValidation] = useForm({
  id: userStore.userInfo.id,
  nickname: userStore.userInfo.nickname,
  phone: userStore.userInfo.phone,
})
async function handleProfileSave() {
  await profileValidation()
  await api.updateProfile(profileForm.value)
  $message.success('资料修改成功')
  await refreshUserInfo()
  profileModalRef.value.close()
}

async function refreshUserInfo() {
  await getUserInfo()
}
</script>
