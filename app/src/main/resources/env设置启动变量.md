 ⌘ + ⇧ + .（Command-Shift-Dot）导入时打开隐藏文件
 
### 一、

在项目根目录下新建一个名为 .env 的文件，内容就像下面这样，每行一个 KEY=VALUE，不要加多余的引号：

``` 
# .env
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
OPENAI_BASE_URL=https://api.openai.com/v1
```

然后在 IntelliJ IDEA 的 Run/Debug Configuration 里：
1. 	安装并启用 “EnvFile” 插件（Settings → Plugins → 搜 “EnvFile”）。
2. 	打开你要运行的配置，切到 EnvFile 选项卡，勾选 “Enable EnvFile” 并指向项目根的 .env。
3. 	保存后启动，`${OPENAI_API_KEY}`、`${OPENAI_BASE_URL}` 就会自动从 .env 里读取。

在 IntelliJ IDEA 的 Run/Debug Configurations 里找 Spring Boot 启动项，打开 “Environment variables” 输入框,选择刚刚新建的.env文件。

### 二、
