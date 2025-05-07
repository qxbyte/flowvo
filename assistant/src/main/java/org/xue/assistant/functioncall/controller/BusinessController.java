package org.xue.assistant.functioncall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.assistant.functioncall.entity.Business;
import org.xue.assistant.functioncall.service.BusinessService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务管理控制器
 */
@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    /**
     * 分页查询业务列表
     * @param name 业务名称（可选）
     * @param type 业务类型（可选）
     * @param status 业务状态（可选）
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 业务分页列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getBusinessList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Business> businessPage = businessService.findBusinesses(name, type, status, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", businessPage.getContent());
        response.put("totalElements", businessPage.getTotalElements());
        response.put("totalPages", businessPage.getTotalPages());
        response.put("currentPage", businessPage.getNumber());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取业务详情
     * @param id 业务ID
     * @return 业务详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBusinessById(@PathVariable Long id) {
        return businessService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新增业务
     * @param business 业务信息
     * @return 保存后的业务信息
     */
    @PostMapping
    public ResponseEntity<Business> createBusiness(@RequestBody Business business) {
        business.setCreateTime(LocalDateTime.now());
        Business savedBusiness = businessService.saveBusiness(business);
        return ResponseEntity.ok(savedBusiness);
    }

    /**
     * 更新业务
     * @param id 业务ID
     * @param business 业务信息
     * @return 更新后的业务信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBusiness(@PathVariable Long id, @RequestBody Business business) {
        return businessService.findById(id)
                .map(existingBusiness -> {
                    business.setId(id);
                    business.setCreateTime(existingBusiness.getCreateTime());
                    Business updatedBusiness = businessService.saveBusiness(business);
                    return ResponseEntity.ok(updatedBusiness);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新业务状态
     * @param id 业务ID
     * @param status 新状态
     * @return 更新后的业务信息
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBusinessStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Business updatedBusiness = businessService.updateBusinessStatus(id, status);
            return ResponseEntity.ok(updatedBusiness);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除业务
     * @param id 业务ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBusiness(@PathVariable Long id) {
        return businessService.findById(id)
                .map(business -> {
                    businessService.deleteBusiness(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}