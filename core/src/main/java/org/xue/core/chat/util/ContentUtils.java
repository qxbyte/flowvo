package org.xue.core.chat.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ContentUtils {

    public static String readFileContent(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        if (extension.equals("txt")) {
            // 纯文本，直接读取
            return readTxt(file);
        } else if (extension.equals("docx")) {
            // Word文档，使用Apache POI
            return readDocx(file);
        } else if (extension.equals("pdf")) {
            // PDF文件，使用PDFBox
            return readPdf(file);
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
    }

    private static String readTxt(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static String readDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph para : document.getParagraphs()) {
                content.append(para.getText()).append("\n");
            }
            return content.toString();
        }
    }

    private static String readPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
