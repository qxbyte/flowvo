package io.mcp.filemcp;

import io.mcp.filemcp.service.FileOperationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.function.Function;

/**
 * 文件操作MCP服务器应用程序
 * 
 * 这是一个基于Spring AI框架的MCP (Model Context Protocol) 服务器
 * 提供文件和文件夹操作的工具集，包括：
 * - 文件创建、读取、写入、删除、移动
 * - 基于行的文件编辑（删除行、插入行）
 * - 目录创建、删除、列出内容
 * - 文件/目录信息查询
 * 
 * 遵循JSON RPC 2.0协议，可以被外部MCP客户端调用
 */
@Slf4j
@SpringBootApplication
public class FileMcpApplication {

	public static void main(String[] args) {
		log.info("正在启动文件操作MCP服务器...");
		SpringApplication.run(FileMcpApplication.class, args);
		log.info("文件操作MCP服务器启动成功！");
	}

	// 文件操作工具Bean注册
	
	@Bean
	public Function<CreateFileRequest, String> createFileFunction(FileOperationsService service) {
		return request -> service.createFile(request.filePath(), request.content());
	}

	@Bean
	public Function<ReadFileRequest, String> readFileFunction(FileOperationsService service) {
		return request -> service.readFile(request.filePath());
	}

	@Bean
	public Function<WriteFileRequest, String> writeFileFunction(FileOperationsService service) {
		return request -> service.writeFile(request.filePath(), request.content());
	}

	@Bean
	public Function<DeleteFileRequest, String> deleteFileFunction(FileOperationsService service) {
		return request -> service.deleteFile(request.filePath());
	}

	@Bean
	public Function<MoveFileRequest, String> moveFileFunction(FileOperationsService service) {
		return request -> service.moveFile(request.sourcePath(), request.destinationPath());
	}

	@Bean
	public Function<DeleteLinesRequest, String> deleteLinesFunction(FileOperationsService service) {
		return request -> service.deleteLines(request.filePath(), request.startLine(), request.lineCount());
	}

	@Bean
	public Function<InsertLinesRequest, String> insertLinesFunction(FileOperationsService service) {
		return request -> service.insertLines(request.filePath(), request.lineNumber(), request.content());
	}

	@Bean
	public Function<CreateDirectoryRequest, String> createDirectoryFunction(FileOperationsService service) {
		return request -> service.createDirectory(request.directoryPath());
	}

	@Bean
	public Function<DeleteDirectoryRequest, String> deleteDirectoryFunction(FileOperationsService service) {
		return request -> service.deleteDirectory(request.directoryPath());
	}

	@Bean
	public Function<ListDirectoryRequest, String> listDirectoryFunction(FileOperationsService service) {
		return request -> service.listDirectory(request.directoryPath());
	}

	@Bean
	public Function<GetFileInfoRequest, String> getFileInfoFunction(FileOperationsService service) {
		return request -> service.getFileInfo(request.filePath());
	}

	// 请求记录类
	public record CreateFileRequest(String filePath, String content) {}
	public record ReadFileRequest(String filePath) {}
	public record WriteFileRequest(String filePath, String content) {}
	public record DeleteFileRequest(String filePath) {}
	public record MoveFileRequest(String sourcePath, String destinationPath) {}
	public record DeleteLinesRequest(String filePath, int startLine, Integer lineCount) {}
	public record InsertLinesRequest(String filePath, int lineNumber, String content) {}
	public record CreateDirectoryRequest(String directoryPath) {}
	public record DeleteDirectoryRequest(String directoryPath) {}
	public record ListDirectoryRequest(String directoryPath) {}
	public record GetFileInfoRequest(String filePath) {}
}
