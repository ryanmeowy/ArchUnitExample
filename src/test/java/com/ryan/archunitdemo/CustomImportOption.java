package com.ryan.archunitdemo;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 自定义ImportOption
 *
 * @author ryan
 */
public class CustomImportOption implements ImportOption {

    private final Set<Pattern> EXCLUDED_PATTERN;

    public CustomImportOption(String... packages) {
        EXCLUDED_PATTERN = new HashSet<>(8);
        for (String eachPackage : packages) {
            EXCLUDED_PATTERN.add(Pattern.compile(String.format(".*/%s/.*", eachPackage.replace("/", "."))));
        }
    }

    /**
     * @param location Location中包含路径信息 是否jar文件等判断属性的元数据, 方便使用正则表达式或者直接的逻辑判断
     * @return false:
     */
    @Override
    public boolean includes(Location location) {
        for (Pattern pattern : EXCLUDED_PATTERN) {
            return !location.matches(pattern);
        }
        return true;
    }

}

