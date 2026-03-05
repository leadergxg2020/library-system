import request from './request'
import type { ReaderVO, ReaderCreateDTO, ReaderUpdateDTO, PageResult } from '@/types/api'
import type { ApiResponse } from './request'

export const readerApi = {
  // 注册读者
  create(data: ReaderCreateDTO) {
    return request
      .post<ApiResponse<ReaderVO>, ApiResponse<ReaderVO>>('/readers', data)
      .then(res => res.data)
  },

  // 分页查询读者列表
  list(params?: { keyword?: string; pageNum?: number; pageSize?: number }) {
    return request
      .get<ApiResponse<PageResult<ReaderVO>>, ApiResponse<PageResult<ReaderVO>>>('/readers', {
        params
      })
      .then(res => res.data)
  },

  // 按 readerId 查询读者
  getById(readerId: string) {
    return request
      .get<ApiResponse<ReaderVO>, ApiResponse<ReaderVO>>(`/readers/${readerId}`)
      .then(res => res.data)
  },

  // 修改读者信息
  update(readerId: string, data: ReaderUpdateDTO) {
    return request
      .put<ApiResponse<ReaderVO>, ApiResponse<ReaderVO>>(`/readers/${readerId}`, data)
      .then(res => res.data)
  },

  // 删除读者
  delete(readerId: string) {
    return request
      .delete<ApiResponse<void>, ApiResponse<void>>(`/readers/${readerId}`)
      .then(res => res.data)
  }
}
