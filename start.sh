#!/bin/bash

# FlowVO 一键启动脚本
# 用于检查和启动所有必要的服务组件

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 启动模式
RESTART_MODE=false
SKIP_DB_SERVICES=false
FORCE_RESTART=false

# 单独服务控制
ONLY_MILVUS=false
ONLY_EMBEDDING=false
ONLY_MCP=false
ONLY_FILEMCP=false
ONLY_MYSQL_MCP=false
ONLY_APP=false
ONLY_AGENTS=false
ONLY_API_GATEWAY=false
ONLY_FRONTEND=false

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

# 解析命令行参数
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --restart|-r)
                RESTART_MODE=true
                shift
                ;;
            --skip-db)
                SKIP_DB_SERVICES=true
                shift
                ;;
            --force|-f)
                FORCE_RESTART=true
                shift
                ;;
            --milvus)
                ONLY_MILVUS=true
                shift
                ;;
            --embedding)
                ONLY_EMBEDDING=true
                shift
                ;;
            --mcp)
                ONLY_MCP=true
                shift
                ;;
            --filemcp)
                ONLY_FILEMCP=true
                shift
                ;;
            --mysql-mcp)
                ONLY_MYSQL_MCP=true
                shift
                ;;
            --app)
                ONLY_APP=true
                shift
                ;;
            --agents)
                ONLY_AGENTS=true
                shift
                ;;
            --api-gateway)
                ONLY_API_GATEWAY=true
                shift
                ;;
            --frontend)
                ONLY_FRONTEND=true
                shift
                ;;
            --help|-h)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# 显示帮助信息
show_help() {
    echo "FlowVO 一键启动脚本"
    echo ""
    echo "用法:"
    echo "  $0 [选项]"
    echo ""
    echo "常规选项:"
    echo "  --restart, -r    重启应用服务（App、Agents、API Gateway、前端）"
    echo "  --skip-db        跳过数据库和嵌入服务检查"
    echo "  --force, -f      强制重启所有服务（包括MCP服务）"
    echo "  --help, -h       显示此帮助信息"
    echo ""
    echo "单独服务控制:"
    echo "  --milvus         仅启动/重启Milvus向量数据库"
    echo "  --embedding      仅启动/重启嵌入模型服务"
    echo "  --mcp            启动/重启所有MCP服务（文件MCP和MySQL MCP）"
    echo "  --filemcp        仅启动/重启文件MCP服务"
    echo "  --mysql-mcp      仅启动/重启MySQL MCP服务"
    echo "  --app            仅启动/重启App应用服务"
    echo "  --agents         仅启动/重启Agents应用服务"
    echo "  --api-gateway    仅启动/重启API Gateway网关服务"
    echo "  --frontend       仅启动/重启前端应用"
    echo ""
    echo "服务说明:"
    echo "  • Milvus向量数据库 (端口19530)"
    echo "  • 嵌入模型服务 (端口8000)"
    echo "  • 文件MCP服务 (端口8082) - 目前暂时跳过"
    echo "  • MySQL MCP服务 (端口50941)"
    echo "  • App应用服务 (端口8080)"
    echo "  • Agents应用服务 (端口8081)"
    echo "  • API Gateway网关 (端口9870)"
    echo "  • 前端应用 (端口5173)"
    echo ""
    echo "示例:"
    echo "  $0               # 常规启动，检查所有服务"
    echo "  $0 --restart     # 重启应用服务，保持数据库和MCP服务运行"
    echo "  $0 --skip-db     # 只启动应用服务（跳过Milvus和嵌入服务）"
    echo "  $0 --force       # 强制重启所有服务，包括数据库和MCP"
    echo "  $0 --app         # 仅启动/重启App服务"
    echo "  $0 --frontend    # 仅启动/重启前端应用"
    echo "  $0 --mcp         # 启动/重启所有MCP服务"
    echo "  $0 --filemcp     # 仅启动/重启文件MCP服务"
    echo "  $0 --mysql-mcp   # 仅启动/重启MySQL MCP服务"
    echo ""
    echo "说明:"
    echo "  • 数据库和嵌入服务默认只在检测不到时才启动"
    echo "  • MCP服务会自动检测端口占用状态并智能启动"
    echo "  • 应用服务支持智能重启，无需手动停止"
    echo "  • 单独服务选项会强制重启对应服务"
    echo "  • 查看详细状态：启动完成后会显示所有服务地址"
}

# 加载环境变量
load_env_file() {
    if [ -f "$PROJECT_ROOT/.env" ]; then
        log_info "加载环境变量文件: .env"
        # 读取.env文件并导出环境变量
        set -a  # 自动导出变量
        source "$PROJECT_ROOT/.env"
        set +a  # 关闭自动导出
        log_success "环境变量加载完成"
    else
        log_warning ".env 文件不存在，将使用系统环境变量"
        log_info "如需配置API密钥，请在项目根目录创建 .env 文件"
    fi
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -i :$port > /dev/null 2>&1; then
        return 0  # 端口被占用
    else
        return 1  # 端口空闲
    fi
}

# 检查服务健康状态
check_service_health() {
    local url=$1
    local timeout=${2:-5}
    
    if curl -s --max-time $timeout "$url" > /dev/null 2>&1; then
        return 0  # 服务健康
    else
        return 1  # 服务不健康
    fi
}

# 停止指定服务
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
        fi
        rm -f "$pid_file"
    fi
}

# 等待服务启动
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    log_info "等待 $service_name 服务启动..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            log_success "$service_name 服务已启动"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "$service_name 服务启动超时"
    return 1
}

# 1. 检查并启动Milvus数据库
start_milvus() {
    if [ "$SKIP_DB_SERVICES" = true ] && [ "$FORCE_RESTART" = false ]; then
        log_info "跳过Milvus数据库检查"
        return 0
    fi

    log_info "检查Milvus数据库服务..."
    
    cd "$PROJECT_ROOT"
    
    # 检查docker-compose文件是否存在
    if [ ! -f "docker-compose.yml" ]; then
        log_error "docker-compose.yml 文件不存在"
        exit 1
    fi
    
    # 检查Milvus是否已经运行且健康
    if check_port 19530 && check_service_health "http://localhost:9091/health" 10; then
        log_success "Milvus数据库服务已在运行且健康 (端口19530)"
        log_info "Milvus可视化工具: http://localhost:3000"
        log_info "MinIO控制台: http://localhost:9001"
        return 0
    fi
    
    # 如果需要强制重启，先停止
    if [ "$FORCE_RESTART" = true ]; then
        log_info "强制重启Milvus数据库集群..."
        docker-compose down
    fi
    
    log_info "启动Milvus数据库集群..."
    docker-compose up -d
    
    # 等待Milvus启动
    if wait_for_service "http://localhost:9091/health" "Milvus"; then
        log_success "Milvus数据库集群启动成功"
        log_info "Milvus可视化工具: http://localhost:3000"
        log_info "MinIO控制台: http://localhost:9001"
    else
        log_error "Milvus启动失败"
        exit 1
    fi
}

# 2. 检查并启动外部嵌入模型服务
start_embedding_service() {
    if [ "$SKIP_DB_SERVICES" = true ] && [ "$FORCE_RESTART" = false ]; then
        log_info "跳过嵌入模型服务检查"
        return 0
    fi

    log_info "检查外部嵌入模型服务..."
    
    # 检查嵌入服务是否已经运行且健康
    if check_port 8000 && check_service_health "http://localhost:8000/health" 5; then
        log_success "嵌入模型服务已在运行且健康 (端口8000)"
        log_info "嵌入模型API文档: http://localhost:8000/docs"
        return 0
    fi
    
    # 如果端口被占用但健康检查失败，或者需要强制重启，则重启服务
    if check_port 8000; then
        log_warning "嵌入模型服务端口被占用但健康检查失败，重启服务..."
        stop_service_by_pid "嵌入模型服务" "$PROJECT_ROOT/pids/embedding_service.pid"
        # 等待端口释放
        sleep 3
    fi
    
    log_info "启动嵌入模型服务..."
    
    # 检查虚拟环境
    if [ ! -d "$PROJECT_ROOT/embedding_env" ]; then
        log_error "虚拟环境 embedding_env 不存在，请先运行安装脚本"
        exit 1
    fi
    
    # 启动嵌入服务（后台运行）
    cd "$PROJECT_ROOT"
    source embedding_env/bin/activate
    
    # 检查依赖
    if ! python -c "import fastapi, uvicorn, sentence_transformers, langchain" 2>/dev/null; then
        log_error "Python依赖包缺失，请先安装依赖"
        exit 1
    fi
    
    # 后台启动嵌入服务
    nohup uvicorn python.embed_tools_server:app --host 0.0.0.0 --port 8000 > logs/embedding_service.log 2>&1 &
    echo $! > pids/embedding_service.pid
    
    # 等待服务启动
    if wait_for_service "http://localhost:8000/health" "嵌入模型服务"; then
        log_success "嵌入模型服务启动成功"
        log_info "嵌入模型API文档: http://localhost:8000/docs"
    else
        log_error "嵌入模型服务启动失败"
        exit 1
    fi
}

# 3. 启动文件MCP服务
start_filemcp_service() {
    log_info "启动文件MCP服务..."
    
    # 启动文件MCP服务 - 暂时跳过（根据用户要求）
    if false && [ -d "$PROJECT_ROOT/mcp/fileMCP" ]; then
        log_info "启动文件MCP服务..."
        
        # 检查端口8082是否已被占用
        if check_port 8082; then
            if [ "$RESTART_MODE" = true ] || [ "$FORCE_RESTART" = true ]; then
                log_info "重启文件MCP服务..."
                stop_service_by_pid "文件MCP服务" "$PROJECT_ROOT/pids/filemcp.pid"
                sleep 3
            else
                log_success "文件MCP服务已在运行 (端口8082)"
                return 0
            fi
        fi
        
        if ! check_port 8082; then
            cd "$PROJECT_ROOT/mcp/fileMCP"
            if [ -f "mvnw" ]; then
                log_info "使用Maven Wrapper启动文件MCP服务..."
                nohup ./mvnw spring-boot:run -Dserver.port=8082 > ../../logs/filemcp.log 2>&1 &
                echo $! > ../../pids/filemcp.pid
            elif command -v mvn > /dev/null 2>&1; then
                log_info "使用系统Maven启动文件MCP服务..."
                nohup mvn spring-boot:run -Dserver.port=8082 > ../../logs/filemcp.log 2>&1 &
                echo $! > ../../pids/filemcp.pid
            else
                log_warning "文件MCP服务启动失败：找不到Maven"
                return 1
            fi
        fi
        
        # 等待服务启动
        sleep 3
        if check_port 8082; then
            log_success "文件MCP服务启动成功 (端口8082)"
        else
            log_warning "文件MCP服务启动可能失败，请检查日志"
        fi
    else
        log_info "跳过文件MCP服务（按用户要求暂时不启动）"
    fi
}

# 4. 启动MySQL MCP服务
start_mysql_mcp_service() {
    log_info "启动MySQL MCP服务..."
    
    # 启动MySQL MCP服务
    if [ -d "$PROJECT_ROOT/mcp/mcp-mysql" ]; then
        # 检查端口50941是否已被占用
        if check_port 50941; then
            if [ "$RESTART_MODE" = true ] || [ "$FORCE_RESTART" = true ]; then
                log_info "重启MySQL MCP服务..."
                stop_service_by_pid "MySQL MCP服务" "$PROJECT_ROOT/pids/mcp-mysql.pid"
                sleep 3
            else
                log_success "MySQL MCP服务已在运行 (端口50941)"
                return 0
            fi
        fi
        
        if ! check_port 50941; then
            cd "$PROJECT_ROOT/mcp/mcp-mysql"
            if [ -f "mvnw" ]; then
                log_info "使用Maven Wrapper启动MySQL MCP服务..."
                nohup ./mvnw spring-boot:run > ../../logs/mcp-mysql.log 2>&1 &
                echo $! > ../../pids/mcp-mysql.pid
            elif command -v mvn > /dev/null 2>&1; then
                log_info "使用系统Maven启动MySQL MCP服务..."
                nohup mvn spring-boot:run > ../../logs/mcp-mysql.log 2>&1 &
                echo $! > ../../pids/mcp-mysql.pid
            else
                log_warning "MySQL MCP服务启动失败：找不到Maven"
                return 1
            fi
        fi
        
        # 等待服务启动
        sleep 3
        if check_port 50941; then
            log_success "MySQL MCP服务启动成功 (端口50941)"
        else
            log_warning "MySQL MCP服务启动可能失败，请检查日志"
        fi
    else
        log_warning "MySQL MCP目录不存在: $PROJECT_ROOT/mcp/mcp-mysql"
        return 1
    fi
}

# 5. 启动所有MCP服务
start_mcp_services() {
    log_info "启动所有MCP服务..."
    
    start_filemcp_service
    start_mysql_mcp_service
    
    log_success "MCP服务启动完成"
}

# 6. 启动App应用
start_app_service() {
    log_info "检查App应用服务..."
    
    # 检查App服务是否已经运行且健康
    if check_port 8080 && check_service_health "http://localhost:8080/api/health" 5; then
        if [ "$RESTART_MODE" = true ] || [ "$FORCE_RESTART" = true ]; then
            log_info "重启App应用服务..."
            stop_service_by_pid "App应用" "$PROJECT_ROOT/pids/app.pid"
            # 等待端口释放
            sleep 3
        else
            log_success "App服务已在运行且健康 (端口8080)"
            return 0
        fi
    elif check_port 8080; then
        log_warning "App服务端口被占用但健康检查失败，重启服务..."
        stop_service_by_pid "App应用" "$PROJECT_ROOT/pids/app.pid"
        sleep 3
    fi
    
    log_info "启动App应用..."
    cd "$PROJECT_ROOT/app"
    
    # 优先使用mvnw，如果没有再使用mvn命令
    if [ -f "mvnw" ]; then
        log_info "使用Maven Wrapper启动App服务..."
        nohup ./mvnw spring-boot:run > ../logs/app.log 2>&1 &
        echo $! > ../pids/app.pid
    elif command -v mvn > /dev/null 2>&1; then
        log_info "使用系统Maven启动App服务..."
        nohup mvn spring-boot:run > ../logs/app.log 2>&1 &
        echo $! > ../pids/app.pid
    elif [ -f "target/app-*.jar" ]; then
        log_info "使用jar包启动App服务..."
        jar_file=$(ls target/app-*.jar | head -1)
        # 切换到项目根目录再启动jar包，确保静态资源路径正确
        cd "$PROJECT_ROOT"
        nohup java -jar "app/$jar_file" > logs/app.log 2>&1 &
        echo $! > pids/app.pid
    else
        log_error "无法启动App应用：既没有Maven Wrapper、Maven命令，也没有可执行的jar包"
        log_info "请先安装maven或构建项目"
        return 1
    fi
    
    # 等待服务启动
    if wait_for_service "http://localhost:8080/api/health" "App应用"; then
        log_success "App应用启动成功"
        log_info "App健康检查: http://localhost:8080/api/health"
    else
        log_error "App应用启动失败"
        exit 1
    fi
}

# 7. 启动Agents应用
start_agents_service() {
    log_info "检查Agents应用服务..."
    
    # 检查Agents服务是否已经运行且健康
    if check_port 8081 && check_service_health "http://localhost:8081/api/health" 5; then
        if [ "$RESTART_MODE" = true ] || [ "$FORCE_RESTART" = true ]; then
            log_info "重启Agents应用服务..."
            stop_service_by_pid "Agents应用" "$PROJECT_ROOT/pids/agents.pid"
            # 等待端口释放
            sleep 3
        else
            log_success "Agents服务已在运行且健康 (端口8081)"
            return 0
        fi
    elif check_port 8081; then
        log_warning "Agents服务端口被占用但健康检查失败，重启服务..."
        stop_service_by_pid "Agents应用" "$PROJECT_ROOT/pids/agents.pid"
        sleep 3
    fi
    
    log_info "启动Agents应用..."
    cd "$PROJECT_ROOT/agents"
    
    # 优先使用mvnw，如果没有再使用mvn命令
    if [ -f "mvnw" ]; then
        log_info "使用Maven Wrapper启动Agents服务..."
        nohup ./mvnw spring-boot:run > ../logs/agents.log 2>&1 &
        echo $! > ../pids/agents.pid
    elif command -v mvn > /dev/null 2>&1; then
        log_info "使用系统Maven启动Agents服务..."
        nohup mvn spring-boot:run > ../logs/agents.log 2>&1 &
        echo $! > ../pids/agents.pid
    elif [ -f "target/agents-*.jar" ]; then
        log_info "使用jar包启动Agents服务..."
        jar_file=$(ls target/agents-*.jar | head -1)
        # 切换到项目根目录再启动jar包，确保静态资源路径正确
        cd "$PROJECT_ROOT"
        nohup java -jar "agents/$jar_file" > logs/agents.log 2>&1 &
        echo $! > pids/agents.pid
    else
        log_error "无法启动Agents应用：既没有Maven Wrapper、Maven命令，也没有可执行的jar包"
        log_info "请先安装maven或构建项目"
        return 1
    fi
    
    # 等待服务启动
    if wait_for_service "http://localhost:8081/api/health" "Agents应用"; then
        log_success "Agents应用启动成功"
        log_info "Agents健康检查: http://localhost:8081/api/health"
    else
        log_error "Agents应用启动失败"
        exit 1
    fi
}

# 8. 启动API Gateway
start_api_gateway() {
    log_info "检查API Gateway服务..."
    
    # 检查API Gateway服务是否已经运行且健康
    if check_port 9870 && check_service_health "http://localhost:9870/actuator/health" 5; then
        if [ "$RESTART_MODE" = true ] || [ "$FORCE_RESTART" = true ]; then
            log_info "重启API Gateway服务..."
            stop_service_by_pid "API Gateway" "$PROJECT_ROOT/pids/api-gateway.pid"
            # 等待端口释放
            sleep 3
        else
            log_success "API Gateway服务已在运行且健康 (端口9870)"
            return 0
        fi
    elif check_port 9870; then
        log_warning "API Gateway服务端口被占用但健康检查失败，重启服务..."
        stop_service_by_pid "API Gateway" "$PROJECT_ROOT/pids/api-gateway.pid"
        sleep 3
    fi
    
    log_info "启动API Gateway..."
    cd "$PROJECT_ROOT/api-gateway"
    
    # 优先使用mvnw，如果没有再使用mvn命令
    if [ -f "mvnw" ]; then
        log_info "使用Maven Wrapper启动API Gateway服务..."
        nohup ./mvnw spring-boot:run > ../logs/api-gateway.log 2>&1 &
        echo $! > ../pids/api-gateway.pid
    elif command -v mvn > /dev/null 2>&1; then
        log_info "使用系统Maven启动API Gateway服务..."
        nohup mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
        echo $! > ../pids/api-gateway.pid
    elif [ -f "target/api-gateway-*.jar" ]; then
        log_info "使用jar包启动API Gateway服务..."
        jar_file=$(ls target/api-gateway-*.jar | head -1)
        # 切换到项目根目录再启动jar包，确保静态资源路径正确
        cd "$PROJECT_ROOT"
        nohup java -jar "api-gateway/$jar_file" > logs/api-gateway.log 2>&1 &
        echo $! > pids/api-gateway.pid
    else
        log_error "无法启动API Gateway：既没有Maven Wrapper、Maven命令，也没有可执行的jar包"
        log_info "请先安装maven或构建项目"
        return 1
    fi
    
    # 等待服务启动
    if wait_for_service "http://localhost:9870/actuator/health" "API Gateway"; then
        log_success "API Gateway启动成功"
        log_info "API Gateway健康检查: http://localhost:9870/actuator/health"
        log_info "API Gateway网关地址: http://localhost:9870"
    else
        log_error "API Gateway启动失败"
        exit 1
    fi
}

# 9. 启动前端应用
start_frontend() {
    log_info "检查前端应用..."
    
    # 检查前端服务是否已经运行
    if check_port 5173; then
        if [ "$RESTART_MODE" = true ] || [ "$FORCE_RESTART" = true ]; then
            log_info "重启前端应用..."
            stop_service_by_pid "前端应用" "$PROJECT_ROOT/pids/frontend.pid"
            # 等待端口释放
            sleep 3
        else
            log_success "前端服务已在运行 (端口5173)"
            return 0
        fi
    fi
    
    log_info "启动前端应用..."
    cd "$PROJECT_ROOT/flowvo-ui"
    
    # 检查package.json是否存在
    if [ ! -f "package.json" ]; then
        log_error "前端package.json文件不存在"
        exit 1
    fi
    
    # 检查node_modules是否存在
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install
    fi
    
    # 后台启动前端服务
    nohup npm run dev > ../logs/frontend.log 2>&1 &
    echo $! > ../pids/frontend.pid
    
    # 等待前端服务启动
    sleep 5
    if check_port 5173; then
        log_success "前端应用启动成功"
        log_info "前端应用: http://localhost:5173"
    else
        log_warning "前端应用可能还在启动中，请稍后检查"
    fi
}

# 显示服务状态
show_service_status() {
    echo ""
    log_info "=== FlowVO 服务状态 ==="
    echo ""
    
    echo "📊 核心服务："
    echo "  • 前端应用: http://localhost:5173"
    echo "  • API Gateway网关: http://localhost:9870"
    echo "  • App服务: http://localhost:8080"
    echo "  • Agents服务: http://localhost:8081"
    echo ""
    
    echo "🔧 数据库和存储："
    echo "  • Milvus向量数据库: localhost:19530"
    echo "  • Milvus可视化工具: http://localhost:3000"
    echo "  • MinIO存储控制台: http://localhost:9001"
    echo ""
    
    echo "🤖 AI和嵌入服务："
    echo "  • 嵌入模型API: http://localhost:8000/docs"
    echo "  • 嵌入模型健康检查: http://localhost:8000/health"
    echo ""
    
    echo "🔌 MCP服务："
    echo "  • 文件MCP服务: http://localhost:8082"
    echo "  • MySQL MCP服务: http://localhost:50941"
    echo ""
    
    echo "💚 健康检查端点："
    echo "  • API Gateway健康检查: http://localhost:9870/actuator/health"
    echo "  • App服务健康检查: http://localhost:8080/api/health"
    echo "  • Agents服务健康检查: http://localhost:8081/api/health"
    echo ""
    
    echo "📝 管理命令："
    echo "  • 查看所有日志: tail -f logs/*.log"
    echo "  • 查看所有PID: ls -la pids/"
    echo "  • 停止所有服务: ./stop.sh"
    echo "  • 重启应用服务: ./start.sh --restart"
    echo "  • 强制重启所有: ./start.sh --force"
}

# 主函数
main() {
    echo ""
    log_info "=== FlowVO 一键启动脚本 ==="
    echo ""
    
    # 解析命令行参数
    parse_args "$@"
    
    # 检查是否选择了单独服务启动
    local single_service_mode=false
    if [ "$ONLY_MILVUS" = true ] || [ "$ONLY_EMBEDDING" = true ] || [ "$ONLY_MCP" = true ] || \
       [ "$ONLY_FILEMCP" = true ] || [ "$ONLY_MYSQL_MCP" = true ] || \
       [ "$ONLY_APP" = true ] || [ "$ONLY_AGENTS" = true ] || [ "$ONLY_API_GATEWAY" = true ] || \
       [ "$ONLY_FRONTEND" = true ]; then
        single_service_mode=true
    fi
    
    # 显示启动模式
    if [ "$single_service_mode" = true ]; then
        log_info "单独服务模式：仅启动选定的服务"
    elif [ "$FORCE_RESTART" = true ]; then
        log_warning "强制重启模式：将重启所有服务"
    elif [ "$RESTART_MODE" = true ]; then
        log_info "重启模式：将重启应用服务，保持数据库服务运行"
    elif [ "$SKIP_DB_SERVICES" = true ]; then
        log_info "应用模式：仅启动应用服务"
    else
        log_info "常规模式：智能检查并启动所有服务"
    fi
    
    # 加载环境变量
    load_env_file
    
    # 创建必要的目录
    mkdir -p "$PROJECT_ROOT/logs"
    mkdir -p "$PROJECT_ROOT/pids"
    
    # 根据模式启动服务
    if [ "$single_service_mode" = true ]; then
        # 单独服务模式 - 强制重启选定的服务
        if [ "$ONLY_MILVUS" = true ]; then
            FORCE_RESTART=true start_milvus
        fi
        if [ "$ONLY_EMBEDDING" = true ]; then
            FORCE_RESTART=true start_embedding_service
        fi
        if [ "$ONLY_MCP" = true ]; then
            FORCE_RESTART=true start_mcp_services
        fi
        if [ "$ONLY_FILEMCP" = true ]; then
            FORCE_RESTART=true start_filemcp_service
        fi
        if [ "$ONLY_MYSQL_MCP" = true ]; then
            FORCE_RESTART=true start_mysql_mcp_service
        fi
        if [ "$ONLY_APP" = true ]; then
            FORCE_RESTART=true start_app_service
        fi
        if [ "$ONLY_AGENTS" = true ]; then
            FORCE_RESTART=true start_agents_service
        fi
        if [ "$ONLY_API_GATEWAY" = true ]; then
            FORCE_RESTART=true start_api_gateway
        fi
        if [ "$ONLY_FRONTEND" = true ]; then
            FORCE_RESTART=true start_frontend
        fi
        
        log_success "选定服务启动完成！"
    else
        # 按顺序启动所有服务
        start_milvus
        start_embedding_service
        start_mcp_services
        start_app_service
        start_agents_service
        start_api_gateway
        start_frontend
        
        # 显示服务状态
        show_service_status
        
        log_success "FlowVO 平台启动完成！"
    fi
    
    echo ""
}

# 执行主函数
main "$@" 