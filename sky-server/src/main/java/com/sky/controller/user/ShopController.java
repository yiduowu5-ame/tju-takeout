package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@Api(tags= "店铺相关接口")
public class ShopController {

    @Autowired
    public ShopService shopService;

    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus(){
        Integer status = shopService.get();
        log.info("获取店铺状态,当前状态为：{}",status==1?"营业中":"打烊中");
        return Result.success(status);
    }


}
