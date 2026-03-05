import request from './request'
import type { BorrowHistoryVO, BorrowRequest, BorrowSuccessVO, PageResult, ReturnResultVO, OverdueRecordVO } from '@/types/api'
import type { ApiResponse } from './request'

export const borrowApi = {
  // 借书
  borrow(data: BorrowRequest) {
    return request
      .post<ApiResponse<BorrowSuccessVO>, ApiResponse<BorrowSuccessVO>>('/borrows', data)
      .then(res => res.data)
  },

  // 还书
  return(data: BorrowRequest) {
    return request
      .post<ApiResponse<ReturnResultVO>, ApiResponse<ReturnResultVO>>('/returns', data)
      .then(res => res.data)
  },

  // 查询借还历史（分页）
  listHistory(params: { readerId?: string; bookIsbn?: string; returned?: boolean; pageNum?: number; pageSize?: number }) {
    return request
      .get<ApiResponse<PageResult<BorrowHistoryVO>>, ApiResponse<PageResult<BorrowHistoryVO>>>('/borrows/history', { params })
      .then((res: any) => res.data as PageResult<BorrowHistoryVO>)
  },

  // 查询逾期列表
  listOverdue() {
    return request
      .get<ApiResponse<OverdueRecordVO[]>, ApiResponse<OverdueRecordVO[]>>('/borrows/overdue')
      .then(res => res.data)
  }
}
