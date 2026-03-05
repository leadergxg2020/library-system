package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BorrowRecordPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 借阅记录 Mapper
 * readerName/bookTitle 冗余存储于记录中，所有查询均通过 BaseMapper 内置方法实现，无需 JOIN
 */
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecordPO> {
}
