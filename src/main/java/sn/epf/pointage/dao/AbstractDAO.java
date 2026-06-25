package sn.epf.pointage.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import sn.epf.pointage.config.HibernateConfig;

import java.util.List;
import java.util.Optional;

public abstract class AbstractDAO<T, ID> implements GenericDAO<T, ID> {

    private static final int DEFAULT_MAX_RESULTS = 100;

    private final Class<T> entityClass;

    protected AbstractDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T save(T entity) {
        Transaction transaction = null;

        try (Session session = openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            return entity;
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Session session = openSession()) {
            return Optional.ofNullable(session.get(entityClass, id));
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = openSession()) {
            return session.createQuery("from " + entityClass.getSimpleName(), entityClass)
                    .setMaxResults(DEFAULT_MAX_RESULTS)
                    .getResultList();
        }
    }

    @Override
    public T update(T entity) {
        Transaction transaction = null;

        try (Session session = openSession()) {
            transaction = session.beginTransaction();
            T mergedEntity = session.merge(entity);
            transaction.commit();
            return mergedEntity;
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    @Override
    public void delete(ID id) {
        Transaction transaction = null;

        try (Session session = openSession()) {
            transaction = session.beginTransaction();
            T entity = session.get(entityClass, id);

            if (entity != null) {
                session.remove(entity);
            }

            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    @Override
    public boolean exists(ID id) {
        return findById(id).isPresent();
    }

    protected Session openSession() {
        return HibernateConfig.getSessionFactory().openSession();
    }

    protected void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
