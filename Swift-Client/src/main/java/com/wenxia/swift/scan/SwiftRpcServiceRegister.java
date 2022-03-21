package com.wenxia.swift.scan;

import com.wenxia.swift.common.annotation.SwiftRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
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

            if (packages == null || packages.length == 0) {
                LOGGER.warn("扫描RPC目录为空");
                return;
            }

            for (String pkg : packages) {
                Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(pkg);
                if (beanDefinitions.isEmpty()) {
                    continue;
                }

                for (BeanDefinition definition : beanDefinitions) {
                    ((GenericBeanDefinition) definition).setBeanClass(SwiftRpcFactoryBean.class);
                    ((GenericBeanDefinition) definition).setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                }
            }
        }
    }

    static class SwiftRpcClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

        public SwiftRpcClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
        }

        @Override
        protected void registerDefaultFilters() {
            addIncludeFilter(new AnnotationTypeFilter(SwiftRpcService.class));
        }
    }
}
