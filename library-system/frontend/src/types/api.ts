// ===== 通用类型 =====

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// ===== 图书相关 =====

export interface BookVO {
  id: number
  isbn: string
  title: string
  author: string
  publisher: string
  totalQuantity: number
  availableQuantity: number
  createdAt: string
  updatedAt: string
}

export interface BookCreateDTO {
  isbn: string
  title: string
  author: string
  publisher: string
  totalQuantity: number
}

export interface BookUpdateDTO {
  title?: string
  author?: string
  publisher?: string
  totalQuantity?: number
}

export interface ImportResultVO {
  successCount: number
  accumulatedCount: number
  failCount: number
  failDetails: string[]
}

// ===== 读者相关 =====

export interface ReaderVO {
  readerId: string
  name: string
  contact: string
  maxBorrowCount: number
  currentBorrowCount: number
  banUntil: string | null
  banReasonDate: string | null
  banned: boolean
  createdAt: string
  updatedAt: string
}

export interface ReaderCreateDTO {
  readerId: string
  name: string
  contact?: string
  maxBorrowCount?: number
}

export interface ReaderUpdateDTO {
  name?: string
  contact?: string
  maxBorrowCount?: number
}

// ===== 借还相关 =====

export interface BorrowRequest {
  readerId: string
  bookIsbn: string
}

export interface BorrowSuccessVO {
  recordId: number
  readerId: string
  readerName: string
  bookIsbn: string
  bookTitle: string
  borrowDate: string
  dueDate: string
}

export interface ReturnResultVO {
  recordId: number
  readerId: string
  readerName: string
  bookIsbn: string
  bookTitle: string
  borrowDate: string
  dueDate: string
  returnDate: string
  overdue: boolean
  overdueDays: number | null
  banUntil: string | null
}

export interface BorrowHistoryVO {
  recordId: number
  readerId: string
  readerName: string
  bookIsbn: string
  bookTitle: string
  borrowDate: string
  dueDate: string
  returnDate: string | null
  returned: boolean
  overdueDays: number | null
  createdAt: string
}

export interface OverdueRecordVO {
  recordId: number
  readerId: string
  readerName: string
  bookIsbn: string
  bookTitle: string
  borrowDate: string
  dueDate: string
  overdueDays: number
  createdAt: string
}
