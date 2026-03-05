package com.library.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ImportResultVO {
    /** 成功导入行数 */
    private Integer successCount;
    /** 跳过（重复累加库存）行数 */
    private Integer accumulatedCount;
    /** 失败行数 */
    private Integer failCount;
    /** 失败详情：行号 + 原因 */
    private List<String> failDetails;
}
