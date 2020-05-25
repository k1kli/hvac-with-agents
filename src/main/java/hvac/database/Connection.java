package hvac.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Closeable;

public class Connection implements Closeable {
    private EntityManagerFactory entityManagerFactory;

    public Connection() {
        entityManagerFactory = Persistence.createEntityManagerFactory(
                "hvac" );
    }

    public Connection(String persistenceUnit) {
        entityManagerFactory = Persistence.createEntityManagerFactory(
                persistenceUnit );
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }
}
