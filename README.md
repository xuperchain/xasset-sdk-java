# XASSET JAVA SDK

## 概述

本文档主要介绍Xasset平台JAVA语言版的开发者工具包（SDK），开发者可基于该SDK使用Java语言接入到Xasset平台的各项服务。SDK封装了便捷的调用接口，保持了多种编程语言版的使用方式、调用接口相似，提供了统一的错误码和返回格式，方便开发者调试。

## 使用说明

- 1.从平台申请获得到API准入[**AK/SK**](https://cloud.baidu.com/product/xuperasset.html)。注意AK/SK是准入凭证，不要泄露，不要下发或配置在客户端使用。
- 2.使用Maven安装，在Maven的pom.xml文件中添加xasset-sdk-java的依赖。
- 3.接入联调环境联调测试，测试通过后更换到线上环境，完成接入。

### 运行环境

```
java version "1.8.0_45"
Java(TM) SE Runtime Environment (build 1.8.0_45-b14)
```

### Maven安装
1. 在pom.xml添加如下依赖：
```
    <dependencies>
        <dependency>
            <groupId>com.baidu.xuper</groupId>
            <artifactId>xasset-sdk-java</artifactId>
            <version>1.0.7</version>
        </dependency>
        <dependency>
            <groupId>com.baidu.xuper</groupId>
            <artifactId>xuper-java-sdk</artifactId>
            <version>0.2.0</version>
        </dependency>
    </dependencies>

```

### 版本记录
|编号 | 版本号 | 修改内容 | 日期 | 
|---|---|---|---|
| 1 | 1.0.0 | 支持基本登记SaaS | 2021/12/10 | 
| 2 | 1.0.1 | 新增文件上传功能 | 2022/03/30 | 
| 3 | 1.0.2 | 新增资产冻结、碎片核销功能 | 2022/04/08 | 
| 4 | 1.0.3 | 新增身份管理功能| 2022/06/14 | 
| 5 | 1.0.4 | 优化接口返回数据格式 | 2022/06/14 | 
| 6 | 1.0.5 | 增加应用场景接口 | 2022/07/14 | 
| 7 | 1.0.6 | 增加unionid查询链上绑定地址接口 | 2022/08/10 |  
| 8 | 1.0.7 | 修复fastjson安全漏洞 | 2022/08/11 | 
| 9 | 1.0.8 | 增加错误异常抛出 | 2022/12/06 |

### 配置说明

```

class XassetCliConfig {
    public String EndPoint;
    public String UserAgent;
    public Credentials Credentials;
    public SignOptions SignOption;
    public Duration ConnectTimeoutMs;
}

// 初始化配置
XassetCliConfig cfg = new XassetCliConfig();
// 配置AK/SK 和 AppId
cfg.SetCredentials(appId, ak, sk);
// 确认Endpoint, 接入联调环境
cfg.SetEndPoint("http://120.48.16.137:8360");

```

### 使用示例

```
import com.baidu.xasset.auth.XchainAccount;
import com.baidu.xasset.client.base.BaseDef.*;
import com.baidu.xasset.client.xasset.XassetDef.*;
import com.baidu.xasset.common.config.Config.*;
import com.baidu.xuper.api.Account;

import java.util.logging.Logger;

class Test {
    public static void main(String[] args) {
        // 配置AK/SK 和 AppId
        long appId = 0;
        String ak = "";
        String sk = "";

        XassetCliConfig cfg = new XassetCliConfig();
        cfg.setCredentials(appId, ak, sk);
        cfg.setEndPoint("http://120.48.16.137:8360");
        
        // 创建区块链账户
        Account acc = XchainAccount.newXchainEcdsaAccount(XchainAccount.mnemStrgthStrong, XchainAccount.mnemLangEN);
        // 初始化接口类
        Asset handle = new Asset(cfg, Logger.getGlobal());
        // 调用方法
        Resp<GetStokenResp> result = handle.getStoken(acc);
        System.out.println(result);
    }
}
```
