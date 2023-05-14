package org.jboss.examples.ticketmonster.rest;

import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.examples.ticketmonster.rest.dto.EventDTO;
import org.jboss.examples.ticketmonster.model.Event;

/**
 * 
 */
@Stateless
@Path("forge/events")
public class EventEndpoint
{
   @PersistenceContext(unitName = "primary")
   private EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(EventDTO dto)
   {
      Event entity = dto.fromDTO(null, em);
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(EventEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id)
   {
      Event entity = em.find(Event.class, id);
      if (entity == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      em.remove(entity);
      return Response.noContent().build();
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   @Produces("application/json")
   public Response findById(@PathParam("id") Long id)
   {
      TypedQuery<Event> findByIdQuery = em.createQuery("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.mediaItem LEFT JOIN FETCH e.category WHERE e.id = :entityId ORDER BY e.id", Event.class);
      findByIdQuery.setParameter("entityId", id);
      Event entity;
      try
      {
         entity = findByIdQuery.getSingleResult();
      }
      catch (NoResultException nre)
      {
         entity = null;
      }
      if (entity == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      EventDTO dto = new EventDTO(entity);
      return Response.ok(dto).build();
   }

   @GET
   @Produces("application/json")
   public List<EventDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<Event> findAllQuery = em.createQuery("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.mediaItem LEFT JOIN FETCH e.category ORDER BY e.id", Event.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<Event> searchResults = findAllQuery.getResultList();
      final List<EventDTO> results = new ArrayList<EventDTO>();
      for (Event searchResult : searchResults)
      {
         EventDTO dto = new EventDTO(searchResult);
         results.add(dto);
      }
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(@PathParam("id") Long id, EventDTO dto)
   {
      TypedQuery<Event> findByIdQuery = em.createQuery("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.mediaItem LEFT JOIN FETCH e.category WHERE e.id = :entityId ORDER BY e.id", Event.class);
      findByIdQuery.setParameter("entityId", id);
      Event entity;
      try
      {
         entity = findByIdQuery.getSingleResult();
      }
      catch (NoResultException nre)
      {
         entity = null;
      }
      entity = dto.fromDTO(entity, em);
      try
      {
         entity = em.merge(entity);
      }
      catch (OptimisticLockException e)
      {
         return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
      }
      return Response.noContent().build();
   }
}
