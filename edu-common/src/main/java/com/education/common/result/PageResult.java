package com.education.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据结果封装类
 * 
 * <p>用于封装分页查询的返回结果，包含分页信息和当前页数据。</p>
 * 
 * <h3>响应结构示例：</h3>
 * <pre>
 * {
 *   "total": 100,        // 总记录数
 *   "rows": [...],       // 当前页数据列表
 *   "pageNum": 1,        // 当前页码
 *   "pageSize": 10,      // 每页大小
 *   "totalPages": 10     // 总页数（自动计算）
 * }
 * </pre>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 方式一：使用静态工厂方法（推荐）
 * Page<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
 * return JsonResult.success(PageResult.of(page.getTotal(), page.getRecords(), pageNum, pageSize));
 * 
 * // 方式二：构造函数创建
 * PageResult<User> result = new PageResult<>(total, userList, pageNum, pageSize);
 * }</pre>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>totalPages由构造函数自动计算，公式：(total + pageSize - 1) / pageSize</li>
 *   <li>pageNum从1开始，非0-based</li>
 *   <li>配合MyBatis-Plus的Page对象使用更便捷</li>
 * </ul>
 * 
 * @param <T> 分页数据项类型
 * @see JsonResult 统一响应结果
 * @see com.education.common.query.BaseQuery 基础查询参数
 * @author education-team
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据
     */
    private List<T> rows;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总页数
     */
    private Long totalPages;

    public PageResult() {
    }

    public PageResult(Long total, List<T> rows, Long pageNum, Long pageSize) {
        this.total = total;
        this.rows = rows;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }

    public static <T> PageResult<T> of(Long total, List<T> rows, Long pageNum, Long pageSize) {
        return new PageResult<>(total, rows, pageNum, pageSize);
    }

    /**
     * 从MyBatis-Plus的Page对象构建PageResult
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }
}
