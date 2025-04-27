package org.xue.milvus;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;

public class MilvusDemo {
    public static void main(String[] args) {
        // 创建连接参数
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost("localhost")
                .withPort(19530)
                .build();
        // 连接 Milvus
        MilvusServiceClient client = new MilvusServiceClient(connectParam);

        // 这里可以进行各种操作，如创建 collection、插入、检索等
        System.out.println("连接成功！");

        client.close();
    }
}
