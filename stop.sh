#!/bin/bash

# FlowVO 服务停止脚本
# 用于停止所有运行中的服务组件

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
log_info "项目根目录: $PROJECT_ROOT"

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -i :$port > /dev/null 2>&1; then
        return 0  # 端口被占用
    else
        return 1  # 端口空闲
    fi
}

# 根据PID文件停止服务
stop_service_by_pid() {
    local service_name=$1
    local pid_file=$2
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            log_info "停止 $service_name 服务 (PID: $pid)..."
            kill "$pid" 2>/dev/null || true
            
            # 等待进程结束，最多等待10秒
            local count=0
            while [ $count -lt 10 ] && kill -0 "$pid" 2>/dev/null; do
                sleep 1
                count=$((count + 1))
            done
            
            # 如果进程仍然存在，强制杀死
            if kill -0 "$pid" 2>/dev/null; then
                log_warning "$service_name 进程未正常退出，强制终止..."
                kill -9 "$pid" 2>/dev/null || true
            fi
            
            log_success "$service_name 服务已停止"
        else
            log_warning "$service_name PID文件存在但进程不在运行"
        fi
        
        # 删除PID文件
        rm -f "$pid_file"
    else
        log_info "$service_name PID文件不存在，可能服务未在运行"
    fi
}

# 根据端口停止服务
stop_service_by_port() {
    local service_name=$1
    local port=$2
    
    if check_port $port; then
        log_info "停止占用端口 $port 的 $service_name 服务..."
        local pid=$(lsof -ti :$port)
        if [ -n "$pid" ]; then
            kill "$pid" 2>/dev/null || true
            sleep 2
            
            # 检查是否还在运行
            if check_port $port; then
                log_warning "$service_name 服务未正常退出，强制终止..."
                kill -9 "$pid" 2>/dev/null || true
            fi
            log_success "$service_name 服务已停止"
        fi
    else
        log_info "$service_name 服务未在运行 (端口 $port)"
    fi
}

# 1. 停止前端应用
stop_frontend() {
    log_info "停止前端应用..."
    stop_service_by_pid "前端应用" "$PROJECT_ROOT/pids/frontend.pid"
    stop_service_by_port "前端应用" 5173
}

# 2. 停止App应用
stop_app_service() {
    log_info "停止App应用..."
    stop_service_by_pid "App应用" "$PROJECT_ROOT/pids/app.pid"
    stop_service_by_port "App应用" 8080
}

# 3. 停止Agents应用
stop_agents_service() {
    log_info "停止Agents应用..."
    stop_service_by_pid "Agents应用" "$PROJECT_ROOT/pids/agents.pid"
    stop_service_by_port "Agents应用" 8081
}

# 4. 停止API Gateway
stop_api_gateway() {
    log_info "停止API Gateway..."
    stop_service_by_pid "API Gateway" "$PROJECT_ROOT/pids/api-gateway.pid"
    stop_service_by_port "API Gateway" 9870
}

# 5. 停止嵌入模型服务
stop_embedding_service() {
    log_info "停止嵌入模型服务..."
    stop_service_by_pid "嵌入模型服务" "$PROJECT_ROOT/pids/embedding_service.pid"
    stop_service_by_port "嵌入模型服务" 8000
}

# 6. 停止文件MCP服务
stop_filemcp_service() {
    log_info "停止文件MCP服务..."
    stop_service_by_pid "文件MCP服务" "$PROJECT_ROOT/pids/filemcp.pid"
    stop_service_by_port "文件MCP服务" 8082
    log_success "文件MCP服务停止完成"
}

# 7. 停止MySQL MCP服务
stop_mysql_mcp_service() {
    log_info "停止MySQL MCP服务..."
    stop_service_by_pid "MySQL MCP服务" "$PROJECT_ROOT/pids/mcp-mysql.pid"
    stop_service_by_port "MySQL MCP服务" 50941
    log_success "MySQL MCP服务停止完成"
}

# 8. 停止所有MCP服务
stop_mcp_services() {
    log_info "停止所有MCP服务..."
    stop_filemcp_service
    stop_mysql_mcp_service
    log_success "所有MCP服务停止完成"
}

# 9. 停止Milvus向量数据库
stop_milvus() {
    log_info "停止Milvus向量数据库..."
    
    cd "$PROJECT_ROOT"
    
    # 检查docker-compose文件是否存在
    if [ ! -f "docker-compose.yml" ]; then
        log_warning "docker-compose.yml 文件不存在"
        return 0
    fi
    
    # 停止Milvus服务集群
    if docker-compose ps --services --filter "status=running" | grep -q .; then
        log_info "停止Milvus数据库集群..."
        docker-compose down
        log_success "Milvus数据库集群已停止"
    else
        log_info "Milvus服务未在运行"
    fi
}

# 10. 清理资源
cleanup() {
    log_info "清理临时文件和资源..."
    
    # 清理可能残留的PID文件
    rm -f "$PROJECT_ROOT/pids/"*.pid 2>/dev/null || true
    
    # 清理临时文件
    rm -f "$PROJECT_ROOT/nohup.out" 2>/dev/null || true
    
    log_success "资源清理完成"
}

# 11. 验证所有服务已停止
verify_all_stopped() {
    log_info "验证所有服务状态..."
    
    local any_running=false
    
    # 检查各服务端口
    if check_port 5173; then
        log_warning "前端应用仍在运行 (端口5173)"
        any_running=true
    fi
    
    if check_port 9870; then
        log_warning "API Gateway仍在运行 (端口9870)"
        any_running=true
    fi
    
    if check_port 8080; then
        log_warning "App服务仍在运行 (端口8080)"
        any_running=true
    fi
    
    if check_port 8081; then
        log_warning "Agents服务仍在运行 (端口8081)"
        any_running=true
    fi
    
    if check_port 8082; then
        log_warning "文件MCP服务仍在运行 (端口8082)"
        any_running=true
    fi
    
    if check_port 50941; then
        log_warning "MySQL MCP服务仍在运行 (端口50941)"
        any_running=true
    fi
    
    if check_port 8000; then
        log_warning "嵌入模型服务仍在运行 (端口8000)"
        any_running=true
    fi
    
    if check_port 19530; then
        log_warning "Milvus服务仍在运行 (端口19530)"
        any_running=true
    fi
    
    if check_port 3000; then
        log_warning "Attu可视化工具仍在运行 (端口3000)"
        any_running=true
    fi
    
    if [ "$any_running" = false ]; then
        log_success "所有服务已成功停止"
    else
        log_warning "部分服务可能仍在运行，请手动检查"
    fi
}

# 显示帮助信息
show_help() {
    echo "FlowVO 服务停止脚本"
    echo ""
    echo "用法:"
    echo "  $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --all, -a        停止所有服务（默认）"
    echo "  --app            仅停止App服务"
    echo "  --agents         仅停止Agents服务"
    echo "  --api-gateway    仅停止API Gateway服务"
    echo "  --frontend       仅停止前端服务"
    echo "  --embedding      仅停止嵌入模型服务"
    echo "  --mcp            停止所有MCP服务（文件MCP + MySQL MCP）"
    echo "  --filemcp        仅停止文件MCP服务"
    echo "  --mysql-mcp      仅停止MySQL MCP服务"
    echo "  --milvus         仅停止Milvus数据库"
    echo "  --help, -h       显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0               # 停止所有服务"
    echo "  $0 --app         # 仅停止App服务"
    echo "  $0 --api-gateway # 仅停止API Gateway服务"
    echo "  $0 --mcp         # 停止所有MCP服务"
    echo "  $0 --filemcp     # 仅停止文件MCP服务"
    echo "  $0 --mysql-mcp   # 仅停止MySQL MCP服务"
    echo "  $0 --milvus      # 仅停止Milvus数据库"
    echo ""
    echo "说明:"
    echo "  • 建议使用 './start.sh --restart' 重启应用服务"
    echo "  • 数据库和嵌入服务通常无需频繁停止"
    echo "  • MCP服务可以独立停止，便于调试和维护"
}

# 主函数
main() {
    echo ""
    log_info "=== FlowVO 服务停止脚本 ==="
    echo ""
    
    # 创建日志目录（如果不存在）
    mkdir -p "$PROJECT_ROOT/pids"
    
    # 解析命令行参数
    case "${1:-}" in
        --help|-h)
            show_help
            exit 0
            ;;
        --app)
            stop_app_service
            ;;
        --agents)
            stop_agents_service
            ;;
        --api-gateway)
            stop_api_gateway
            ;;
        --frontend)
            stop_frontend
            ;;
        --embedding)
            stop_embedding_service
            ;;
        --mcp)
            stop_mcp_services
            ;;
        --filemcp)
            stop_filemcp_service
            ;;
        --mysql-mcp)
            stop_mysql_mcp_service
            ;;
        --milvus)
            stop_milvus
            ;;
        --all|-a|"")
            # 按相反顺序停止服务（与启动顺序相反）
            stop_frontend
            stop_api_gateway
            stop_agents_service
            stop_app_service
            stop_mcp_services
            stop_embedding_service
            stop_milvus
            cleanup
            verify_all_stopped
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
    
    log_success "FlowVO 服务停止操作完成！"
    echo ""
}

# 执行主函数
main "$@" 