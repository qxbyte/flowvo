package org.xue.mcpmysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.mcpmysql.enums.ApiFormatType;
import org.xue.mcpmysql.service.ExposureApiService;

import java.util.Map;

@SpringBootTest
public class ApiSchameTest {

    @Autowired
    private ExposureApiService exposureApiService;

    @Test
    void getApi() throws JsonProcessingException {

        Map<String,Object> m1 = exposureApiService.getApiDescription(ApiFormatType.RPC_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(m1));
    }
}
