package com.green.Nolloo.restAPI.controller.kakao;

import com.green.Nolloo.restAPI.service.KakaoApiService;
import com.green.Nolloo.restAPI.vo.AddressVO;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AddressControl {


    @Autowired
    private KakaoApiService kakaoApiService;

    @GetMapping("/address")
    public String index() {
        return "/content/restAPI/address";
    }

    @GetMapping("/getAddress")
    @ResponseBody
    public AddressVO getAddress(@RequestParam(name="latitude") double latitude, @RequestParam(name="longitude") double longitude) {
        return kakaoApiService.getAddressFromGeolocation(latitude, longitude);
    }
}
