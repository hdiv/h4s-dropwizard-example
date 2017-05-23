package com.example.helloworld.resources;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.hdiv.services.TrustAssertion;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;

import io.dropwizard.hibernate.UnitOfWork;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchPeopleResource {

	private final PersonDAO peopleDAO;

	public SearchPeopleResource(final PersonDAO peopleDAO) {
		this.peopleDAO = peopleDAO;
	}

	@GET
	@UnitOfWork
	public Person getPerson(@TrustAssertion(idFor = Person.class) @QueryParam("personId") final Optional<String> personId) {
		return peopleDAO.findById(Long.parseLong(personId.get())).orElseThrow(() -> new NotFoundException("No such user."));
	}

}
