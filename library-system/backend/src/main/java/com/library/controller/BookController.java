package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.BookCreateDTO;
import com.library.dto.BookUpdateDTO;
import com.library.service.BookService;
import com.library.vo.BookVO;
import com.library.vo.ImportResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public Result<BookVO> createBook(@Valid @RequestBody BookCreateDTO dto) {
        return Result.success(bookService.createBook(dto));
    }

    @PostMapping("/import")
    public Result<ImportResultVO> importBooks(@RequestParam("file") MultipartFile file) {
        return Result.success(bookService.importBooks(file));
    }

    @GetMapping
    public Result<PageResult<BookVO>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bookService.listBooks(keyword, pageNum, pageSize));
    }

    @GetMapping("/{isbn}")
    public Result<BookVO> getBook(@PathVariable String isbn) {
        return Result.success(bookService.getBookByIsbn(isbn));
    }

    @PutMapping("/{isbn}")
    public Result<BookVO> updateBook(@PathVariable String isbn,
                                     @Valid @RequestBody BookUpdateDTO dto) {
        return Result.success(bookService.updateBook(isbn, dto));
    }

    @DeleteMapping("/{isbn}")
    public Result<Void> deleteBook(@PathVariable String isbn) {
        bookService.deleteBook(isbn);
        return Result.success(null);
    }
}
