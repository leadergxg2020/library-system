package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.common.exception.BusinessException;
import com.library.dto.BookCreateDTO;
import com.library.dto.BookUpdateDTO;
import com.library.entity.BookPO;
import com.library.mapper.BookMapper;
import com.library.vo.BookVO;
import com.library.vo.ImportResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;

    // ===== 内部工具方法 =====

    /** 将 BookPO 转换为 BookVO */
    private BookVO toVO(BookPO po) {
        BookVO vo = new BookVO();
        vo.setId(po.getId());
        vo.setIsbn(po.getIsbn());
        vo.setTitle(po.getTitle());
        vo.setAuthor(po.getAuthor());
        vo.setPublisher(po.getPublisher());
        vo.setTotalQuantity(po.getTotalQuantity());
        vo.setAvailableQuantity(po.getAvailableQuantity());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }

    /** 按 ISBN 查询 BookPO，不存在则抛 404 */
    private BookPO getBookOrThrow(String isbn) {
        BookPO book = bookMapper.selectOne(
                new LambdaQueryWrapper<BookPO>().eq(BookPO::getIsbn, isbn));
        if (book == null) {
            throw new BusinessException(404, "图书不存在：ISBN " + isbn);
        }
        return book;
    }

    // ===== 业务方法 =====

    /**
     * 新增图书
     */
    @Transactional(rollbackFor = Exception.class)
    public BookVO createBook(BookCreateDTO dto) {
        // 校验 ISBN 唯一性
        if (bookMapper.selectOne(new LambdaQueryWrapper<BookPO>().eq(BookPO::getIsbn, dto.getIsbn())) != null) {
            throw new BusinessException(409, "ISBN " + dto.getIsbn() + " 已存在，如需增加库存请使用修改功能");
        }
        BookPO book = BookPO.builder()
                .isbn(dto.getIsbn())
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .publisher(dto.getPublisher())
                .totalQuantity(dto.getTotalQuantity())
                .availableQuantity(dto.getTotalQuantity())  // 新书可借数量 = 总库存
                .build();
        bookMapper.insert(book);
        log.info("[BOOK_CREATE] isbn={}, title={} - SUCCESS", dto.getIsbn(), dto.getTitle());
        return toVO(book);
    }

    /**
     * 分页查询图书列表（关键字模糊匹配书名/作者/ISBN）
     */
    @Transactional(readOnly = true)
    public PageResult<BookVO> listBooks(String keyword, int pageNum, int pageSize) {
        if (pageSize > 100) {
            throw new BusinessException(400, "pageSize不能超过100");
        }
        LambdaQueryWrapper<BookPO> wrapper = new LambdaQueryWrapper<BookPO>()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(BookPO::getTitle, keyword)
                        .or().like(BookPO::getAuthor, keyword)
                        .or().like(BookPO::getIsbn, keyword))
                .orderByDesc(BookPO::getCreatedAt);
        Page<BookPO> page = bookMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 转换 BookPO → BookVO
        List<BookVO> voList = page.getRecords().stream().map(this::toVO).toList();
        Page<BookVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return PageResult.of(voPage);
    }

    /**
     * 按 ISBN 查询单本图书
     */
    @Transactional(readOnly = true)
    public BookVO getBookByIsbn(String isbn) {
        return toVO(getBookOrThrow(isbn));
    }

    /**
     * 修改图书信息（ISBN 不可改）
     */
    @Transactional(rollbackFor = Exception.class)
    public BookVO updateBook(String isbn, BookUpdateDTO dto) {
        BookPO book = getBookOrThrow(isbn);

        if (dto.getTotalQuantity() != null) {
            // 未还数 = 总库存 - 可借数量
            int unreturned = book.getTotalQuantity() - book.getAvailableQuantity();
            if (dto.getTotalQuantity() < unreturned) {
                throw new BusinessException(422,
                        "总库存不能小于当前未还借阅数量（当前未还：" + unreturned + "本）");
            }
        }

        LambdaUpdateWrapper<BookPO> wrapper = new LambdaUpdateWrapper<BookPO>()
                .eq(BookPO::getIsbn, isbn);
        if (dto.getTitle() != null) wrapper.set(BookPO::getTitle, dto.getTitle());
        if (dto.getAuthor() != null) wrapper.set(BookPO::getAuthor, dto.getAuthor());
        if (dto.getPublisher() != null) wrapper.set(BookPO::getPublisher, dto.getPublisher());
        if (dto.getTotalQuantity() != null) {
            // 同步调整 availableQuantity（保持 unreturned 不变）
            int unreturned = book.getTotalQuantity() - book.getAvailableQuantity();
            int newAvailable = dto.getTotalQuantity() - unreturned;
            wrapper.set(BookPO::getTotalQuantity, dto.getTotalQuantity());
            wrapper.set(BookPO::getAvailableQuantity, newAvailable);
        }
        bookMapper.update(null, wrapper);
        log.info("[BOOK_UPDATE] isbn={} - SUCCESS", isbn);
        return toVO(getBookOrThrow(isbn));
    }

    /**
     * 删除图书（有未还记录则禁删）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(String isbn) {
        BookPO book = getBookOrThrow(isbn);
        int unreturned = book.getTotalQuantity() - book.getAvailableQuantity();
        if (unreturned > 0) {
            throw new BusinessException(422,
                    "该图书有 " + unreturned + " 条未归还借阅记录，无法删除");
        }
        bookMapper.delete(new LambdaQueryWrapper<BookPO>().eq(BookPO::getIsbn, isbn));
        log.info("[BOOK_DELETE] isbn={} - SUCCESS", isbn);
    }

    /**
     * CSV 批量导入图书
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importBooks(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传有效的CSV文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BusinessException(400, "只支持CSV格式文件");
        }

        int successCount = 0;
        int accumulatedCount = 0;
        int failCount = 0;
        List<String> failDetails = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(reader)) {

            java.util.Map<String, Integer> headers = parser.getHeaderMap();
            if (headers == null || !headers.containsKey("isbn") || !headers.containsKey("title")
                    || !headers.containsKey("author") || !headers.containsKey("publisher")
                    || !headers.containsKey("quantity")) {
                throw new BusinessException(400,
                        "CSV文件格式错误，请确认表头包含：isbn,title,author,publisher,quantity");
            }

            int rowNum = 1;
            for (CSVRecord record : parser) {
                rowNum++;
                String isbn = record.get("isbn");
                String title = record.get("title");
                String author = record.get("author");
                String publisher = record.get("publisher");
                String quantityStr = record.get("quantity");

                if (isbn == null || isbn.isBlank() || !isbn.matches("^\\d{10}(\\d{3})?$")) {
                    failCount++;
                    failDetails.add("第" + rowNum + "行：ISBN格式不正确（isbn=" + isbn + "）");
                    continue;
                }
                if (title == null || title.isBlank()) {
                    failCount++;
                    failDetails.add("第" + rowNum + "行：书名不能为空（isbn=" + isbn + "）");
                    continue;
                }
                if (author == null || author.isBlank()) {
                    failCount++;
                    failDetails.add("第" + rowNum + "行：作者不能为空（isbn=" + isbn + "）");
                    continue;
                }
                if (publisher == null || publisher.isBlank()) {
                    failCount++;
                    failDetails.add("第" + rowNum + "行：出版社不能为空（isbn=" + isbn + "）");
                    continue;
                }
                int quantity;
                try {
                    quantity = Integer.parseInt(quantityStr);
                    if (quantity <= 0) {
                        failCount++;
                        failDetails.add("第" + rowNum + "行：数量必须大于0（isbn=" + isbn + "）");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    failCount++;
                    failDetails.add("第" + rowNum + "行：数量格式不正确（isbn=" + isbn + "）");
                    continue;
                }

                BookPO existing = bookMapper.selectOne(
                        new LambdaQueryWrapper<BookPO>().eq(BookPO::getIsbn, isbn));
                if (existing != null) {
                    // ISBN 已存在：totalQuantity 和 availableQuantity 同步累加
                    bookMapper.update(null, new LambdaUpdateWrapper<BookPO>()
                            .eq(BookPO::getIsbn, isbn)
                            .set(BookPO::getTotalQuantity, existing.getTotalQuantity() + quantity)
                            .set(BookPO::getAvailableQuantity, existing.getAvailableQuantity() + quantity));
                    accumulatedCount++;
                    log.info("[BOOK_IMPORT_ACCUMULATE] isbn={}, added={}", isbn, quantity);
                } else {
                    BookPO book = BookPO.builder()
                            .isbn(isbn)
                            .title(title)
                            .author(author)
                            .publisher(publisher)
                            .totalQuantity(quantity)
                            .availableQuantity(quantity)  // 新书可借 = 总库存
                            .build();
                    bookMapper.insert(book);
                    successCount++;
                    log.info("[BOOK_IMPORT_NEW] isbn={}, title={}", isbn, title);
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[BOOK_IMPORT_ERROR] 解析CSV失败", e);
            throw new BusinessException(400, "CSV文件解析失败：" + e.getMessage());
        }

        log.info("[BOOK_IMPORT] 导入完成 - 新增:{}, 累加:{}, 失败:{}", successCount, accumulatedCount, failCount);
        return ImportResultVO.builder()
                .successCount(successCount)
                .accumulatedCount(accumulatedCount)
                .failCount(failCount)
                .failDetails(failDetails)
                .build();
    }
}
