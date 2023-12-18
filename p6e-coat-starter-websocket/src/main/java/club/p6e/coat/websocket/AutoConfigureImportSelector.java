package club.p6e.coat.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 自动配置导入选择器
 *
 * @author lidashuang
 * @version 1.0
 */
public class AutoConfigureImportSelector implements ImportSelector {

    /**
     * 扫描包下面的路径
     */
    private static final String SCAN_PATH = "/**";

    /**
     * 扫描包下面的文件名称
     */
    private static final String SCAN_FILE_NAME = "/**.class";

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoConfigureImportSelector.class);

    @SuppressWarnings("ALL")
    @NonNull
    @Override
    public String[] selectImports(@NonNull AnnotationMetadata importingClassMetadata) {
        final Map<String, Object> attributes =
                importingClassMetadata.getAnnotationAttributes(EnableP6eWebSocket.class.getName());
        if (attributes != null) {
            if (attributes.get("value") instanceof final AnnotationAttributes[] annotationAttributes) {
                for (final AnnotationAttributes annotationAttribute : annotationAttributes) {
                    WebSocketMain.CONFIGS.add(new WebSocketMain.Config()
                            .setType(String.valueOf(annotationAttribute.get("type")))
                            .setName(String.valueOf(annotationAttribute.get("name")))
                            .setPort(Integer.parseInt(String.valueOf(annotationAttribute.get("port"))))
                    );
                }
            }
            if (attributes.get("threadPoolLength") instanceof Integer) {
                WebSocketMain.THREAD_POOL_LENGTH = Integer.parseInt(String.valueOf(attributes.get("threadPoolLength")));
            }
        }
        final List<String> register = new ArrayList<>();
        final String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(this.getClass().getPackageName())
                + SCAN_PATH + SCAN_FILE_NAME;
        final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            final Resource[] resources = resourcePatternResolver.getResources(pattern);
            final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            for (final Resource resource : resources) {
                final MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                final String classname = metadataReader.getClassMetadata().getClassName();
                final Class<?> clazz = Class.forName(classname);
                final Component componentAnnotation = clazz.getAnnotation(Component.class);
                if (componentAnnotation != null) {
                    register.add(clazz.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("p6e websocket auto configure import selector", e);
        }
        return register.toArray(new String[0]);
    }

}
