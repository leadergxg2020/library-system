package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.ReaderCreateDTO;
import com.library.dto.ReaderUpdateDTO;
import com.library.service.ReaderService;
import com.library.vo.ReaderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @PostMapping
    public Result<ReaderVO> createReader(@Valid @RequestBody ReaderCreateDTO dto) {
        return Result.success(readerService.createReader(dto));
    }

    @GetMapping
    public Result<PageResult<ReaderVO>> listReaders(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(readerService.listReaders(keyword, pageNum, pageSize));
    }

    @GetMapping("/{readerId}")
    public Result<ReaderVO> getReader(@PathVariable String readerId) {
        return Result.success(readerService.getReaderById(readerId));
    }

    @PutMapping("/{readerId}")
    public Result<ReaderVO> updateReader(@PathVariable String readerId,
                                          @Valid @RequestBody ReaderUpdateDTO dto) {
        return Result.success(readerService.updateReader(readerId, dto));
    }

    @DeleteMapping("/{readerId}")
    public Result<Void> deleteReader(@PathVariable String readerId) {
        readerService.deleteReader(readerId);
        return Result.success(null);
    }
}
