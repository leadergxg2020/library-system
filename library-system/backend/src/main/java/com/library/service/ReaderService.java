package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.common.exception.BusinessException;
import com.library.dto.ReaderCreateDTO;
import com.library.dto.ReaderUpdateDTO;
import com.library.entity.ReaderPO;
import com.library.mapper.ReaderMapper;
import com.library.vo.ReaderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReaderService {

    private final ReaderMapper readerMapper;

    // ===== 内部工具方法 =====

    /** 将 ReaderPO 转换为 ReaderVO（banned 字段在 Java 层计算） */
    private ReaderVO toVO(ReaderPO po) {
        ReaderVO vo = new ReaderVO();
        vo.setReaderId(po.getReaderId());
        vo.setName(po.getName());
        vo.setContact(po.getContact());
        vo.setMaxBorrowCount(po.getMaxBorrowCount());
        vo.setCurrentBorrowCount(po.getCurrentBorrowCount() != null ? po.getCurrentBorrowCount() : 0);
        vo.setBanUntil(po.getBanUntil());
        vo.setBanReasonDate(po.getBanReasonDate());
        // banned = 禁借截止日期不为空 且 今天 <= 禁借截止日期（含当天）
        boolean banned = po.getBanUntil() != null && !LocalDate.now().isAfter(po.getBanUntil());
        vo.setBanned(banned);
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }

    /** 按 readerId 查询 ReaderPO，不存在则抛 404 */
    private ReaderPO getReaderOrThrow(String readerId) {
        ReaderPO reader = readerMapper.selectById(readerId);
        if (reader == null) {
            throw new BusinessException(404, "读者不存在：" + readerId);
        }
        return reader;
    }

    // ===== 业务方法 =====

    /**
     * 注册读者
     */
    @Transactional(rollbackFor = Exception.class)
    public ReaderVO createReader(ReaderCreateDTO dto) {
        if (readerMapper.selectById(dto.getReaderId()) != null) {
            throw new BusinessException(409, "读者证号 " + dto.getReaderId() + " 已存在");
        }
        int maxBorrow = dto.getMaxBorrowCount() != null ? dto.getMaxBorrowCount() : 5;
        ReaderPO reader = ReaderPO.builder()
                .readerId(dto.getReaderId())
                .name(dto.getName())
                .contact(dto.getContact())
                .maxBorrowCount(maxBorrow)
                .currentBorrowCount(0)  // 新注册读者初始借阅数为 0
                .build();
        readerMapper.insert(reader);
        log.info("[READER_CREATE] readerId={}, name={} - SUCCESS", dto.getReaderId(), dto.getName());
        return toVO(reader);
    }

    /**
     * 分页查询读者列表
     */
    @Transactional(readOnly = true)
    public PageResult<ReaderVO> listReaders(String keyword, int pageNum, int pageSize) {
        if (pageSize > 100) {
            throw new BusinessException(400, "pageSize不能超过100");
        }
        LambdaQueryWrapper<ReaderPO> wrapper = new LambdaQueryWrapper<ReaderPO>()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(ReaderPO::getName, keyword)
                        .or().like(ReaderPO::getReaderId, keyword))
                .orderByDesc(ReaderPO::getCreatedAt);
        Page<ReaderPO> page = readerMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<ReaderVO> voList = page.getRecords().stream().map(this::toVO).toList();
        Page<ReaderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return PageResult.of(voPage);
    }

    /**
     * 按 readerId 查询读者
     */
    @Transactional(readOnly = true)
    public ReaderVO getReaderById(String readerId) {
        return toVO(getReaderOrThrow(readerId));
    }

    /**
     * 修改读者信息（readerId 不可改）
     */
    @Transactional(rollbackFor = Exception.class)
    public ReaderVO updateReader(String readerId, ReaderUpdateDTO dto) {
        ReaderPO reader = getReaderOrThrow(readerId);
        LambdaUpdateWrapper<ReaderPO> wrapper = new LambdaUpdateWrapper<ReaderPO>()
                .eq(ReaderPO::getReaderId, readerId);
        if (dto.getName() != null) wrapper.set(ReaderPO::getName, dto.getName());
        if (dto.getContact() != null) wrapper.set(ReaderPO::getContact, dto.getContact());
        if (dto.getMaxBorrowCount() != null) wrapper.set(ReaderPO::getMaxBorrowCount, dto.getMaxBorrowCount());
        readerMapper.update(null, wrapper);
        log.info("[READER_UPDATE] readerId={} - SUCCESS", readerId);
        return toVO(getReaderOrThrow(readerId));
    }

    /**
     * 删除读者（有未还借阅记录则禁删）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteReader(String readerId) {
        ReaderPO reader = getReaderOrThrow(readerId);
        // 直接从存储字段判断，无需额外 COUNT 查询
        if (reader.getCurrentBorrowCount() != null && reader.getCurrentBorrowCount() > 0) {
            throw new BusinessException(422,
                    "该读者有 " + reader.getCurrentBorrowCount() + " 条未归还借阅记录，无法删除");
        }
        readerMapper.deleteById(readerId);
        log.info("[READER_DELETE] readerId={} - SUCCESS", readerId);
    }
}
