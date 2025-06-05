package io.mcp.filemcp;

import io.mcp.filemcp.service.FileOperationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 文件操作MCP服务器应用程序 - Spring AI标准实现
 * 
 * 🚀 Spring AI MCP Server特性：
 * - 使用@Tool注解自动注册工具
 * - ToolCallbackProvider自动发现和管理工具
 * - 支持SSE和STDIO传输方式
 * - 符合MCP协议规范
 * 
 * 📁 文件操作工具集（来自FileOperationsService）：
 * - createFile: 创建文件并可选写入内容
 * - readFile: 智能读取文件内容（支持PDF、Word等）
 * - writeFile: 写入文件内容  
 * - deleteFile: 删除文件
 * - moveFile: 移动/重命名文件
 * - deleteLines: 删除文件中的指定行
 * - insertLines: 在文件中插入新行
 * - createDirectory: 创建目录
 * - deleteDirectory: 删除目录及内容
 * - listDirectory: 列出目录内容
 * - getFileInfo: 获取文件/目录详细信息
 * 
 * 🔧 可用端点：
 * - /sse - SSE端点（MCP客户端连接）
 * - /mcp/message - 消息端点
 * - /actuator/health - 健康检查
 */
@Slf4j
@SpringBootApplication
public class FileMcpApplication {

	@Autowired
	private Environment environment;

	public static void main(String[] args) {
		log.info("🚀 正在启动Spring AI MCP文件服务器...");
		SpringApplication.run(FileMcpApplication.class, args);
	}

	/**
	 * 创建ToolCallbackProvider Bean
	 * 使用MethodToolCallbackProvider自动发现FileOperationsService中的@Tool注解工具
	 */
	@Bean
	public ToolCallbackProvider fileTools(FileOperationsService fileOperationsService) {
		log.info("🔧 正在注册文件操作工具...");
		
		// 使用MethodToolCallbackProvider扫描@Tool注解的方法
		ToolCallbackProvider provider = MethodToolCallbackProvider.builder()
			.toolObjects(fileOperationsService)  // 传入现有的FileOperationsService
			.build();
		
		// 获取注册的工具数量
		ToolCallback[] callbacks = provider.getToolCallbacks();
		log.info("✅ 成功注册 {} 个文件操作工具", callbacks.length);
		
		// 打印工具列表(暂时不输出)
//		for (ToolCallback callback : callbacks) {
//			log.info("   🛠️ 工具: {}", callback.getToolDefinition());
//		}
		
		return provider;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		String port = environment.getProperty("server.port", "9091");
		String baseUrl = "http://localhost:" + port;
		String profile = environment.getProperty("spring.profiles.active", "default");
		
		log.info("🎉 Spring AI MCP文件服务器启动成功！");
		log.info("📋 服务器信息:");
		log.info("   🌐 基础地址: {}", baseUrl);
		log.info("   📦 运行环境: {}", profile);
		log.info("   🔧 MCP协议版本: 2024-11-05");
		log.info("   ⚡ Spring AI版本: 1.0.0");
		log.info("");
		log.info("🔌 MCP端点:");
		log.info("   📡 SSE端点: {}/sse", baseUrl);
		log.info("   📮 消息端点: {}/mcp/message", baseUrl);
		log.info("   ❤️ 健康检查: {}/actuator/health", baseUrl);
		log.info("");
		log.info("🛠️ 已注册的文件操作工具:");
		log.info("   📄 createFile - 创建文件并可选写入内容");
		log.info("   📖 readFile - 智能读取文件内容（支持PDF、Word等）");
		log.info("   ✏️ writeFile - 写入文件内容");
		log.info("   🗑️ deleteFile - 删除文件");
		log.info("   🔄 moveFile - 移动/重命名文件");
		log.info("   ➖ deleteLines - 删除文件中的指定行");
		log.info("   ➕ insertLines - 在文件中插入新行");
		log.info("   📁 createDirectory - 创建目录");
		log.info("   🗂️ deleteDirectory - 删除目录及内容");
		log.info("   📂 listDirectory - 列出目录内容");
		log.info("   ℹ️ getFileInfo - 获取文件/目录详细信息");
		log.info("");
		log.info("📚 客户端配置示例:");
		log.info("   spring:");
		log.info("     ai:");
		log.info("       mcp:");
		log.info("         client:");
		log.info("           connections:");
		log.info("             file-server:");
		log.info("               url: {}", baseUrl);
		log.info("               transport: sse");
		log.info("               sse-endpoint: /sse");
		log.info("");
		log.info("✅ MCP服务器就绪，等待客户端连接...");
	}
}
