package com.education.system.controller;

import com.education.common.result.RpcResult;
import com.education.system.entity.WordWrongBook;
import com.education.system.service.IWordWrongBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 单词错题本控制器
 */
@Slf4j
@RestController
@RequestMapping("/word/wrongbook")
public class WordWrongBookController {

    @Autowired
    private IWordWrongBookService wrongBookService;

    /**
     * 获取今日错题(优先展示)
     */
    @GetMapping("/today")
    public RpcResult<List<WordWrongBook>> getTodayWrongBooks() {
        return wrongBookService.getTodayWrongBooks();
    }

    /**
     * 获取错题列表(分页)
     */
    @GetMapping("/page")
    public RpcResult<List<WordWrongBook>> getWrongBookPage(
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return wrongBookService.getWrongBookPage(reviewStatus, pageNum, pageSize);
    }

    /**
     * 开始错题复习
     */
    @PostMapping("/review")
    public RpcResult<Long> startWrongBookReview(@RequestBody List<Long> wordIds) {
        return wrongBookService.startWrongBookReview(wordIds);
    }

    /**
     * 更新错题掌握状态
     */
    @PutMapping("/{wrongBookId}/mastery")
    public RpcResult<Void> updateMasteryLevel(
            @PathVariable Long wrongBookId,
            @RequestParam Integer masteryLevel) {
        return wrongBookService.updateMasteryLevel(wrongBookId, masteryLevel);
    }

    /**
     * 删除错题
     */
    @DeleteMapping("/{wrongBookId}")
    public RpcResult<Void> deleteWrongBook(@PathVariable Long wrongBookId) {
        return wrongBookService.deleteWrongBook(wrongBookId);
    }

    /**
     * 导出错题本
     */
    @GetMapping("/export")
    public void exportWrongBook(HttpServletResponse response) {
        wrongBookService.exportWrongBook(response);
    }
}
