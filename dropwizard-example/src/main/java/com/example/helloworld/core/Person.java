package com.example.helloworld.core;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hdiv.services.SecureIdentifiable;
import org.hdiv.services.TrustAssertion;

@Entity
@Table(name = "people")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.Person.findAll", query = "SELECT p FROM Person p") })
public class Person implements SecureIdentifiable<Long> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@TrustAssertion(pattern = "^[a-zA-Z]{1,}$")
	@Column(name = "fullName", nullable = false)
	private String fullName;

	@Column(name = "jobTitle", nullable = false)
	private String jobTitle;

	public Person() {
	}

	public Person(final String fullName, final String jobTitle) {
		this.fullName = fullName;
		this.jobTitle = jobTitle;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(final String jobTitle) {
		this.jobTitle = jobTitle;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Person)) {
			return false;
		}

		final Person that = (Person) o;

		return Objects.equals(id, that.id) && Objects.equals(fullName, that.fullName) && Objects.equals(jobTitle, that.jobTitle);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fullName, jobTitle);
	}
}
