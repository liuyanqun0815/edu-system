package com.education.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.RpcResult;
import com.education.common.utils.SecurityUtils;
import com.education.system.dto.WrongBookExportVO;
import com.education.system.entity.SysWord;
import com.education.system.entity.WordWrongBook;
import com.education.system.mapper.SysWordMapper;
import com.education.system.mapper.WordWrongBookMapper;
import com.education.system.service.IWordWrongBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单词错题本服务实现
 */
@Slf4j
@Service
public class WordWrongBookServiceImpl implements IWordWrongBookService {

    @Autowired
    private WordWrongBookMapper wrongBookMapper;

    @Autowired
    private SysWordMapper wordMapper;

    @Override
    public RpcResult<List<WordWrongBook>> getTodayWrongBooks() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // 查询今日错题(最后错误时间是今天)
            LocalDate today = LocalDate.now();
            Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            LambdaQueryWrapper<WordWrongBook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WordWrongBook::getUserId, userId)
                   .eq(WordWrongBook::getDr, 0)
                   .ge(WordWrongBook::getLastWrongTime, startOfDay)
                   .lt(WordWrongBook::getLastWrongTime, endOfDay)
                   .orderByDesc(WordWrongBook::getLastWrongTime);

            List<WordWrongBook> wrongBooks = wrongBookMapper.selectList(wrapper);
            
            log.info("用户{}查询今日错题,数量:{}", userId, wrongBooks.size());
            return RpcResult.success(wrongBooks);
        } catch (Exception e) {
            log.error("查询今日错题失败:", e);
            return RpcResult.fail("查询失败:" + e.getMessage());
        }
    }

    @Override
    public RpcResult<List<WordWrongBook>> getWrongBookPage(Integer reviewStatus, Integer pageNum, Integer pageSize) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            LambdaQueryWrapper<WordWrongBook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WordWrongBook::getUserId, userId)
                   .eq(WordWrongBook::getDr, 0);
            
            if (reviewStatus != null) {
                wrapper.eq(WordWrongBook::getReviewStatus, reviewStatus);
            }
            
            wrapper.orderByDesc(WordWrongBook::getLastWrongTime)
                   .orderByDesc(WordWrongBook::getWrongCount);

            // 分页查询
            int offset = (pageNum - 1) * pageSize;
            wrapper.last("LIMIT " + offset + ", " + pageSize);

            List<WordWrongBook> wrongBooks = wrongBookMapper.selectList(wrapper);
            
            log.info("用户{}查询错题本,页码:{}, 数量:{}", userId, pageNum, wrongBooks.size());
            return RpcResult.success(wrongBooks);
        } catch (Exception e) {
            log.error("查询错题本失败:", e);
            return RpcResult.fail("查询失败:" + e.getMessage());
        }
    }

    @Override
    public RpcResult<Long> startWrongBookReview(List<Long> wordIds) {
        try {
            // 错题复习复用训练会话,这里仅返回单词ID列表供前端使用
            // 实际训练逻辑由WordTrainingService处理
            log.info("开始错题复习,单词数量:{}", wordIds.size());
            return RpcResult.success(0L); // 0表示特殊模式
        } catch (Exception e) {
            log.error("开始错题复习失败:", e);
            return RpcResult.fail("失败:" + e.getMessage());
        }
    }

    @Override
    public RpcResult<Void> updateMasteryLevel(Long wrongBookId, Integer masteryLevel) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            WordWrongBook wrongBook = wrongBookMapper.selectById(wrongBookId);
            if (wrongBook == null || wrongBook.getDr() == 1) {
                return RpcResult.fail("错题不存在");
            }

            if (!wrongBook.getUserId().equals(userId)) {
                return RpcResult.fail("无权操作");
            }

            wrongBook.setMasteryLevel(masteryLevel);
            
            // 如果已掌握,更新复习状态
            if (masteryLevel >= 2) {
                wrongBook.setReviewStatus(1); // 已掌握
            } else if (masteryLevel >= 1) {
                wrongBook.setReviewStatus(2); // 需强化
            }

            wrongBookMapper.updateById(wrongBook);
            
            log.info("用户{}更新错题{}掌握程度为:{}", userId, wrongBookId, masteryLevel);
            return RpcResult.success(null);
        } catch (Exception e) {
            log.error("更新掌握程度失败:", e);
            return RpcResult.fail("更新失败:" + e.getMessage());
        }
    }

    @Override
    public RpcResult<Void> deleteWrongBook(Long wrongBookId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            WordWrongBook wrongBook = wrongBookMapper.selectById(wrongBookId);
            if (wrongBook == null || wrongBook.getDr() == 1) {
                return RpcResult.fail("错题不存在");
            }

            if (!wrongBook.getUserId().equals(userId)) {
                return RpcResult.fail("无权操作");
            }

            // 逻辑删除
            wrongBook.setDr(1);
            wrongBookMapper.updateById(wrongBook);
            
            log.info("用户{}删除错题:{}", userId, wrongBookId);
            return RpcResult.success(null);
        } catch (Exception e) {
            log.error("删除错题失败:", e);
            return RpcResult.fail("删除失败:" + e.getMessage());
        }
    }

    @Override
    public void exportWrongBook(HttpServletResponse response) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // 查询所有错题
            LambdaQueryWrapper<WordWrongBook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WordWrongBook::getUserId, userId)
                   .eq(WordWrongBook::getDr, 0)
                   .orderByDesc(WordWrongBook::getWrongCount);
            
            List<WordWrongBook> wrongBooks = wrongBookMapper.selectList(wrapper);
            
            // 转换为导出VO
            List<WrongBookExportVO> exportList = new ArrayList<>();
            for (WordWrongBook wrongBook : wrongBooks) {
                SysWord word = wordMapper.selectById(wrongBook.getWordId());
                if (word != null) {
                    WrongBookExportVO vo = new WrongBookExportVO();
                    vo.setWord(word.getWord());
                    vo.setPhonetic(word.getPhonetic());
                    vo.setTranslation(word.getTranslation());
                    vo.setQuestionType(wrongBook.getQuestionType());
                    vo.setWrongCount(wrongBook.getWrongCount());
                    vo.setLastWrongTime(wrongBook.getLastWrongTime());
                    vo.setMasteryLevel(wrongBook.getMasteryLevel());
                    vo.setExample(word.getExample());
                    exportList.add(vo);
                }
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("单词错题本_" + LocalDate.now(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 使用EasyExcel导出
            EasyExcel.write(response.getOutputStream(), WrongBookExportVO.class)
                    .sheet("错题本")
                    .doWrite(exportList);

            log.info("用户{}导出错题本,数量:{}", userId, exportList.size());
        } catch (IOException e) {
            log.error("导出错题本失败:", e);
            throw new RuntimeException("导出失败:" + e.getMessage());
        }
    }
}
