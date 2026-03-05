package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.library.common.exception.BusinessException;
import com.library.dto.BorrowRequest;
import com.library.entity.BookPO;
import com.library.entity.BorrowRecordPO;
import com.library.entity.ReaderPO;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.ReaderMapper;
import com.library.common.PageResult;
import com.library.vo.BorrowHistoryVO;
import com.library.vo.BorrowSuccessVO;
import com.library.vo.OverdueRecordVO;
import com.library.vo.ReturnResultVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    // ===== 内部工具方法 =====

    private BorrowHistoryVO toHistoryVO(BorrowRecordPO record) {
        BorrowHistoryVO vo = new BorrowHistoryVO();
        vo.setRecordId(record.getId());
        vo.setReaderId(record.getReaderId());
        vo.setReaderName(record.getReaderName());
        vo.setBookIsbn(record.getBookIsbn());
        vo.setBookTitle(record.getBookTitle());
        vo.setBorrowDate(record.getBorrowDate());
        vo.setDueDate(record.getDueDate());
        vo.setReturnDate(record.getReturnDate());
        vo.setCreatedAt(record.getCreatedAt());
        boolean isReturned = record.getReturnDate() != null;
        vo.setReturned(isReturned);
        if (isReturned && record.getReturnDate().isAfter(record.getDueDate())) {
            vo.setOverdueDays((int) (record.getReturnDate().toEpochDay() - record.getDueDate().toEpochDay()));
        }
        return vo;
    }

    private BorrowSuccessVO toBorrowSuccessVO(BorrowRecordPO record) {
        BorrowSuccessVO vo = new BorrowSuccessVO();
        vo.setRecordId(record.getId());
        vo.setReaderId(record.getReaderId());
        vo.setReaderName(record.getReaderName());
        vo.setBookIsbn(record.getBookIsbn());
        vo.setBookTitle(record.getBookTitle());
        vo.setBorrowDate(record.getBorrowDate());
        vo.setDueDate(record.getDueDate());
        return vo;
    }

    private OverdueRecordVO toOverdueVO(BorrowRecordPO record) {
        OverdueRecordVO vo = new OverdueRecordVO();
        vo.setRecordId(record.getId());
        vo.setReaderId(record.getReaderId());
        vo.setReaderName(record.getReaderName());
        vo.setBookIsbn(record.getBookIsbn());
        vo.setBookTitle(record.getBookTitle());
        vo.setBorrowDate(record.getBorrowDate());
        vo.setDueDate(record.getDueDate());
        vo.setOverdueDays((int) (LocalDate.now().toEpochDay() - record.getDueDate().toEpochDay()));
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    // ===== 业务方法 =====

    /**
     * 借书
     * 校验顺序：读者存在 → 禁借期 → 借阅上限 → 图书存在 → 库存
     * 借出后：availableQuantity - 1，currentBorrowCount + 1
     */
    @Transactional(rollbackFor = Exception.class)
    public BorrowSuccessVO borrowBook(BorrowRequest request) {
        // 1. 校验读者存在
        ReaderPO reader = readerMapper.selectById(request.getReaderId());
        if (reader == null) {
            throw new BusinessException(404, "读者不存在：" + request.getReaderId());
        }

        // 2. 校验禁借期（ban_until 不为 null 且今天 <= ban_until 则禁借，含当天）
        LocalDate today = LocalDate.now();
        if (reader.getBanUntil() != null && !today.isAfter(reader.getBanUntil())) {
            throw new BusinessException(422,
                    "读者" + reader.getName() + "在禁借期内，禁借截止日期：" + reader.getBanUntil()
                    + "（因" + reader.getBanReasonDate() + "超期还书）");
        }

        // 3. 校验借阅上限（直接读存储字段，无需 COUNT 查询）
        int currentCount = reader.getCurrentBorrowCount() != null ? reader.getCurrentBorrowCount() : 0;
        if (currentCount >= reader.getMaxBorrowCount()) {
            throw new BusinessException(422,
                    "读者" + reader.getName() + "已借阅" + currentCount + "本，已达最大借阅上限（"
                    + reader.getMaxBorrowCount() + "本），请先归还后再借");
        }

        // 4. 校验图书存在
        BookPO book = bookMapper.selectOne(
                new LambdaQueryWrapper<BookPO>().eq(BookPO::getIsbn, request.getBookIsbn()));
        if (book == null) {
            throw new BusinessException(404, "图书不存在：ISBN " + request.getBookIsbn());
        }

        // 5. 校验库存（直接读存储字段，无需子查询）
        int available = book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 0;
        if (available <= 0) {
            throw new BusinessException(422,
                    "《" + book.getTitle() + "》库存不足，当前可借数量：" + available);
        }

        // 6. 创建借阅记录（冗余存储读者姓名和书名，避免后续 JOIN）
        LocalDate dueDate = today.plusDays(30);
        BorrowRecordPO record = BorrowRecordPO.builder()
                .readerId(request.getReaderId())
                .bookIsbn(request.getBookIsbn())
                .readerName(reader.getName())
                .bookTitle(book.getTitle())
                .borrowDate(today)
                .dueDate(dueDate)
                .build();
        borrowRecordMapper.insert(record);

        // 7. 同步更新库存和借阅数（在同一事务中）
        bookMapper.update(null, new LambdaUpdateWrapper<BookPO>()
                .eq(BookPO::getIsbn, request.getBookIsbn())
                .set(BookPO::getAvailableQuantity, available - 1));
        readerMapper.update(null, new LambdaUpdateWrapper<ReaderPO>()
                .eq(ReaderPO::getReaderId, request.getReaderId())
                .set(ReaderPO::getCurrentBorrowCount, currentCount + 1));

        log.info("[BORROW] readerId={}, isbn={}, dueDate={} - SUCCESS",
                request.getReaderId(), request.getBookIsbn(), dueDate);

        return toBorrowSuccessVO(record);
    }

    /**
     * 还书
     * 归还后：availableQuantity + 1，currentBorrowCount - 1
     * 超期时更新禁借信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnResultVO returnBook(BorrowRequest request) {
        // 1. 查找未还借阅记录（用 BaseMapper.selectOne + wrapper 替代原自定义方法）
        BorrowRecordPO record = borrowRecordMapper.selectOne(
                new LambdaQueryWrapper<BorrowRecordPO>()
                        .eq(BorrowRecordPO::getReaderId, request.getReaderId())
                        .eq(BorrowRecordPO::getBookIsbn, request.getBookIsbn())
                        .isNull(BorrowRecordPO::getReturnDate)
                        .last("LIMIT 1"));
        if (record == null) {
            throw new BusinessException(404,
                    "未找到读者" + request.getReaderId()
                    + "借阅ISBN " + request.getBookIsbn() + "的未还记录");
        }

        // 2. 设置还书日期（用 updateById 替代原自定义 SQL）
        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);
        borrowRecordMapper.updateById(record);

        // 3. 恢复图书可借数量 + 1
        bookMapper.update(null, new LambdaUpdateWrapper<BookPO>()
                .eq(BookPO::getIsbn, request.getBookIsbn())
                .setSql("available_quantity = available_quantity + 1"));

        // 4. 减少读者借阅数 - 1（不低于0）
        ReaderPO reader = readerMapper.selectById(request.getReaderId());
        int newBorrowCount = reader != null && reader.getCurrentBorrowCount() != null
                ? Math.max(0, reader.getCurrentBorrowCount() - 1) : 0;
        readerMapper.update(null, new LambdaUpdateWrapper<ReaderPO>()
                .eq(ReaderPO::getReaderId, request.getReaderId())
                .set(ReaderPO::getCurrentBorrowCount, newBorrowCount));

        // 5. 判断是否超期（严格大于 due_date）
        boolean overdue = returnDate.isAfter(record.getDueDate());
        int overdueDays = 0;
        LocalDate effectiveBanUntil = null;

        if (overdue) {
            overdueDays = (int) (returnDate.toEpochDay() - record.getDueDate().toEpochDay());
            LocalDate candidateBan = returnDate.plusDays(30);
            // Java 层实现 GREATEST 逻辑：取新旧禁借截止日期中较晚的一个
            LocalDate existingBan = reader != null ? reader.getBanUntil() : null;
            effectiveBanUntil = (existingBan == null || candidateBan.isAfter(existingBan))
                    ? candidateBan : existingBan;
            boolean updateReason = (existingBan == null || candidateBan.isAfter(existingBan));
            LambdaUpdateWrapper<ReaderPO> banWrapper = new LambdaUpdateWrapper<ReaderPO>()
                    .eq(ReaderPO::getReaderId, request.getReaderId())
                    .set(ReaderPO::getBanUntil, effectiveBanUntil);
            if (updateReason) {
                banWrapper.set(ReaderPO::getBanReasonDate, returnDate);
            }
            readerMapper.update(null, banWrapper);
            log.info("[RETURN_OVERDUE] readerId={}, isbn={}, overdueDays={}, banUntil={}",
                    request.getReaderId(), request.getBookIsbn(), overdueDays, effectiveBanUntil);
        } else {
            log.info("[RETURN] readerId={}, isbn={} - ON_TIME", request.getReaderId(), request.getBookIsbn());
        }

        // 6. 构建返回结果（readerName/bookTitle 从冗余字段读取，无需额外查询）
        ReturnResultVO vo = new ReturnResultVO();
        vo.setRecordId(record.getId());
        vo.setReaderId(record.getReaderId());
        vo.setReaderName(record.getReaderName());
        vo.setBookIsbn(record.getBookIsbn());
        vo.setBookTitle(record.getBookTitle());
        vo.setBorrowDate(record.getBorrowDate());
        vo.setDueDate(record.getDueDate());
        vo.setReturnDate(returnDate);
        vo.setOverdue(overdue);
        vo.setOverdueDays(overdue ? overdueDays : null);
        vo.setBanUntil(effectiveBanUntil);
        return vo;
    }

    /**
     * 分页查询借还历史（全量，可按读者/ISBN/状态过滤）
     */
    @Transactional(readOnly = true)
    public PageResult<BorrowHistoryVO> listBorrowHistory(
            String readerId, String bookIsbn, Boolean returned, int pageNum, int pageSize) {
        LambdaQueryWrapper<BorrowRecordPO> wrapper = new LambdaQueryWrapper<BorrowRecordPO>()
                .eq(readerId != null && !readerId.isBlank(), BorrowRecordPO::getReaderId, readerId)
                .eq(bookIsbn != null && !bookIsbn.isBlank(), BorrowRecordPO::getBookIsbn, bookIsbn)
                .isNull(Boolean.FALSE.equals(returned), BorrowRecordPO::getReturnDate)
                .isNotNull(Boolean.TRUE.equals(returned), BorrowRecordPO::getReturnDate)
                .orderByDesc(BorrowRecordPO::getId);
        Page<BorrowRecordPO> page = borrowRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<BorrowHistoryVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toHistoryVO).toList());
        return PageResult.of(voPage);
    }

    /**
     * 查询当前逾期未还的借阅列表（return_date IS NULL AND due_date < 今天）
     * 超期天数在 Java 层计算，无需 JOIN 查询（readerName/bookTitle 已冗余存储）
     */
    @Transactional(readOnly = true)
    public List<OverdueRecordVO> listOverdueRecords() {
        LocalDate today = LocalDate.now();
        return borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecordPO>()
                        .isNull(BorrowRecordPO::getReturnDate)
                        .lt(BorrowRecordPO::getDueDate, today)
                        .orderByAsc(BorrowRecordPO::getDueDate))
                .stream().map(this::toOverdueVO).toList();
    }
}
