package org.xue.milvus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.milvus.service.ChunkMilvusService;

import java.util.List;

@SpringBootTest
public class ChunkMilvusServiceTest {

    @Autowired
    private ChunkMilvusService chunkMilvusService;

    //测试文档向量化写入
    @Test
    public void testSplitEmbed() {
        String doc = "财务各系统信息登录\n" +
                "事项处理\n" +
                "系统维护协议类:\n" +
                " 1、费控系统、增值税系统、oracle系统续签维护协议（2022/7/31-2023/8/1）\n" +
                " 2、税控系统维护协议（2022年12月1日签署，有效期3年，自2025年11月30日止）\n" +
                " 3、资金系统维护协议（2022/10/18-2023/10/17）\n" +
                "测试类:\n" +
                " 核心系统测试（个、团、全渠道）各项需求测试（主要为新业务各类场景的账务验证以及手续费各场景账务验证）\n" +
                " 核心系统和资金系统/费控系统测试（主要收付费或系统对接类）\n" +
                " 费控系统或资金系统或oracle系统测试（系统需求满足程度以及优化）\n" +
                "维护类：\n" +
                " 新机构成立（oracle、费控、资金、增值税、税控）\n" +
                " 人事任命（费控审批流）\n" +
                " 新员工入职（费控系统）\n" +
                " 科目、渠道、预算、部门（费控系统、oracle系统）\n" +
                " 各机构问题咨询\n" +
                "供应商VPN用户管理：\n" +
                " 每年延期一次，提交OA权限申请\n" +
                " 费控、增值税供应商汉得，VPN用户名duandingding\n" +
                " 资金供应商保融，VPN用户名jianglei、lind\n" +
                "税控:\n" +
                " 每年3月中旬，向中银保信申请税控系统注册码，以便机构进行开票\n" +
                "账务处理类:\n" +
                " 每月oracle中冲销上月非财数据，冲销期间在上月，并导入报表处倪旻婧所提供的非财务数据\n" +
                " 凭证自动处理，每天查看59211system/Aa111111用户的批处理结果，如有报错查看原因并进行后续处理\n" +
                "收付费类:\n" +
                " 收付费通道的搭建、评估、设立、配置等评估。\n" +
                " 收付费系统优化配置等\n" +
                "生产环境信息登记：\n" +
                "Oracle：59211yulixing\\ ShGxy232970\n" +
                " http://jkerp.founder.com:8008/\n" +
                " 数据库172.25.1.37:1521/F50PROD       selapps/apps \n" +
                " 接口表 172.25.1.37:1529/f50prod        renshou/renshoupwd\n" +
                "费控系统：\n" +
                " http://192.168.103.152:8080/fz_hec/    SH0612/Bb777777\n" +
                " 数据库 192.168.103.153:6820/pfeikdb01  fz_hec/Aa111111\n" +
                "资金系统： SH0612/Aa333333   admin/Zz111111\n" +
                " http://192.168.103.107:7001/pkufi-front/bizframe/jsp/login.jsp\n" +
                " http://192.168.103.108:7001/pkufi-admin/\n" +
                " 数据库192.168.103.109:6820/pinvestdb   ats001/atspassword\n" +
                "增值税系统： SH0612/Zz111111\n" +
                " http://192.168.101.93:9082/hvat2/\n" +
                " 数据库  192.168.101.93:1521/feikong   zzsdb/yong3da8#\n" +
                "税控：admin/123456\n" +
                " http://192.168.101.232:9080/SKServer/index.jsp\n" +
                " 注册码更新用地址\n" +
                " http://192.168.101.232:9080/SKServer/sys/adminIndex.do?socketzcm=0\n" +
                "恒生\n" +
                " 0612/Aa111111\n" +
                "发票邮箱管理\n" +
                " http://192.168.100.51/pkufi_wechat_mis/loginController.do?method=login#\n" +
                " joy_tan /Aa666666\n" +
                "\n" +
                "测试环境信息登记:\n" +
                "Oracle:\n" +
                " 测试账号：59211yulixing/Sh800113\n" +
                " http://f50sit.founder.com:8020/\n" +
                " 接口表   表名CUX_59_GL_INTERFACE_NEW\n" +
                " 172.25.1.178:1541/F50SIT     renshou/renshoupwd    apps/apps\n" +
                "费控系统:\n" +
                " 测试账号：sh0612/Aa000000\n" +
                " http:// 192.161.4.210:9082/fzhec/login.screen\n" +
                " 数据库  192.161.4.210:1521/fintstdb \thectest/hectest\n" +
                "资金系统【资金一般供应商去测试】:\n" +
                " 测试账号：server/Aa777777\n" +
                " 测试前台密码 server/fingard!1\n" +
                " http://192.161.4.36:8001/pkufi-front/bizframe/jsp/login.jsp\n" +
                " 数据库  192.161.4.210:1521/fintstdb  ats001/ats001\n" +
                "增值税系统:\n" +
                " 测试账号：shtest01/Aa222222\n" +
                " http://192.161.4.180:8082/HVAT/login.screen\n" +
                "数据库 \t192.161.4.180:1521/fktest   HVAT/HVAT\n" +
                "税控系统：\n" +
                " 测试账号：管理员账号：bdfz_001    开票员账号：bdfz_002     密码都是zbx@12345678\n" +
                " http://172.20.241.87:7015/SKServer\n" +
                "供应商信息：\n" +
                "oracle: 集团it-李际东,集团财务部黄海鸣，oracle顾问-贾雅慧\n" +
                "投资后台的估值系统：恒生实施-签收日落，恒生商务-高伟\n" +
                "oracle、费控、增值税：汉得商务-王周波,顾问-陶盈\n" +
                "费控系统：增值税顾问-陶盈\n" +
                "税控系统：中银保信-余玲鑫\n" +
                "资金系统：保融运维-傅佳莹\n" +
                "特殊备注：\n" +
                "Oracle：\n" +
                " 9个段值、机构【最细颗粒度可到4级】、部门【需要和预算进行确认】、科目、子科目、产品、项目、往来、备用1、备用2【渠道】\n" +
                "     机构、往来：新增需要向集团管控申请\n" +
                "     科目：新增需要向集团管控申请\n" +
                "     部门：需要和预算进行确认后扩充【和渠道做同步映射】\n" +
                "     子科目：银行、投资、预算、S开头，现状只会扩充不会缩减\n" +
                "     产品：使用产品细类，internal_id\n" +
                "     项目：万能部分新增的时候，恒生部分同步需要新增【对应T1code】\n" +
                "     备用2:【T4code】region\n" +
                "Oracle自动处理凭证时间点\n" +
                " 费控：每天7点运行一次\n" +
                " 资金：每天8点运行一次\n" +
                " 团险核心：每天5点开始，每隔6小时运行\n" +
                " 个险核心：每天6天开始，每隔6小时运行\n" +
                "增值税、税控：\n" +
                "增值税：\n" +
                "    每月底等机构反馈异常发票数据，待机构与税控系统核对后明确要调整内容，进行后台数据维护。\n" +
                "新机构开设税控设备采购流程【周期较长事项】：\n" +
                " 新机构开设初期阶段，可以询问中银保信的人员确认需要准备的信息，整理初版信息\n" +
                " 新机构营业执照、税务信息、当地机构银行信息确认后，整理定稿信息，发送至中银保信，进行付款采购申请流程【机构自行处理】\n" +
                " 获取报税盘【主备】、核心板【主备】、钥匙，获取核心板后协商TS部门进行硬件替换，进行全财务通知下发，停机替换设备，并开设对应机构管理员账号，机构开票员账号。\n" +
                " 机构关闭：进行核心板拆卸和账号回收，未结束流程的税务流程，递归至上级分公司进行统一处理\n" +
                "\n";
//        String doc = "明朝嘉靖元年，《三国志通俗演义》刊刻而成，题“晋平阳侯陈寿史传，后学罗贯中编次”，这就是后来《三国演义》各种版本的祖本。《三国志演义》与《三国演义》，都不是罗贯中原作的名称，而是在小说流传的过程中出现的。前者见于明朝周弘祖的《古今书刻》，相沿已久；后者则见于清朝毛宗岗的《读三国志法》。它们各自从一定角度反映了《三国》的特点。";
//        String doc = "我们过了江，进了车站。我买票，他忙着照看行李。行李太多了，得向脚夫⑾行些小费才可过去。他便又忙着和他们讲价钱。我那时真是聪明过分，总觉他说话不大漂亮，非自己插嘴不可，但他终于讲定了价钱；就送我上车。他给我拣定了靠车门的一张椅子；我将他给我做的紫毛大衣铺好座位。他嘱我路上小心，夜里要警醒些，不要受凉。又嘱托茶房好好照应我。我心里暗笑他的迂；他们只认得钱，托他们只是白托！而且我这样大年纪的人，难道还不能料理自己么？我现在想想，我那时真是太聪明了。";

        chunkMilvusService.insertChunks(doc);
    }

    //测试向量检索
    @Test
    public void searchSimilarChunks() {
//        String queryText = "录鬼簿";
        String queryText = "增值税系统";
        int topK = 1;
        List<String> hits = chunkMilvusService.searchSimilarChunks(queryText, topK);
        for (String h : hits) {
            int i = 1;
            System.out.println("chunk-" + i + ":" + h);
        }

    }
}
