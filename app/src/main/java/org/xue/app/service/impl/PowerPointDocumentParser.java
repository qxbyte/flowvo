package org.xue.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;
import org.xue.app.service.DocumentParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * PowerPoint文档解析器 (支持.ppt和.pptx)
 */
@Slf4j
@Component
public class PowerPointDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileName, String mimeType) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".ppt") || 
               lowerFileName.endsWith(".pptx") ||
               "application/vnd.ms-powerpoint".equals(mimeType) ||
               "application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(mimeType);
    }

    @Override
    public String parseDocument(InputStream inputStream, String fileName) throws IOException {
        log.info("开始解析PowerPoint文档: {}", fileName);
        
        try {
            String text;
            if (fileName.toLowerCase().endsWith(".pptx")) {
                text = parsePptx(inputStream, fileName);
            } else {
                text = parsePpt(inputStream, fileName);
            }
            
            // 限制文本长度
            if (text.length() > 50000) {
                text = text.substring(0, 50000) + "\n...[内容过长，已截断]";
                log.warn("PowerPoint文档 {} 内容过长，已截断到50000字符", fileName);
            }
            
            log.info("PowerPoint文档 {} 解析完成，提取文本长度: {}", fileName, text.length());
            return text;
            
        } catch (Exception e) {
            log.error("解析PowerPoint文档 {} 失败: {}", fileName, e.getMessage(), e);
            throw new IOException("PowerPoint文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析.pptx文件
     */
    private String parsePptx(InputStream inputStream, String fileName) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            StringBuilder text = new StringBuilder();
            List<XSLFSlide> slides = ppt.getSlides();
            
            // 限制处理的幻灯片数量
            int slideCount = Math.min(slides.size(), 50);
            
            for (int i = 0; i < slideCount; i++) {
                XSLFSlide slide = slides.get(i);
                text.append("=== 幻灯片 ").append(i + 1).append(" ===\n");
                
                slide.getShapes().forEach(shape -> {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                            for (XSLFTextRun run : paragraph.getTextRuns()) {
                                String runText = run.getRawText();
                                if (runText != null && !runText.trim().isEmpty()) {
                                    text.append(runText).append("\n");
                                }
                            }
                        }
                    }
                });
                
                text.append("\n");
            }
            
            return text.toString();
        }
    }

    /**
     * 解析.ppt文件
     */
    private String parsePpt(InputStream inputStream, String fileName) throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow(inputStream)) {
            StringBuilder text = new StringBuilder();
            List<HSLFSlide> slides = ppt.getSlides();
            
            // 限制处理的幻灯片数量
            int slideCount = Math.min(slides.size(), 50);
            
            for (int i = 0; i < slideCount; i++) {
                HSLFSlide slide = slides.get(i);
                text.append("=== 幻灯片 ").append(i + 1).append(" ===\n");
                
                List<List<HSLFTextParagraph>> textParagraphs = slide.getTextParagraphs();
                for (List<HSLFTextParagraph> paragraphList : textParagraphs) {
                    for (HSLFTextParagraph paragraph : paragraphList) {
                        for (HSLFTextRun run : paragraph.getTextRuns()) {
                            String runText = run.getRawText();
                            if (runText != null && !runText.trim().isEmpty()) {
                                text.append(runText).append("\n");
                            }
                        }
                    }
                }
                
                text.append("\n");
            }
            
            return text.toString();
        }
    }

    @Override
    public String getParserType() {
        return "PowerPoint";
    }
} 