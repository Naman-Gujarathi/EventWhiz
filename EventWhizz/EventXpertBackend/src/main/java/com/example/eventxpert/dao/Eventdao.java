package com.example.eventxpert.dao;

import com.example.eventxpert.pojo.Event;
import com.example.eventxpert.pojo.User;
import com.example.eventxpert.pojo.UserEvent;
import jakarta.persistence.NoResultException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Eventdao extends DAO {
    public List<Event> getAllCurrentEvents() {
        try {
            Query<Event> query = getSession().createQuery("from Event where date >= :date", Event.class);
            query.setParameter("date", new Date());
            return query.list();
        }
        catch(HibernateException e) {
            System.out.println(e);
            return new ArrayList<Event>();
        }
    }

    public Event getOneEvent(int eventId) {
        try {
            Session session = getSession();
            session.clear();
            Query<Event> query = session.createQuery("from Event where id = :eventId", Event.class);
            query.setParameter("eventId", eventId);
            Event event = query.getSingleResult();
            return event;
        }
        catch (NoResultException e) {
            System.out.println(e);
            return null;
        }
        catch (HibernateException e) {
            System.out.println(e);
            return null;
        }
    }

    public void addEvent(Event event) throws Exception {
        try {
            begin();
            getSession().save(event);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new Exception("Could not add customer " + event.getName());
        }
    }

    public void registerForEvent(User user, Event event, Userdao userdao) throws Exception {
        try {
            begin();
            Session session = getSession();
            session.clear();
            UserEvent userEvent = new UserEvent();
            userEvent.setUser(userdao.getUserByUserId(user.getUserId()));
            userEvent.setEvent(getOneEvent(event.getId()));
            userEvent.setTimestamp(new Date());
            session.save(userEvent);
            commit();
        } catch (HibernateException e) {
            System.out.println(e);
            rollback();
        }
    }

    public boolean isRegisteredForEvent(User user, Event event, Userdao userdao) throws Exception {
        try {
            Query<UserEvent> query = getSession().createQuery("from UserEvent where user = :user and event = :event", UserEvent.class);
            System.out.println(user.getUserId() + " " + event.getId());
            query.setParameter("user", userdao.getUserByUserId(user.getUserId()));
            query.setParameter("event", getOneEvent(event.getId()));
            UserEvent userEvent = query.getSingleResult();
            System.out.println(userEvent);
            System.out.println(userEvent != null);
            return userEvent != null;
        }
        catch (HibernateException e) {
            System.out.println(e);
            rollback();
            return false;
        }
        catch (NoResultException e) {
            System.out.println(e);
            rollback();
            return false;
        }
    }

    public boolean updateEvent(Event event, String eventId) {
        try {
            begin();
            Query<Event> updateQuery = getSession().createQuery("update Event set location = :location, name = :name, description = :description, date = :date where eventId = :eventId");
            updateQuery.setParameter("location", event.getLocation());
            updateQuery.setParameter("name", event.getName());
            updateQuery.setParameter("description", event.getDescription());
            updateQuery.setParameter("date", event.getDate());
            updateQuery.setParameter("eventId", Integer.parseInt(eventId));
            updateQuery.executeUpdate();
            commit();
            return true;
        }
        catch (HibernateException e) {
            System.out.println(e);
            rollback();
            return false;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean deleteRegistration(User user, Event event) {
        try {
            begin();
            Query<Event> deleteQuery = getSession().createQuery("delete from UserEvent where user = :user and event = :event");
            deleteQuery.setParameter("user", user);
            deleteQuery.setParameter("event", event);
            deleteQuery.executeUpdate();
            commit();
            return true;
        }
        catch (HibernateException e) {
            System.out.println(e);
            rollback();
            return false;
        }
        catch (Exception e) {
            System.out.println(e);
            rollback();
            return false;
        }
    }
}
