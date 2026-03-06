package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.AdminPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper extends BaseMapper<AdminPO> {

    @Select("SELECT * FROM t_admin WHERE username = #{username} LIMIT 1")
    AdminPO findByUsername(@Param("username") String username);
}