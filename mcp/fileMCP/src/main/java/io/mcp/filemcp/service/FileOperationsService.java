package io.mcp.filemcp.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MCP服务类，提供全面的文件和文件夹操作功能
 * 支持文件创建、读取、写入、删除、移动以及基于行的编辑操作
 * 使用Apache Tika支持多种文档格式的智能解析
 */
@Slf4j
@Service
public class FileOperationsService {

    @Value("${file.operations.base-path:#{systemProperties['user.home']}/mcp-files}")
    private String basePath;

    @Value("${file.operations.max-file-size:10MB}")
    private String maxFileSize;

    @Value("${file.operations.allowed-extensions:.txt,.md,.json,.xml,.yaml,.yml,.properties,.log,.csv,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.rtf,.odt,.ods,.odp}")
    private String allowedExtensions;

    /**
     * 创建文件到指定目录
     */
    @Tool(description = "在指定路径创建新文件，可选择性地写入初始内容。如果父目录不存在会自动创建。支持相对路径，路径安全检查防止目录遍历攻击。")
    public String createFile(String filePath, String content) {
        try {
            Path resolvedPath = resolveAndValidatePath(filePath);
            
            // 创建父目录
            Files.createDirectories(resolvedPath.getParent());
            
            // 创建文件
            if (Files.exists(resolvedPath)) {
                return "文件已存在: " + resolvedPath;
            }
            
            Files.createFile(resolvedPath);
            
            // 如果提供了内容，写入内容
            if (content != null && !content.isEmpty()) {
                Files.write(resolvedPath, content.getBytes(StandardCharsets.UTF_8));
            }
            
            log.info("文件创建成功: {}", resolvedPath);
            return "文件创建成功: " + resolvedPath;
            
        } catch (Exception e) {
            log.error("创建文件失败: {}", e.getMessage());
            return "创建文件失败: " + e.getMessage();
        }
    }

    /**
     * 读取文件内容 - 智能解析多种格式
     * 支持纯文本文件直接读取，以及PDF、Word、Excel等格式的智能解析
     */
    @Tool(description = "智能读取文件内容。支持纯文本文件直接读取，以及PDF、Word、Excel、PowerPoint等格式的解析。使用Apache Tika技术自动检测文档格式并提取文本内容。对于不支持的格式会降级到纯文本读取。")
    public String readFile(String filePath) {
        try {
            Path resolvedPath = resolveAndValidatePath(filePath);
            
            if (!Files.exists(resolvedPath)) {
                return "文件不存在: " + resolvedPath;
            }
            
            if (!Files.isRegularFile(resolvedPath)) {
                return "不是一个文件: " + resolvedPath;
            }
            
            String content = readFileContent(resolvedPath);
            log.info("文件读取成功: {}", resolvedPath);
            return content;
            
        } catch (Exception e) {
            log.error("读取文件失败: {}", e.getMessage());
            return "读取文件失败: " + e.getMessage();
        }
    }

    /**
     * 智能读取文件内容
     * 对于常见的文档格式（PDF、Word、Excel等）使用Tika解析
     * 对于纯文本文件直接读取
     */
    private String readFileContent(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString().toLowerCase();
        String extension = getFileExtension(fileName);
        
        // 检查是否为纯文本格式
        if (isPlainTextFile(extension)) {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        }
        
        // 使用Tika解析文档格式
        try {
            log.debug("尝试使用Tika解析文件: {}", filePath);
            
            // 使用TikaDocumentReader解析文档
            TikaDocumentReader reader = new TikaDocumentReader(filePath.toUri().toString());
            List<Document> documents = reader.get();
            
            if (!documents.isEmpty()) {
                Document doc = documents.get(0);
                String content = doc.getText(); // 修改为getText()方法
                log.debug("Tika解析成功，提取文本长度: {}", content.length());
                return content;
            } else {
                log.warn("Tika解析返回空文档列表");
            }
        } catch (Exception tikaException) {
            log.warn("Tika解析失败，将降级到纯文本读取: {}", tikaException.getMessage());
        }
        
        // 降级到纯文本读取
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (Exception textException) {
            throw new IOException("无法读取文件内容: " + textException.getMessage(), textException);
        }
    }

    /**
     * 检查是否为纯文本文件
     */
    private boolean isPlainTextFile(String extension) {
        String[] textExtensions = {".txt", ".md", ".json", ".xml", ".yaml", ".yml", 
                                 ".properties", ".log", ".csv", ".html", ".css", ".js", 
                                 ".java", ".py", ".php", ".sql", ".sh", ".bat"};
        
        for (String textExt : textExtensions) {
            if (textExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 写入内容到文件
     */
    @Tool(description = "将内容写入到指定文件。如果文件不存在会自动创建，如果父目录不存在也会自动创建。会覆盖原有内容。使用UTF-8编码。")
    public String writeFile(String filePath, String content) {
        try {
            Path resolvedPath = resolveAndValidatePath(filePath);
            
            // 创建父目录
            Files.createDirectories(resolvedPath.getParent());
            
            // 写入内容
            Files.write(resolvedPath, content.getBytes(StandardCharsets.UTF_8));
            
            log.info("文件写入成功: {}", resolvedPath);
            return "文件写入成功: " + resolvedPath;
            
        } catch (Exception e) {
            log.error("写入文件失败: {}", e.getMessage());
            return "写入文件失败: " + e.getMessage();
        }
    }

    /**
     * 删除文件
     */
    @Tool(description = "删除指定的文件。只能删除常规文件，不能删除目录。如果要删除目录请使用deleteDirectory工具。")
    public String deleteFile(String filePath) {
        try {
            Path resolvedPath = resolveAndValidatePath(filePath);
            
            if (!Files.exists(resolvedPath)) {
                return "文件不存在: " + resolvedPath;
            }
            
            if (!Files.isRegularFile(resolvedPath)) {
                return "不是一个文件: " + resolvedPath;
            }
            
            Files.delete(resolvedPath);
            
            log.info("文件删除成功: {}", resolvedPath);
            return "文件删除成功: " + resolvedPath;
            
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
            return "删除文件失败: " + e.getMessage();
        }
    }

    /**
     * 移动文件
     */
    @Tool(description = "移动或重命名文件。可以在同一目录内重命名，也可以移动到不同目录。如果目标目录不存在会自动创建。如果目标文件已存在会被覆盖。")
    public String moveFile(String sourcePath, String destinationPath) {
        try {
            Path sourceResolved = resolveAndValidatePath(sourcePath);
            Path destinationResolved = resolveAndValidatePath(destinationPath);
            
            if (!Files.exists(sourceResolved)) {
                return "源文件不存在: " + sourceResolved;
            }
            
            if (!Files.isRegularFile(sourceResolved)) {
                return "源路径不是一个文件: " + sourceResolved;
            }
            
            // 创建目标目录
            Files.createDirectories(destinationResolved.getParent());
            
            // 移动文件
            Files.move(sourceResolved, destinationResolved, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("文件移动成功: {} -> {}", sourceResolved, destinationResolved);
            return "文件移动成功: " + sourceResolved + " -> " + destinationResolved;
            
        } catch (Exception e) {
            log.error("移动文件失败: {}", e.getMessage());
            return "移动文件失败: " + e.getMessage();
        }
    }

    /**
     * 删除指定行
     */
    @Tool(description = "删除文件中的指定行或行范围。使用1基索引（第一行为1）。如果不指定lineCount，默认删除1行。支持删除多行。")
    public String deleteLines(String filePath, int startLine, Integer lineCount) {
        try {
            Path resolvedPath = resolveAndValidatePath(filePath);
            
            if (!Files.exists(resolvedPath)) {
                return "文件不存在: " + resolvedPath;
            }
            
            List<String> lines = Files.readAllLines(resolvedPath, StandardCharsets.UTF_8);
            
            int count = lineCount != null ? lineCount : 1;
            int startIndex = startLine - 1; // 转换为0基索引
            
            if (startIndex < 0 || startIndex >= lines.size()) {
                return "行号超出范围: " + startLine + " (文件总行数: " + lines.size() + ")";
            }
            
            int endIndex = Math.min(startIndex + count, lines.size());
            
            // 删除指定行
            for (int i = endIndex - 1; i >= startIndex; i--) {
                lines.remove(i);
            }
            
            // 写回文件
            Files.write(resolvedPath, lines, StandardCharsets.UTF_8);
            
            log.info("删除行成功: {} (行 {}-{})", resolvedPath, startLine, startLine + count - 1);
            return String.format("删除行成功: %s (删除了第 %d 行开始的 %d 行)", resolvedPath, startLine, count);
            
        } catch (Exception e) {
            log.error("删除行失败: {}", e.getMessage());
            return "删除行失败: " + e.getMessage();
        }
    }

    /**
     * 在指定行插入内容
     */
    @Tool(description = "在文件的指定行位置插入新内容。使用1基索引（第一行为1）。支持插入多行内容（使用\\n分隔）。插入位置的原有内容会向下移动。")
    public String insertLines(String filePath, int lineNumber, String content) {
        try {
            Path resolvedPath = resolveAndValidatePath(filePath);
            
            if (!Files.exists(resolvedPath)) {
                return "文件不存在: " + resolvedPath;
            }
            
            List<String> lines = Files.readAllLines(resolvedPath, StandardCharsets.UTF_8);
            
            int insertIndex = lineNumber - 1; // 转换为0基索引
            
            if (insertIndex < 0 || insertIndex > lines.size()) {
                return "行号超出范围: " + lineNumber + " (文件总行数: " + lines.size() + ")";
            }
            
            // 按行分割插入的内容
            String[] contentLines = content.split("\n");
            
            // 在指定位置插入内容
            for (int i = 0; i < contentLines.length; i++) {
                lines.add(insertIndex + i, contentLines[i]);
            }
            
            // 写回文件
            Files.write(resolvedPath, lines, StandardCharsets.UTF_8);
            
            log.info("插入内容成功: {} (在第 {} 行)", resolvedPath, lineNumber);
            return String.format("插入内容成功: %s (在第 %d 行插入了 %d 行)", resolvedPath, lineNumber, contentLines.length);
            
        } catch (Exception e) {
            log.error("插入内容失败: {}", e.getMessage());
            return "插入内容失败: " + e.getMessage();
        }
    }

    /**
     * 创建目录
     */
    @Tool(description = "创建新目录。支持递归创建，即如果父目录不存在会自动创建所有必要的父目录。如果目录已存在不会报错。")
    public String createDirectory(String directoryPath) {
        try {
            Path resolvedPath = resolveAndValidatePath(directoryPath);
            
            if (Files.exists(resolvedPath)) {
                if (Files.isDirectory(resolvedPath)) {
                    return "目录已存在: " + resolvedPath;
                } else {
                    return "路径已存在但不是目录: " + resolvedPath;
                }
            }
            
            Files.createDirectories(resolvedPath);
            
            log.info("目录创建成功: {}", resolvedPath);
            return "目录创建成功: " + resolvedPath;
            
        } catch (Exception e) {
            log.error("创建目录失败: {}", e.getMessage());
            return "创建目录失败: " + e.getMessage();
        }
    }

    /**
     * 删除目录
     */
    @Tool(description = "删除目录及其所有内容。这是一个递归操作，会删除目录下的所有文件和子目录。请谨慎使用，操作不可逆。")
    public String deleteDirectory(String directoryPath) {
        try {
            Path resolvedPath = resolveAndValidatePath(directoryPath);
            
            if (!Files.exists(resolvedPath)) {
                return "目录不存在: " + resolvedPath;
            }
            
            if (!Files.isDirectory(resolvedPath)) {
                return "路径不是目录: " + resolvedPath;
            }
            
            // 递归删除目录及其内容
            FileUtils.deleteDirectory(resolvedPath.toFile());
            
            log.info("目录删除成功: {}", resolvedPath);
            return "目录删除成功: " + resolvedPath;
            
        } catch (Exception e) {
            log.error("删除目录失败: {}", e.getMessage());
            return "删除目录失败: " + e.getMessage();
        }
    }

    /**
     * 列出目录内容
     */
    @Tool(description = "列出指定目录中的所有文件和子目录。显示每个项目的类型（文件或目录）、名称和大小信息。结果按名称排序。")
    public String listDirectory(String directoryPath) {
        try {
            Path resolvedPath = resolveAndValidatePath(directoryPath);
            
            if (!Files.exists(resolvedPath)) {
                return "目录不存在: " + resolvedPath;
            }
            
            if (!Files.isDirectory(resolvedPath)) {
                return "路径不是目录: " + resolvedPath;
            }
            
            try (Stream<Path> paths = Files.list(resolvedPath)) {
                List<String> contents = paths
                        .map(path -> {
                            String type = Files.isDirectory(path) ? "[DIR]" : "[FILE]";
                            String size = "";
                            if (Files.isRegularFile(path)) {
                                try {
                                    long bytes = Files.size(path);
                                    size = " (" + formatFileSize(bytes) + ")";
                                } catch (IOException e) {
                                    size = " (size unknown)";
                                }
                            }
                            return type + " " + path.getFileName() + size;
                        })
                        .sorted()
                        .collect(Collectors.toList());
                
                log.info("目录列表获取成功: {}", resolvedPath);
                return "目录内容:\n" + String.join("\n", contents);
            }
            
        } catch (Exception e) {
            log.error("列出目录失败: {}", e.getMessage());
            return "列出目录失败: " + e.getMessage();
        }
    }

    /**
     * 获取文件信息
     */
    @Tool(description = "获取文件或目录的详细信息，包括类型、大小、最后修改时间、权限信息。对于文本文件还会显示行数，对于支持的文档格式会显示解析支持信息。")
    public String getFileInfo(String filePath) {
        try {
            Path resolvedPath = resolveAndValidatePath(filePath);
            
            if (!Files.exists(resolvedPath)) {
                return "文件或目录不存在: " + resolvedPath;
            }
            
            StringBuilder info = new StringBuilder();
            info.append("路径: ").append(resolvedPath).append("\n");
            info.append("类型: ").append(Files.isDirectory(resolvedPath) ? "目录" : "文件").append("\n");
            
            if (Files.isRegularFile(resolvedPath)) {
                info.append("大小: ").append(formatFileSize(Files.size(resolvedPath))).append("\n");
                
                // 对于文本文件显示行数，对于其他文件显示格式信息
                String extension = getFileExtension(resolvedPath.getFileName().toString().toLowerCase());
                if (isPlainTextFile(extension)) {
                    info.append("行数: ").append(Files.readAllLines(resolvedPath).size()).append("\n");
                } else {
                    info.append("格式: ").append(extension.isEmpty() ? "未知" : extension.substring(1).toUpperCase()).append("\n");
                    info.append("解析支持: ").append(isSupportedByTika(extension) ? "是（Tika解析）" : "否（仅二进制）").append("\n");
                }
            }
            
            info.append("最后修改: ").append(Files.getLastModifiedTime(resolvedPath)).append("\n");
            info.append("可读: ").append(Files.isReadable(resolvedPath)).append("\n");
            info.append("可写: ").append(Files.isWritable(resolvedPath)).append("\n");
            
            log.info("文件信息获取成功: {}", resolvedPath);
            return info.toString();
            
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            return "获取文件信息失败: " + e.getMessage();
        }
    }

    /**
     * 检查文件格式是否被Tika支持
     */
    private boolean isSupportedByTika(String extension) {
        String[] supportedExtensions = {".pdf", ".doc", ".docx", ".xls", ".xlsx", 
                                      ".ppt", ".pptx", ".rtf", ".odt", ".ods", ".odp"};
        
        for (String supported : supportedExtensions) {
            if (supported.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析并验证路径
     */
    private Path resolveAndValidatePath(String filePath) throws IOException {
        Path base = Paths.get(basePath);
        Path resolved = base.resolve(filePath).normalize();
        
        // 安全检查：确保路径在基础目录内
        if (!resolved.startsWith(base)) {
            throw new SecurityException("路径超出允许的基础目录范围: " + filePath);
        }
        
        return resolved;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
} 