package com.example.helloworld;

import java.util.EnumSet;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.hdiv.filter.ValidatorFilter;
import org.springframework.web.context.request.RequestContextListener;

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.Person;
import com.example.helloworld.core.Template;
import com.example.helloworld.core.User;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.filter.DateRequiredFeature;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.resources.FilteredResource;
import com.example.helloworld.resources.HelloWorldResource;
import com.example.helloworld.resources.PeopleResource;
import com.example.helloworld.resources.PersonResource;
import com.example.helloworld.resources.ProtectedResource;
import com.example.helloworld.resources.SearchPeopleResource;
import com.example.helloworld.resources.ViewResource;
import com.example.helloworld.tasks.EchoTask;
import com.hdivsecurity.services.pb.technology.jaxrs.ServerWriterInterceptor;
import com.hdivsecurity.services.pb.technology.jaxrs.jersey.EndPointCapturer;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

	public static Environment environment;

	public static void main(final String[] args) throws Exception {
		new HelloWorldApplication().run(args);
	}

	private final HibernateBundle<HelloWorldConfiguration> hibernateBundle = new HibernateBundle<HelloWorldConfiguration>(Person.class) {
		@Override
		public DataSourceFactory getDataSourceFactory(final HelloWorldConfiguration configuration) {
			return configuration.getDataSourceFactory();
		}
	};

	@Override
	public String getName() {
		return "hello-world";
	}

	@Override
	public void initialize(final Bootstrap<HelloWorldConfiguration> bootstrap) {
		// Enable variable substitution with environment variables
		bootstrap.setConfigurationSourceProvider(
				new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));

		bootstrap.addCommand(new RenderCommand());
		bootstrap.addBundle(new AssetsBundle());
		bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(final HelloWorldConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});
		bootstrap.addBundle(hibernateBundle);
		bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>() {
			@Override
			public Map<String, Map<String, String>> getViewConfiguration(final HelloWorldConfiguration configuration) {
				return configuration.getViewRendererConfiguration();
			}
		});
	}

	@Override
	public void run(final HelloWorldConfiguration configuration, final Environment environment) {
		HelloWorldApplication.environment = environment;
		final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
		final Template template = configuration.buildTemplate();

		environment.healthChecks().register("template", new TemplateHealthCheck(template));
		environment.admin().addTask(new EchoTask());
		environment.jersey().register(DateRequiredFeature.class);
		environment.jersey()
				.register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>().setAuthenticator(new ExampleAuthenticator())
						.setAuthorizer(new ExampleAuthorizer()).setRealm("SUPER SECRET STUFF").buildAuthFilter()));
		environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
		environment.jersey().register(RolesAllowedDynamicFeature.class);
		environment.jersey().register(new HelloWorldResource(template));
		environment.jersey().register(new ViewResource());
		environment.jersey().register(new ProtectedResource());
		environment.jersey().register(new PeopleResource(dao));
		environment.jersey().register(new SearchPeopleResource(dao));
		environment.jersey().register(new PersonResource(dao));
		environment.jersey().register(new FilteredResource());

		/**
		 * Hdiv
		 */
		environment.jersey().register(ServerWriterInterceptor.class);
		environment.jersey().register(new EndPointCapturer("/"));
		environment.servlets().addFilter("ValidatorFilter", ValidatorFilter.class)
				.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
		environment.servlets().addServletListeners(new DropwizardContextInitializer(), new RequestContextListener());
		DropwizardContextInitializer.mapper = environment.getObjectMapper();

		EndPointCapturer.setServletContainer((ServletContainer) environment.getJerseyServletContainer());
	}
}
