package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags= "店铺相关接口")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus(){
        Integer status = shopService.get();
        log.info("获取店铺状态,当前状态为：{}",status==1?"营业中":"打烊中");
        return Result.success(status);
    }

    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺状态")
    public Result<Void>setStatus(@PathVariable Integer status){
        log.info("设置店铺状态为:{}",status==1?"营业中":"打烊中");
        shopService.set(status);
        return Result.success();
    }

}
