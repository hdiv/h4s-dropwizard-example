package com.example.helloworld.resources;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hdiv.services.TrustAssertion;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.views.PersonView;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;

@Path("/people/{personId}")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

	private final PersonDAO peopleDAO;

	public PersonResource(final PersonDAO peopleDAO) {
		this.peopleDAO = peopleDAO;
	}

	@GET
	@UnitOfWork
	public Person getPerson(@TrustAssertion(idFor = Person.class) @PathParam("personId") final LongParam personId) {
		return findSafely(personId.get());
	}

	@GET
	@Path("/view_freemarker")
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public PersonView getPersonViewFreemarker(@TrustAssertion(idFor = Person.class) @PathParam("personId") final LongParam personId) {
		return new PersonView(PersonView.Template.FREEMARKER, findSafely(personId.get()));
	}

	@GET
	@Path("/view_mustache")
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public PersonView getPersonViewMustache(@PathParam("personId") final LongParam personId) {
		return new PersonView(PersonView.Template.MUSTACHE, findSafely(personId.get()));
	}

	private Person findSafely(final long personId) {
		return peopleDAO.findById(personId).orElseThrow(() -> new NotFoundException("No such user."));
	}
}
