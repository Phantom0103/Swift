package com.wenxia.swift.scan;

import com.wenxia.swift.common.annotation.SwiftRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Set;

/**
 * @author zhouw
 * @date 2022-03-18
 */
public class SwiftRpcServiceRegister implements ImportBeanDefinitionRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftRpcServiceRegister.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(SwiftRpcServiceScan.class.getName());
        if (annotationAttributes != null) {
            String[] packages = (String[]) annotationAttributes.get("packages");
            SwiftRpcClassPathBeanDefinitionScanner scanner = new SwiftRpcClassPathBeanDefinitionScanner(registry);
            scanner.addIncludeFilter(new AnnotationTypeFilter(SwiftRpcService.class));

            if (packages == null || packages.length == 0) {
                LOGGER.warn("扫描RPC目录为空");
                return;
            }

            Set<BeanDefinitionHolder> definitionHolders = scanner.doScan(packages);
            for (BeanDefinitionHolder holder : definitionHolders) {
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition) holder.getBeanDefinition();
                String className = beanDefinition.getBeanClassName();
                if (className == null) {
                    continue;
                }

                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(className);
                beanDefinition.setBeanClass(SwiftRpcFactoryBean.class);
                beanDefinition.getPropertyValues().add("rpcInterface", className);
                beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            }
        }
    }

    static class SwiftRpcClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

        SwiftRpcClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            return metadata.isInterface() && metadata.isIndependent();
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            return super.doScan(basePackages);
        }
    }
}
