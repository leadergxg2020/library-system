package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.BorrowRequest;
import com.library.service.BorrowService;
import com.library.vo.BorrowHistoryVO;
import com.library.vo.BorrowSuccessVO;
import com.library.vo.OverdueRecordVO;
import com.library.vo.ReturnResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /**
     * POST /api/borrows — 借书
     */
    @PostMapping("/api/borrows")
    public Result<BorrowSuccessVO> borrowBook(@Valid @RequestBody BorrowRequest request) {
        return Result.success(borrowService.borrowBook(request));
    }

    /**
     * POST /api/returns — 还书
     */
    @PostMapping("/api/returns")
    public Result<ReturnResultVO> returnBook(@Valid @RequestBody BorrowRequest request) {
        return Result.success(borrowService.returnBook(request));
    }

    /**
     * GET /api/borrows/history — 借还历史（分页，可按读者/ISBN/状态过滤）
     */
    @GetMapping("/api/borrows/history")
    public Result<PageResult<BorrowHistoryVO>> listBorrowHistory(
            @RequestParam(required = false) String readerId,
            @RequestParam(required = false) String bookIsbn,
            @RequestParam(required = false) Boolean returned,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(borrowService.listBorrowHistory(readerId, bookIsbn, returned, pageNum, pageSize));
    }

    /**
     * GET /api/borrows/overdue — 逾期未还列表
     */
    @GetMapping("/api/borrows/overdue")
    public Result<List<OverdueRecordVO>> listOverdueRecords() {
        return Result.success(borrowService.listOverdueRecords());
    }
}
