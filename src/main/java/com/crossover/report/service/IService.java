package com.crossover.report.service;

public interface IService {

    <T> T getEntity(Class<T> clazz, String... params);

    <T> T postEntity(Class<T> clazz, Object body, String... params);
}