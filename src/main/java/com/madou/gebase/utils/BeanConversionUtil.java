package com.madou.gebase.utils;

import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

public class BeanConversionUtil {
    
    /**
     * 将List<T>转换为List<V>，其中T是源对象类型，V是目标对象类型
     * @param sourceList 源List
     * @param targetClass 目标类
     * @param <T> 源对象类型
     * @param <V> 目标对象类型
     * @return 转换后的目标List
     */
    public static <T, V> List<V> convertList(List<T> sourceList, Class<V> targetClass) {
        return sourceList.stream()
                .map(source -> {
                    try {
                        V target = targetClass.getDeclaredConstructor().newInstance();
                        BeanUtils.copyProperties(source, target);
                        return target;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }
}
