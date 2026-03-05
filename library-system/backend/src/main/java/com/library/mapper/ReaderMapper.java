package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.ReaderPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 读者 Mapper
 * 禁借逻辑（原 GREATEST/CASE WHEN）已移至 BorrowService Java 层处理，无需自定义 SQL
 */
@Mapper
public interface ReaderMapper extends BaseMapper<ReaderPO> {
}
