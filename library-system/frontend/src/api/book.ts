import request from './request'
import type { BookVO, BookCreateDTO, BookUpdateDTO, ImportResultVO, PageResult } from '@/types/api'
import type { ApiResponse } from './request'

export const bookApi = {
  // 新增图书
  create(data: BookCreateDTO) {
    return request.post<ApiResponse<BookVO>, ApiResponse<BookVO>>('/books', data).then(res => res.data)
  },

  // CSV 批量导入
  importCsv(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request
      .post<ApiResponse<ImportResultVO>, ApiResponse<ImportResultVO>>('/books/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      .then(res => res.data)
  },

  // 分页查询图书列表
  list(params?: { keyword?: string; pageNum?: number; pageSize?: number }) {
    return request
      .get<ApiResponse<PageResult<BookVO>>, ApiResponse<PageResult<BookVO>>>('/books', { params })
      .then(res => res.data)
  },

  // 按 ISBN 查询单本图书
  getByIsbn(isbn: string) {
    return request
      .get<ApiResponse<BookVO>, ApiResponse<BookVO>>(`/books/${isbn}`)
      .then(res => res.data)
  },

  // 修改图书
  update(isbn: string, data: BookUpdateDTO) {
    return request
      .put<ApiResponse<BookVO>, ApiResponse<BookVO>>(`/books/${isbn}`, data)
      .then(res => res.data)
  },

  // 删除图书
  delete(isbn: string) {
    return request
      .delete<ApiResponse<void>, ApiResponse<void>>(`/books/${isbn}`)
      .then(res => res.data)
  }
}
