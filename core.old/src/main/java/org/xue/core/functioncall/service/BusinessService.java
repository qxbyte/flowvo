package org.xue.core.functioncall.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.xue.core.functioncall.entity.Business;

import java.util.Optional;

/**
 * 业务管理服务接口
 */
public interface BusinessService {
    
    /**
     * 保存业务信息
     * @param business 业务信息
     * @return 保存后的业务信息
     */
    Business saveBusiness(Business business);
    
    /**
     * 根据ID查询业务信息
     * @param id 业务ID
     * @return 业务信息
     */
    Optional<Business> findById(Long id);
    
    /**
     * 根据条件分页查询业务列表
     * @param name 业务名称（可选）
     * @param type 业务类型（可选）
     * @param status 业务状态（可选）
     * @param pageable 分页参数
     * @return 业务分页列表
     */
    Page<Business> findBusinesses(String name, String type, String status, Pageable pageable);
    
    /**
     * 删除业务
     * @param id 业务ID
     */
    void deleteBusiness(Long id);
    
    /**
     * 更新业务状态
     * @param id 业务ID
     * @param status 新状态
     * @return 更新后的业务信息
     */
    Business updateBusinessStatus(Long id, String status);
}