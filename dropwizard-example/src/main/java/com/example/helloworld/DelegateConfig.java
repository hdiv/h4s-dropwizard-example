package com.example.helloworld;

import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.hdiv.config.annotation.ExclusionRegistry;
import org.hdiv.config.annotation.RuleRegistry;
import org.hdiv.config.annotation.ValidationConfigurer;
import org.hdiv.ee.config.SessionType;
import org.hdiv.ee.config.SingleCacheConfig;
import org.hdiv.ee.config.annotation.ExternalStateStorageConfigurer;
import org.hdiv.ee.session.cache.CacheType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.hal.CurieProvider;

import com.hdivsecurity.services.config.EnableHdiv4ServicesSecurityConfiguration;
import com.hdivsecurity.services.config.HdivCurieProvider;
import com.hdivsecurity.services.config.HdivServicesSecurityConfigurerAdapter;
import com.hdivsecurity.services.config.ServicesSecurityConfigBuilder;

@Configuration
@EnableHdiv4ServicesSecurityConfiguration
public class DelegateConfig extends HdivServicesSecurityConfigurerAdapter {

	// TODO: review when create the Curie Provider
	@Bean
	public CurieProvider curieProvider() {
		return new HdivCurieProvider("halforms", new UriTemplate("{href}{&rel}"));
	}

	@Override
	public void configure(final ServicesSecurityConfigBuilder builder) {
		builder.confidentiality(false);
		builder.sessionType(SessionType.COOKIE);
		builder.showErrorPageOnEditableValidation(true);
		builder.reuseExistingPageInAjaxRequest(true);
		builder.hypermediaSupport(false).csrfHeader(false);
	}

	@Override
	public void addExclusions(final ExclusionRegistry registry) {
		registry.addUrlExclusions("/", "/favicon.ico");
	}

	@Override
	public void addRules(final RuleRegistry registry) {
		registry.addRule("safeText").acceptedPattern("^[a-zA-Z0-9 @.\\-_+#]*$").rejectedPattern("(\\s|\\S)*(--)(\\s|\\S)*]");
		registry.addRule("numbers").acceptedPattern("^[1-9]\\d*$");
	}

	@Override
	public void configureEditableValidation(final ValidationConfigurer validationConfigurer) {
		validationConfigurer.addValidation("/.*");
	}

	public DataSource externalStorageDataSource() {
		final BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost/postgres");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres");
		return dataSource;
	}

	@Override
	public void configureExternalStateStorage(final ExternalStateStorageConfigurer externalStateStorageConfigurer) {

		SingleCacheConfig config = new SingleCacheConfig(CacheType.EXT_DB);
		externalStateStorageConfigurer.databaseExternalStateStore().dataSource(externalStorageDataSource()).numberOfTables(4)
				.tablesSubjectName("Hdiv_Pages_");
		externalStateStorageConfigurer.cacheConfig(Arrays.asList(config));

		super.configureExternalStateStorage(externalStateStorageConfigurer);
	}

}
