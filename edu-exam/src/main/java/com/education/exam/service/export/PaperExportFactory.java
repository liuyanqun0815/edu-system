package com.education.exam.service.export;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 试卷导出策略工厂
 * 
 * <p>使用工厂模式管理试卷导出策略，根据导出类型返回对应的策略实现。</p>
 * 
 * <h3>设计模式：</h3>
 * <ul>
 *   <li><b>工厂模式</b>：封装策略创建逻辑，调用方无需关心具体实现类</li>
 *   <li><b>策略模式</b>：不同导出格式由不同策略类实现，便于扩展</li>
 * </ul>
 * 
 * <h3>支持的导出格式：</h3>
 * <table border="1">
 *   <tr><th>类型</th><th>策略类</th><th>说明</th></tr>
 *   <tr><td>word</td><td>WordExportStrategy</td><td>导出为Word文档(.docx)</td></tr>
 *   <tr><td>pdf</td><td>PdfExportStrategy</td><td>导出为PDF文档(.pdf)</td></tr>
 * </table>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private PaperExportFactory exportFactory;
 * 
 * public void export(Long paperId, String type) {
 *     PaperExportStrategy strategy = exportFactory.getStrategy(type);
 *     byte[] data = strategy.export(paperId);
 *     // 写入响应流...
 * }
 * }</pre>
 * 
 * <h3>扩展新格式：</h3>
 * <pre>
 * 1. 实现 PaperExportStrategy 接口
 * 2. 添加 @Service 注解，Spring自动注入工厂
 * 3. getType() 返回新格式标识（如 "excel"）
 * </pre>
 * 
 * @see PaperExportStrategy 导出策略接口
 * @see WordExportStrategy Word导出策略
 * @see PdfExportStrategy PDF导出策略
 * @author education-team
 */
@Component
public class PaperExportFactory {
    
    private final List<PaperExportStrategy> strategies;
    private final Map<String, PaperExportStrategy> strategyMap = new HashMap<>();
    
    public PaperExportFactory(List<PaperExportStrategy> strategies) {
        this.strategies = strategies;
    }
    
    @PostConstruct
    public void init() {
        for (PaperExportStrategy strategy : strategies) {
            strategyMap.put(strategy.getType().toLowerCase(), strategy);
        }
    }
    
    /**
     * 获取导出策略
     * @param type 导出类型 (word/pdf/image)
     * @return 对应的导出策略
     * @throws IllegalArgumentException 不支持的导出类型
     */
    public PaperExportStrategy getStrategy(String type) {
        PaperExportStrategy strategy = strategyMap.get(type.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的导出类型: " + type);
        }
        return strategy;
    }
    
    /**
     * 检查是否支持该导出类型
     */
    public boolean supports(String type) {
        return strategyMap.containsKey(type.toLowerCase());
    }
    
    /**
     * 获取所有支持的导出类型
     */
    public java.util.Set<String> getSupportedTypes() {
        return strategyMap.keySet();
    }
}
