package com.education.system.service;

import com.education.common.result.RpcResult;
import com.education.system.dto.WrongBookExportVO;
import com.education.system.entity.WordWrongBook;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 单词错题本服务接口
 */
public interface IWordWrongBookService {

    /**
     * 获取今日错题(优先展示)
     *
     * @return 错题列表
     */
    RpcResult<List<WordWrongBook>> getTodayWrongBooks();

    /**
     * 获取错题列表(分页)
     *
     * @param reviewStatus 复习状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 错题列表
     */
    RpcResult<List<WordWrongBook>> getWrongBookPage(Integer reviewStatus, Integer pageNum, Integer pageSize);

    /**
     * 开始错题复习
     *
     * @param wordIds 单词ID列表
     * @return 会话ID
     */
    RpcResult<Long> startWrongBookReview(List<Long> wordIds);

    /**
     * 更新错题掌握状态
     *
     * @param wrongBookId 错题ID
     * @param masteryLevel 掌握程度
     * @return 是否成功
     */
    RpcResult<Void> updateMasteryLevel(Long wrongBookId, Integer masteryLevel);

    /**
     * 删除错题
     *
     * @param wrongBookId 错题ID
     * @return 是否成功
     */
    RpcResult<Void> deleteWrongBook(Long wrongBookId);

    /**
     * 导出错题本
     *
     * @param response HTTP响应
     */
    void exportWrongBook(HttpServletResponse response);
}
