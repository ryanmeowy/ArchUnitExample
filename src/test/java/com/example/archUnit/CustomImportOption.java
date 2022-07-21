package com.example.archUnit;

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

	private static final Set<Pattern> EXCLUDED_PATTERN = new HashSet<>(8);
	//定义默认排除的包 ，也可在创建实例的时候传入需要排除的包
	private static final String[] DEF_DONT_SCAN_PACKAGES = {"com/example/archUnit/config",
					"com/example/archUnit/converter", "com/example/archUnit/annotation"};

	static {
		for (String defDontScanPackage : DEF_DONT_SCAN_PACKAGES) {
			EXCLUDED_PATTERN.add(Pattern.compile(String.format(".*/%s/.*", defDontScanPackage)));
		}
	}

	public CustomImportOption() {
	}

	public CustomImportOption(String... packages) {
		for (String eachPackage : packages) {
			EXCLUDED_PATTERN.add(Pattern.compile(String.format(".*/%s/.*", eachPackage)));
		}
	}

	@Override
	public boolean includes(Location location) {
		//遍历pattern依次与location中的uri进行匹配
		for (Pattern pattern : EXCLUDED_PATTERN) {
			if (location.matches(pattern)) {
				return false;
			}
		}
		return true;
	}
}

