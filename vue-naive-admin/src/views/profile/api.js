/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/05 21:30:03
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  upload: data => request.post('/api/public/files/avatar', data),
  changePassword: data => request.post('/api/auth/change-password', data),
  updateProfile: data => request.post(`/api/auth/update-profile`, data),
}
