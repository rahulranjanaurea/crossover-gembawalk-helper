package com.crossover.report.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoginService extends BaseService implements IService {

    public static void main(String[] args) {
        LoginService loginService = new LoginService();
        Map map = new HashMap<>();
        loginService.username = "john.isaac@aurea.com";
        loginService.password = "Hackathon#1Pa$$";
        loginService.baseUrl = "https://api.crossover.com/api/";
        System.out.print(loginService.postEntity(Map.class, map).get("token"));
    }

    @Override
    public String getUrl() {
        return "identity/authentication";
    }

}
