package com.library.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 图书相关
    BOOK_NOT_FOUND(404, "图书不存在"),
    ISBN_DUPLICATE(409, "ISBN已存在"),
    BOOK_OUT_OF_STOCK(422, "该图书暂无可借库存"),
    BOOK_HAS_UNRETURNED(409, "图书存在未归还借阅记录，无法删除"),

    // 读者相关
    READER_NOT_FOUND(404, "读者不存在"),
    READER_BANNED(422, "读者处于禁借期，无法借书"),
    BORROW_LIMIT_EXCEEDED(422, "读者已达最大借阅数量上限"),

    // 借阅记录相关
    BORROW_RECORD_NOT_FOUND(404, "未找到对应的未还借阅记录"),
    ALREADY_BORROWED(409, "该读者已借阅此书且未归还"),

    // 参数相关
    PARAM_ERROR(400, "请求参数错误"),

    // 系统错误
    SYSTEM_ERROR(500, "系统错误，请联系管理员");

    private final int code;
    private final String message;
}
