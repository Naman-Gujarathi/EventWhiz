package com.example.eventxpert.dao;

import com.example.eventxpert.pojo.User;
import org.hibernate.query.Query;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class Userdao extends DAO {
    public void addUser(User user) throws Exception {
        try {
            begin();
            getSession().save(user);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new Exception("Could not add user " + user.getUsername());
        }
    }

    public boolean isUserExisting(String username) {
        try {
            Session session = getSession();

            Query queryForUsername = session.createQuery("select count(*) from User where username = :username");
            queryForUsername.setParameter("username", username);

            Long usernameCount = (Long)queryForUsername.uniqueResult();

            if (usernameCount == 0) {
                return false;
            }

            return true;
        }
        catch(HibernateException e) {
            System.out.println(e);
            return true;
        }
    }

    public User getUserByUsername(String username) {
        Query<User> query = getSession().createQuery("from User where username = (?1)", User.class);
        query.setParameter(1, username);

        User user = query.getSingleResult();
        return user;
    }

    public User getUserByUserId(int userId) {
        Query<User> query = getSession().createQuery("from User where userId = (?1)", User.class);
        query.setParameter(1, userId);

        User user = query.getSingleResult();
        return user;
    }

    public boolean isUserExisting(String email, String username) {
        try {
            Session session = getSession();

            Query queryForUsername = session.createQuery("select count(*) from User where username = :username");
            queryForUsername.setParameter("username", username);

            Query queryForEmail = session.createQuery("select count(*) from User where email = :email");
            queryForEmail.setParameter("email", email);

            Long usernameCount = (Long)queryForUsername.uniqueResult();
            Long emailCount = (Long)queryForEmail.uniqueResult();

            if (usernameCount == 0 && emailCount == 0) {
                return false;
            }

            return true;
        }
        catch(HibernateException e) {
            System.out.println(e);
            return true;
        }
    }
}
