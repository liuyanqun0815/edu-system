package com.education.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * MyBatis-Plus 核心配置类
 * 
 * <p>配置MyBatis-Plus的核心功能组件，包括分页插件和字段自动填充。</p>
 * 
 * <h3>配置内容：</h3>
 * <ul>
 *   <li><b>分页插件</b>：基于MySQL数据库的分页支持，拦截带有Page参数的查询自动添加LIMIT</li>
 *   <li><b>自动填充处理器</b>：为BaseEntity中的createTime、updateTime、dr字段自动赋值</li>
 * </ul>
 * 
 * <h3>自动填充字段说明：</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>填充时机</th><th>填充值</th></tr>
 *   <tr><td>createTime</td><td>INSERT</td><td>当前时间</td></tr>
 *   <tr><td>updateTime</td><td>INSERT/UPDATE</td><td>当前时间</td></tr>
 *   <tr><td>dr</td><td>INSERT</td><td>0（正常状态）</td></tr>
 * </table>
 * 
 * <h3>分页使用示例：</h3>
 * <pre>{@code
 * // Controller层
 * @GetMapping("/page")
 * public JsonResult<PageResult<User>> page(@RequestParam(defaultValue = "1") Long pageNum,
 *                                          @RequestParam(defaultValue = "10") Long pageSize) {
 *     Page<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
 *     return JsonResult.success(PageResult.of(page.getTotal(), page.getRecords(), pageNum, pageSize));
 * }
 * }</pre>
 * 
 * @see com.education.common.entity.BaseEntity 实体基类
 * @author education-team
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
                this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
                this.strictInsertFill(metaObject, "dr", Integer.class, 0);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
            }
        };
    }
}
