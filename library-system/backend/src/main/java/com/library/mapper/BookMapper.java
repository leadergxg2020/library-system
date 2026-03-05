package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书 Mapper
 * 所有查询均通过 BaseMapper 内置方法 + LambdaQueryWrapper 实现，无自定义 SQL
 */
@Mapper
public interface BookMapper extends BaseMapper<BookPO> {
}
