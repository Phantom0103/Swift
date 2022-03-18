package com.wenxia.swift.scan;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zhouw
 * @date 2022-03-18
 */
public class SwiftRpcServiceRegister implements ImportBeanDefinitionRegistrar, BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    private Set<BeanDefinition> scanPackages(ClassPathBeanDefinitionScanner scanner) {
        Set<BeanDefinition> set = new HashSet<>();
        return set;
    }
}
