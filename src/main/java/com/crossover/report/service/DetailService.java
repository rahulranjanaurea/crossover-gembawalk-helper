package com.crossover.report.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DetailService extends BaseService implements IService {

    public static void main(String[] args) {
        DetailService loginService = new DetailService();
        Map map = new HashMap<>();
        loginService.username = "john.isaac@aurea.com";
        loginService.password = "Hackathon#1Pa$$1ß";
        loginService.baseUrl = "https://api.crossover.com/api/";
        System.out.print(loginService.postEntity(Map.class, map).get("token"));
    }

    @Override
    public String getUrl() {
        return "identity/users/current/detail";
    }

}
